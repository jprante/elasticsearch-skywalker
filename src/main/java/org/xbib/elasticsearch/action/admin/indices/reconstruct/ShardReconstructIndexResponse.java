
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