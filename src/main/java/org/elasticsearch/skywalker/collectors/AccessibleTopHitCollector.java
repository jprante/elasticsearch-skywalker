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
package org.elasticsearch.skywalker.collectors;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;

import java.io.IOException;

public class AccessibleTopHitCollector extends AccessibleHitCollector {

    private TopScoreDocCollector tdc;
    private TopDocs topDocs = null;
    private int size;

    public AccessibleTopHitCollector(int size, boolean outOfOrder, boolean shouldScore) {
        tdc = TopScoreDocCollector.create(size, outOfOrder);
        this.shouldScore = shouldScore;
        this.outOfOrder = outOfOrder;
        this.size = size;
    }

    @Override
    public int getDocId(int pos) {
        if (topDocs == null) {
            topDocs = tdc.topDocs();
        }
        return topDocs.scoreDocs[pos].doc;
    }

    @Override
    public float getScore(int pos) {
        if (topDocs == null) {
            topDocs = tdc.topDocs();
        }
        return topDocs.scoreDocs[pos].score;
    }

    @Override
    public int getTotalHits() {
        return tdc.getTotalHits();
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return tdc.acceptsDocsOutOfOrder();
    }

    @Override
    public void collect(int doc) throws IOException {
        tdc.collect(doc);
    }

    @Override
    public void setNextReader(IndexReader r, int base) throws IOException {
        this.docBase = base;
        tdc.setNextReader(r, base);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        if (shouldScore) {
            tdc.setScorer(scorer);
        } else {
            tdc.setScorer(NoScoringScorer.INSTANCE);
        }
    }

    @Override
    public void reset() {
        tdc = TopScoreDocCollector.create(size, outOfOrder);
        topDocs = null;
    }
}
