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
package org.xbib.elasticsearch.skywalker.collectors;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;

import java.io.IOException;

/**
 *  Count limited hit collector
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class CountLimitedHitCollector extends LimitedHitCollector {

    private int maxSize;
    private int count;
    private int lastDoc;
    private TopScoreDocCollector tdc;
    private TopDocs topDocs = null;

    public CountLimitedHitCollector(int maxSize, boolean outOfOrder, boolean shouldScore) {
        this.maxSize = maxSize;
        this.outOfOrder = outOfOrder;
        this.shouldScore = shouldScore;
        count = 0;
        tdc = TopScoreDocCollector.create(maxSize, outOfOrder);
    }

    @Override
    public long limitSize() {
        return maxSize;
    }

    @Override
    public int limitType() {
        return TYPE_SIZE;
    }

    @Override
    public void collect(int doc) throws IOException {
        count++;
        if (count > maxSize) {
            count--;
            throw new LimitedException(TYPE_SIZE, maxSize, count, lastDoc);
        }
        lastDoc = docBase + doc;

        tdc.collect(doc);
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
        return count;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return tdc.acceptsDocsOutOfOrder();
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
        tdc.setNextReader(context);
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
        count = 0;
        lastDoc = 0;
        topDocs = null;
        tdc = TopScoreDocCollector.create(maxSize, outOfOrder);
    }
}
