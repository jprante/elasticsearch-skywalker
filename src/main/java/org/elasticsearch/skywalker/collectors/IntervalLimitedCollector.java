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
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;

import java.io.IOException;

public class IntervalLimitedCollector extends LimitedHitCollector {

    private long maxTime;
    private long lastDoc = 0;
    private TopScoreDocCollector tdc;
    private TopDocs topDocs = null;
    private TimeLimitingCollector thc;

    public IntervalLimitedCollector(int maxTime, boolean outOfOrder, boolean shouldScore) {
        this.maxTime = maxTime;
        this.outOfOrder = outOfOrder;
        this.shouldScore = shouldScore;
        tdc = TopScoreDocCollector.create(1000, outOfOrder);
        thc = new TimeLimitingCollector(tdc, TimeLimitingCollector.getGlobalCounter(), maxTime);
    }

    @Override
    public long limitSize() {
        return maxTime;
    }

    @Override
    public int limitType() {
        return TYPE_TIME;
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
    public void collect(int docNum) throws IOException {
        try {
            thc.collect(docNum);
        } catch (TimeExceededException tee) {
            // re-throw
            throw new LimitedException(TYPE_TIME, maxTime, tee.getTimeElapsed(), tee.getLastDocCollected());
        }
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return outOfOrder;
    }

    @Override
    public void setNextReader(IndexReader r, int base) throws IOException {
        this.docBase = base;
        thc.setNextReader(r, base);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
        if (shouldScore) {
            thc.setScorer(scorer);
        } else {
            thc.setScorer(NoScoringScorer.INSTANCE);
        }
    }

    @Override
    public void reset() {
        lastDoc = 0;
        tdc = TopScoreDocCollector.create(1000, outOfOrder);
        thc = new TimeLimitingCollector(tdc, TimeLimitingCollector.getGlobalCounter(), maxTime);
    }
}
