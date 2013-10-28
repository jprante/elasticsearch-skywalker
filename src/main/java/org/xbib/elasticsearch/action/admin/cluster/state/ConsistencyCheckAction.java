
package org.xbib.elasticsearch.action.admin.cluster.state;

import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

/**
 * Consistency check action
 */
public class ConsistencyCheckAction extends ClusterAction<ConsistencyCheckRequest, ConsistencyCheckResponse, ConsistencyCheckRequestBuilder> {

    public static final ConsistencyCheckAction INSTANCE = new ConsistencyCheckAction();
    public static final String NAME = "cluster/state/consistencycheck";

    private ConsistencyCheckAction() {
        super(NAME);
    }

    @Override
    public ConsistencyCheckResponse newResponse() {
        return new ConsistencyCheckResponse();
    }

    @Override
    public ConsistencyCheckRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new ConsistencyCheckRequestBuilder(client);
    }
}
