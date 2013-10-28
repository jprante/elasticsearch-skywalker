
package org.xbib.elasticsearch.skywalker.stats;

import org.apache.lucene.util.PriorityQueue;

/**
 *  Term stats queue
 */
public class TermStatsQueue extends PriorityQueue<TermStats> {

    public TermStatsQueue(int size) {
        super(size);
    }

    @Override
    protected boolean lessThan(TermStats termInfoA, TermStats termInfoB) {
        return termInfoA.docFreq() < termInfoB.docFreq();
    }
}