
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Shard Skywalker request
 */
public class ShardReconstructIndexRequest extends BroadcastShardOperationRequest {

    ShardReconstructIndexRequest() {
    }

    public ShardReconstructIndexRequest(String index, int shardId, ReconstructIndexRequest request) {
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