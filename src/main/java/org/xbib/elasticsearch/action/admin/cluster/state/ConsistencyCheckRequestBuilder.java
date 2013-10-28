
package org.xbib.elasticsearch.action.admin.cluster.state;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.master.MasterNodeOperationRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.internal.InternalClusterAdminClient;

/**
 *  Consistency check request builder
 *
 */
public class ConsistencyCheckRequestBuilder extends MasterNodeOperationRequestBuilder<ConsistencyCheckRequest, ConsistencyCheckResponse, ConsistencyCheckRequestBuilder> {

    public ConsistencyCheckRequestBuilder(ClusterAdminClient clusterClient) {
        super((InternalClusterAdminClient) clusterClient, new ConsistencyCheckRequest());
    }

    @Override
    protected void doExecute(ActionListener<ConsistencyCheckResponse> listener) {
        ((ClusterAdminClient) client).execute(ConsistencyCheckAction.INSTANCE, request, listener);
    }
}
