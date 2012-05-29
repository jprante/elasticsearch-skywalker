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
package org.elasticsearch.skywalker.collectors;

public class LimitedException extends RuntimeException {

    private int limitType;
    private long limitSize;
    private int lastDoc;
    private long currentSize;

    public LimitedException(int limitType, long limitSize, long currentSize, int lastDoc) {
        super();
        this.limitType = limitType;
        this.limitSize = limitSize;
        this.currentSize = currentSize;
        this.lastDoc = lastDoc;
    }

    /**
     * @return the limitType
     */
    public int getLimitType() {
        return limitType;
    }

    /**
     * @param limitType the limitType to set
     */
    public void setLimitType(int limitType) {
        this.limitType = limitType;
    }

    /**
     * @return the limitSize
     */
    public long getLimitSize() {
        return limitSize;
    }

    /**
     * @param limitSize the limitSize to set
     */
    public void setLimitSize(long limitSize) {
        this.limitSize = limitSize;
    }

    /**
     * @return the currentSize
     */
    public long getCurrentSize() {
        return currentSize;
    }

    /**
     * @param currentSize the currentSize to set
     */
    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    /**
     * @return the lastDoc
     */
    public int getLastDoc() {
        return lastDoc;
    }

    /**
     * @param lastDoc the lastDoc to set
     */
    public void setLastDoc(int lastDoc) {
        this.lastDoc = lastDoc;
    }
}
