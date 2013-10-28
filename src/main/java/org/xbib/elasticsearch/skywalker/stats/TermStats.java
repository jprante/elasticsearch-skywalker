
package org.xbib.elasticsearch.skywalker.stats;

import org.apache.lucene.util.BytesRef;

/**
 * Term stats
 */
public class TermStats {

    private String field;
    private BytesRef termtext;
    private int docFreq;

    public TermStats text(BytesRef text) {
        this.termtext =  text;
        return this;
    }

    public String text() {
        return termtext.utf8ToString();
    }

    public TermStats field(String field) {
        this.field = field;
        return this;
    }

    public String field() {
        return field;
    }

    public TermStats docFreq(int docFreq) {
        this.docFreq = docFreq;
        return this;
    }

    public int docFreq() {
        return docFreq;
    }

    public String toString() {
        return field + ":" + termtext.utf8ToString() + ":" + docFreq;
    }
}
