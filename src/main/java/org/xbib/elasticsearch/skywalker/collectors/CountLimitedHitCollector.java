
package org.xbib.elasticsearch.skywalker.collectors;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;

import java.io.IOException;

/**
 *  Count limited hit collector
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
