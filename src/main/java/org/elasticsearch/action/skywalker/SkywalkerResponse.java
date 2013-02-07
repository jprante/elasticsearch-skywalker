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
package org.elasticsearch.action.skywalker;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A response for a skywalker action.
 */
public class SkywalkerResponse extends BroadcastOperationResponse {

    private Map<String, Map<String, Map<String, Object>>> response;

    SkywalkerResponse() {
    }

    SkywalkerResponse(int totalShards, int successfulShards, int failedShards, List<ShardOperationFailedException> shardFailures) {
        super(totalShards, successfulShards, failedShards, shardFailures);
    }

    public SkywalkerResponse setResponse(Map<String, Map<String, Map<String, Object>>> response) {
        this.response = response;
        return this;
    }

    public Map<String, Map<String, Map<String, Object>>> getResponse() {
        return response;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        int indexCount = in.readInt();
        this.response = new HashMap();
        for (int i = 0; i < indexCount; i++) {
            String index = in.readString();
            Map<String, Map<String, Object>> shards = new HashMap();
            int shardCount = in.readInt();
            for (int j = 0; j < shardCount; j++) {
                String shard = in.readString();
                Map<String, Object> fields = in.readMap();
                shards.put(shard, fields);
            }
            response.put(index, shards);
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        int indexCount = response.keySet().size();
        out.writeInt(indexCount);
        for (String index : response.keySet()) {
            out.writeString(index);
            int shardCount = response.get(index).keySet().size();
            out.writeInt(shardCount);
            for (String shard : response.get(index).keySet()) {
                out.writeString(shard);
                out.writeMap(response.get(index).get(shard));
            }
        }
    }
}