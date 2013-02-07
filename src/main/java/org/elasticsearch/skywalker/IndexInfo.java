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
package org.elasticsearch.skywalker;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FormatDetails;
import org.apache.lucene.index.IndexGate;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.ReaderUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IndexInfo {

    private IndexReader reader;
    private Directory dir;
    private int numTerms;
    private int indexFormat;
    private FormatDetails formatDetails;
    private TermInfo[] topTerms;
    private List<FieldInfo> fieldInfo;
    private List<String> fieldNames;
    private String version;
    private String dirImpl;

    public IndexInfo(IndexReader reader) throws IOException {
        this.reader = reader;
        try {
            this.dir = reader.directory();
            this.dirImpl = dir.getClass().getName();
        } catch (UnsupportedOperationException uoe) {
            this.dir = null;
            this.dirImpl = "N/A";
        }
        try {
            this.version = Long.toString(reader.getVersion());
        } catch (UnsupportedOperationException uoe) {
            this.dir = null;
        }
        numTerms = 0;
        TermEnum te = null;
        try {
            te = reader.terms();
            while (te.next()) {
                numTerms++;
            }
        } finally {
            te.close();
        }
        fieldInfo = new ArrayList();
        fieldNames = new ArrayList();
        final Iterator<FieldInfo> it = ReaderUtil.getMergedFieldInfos(reader).iterator();
        while (it.hasNext()) {
            final FieldInfo info = it.next();
            fieldInfo.add(info);
            fieldNames.add(info.name);
        }
        Collections.sort(fieldNames);
        if (dir != null) {
            indexFormat = IndexGate.getIndexFormat(dir);
            formatDetails = IndexGate.getFormatDetails(indexFormat);
        } else {
            indexFormat = -1;
            formatDetails = new FormatDetails();
        }
        topTerms = HighFreqTerms.getHighFreqTerms(reader, null, 51, fieldNames.toArray(new String[fieldNames.size()]));
    }

    /**
     * @return the reader
     */
    public IndexReader getReader() {
        return reader;
    }

    public Directory getDirectory() {
        return dir;
    }

    /**
     * @return the numTerms
     */
    public int getNumTerms() {
        return numTerms;
    }

    /**
     * @return the indexFormat
     */
    public int getIndexFormat() {
        return indexFormat;
    }

    /**
     * @return the formatDetails
     */
    public FormatDetails getFormatDetails() {
        return formatDetails;
    }

    /**
     * @return the topTerms
     */
    public TermInfo[] getTopTerms() {
        return topTerms;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfo;
    }

    /**
     * @return the fieldNames
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    public String getVersion() {
        return version;
    }

    public String getDirImpl() {
        return dirImpl;
    }
}
