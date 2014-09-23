
package org.xbib.elasticsearch.action.admin.cluster.state;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;

/**
 *  Consistency check request builder
 *
 */
public class ConsistencyCheckRequestBuilder extends ActionRequestBuilder<ConsistencyCheckRequest, ConsistencyCheckResponse, ConsistencyCheckRequestBuilder, ClusterAdminClient> {

    public ConsistencyCheckRequestBuilder(ClusterAdminClient clusterClient) {
        super(clusterClient, new ConsistencyCheckRequest());
    }

    @Override
    protected void doExecute(ActionListener<ConsistencyCheckResponse> listener) {
        client.execute(ConsistencyCheckAction.INSTANCE, request, listener);
    }
}
