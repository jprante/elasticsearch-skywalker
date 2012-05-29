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

import java.io.IOException;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;

/**
 * <code>HighFreqTerms</code> class extracts terms and their frequencies out of
 * an existing Lucene index.
 */
public class HighFreqTerms {

    public static int defaultNumTerms = 100;

    public static TermInfo[] getHighFreqTerms(IndexReader ir, Map junkWords, String[] fields) throws IOException {
        return getHighFreqTerms(ir, junkWords, defaultNumTerms, fields);
    }

    public static TermInfo[] getHighFreqTerms(IndexReader reader, Map junkWords, int numTerms, String[] fields) throws IOException {
        if (reader == null || fields == null) {
            return null;
        }
        TermInfoQueue tiq = new TermInfoQueue(numTerms);
        TermEnum terms = reader.terms();

        int minFreq = 0;
        while (terms.next()) {
            String field = terms.term().field();
            if (fields != null && fields.length > 0) {
                boolean skip = true;
                for (int i = 0; i < fields.length; i++) {
                    if (field.equals(fields[i])) {
                        skip = false;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
            }
            if (junkWords != null && junkWords.get(terms.term().text()) != null) {
                continue;
            }
            if (terms.docFreq() > minFreq) {
                tiq.insertWithOverflow(new TermInfo(terms.term(), terms.docFreq()));
                if (tiq.size() >= numTerms) {
                    tiq.pop();
                    minFreq = ((TermInfo) tiq.top()).docFreq;
                }
            }
        }
        TermInfo[] res = new TermInfo[tiq.size()];
        for (int i = 0; i < res.length; i++) {
            res[res.length - i - 1] = (TermInfo) tiq.pop();
        }
        return res;
    }
}
