
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.common.collect.Lists.newArrayList;

/**
 * A response for a reconstruct action.
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
        shards = newArrayList();
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