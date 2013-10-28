
package org.xbib.elasticsearch.skywalker.collectors;

import org.apache.lucene.search.Scorer;

import java.io.IOException;

/**
 *  No scoring scorer
 */
public class NoScoringScorer extends Scorer {

    public static final NoScoringScorer INSTANCE = new NoScoringScorer();

    protected NoScoringScorer() {
        super(null);
    }

    @Override
    public int freq() throws IOException {
        return 0;
    }

    @Override
    public float score() throws IOException {
        return 1.0f;
    }

    @Override
    public int advance(int doc) throws IOException {
        return 0;
    }

    @Override
    public int docID() {
        return 0;
    }

    @Override
    public int nextDoc() throws IOException {
        return 0;
    }

}
