
package org.xbib.elasticsearch.skywalker.collectors;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllHitsCollector extends AccessibleHitCollector {

    private List<AllHit> hits = new ArrayList();

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
        return hits.get(i).docId();
    }

    @Override
    public float getScore(int i) {
        return hits.get(i).score();
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return outOfOrder;
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
    }

    @Override
    public void reset() {
        hits.clear();
    }

}