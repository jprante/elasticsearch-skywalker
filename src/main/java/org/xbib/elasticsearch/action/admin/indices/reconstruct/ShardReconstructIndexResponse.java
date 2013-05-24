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

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ShardReconstructIndexResponse extends BroadcastShardOperationResponse {

    private boolean exists;
    private XContentBuilder builder;

    ShardReconstructIndexResponse() {
    }

    ShardReconstructIndexResponse(boolean exists) {
        this.exists = exists;
    }

    ShardReconstructIndexResponse(boolean exists, XContentBuilder builder) {
        this.exists = exists;
        this.builder = builder;
    }

    public XContentBuilder getReconstructedIndex() {
        return builder;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        exists = in.readBoolean();
        if (in.readBoolean()) {
            builder = jsonBuilder();
            XContentParser p = XContentHelper.createParser(in.readBytesReference());
            builder.copyCurrentStructure(p);
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(exists);
        if (builder == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeBytesReference(builder.bytes());
        }
    }
}