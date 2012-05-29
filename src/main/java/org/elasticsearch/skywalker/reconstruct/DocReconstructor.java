/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.skywalker.reconstruct;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.util.ReaderUtil;

/**
 * This class attempts to reconstruct all fields from a document existing in a
 * Lucene index. This operation may be (and usually) is lossy - e.g. unstored
 * fields are rebuilt from terms present in the index, and these terms may have
 * been changed (e.g. lowercased, stemmed), and many other input tokens may have
 * been skipped altogether by the Analyzer, when fields were originally added to
 * the index.
 *
 * @author ab
 *
 */
public class DocReconstructor {

    //private ProgressNotification progress = new ProgressNotification();
    private String[] fieldNames = null;
    private IndexReader reader = null;

    /**
     * Prepare a document reconstructor.
     *
     * @param reader IndexReader to read from.
     * @throws Exception
     */
    public DocReconstructor(IndexReader reader) throws Exception {
        this(reader, null, -1);
    }

    /**
     * Prepare a document reconstructor.
     *
     * @param reader IndexReader to read from.
     * @param fieldNames if non-null or not empty, data will be collected only
     * from these fields, otherwise data will be collected from all fields
     * @param numTerms total number of terms in the index, or -1 if unknown
     * (will be calculated)
     * @throws Exception
     */
    public DocReconstructor(IndexReader reader, String[] fieldNames, int numTerms) throws Exception {
        if (reader == null) {
            throw new Exception("IndexReader cannot be null.");
        }
        this.reader = reader;
        if (fieldNames == null || fieldNames.length == 0) {
            // collect fieldNames
            final Iterator<FieldInfo> it = ReaderUtil.getMergedFieldInfos(reader).iterator();
            final List<String> names = new LinkedList();
            while (it.hasNext()) {
                final FieldInfo info = it.next();
                names.add(info.name);
            }
            this.fieldNames = names.toArray(new String[names.size()]);
        } else {
            this.fieldNames = fieldNames;
        }
        if (numTerms == -1) {
            TermEnum te = null;
            try  {
                te = reader.terms();
                numTerms = 0;
                while (te.next()) {
                    numTerms++;
                }
            } finally {
                te.close();
            }
        }
    }

    /**
     * Reconstruct document fields.
     *
     * @param docNum document number. If this document is deleted, but the index
     * is not optimized yet, the reconstruction process may still yield the
     * reconstructed field content even from deleted documents.
     * @return reconstructed document
     * @throws Exception
     */
    public Reconstructed reconstruct(int docNum) throws Exception {
        if (docNum < 0 || docNum > reader.maxDoc()) {
            throw new Exception("Document number outside of valid range.");
        }
        Reconstructed res = new Reconstructed();
        if (reader.isDeleted(docNum)) {
            throw new Exception("Document is deleted.");
        } else {
            Document doc = reader.document(docNum);
            for (int i = 0; i < fieldNames.length; i++) {
                Fieldable[] fs = doc.getFieldables(fieldNames[i]);
                if (fs != null && fs.length > 0) {
                    res.getStoredFields().put(fieldNames[i], fs);
                }
            }
        }
        // collect values from unstored fields
        HashSet<String> fields = new HashSet(Arrays.asList(fieldNames));
        // try to use term vectors if available
        //progress.maxValue = fieldNames.length;
        //progress.curValue = 0;
        //progress.minValue = 0;
        for (int i = 0; i < fieldNames.length; i++) {
            TermFreqVector tvf = reader.getTermFreqVector(docNum, fieldNames[i]);
            if (tvf != null && tvf.size() > 0 && (tvf instanceof TermPositionVector)) {
                TermPositionVector tpv = (TermPositionVector) tvf;
                //progress.message = "Reading term vectors ...";
                //progress.curValue = i;
                //setChanged();
                //notifyObservers(progress);
                String[] tv = tpv.getTerms();
                for (int k = 0; k < tv.length; k++) {
                    // do we have positions?
                    int[] posArr = tpv.getTermPositions(k);
                    if (posArr == null) {
                        // only offsets
                        TermVectorOffsetInfo[] offsets = tpv.getOffsets(k);
                        if (offsets.length == 0) {
                            continue;
                        }
                        // convert offsets into positions
                        posArr = convertOffsets(offsets);
                    }
                    GrowableStringArray gsa = res.getReconstructedFields().get(fieldNames[i]);
                    if (gsa == null) {
                        gsa = new GrowableStringArray();
                        res.getReconstructedFields().put(fieldNames[i], gsa);
                    }
                    for (int m = 0; m < posArr.length; m++) {
                        gsa.append(posArr[m], "|", tv[k]);
                    }
                }
                fields.remove(fieldNames[i]); // got what we wanted
            }
        }
        String term;
        TermPositions tp = null;
        try {
            tp = reader.termPositions();
            for (String fld : fields) {
                TermEnum te = null;
                try {
                    te = reader.terms(new Term(fld, ""));
                    if (te == null || te.term() == null || !te.term().field().equals(fld)) {
                        continue;
                    }
                    // TermEnum is already positioned
                    do {
                        if (!te.term().field().equals(fld)) {
                            // end of terms in this field
                            break;
                        }
                        tp.seek(te.term());
                        if (!tp.skipTo(docNum) || tp.doc() != docNum) {
                            // this term is not found in the doc
                            continue;
                        }
                        term = te.term().text();
                        GrowableStringArray gsa = (GrowableStringArray) res.getReconstructedFields().get(te.term().field());
                        if (gsa == null) {
                            gsa = new GrowableStringArray();
                            res.getReconstructedFields().put(te.term().field(), gsa);
                        }
                        for (int k = 0; k < tp.freq(); k++) {
                            int pos = tp.nextPosition();
                            gsa.append(pos, "|", term);
                        }
                    } while (te.next());
                } finally {
                    te.close();
                }
            }
        } finally {
            tp.close();
        }
        //progress.message = "Done.";
        //progress.curValue = 100;
        //setChanged();
        //notifyObservers(progress);
        return res;
    }

    private int[] convertOffsets(TermVectorOffsetInfo[] offsets) {
        int[] posArr = new int[offsets.length];
        int curPos = 0;
        int maxDelta = 3; // allow 3 characters diff, otherwise insert a skip
        int avgTermLen = 5; // assume this is the avg. term length of missing terms
        for (int m = 0; m < offsets.length; m++) {
            int curStart = offsets[m].getStartOffset();
            if (m > 0) {
                int prevEnd = offsets[m - 1].getEndOffset();
                int prevStart = offsets[m - 1].getStartOffset();
                if (curStart == prevStart) {
                    curPos--; // overlapping token
                } else {
                    if (prevEnd + maxDelta < curStart) { // possibly a gap
                        // calculate the number of missing tokens
                        int increment = (curStart - prevEnd) / (maxDelta + avgTermLen);
                        if (increment == 0) {
                            increment++;
                        }
                        curPos += increment;
                    }
                }
            }
            posArr[m] = curPos;
            curPos++;
        }
        return posArr;
    }


}
