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
package org.xbib.elasticsearch.skywalker.reconstruct;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.CompositeReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.Bits;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.elasticsearch.action.skywalker.support.IndexableFieldToXContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * This class attempts to reconstruct all fields from a document existing in a
 * Lucene index. This operation may be (and usually) is lossy - e.g. unstored
 * fields are rebuilt from terms present in the index, and these terms may have
 * been changed (e.g. lowercased, stemmed), and many other input tokens may have
 * been skipped altogether by the Analyzer, when fields were originally added to
 * the index.
 *
 *  @author ab
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DocumentReconstructor {

    private AtomicReader reader;

    /**
     * Prepare a document reconstructor.
     *
     * @param indexReader IndexReader to read from.
     * @throws Exception
     */
    public DocumentReconstructor(IndexReader indexReader) {
        if (indexReader == null) {
            throw new ElasticSearchIllegalArgumentException("reader cannot be null");
        }
        try {
            if (indexReader instanceof CompositeReader) {
                this.reader = new SlowCompositeReaderWrapper((CompositeReader) indexReader);
            } else if (indexReader instanceof AtomicReader) {
                this.reader = (AtomicReader) indexReader;
            } else {
                throw new ElasticSearchIllegalArgumentException("unsupported IndexReader class " + indexReader.getClass().getName());
            }
        } catch (IOException e) {
            throw new ElasticSearchIllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Reconstruct an index shard
     *
     * @return reconstructed document
     * @throws Exception
     */
    public XContentBuilder reconstruct(int shardId) throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .field("shardId", shardId)
                .field("numDeletions", reader.numDeletedDocs());
        builder.startArray("docs");
        FieldInfos fieldInfos = reader.getFieldInfos();
        Bits live = MultiFields.getLiveDocs(reader);
        for (int docNum = 0; docNum < reader.maxDoc(); docNum++) {
            Document doc = reader.document(docNum);
            if (live != null && live.get(docNum)) {
                continue; // not deleted
            }
            builder.startObject().startArray("fields");
            if (fieldInfos != null) {
                for (FieldInfo fi : fieldInfos) {
                    String name = fi.name;
                    IndexableField[] fs = doc.getFields(name);
                    if (fs != null && fs.length > 0) {
                        for (IndexableField f : fs) {
                            IndexableFieldToXContent x = new IndexableFieldToXContent().field(f);
                            x.toXContent(builder, ToXContent.EMPTY_PARAMS);
                        }
                    }
                }
            }
            builder.endArray();
            builder.startArray("terms");
            if (fieldInfos != null) {
                TermsEnum te = null;
                DocsAndPositionsEnum dpe = null;
                for (FieldInfo fi : fieldInfos) {
                    Terms terms = MultiFields.getTerms(reader, fi.name);
                    if (terms == null) { // no terms in this field
                        continue;
                    }
                    te = terms.iterator(te);
                    while (te.next() != null) {
                        DocsAndPositionsEnum newDpe = te.docsAndPositions(live, dpe, 0);
                        if (newDpe == null) { // no position info for this field
                            break;
                        }
                        dpe = newDpe;
                        int num = dpe.advance(docNum);
                        if (num != docNum) { // either greater than or NO_MORE_DOCS
                            continue; // no data for this term in this doc
                        }
                        String text = te.term().utf8ToString();
                        List<Integer> positions = new ArrayList();
                        List<Integer> starts = new ArrayList();
                        List<Integer> ends = new ArrayList();
                        for (int k = 0; k < dpe.freq(); k++) {
                            int pos = dpe.nextPosition();
                            positions.add(pos);
                            starts.add(dpe.startOffset());
                            ends.add(dpe.endOffset());
                        }
                        builder.startObject()
                                .field("text", text)
                                .field("positions", positions)
                                .field("starts", starts)
                                .field("ends", ends)
                                .field("count", dpe.freq())
                                .endObject();
                    }
                }
            }
            builder.endArray();
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

}
