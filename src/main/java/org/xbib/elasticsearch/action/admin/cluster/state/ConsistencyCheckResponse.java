
package org.xbib.elasticsearch.action.admin.cluster.state;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Consistency check response
 */
public class ConsistencyCheckResponse extends ActionResponse {

    private ClusterName clusterName;

    private ClusterState clusterState;

    private List<File> files;

    public ConsistencyCheckResponse() {
    }

    ConsistencyCheckResponse(ClusterName clusterName, ClusterState clusterState, List<File> files) {
        this.clusterName = clusterName;
        this.clusterState = clusterState;
        this.files = files;
    }

    public ClusterState getState() {
        return this.clusterState;
    }

    public ClusterName getClusterName() {
        return this.clusterName;
    }

    public List<File> getFiles() {
        return this.files;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        clusterName = ClusterName.readClusterName(in);
        clusterState = ClusterState.Builder.readFrom(in, null);
        int n = in.read();
        files = new ArrayList();
        for (int i = 0; i < n; i++) {
            files.set(i, new File(in.readString()));
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        clusterName.writeTo(out);
        ClusterState.Builder.writeTo(clusterState, out);
        out.write(files.size());
        for (File file : files) {
            out.writeString(file.getAbsolutePath());
        }
    }
}
