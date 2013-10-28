
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.support.broadcast.BroadcastOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class ReconstructIndexRequest extends BroadcastOperationRequest<ReconstructIndexRequest> {

    private String index;

    ReconstructIndexRequest() {
    }

    public ReconstructIndexRequest(String index) {
        this.index = index;
    }

    public String index() {
        return index;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        this.index = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(index);
    }
}
