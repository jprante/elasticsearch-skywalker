
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Shard Skywalker request
 */
public class ShardSkywalkerRequest extends BroadcastShardOperationRequest {

    ShardSkywalkerRequest() {
    }

    public ShardSkywalkerRequest(String index, int shardId, SkywalkerRequest request) {
        super(index, shardId, request);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }
}