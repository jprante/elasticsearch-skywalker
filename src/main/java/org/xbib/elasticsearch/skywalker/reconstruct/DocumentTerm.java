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

import java.util.ArrayList;
import java.util.List;

/**
 * Document term
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DocumentTerm {

    private String text;
    private List<Integer> positions;
    private List<Integer> starts;
    private List<Integer> ends;
    private long count;

    public DocumentTerm text(String text) {
        this.text = text;
        return this;
    }

    public String text() {
        return text();
    }

    public DocumentTerm count(long count) {
        this.count = count;
        return this;
    }

    public long count() {
        return count;
    }

    public DocumentTerm positions(int size) {
        this.positions = new ArrayList(size);
        return this;
    }

    public DocumentTerm positions(int pos, int position) {
        this.positions.set(pos, position);
        return this;
    }

    public List<Integer> positions() {
        return positions;
    }

    public DocumentTerm starts(int size) {
        this.starts = new ArrayList(size);
        return this;
    }

    public DocumentTerm starts(int pos, int starts) {
        this.starts.set(pos, starts);
        return this;
    }

    public List<Integer> starts() {
        return starts();
    }

    public DocumentTerm ends(int size) {
        this.ends = new ArrayList(size);
        return this;
    }

    public DocumentTerm ends(int pos, int ends) {
        this.ends.set(pos, ends);
        return this;
    }

    public List<Integer> ends() {
        return ends;
    }

    public String toString() {
        return count + ":'" + text + "'";
    }

}
