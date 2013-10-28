
package org.xbib.elasticsearch.skywalker.reconstruct;

import java.util.Comparator;

/**
 *  Document term comparator
 */
public class DocumentTermComparator implements Comparator<DocumentTerm> {
    private boolean ascending;
    private boolean byText;

    public DocumentTermComparator(boolean byText, boolean ascending) {
        this.ascending = ascending;
        this.byText = byText;
    }

    public int compare(DocumentTerm h1, DocumentTerm h2) {
        if (byText) {
            return ascending ? h1.text().compareTo(h2.text()) : h2.text().compareTo(h1.text());
        } else {
            if (h1.count() > h2.count()) {
                return ascending ? -1 : 1;
            }
            if (h1.count() < h2.count()) {
                return ascending ? 1 : -1;
            }
        }
        return 0;
    }
}