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
package org.xbib.elasticsearch.skywalker.stats;

import org.apache.lucene.util.BytesRef;

/**
 * Term stats
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TermStats {

    private String field;
    private BytesRef termtext;
    private int docFreq;

    public TermStats text(BytesRef text) {
        this.termtext =  text;
        return this;
    }

    public String text() {
        return termtext.utf8ToString();
    }

    public TermStats field(String field) {
        this.field = field;
        return this;
    }

    public String field() {
        return field;
    }

    public TermStats docFreq(int docFreq) {
        this.docFreq = docFreq;
        return this;
    }

    public int docFreq() {
        return docFreq;
    }

    public String toString() {
        return field + ":" + termtext.utf8ToString() + ":" + docFreq;
    }
}
