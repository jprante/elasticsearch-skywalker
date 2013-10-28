
package org.xbib.elasticsearch.skywalker.collectors;

/**
 *  Limited hit collector
 */
public abstract class LimitedHitCollector extends AccessibleHitCollector {

    public static final int TYPE_TIME = 0;

    public static final int TYPE_SIZE = 1;

    public abstract int limitType();

    public abstract long limitSize();
}
