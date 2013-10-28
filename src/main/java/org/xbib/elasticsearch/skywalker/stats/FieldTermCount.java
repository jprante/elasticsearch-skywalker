
package org.xbib.elasticsearch.skywalker.stats;

/**
 *  Field term count
 */
public class FieldTermCount implements Comparable<FieldTermCount> {

    private String fieldname;

    private long termCount;

    public FieldTermCount(String fieldname, long termCount) {
        this.fieldname = fieldname;
        this.termCount = termCount;
    }

    public String getFieldname() {
        return fieldname;
    }

    public long getTermCount() {
        return termCount;
    }

    public int compareTo(FieldTermCount f2) {
        if (termCount > f2.termCount) {
            return -1;
        } else if (termCount < f2.termCount) {
            return 1;
        } else {
            return 0;
        }
    }
}
