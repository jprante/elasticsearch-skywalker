
package org.xbib.elasticsearch.skywalker.stats;

import java.util.Comparator;

/**
 *  Document frequence comparator for descending sort
 */
public class DocFreqComparatorSortDescending implements Comparator<TermStats> {

    public int compare(TermStats a, TermStats b) {
        if (a.docFreq() < b.docFreq()) {
            return 1;
        } else if (a.docFreq() > b.docFreq()) {
            return -1;
        } else {
            return 0;
        }
    }
}