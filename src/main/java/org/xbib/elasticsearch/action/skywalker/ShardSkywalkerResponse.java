
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.Map;

public class ShardSkywalkerResponse extends BroadcastShardOperationResponse {

    private Map<String, Object> response;

    ShardSkywalkerResponse() {
    }

    public ShardSkywalkerResponse(String index, int shardId) {
        super(index, shardId);
    }

    public ShardSkywalkerResponse setResponse(Map<String, Object> response) {
        this.response = response;
        return this;
    }

    public Map<String, Object> getResponse() {
        return response;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        response = in.readMap();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeMap(response);
    }
}