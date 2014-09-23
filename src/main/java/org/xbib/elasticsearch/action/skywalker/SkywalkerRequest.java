
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.support.broadcast.BroadcastOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class SkywalkerRequest extends BroadcastOperationRequest<SkywalkerRequest> {

    SkywalkerRequest() {
    }

    public SkywalkerRequest(String... indices) {
        super(indices);
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
