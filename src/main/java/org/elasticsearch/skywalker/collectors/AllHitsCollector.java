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

import java.io.IOException;
import java.util.ArrayList;

public class AllHitsCollector extends AccessibleHitCollector {

    private ArrayList<AllHit> hits = new ArrayList();

    public AllHitsCollector(boolean outOfOrder, boolean shouldScore) {
        this.outOfOrder = outOfOrder;
        this.shouldScore = shouldScore;
    }

    @Override
    public void collect(int doc) {
        float score = 1.0f;
        if (shouldScore) {
            try {
                score = scorer.score();
            } catch (IOException e) {
            }
        }
        hits.add(new AllHit(docBase + doc, score));
    }

    @Override
    public int getTotalHits() {
        return hits.size();
    }

    @Override
    public int getDocId(int i) {
        return hits.get(i).docId;
    }

    @Override
    public float getScore(int i) {
        return hits.get(i).score;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return outOfOrder;
    }

    @Override
    public void setNextReader(IndexReader r, int docBase) throws IOException {
        this.docBase = docBase;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
    }

    @Override
    public void reset() {
        hits.clear();
    }

    private static class AllHit {

        public int docId;
        public float score;

        public AllHit(int docId, float score) {
            this.docId = docId;
            this.score = score;
        }
    }
}