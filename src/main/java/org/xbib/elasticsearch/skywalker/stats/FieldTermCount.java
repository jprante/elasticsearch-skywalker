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

/**
 *  Field term count
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class FieldTermCount implements Comparable<FieldTermCount> {

    private String fieldname;

    private long termCount;

    public FieldTermCount(String fieldname, long termCount) {
        this.fieldname = fieldname;
        this.termCount = termCount;
    }

    public String getFieldname() {
        return fieldname;
    }

    public long getTermCount() {
        return termCount;
    }

    public int compareTo(FieldTermCount f2) {
        if (termCount > f2.termCount) {
            return -1;
        } else if (termCount < f2.termCount) {
            return 1;
        } else {
            return 0;
        }
    }
}
