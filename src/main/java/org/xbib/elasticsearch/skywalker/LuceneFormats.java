/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.elasticsearch.skywalker;

/**
 * Lucene format constants
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface LuceneFormats {

    // old version constants
    int OLD_FORMAT = -1;

    /**
     * This format adds details used for lockless commits.  It differs
     * slightly from the previous format in that file names
     * are never re-used (write once).  Instead, each file is
     * written to the next generation.  For example,
     * segments_1, segments_2, etc.  This allows us to not use
     * a commit lock.  See <a
     * href="http://lucene.apache.org/java/docs/fileformats.html">file
     * formats</a> for details.
     */
    int FORMAT_LOCKLESS = -2;

    /**
     * This format adds a "hasSingleNormFile" flag into each segment info.
     * See <a href="http://issues.apache.org/jira/browse/LUCENE-756">LUCENE-756</a>
     * for details.
     */
    int FORMAT_SINGLE_NORM_FILE = -3;

    /**
     * This format allows multiple segments to share a single
     * vectors and stored fields file.
     */
    int FORMAT_SHARED_DOC_STORE = -4;

    /**
     * This format adds a checksum at the end of the file to
     * ensure all bytes were successfully written.
     */
    int FORMAT_CHECKSUM = -5;

    /**
     * This format adds the deletion count for each segment.
     * This way IndexWriter can efficiently report numDocs().
     */
    int FORMAT_DEL_COUNT = -6;

    /**
     * This format adds the boolean hasProx to record if any
     * fields in the segment store prox information (ie, have
     * omitTermFreqAndPositions==false)
     */
    int FORMAT_HAS_PROX = -7;

    /**
     * This format adds optional commit userData (String) storage.
     */
    int FORMAT_USER_DATA = -8;

    /**
     * This format adds optional per-segment String
     * diagnostics storage, and switches userData to Map
     */
    int FORMAT_DIAGNOSTICS = -9;

    /**
     * Each segment records whether it has term vectors
     */
    int FORMAT_HAS_VECTORS = -10;

    /**
     * Each segment records the Lucene version that created it.
     */
    int FORMAT_3_1 = -11;
    /**
     * Some early 4.0 pre-alpha
     */
    int FORMAT_PRE_4 = -12;
}
