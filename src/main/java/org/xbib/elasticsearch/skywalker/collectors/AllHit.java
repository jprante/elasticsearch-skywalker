
package org.xbib.elasticsearch.skywalker.collectors;

/**
 *  All hit
 */
public class AllHit {

    private int docId;
    private float score;

    public AllHit(int docId, float score) {
        this.docId = docId;
        this.score = score;
    }

    public int docId() {
        return docId;
    }

    public float score() {
        return score;
    }
}
