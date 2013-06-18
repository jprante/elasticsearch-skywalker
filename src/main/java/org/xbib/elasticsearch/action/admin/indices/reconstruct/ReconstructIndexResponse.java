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
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;


/**
 * A response for a reconstruct action.
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ReconstructIndexResponse extends BroadcastOperationResponse {

    protected List<ShardReconstructIndexResponse> shards;

    ReconstructIndexResponse() {
    }

    ReconstructIndexResponse(List<ShardReconstructIndexResponse> shards, int totalShards, int successfulShards, int failedShards, List<ShardOperationFailedException> shardFailures) {
        super(totalShards, successfulShards, failedShards, shardFailures);
        this.shards = shards;
    }

    public List<ShardReconstructIndexResponse> shards() {
        return shards;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        int n = in.readVInt();
        shards = Lists.newArrayList();
        for (int i = 0; i < n; i++) {
            ShardReconstructIndexResponse r = new ShardReconstructIndexResponse();
            r.readFrom(in);
            shards.add(r);
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeVInt(shards.size());
        for (ShardReconstructIndexResponse r : shards) {
            r.writeTo(out);
        }
    }
}