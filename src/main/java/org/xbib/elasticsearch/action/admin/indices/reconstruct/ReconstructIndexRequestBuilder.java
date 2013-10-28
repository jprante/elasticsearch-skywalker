
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.internal.InternalClient;

/**
 * A request builder for reconstructing deleted documents.
 */
public class ReconstructIndexRequestBuilder extends BroadcastOperationRequestBuilder<ReconstructIndexRequest, ReconstructIndexResponse, ReconstructIndexRequestBuilder> {

    /**
     * Constructor
     *
     * @param client
     */
    public ReconstructIndexRequestBuilder(IndicesAdminClient client) {
        super((InternalClient) client, new ReconstructIndexRequest());
    }

    /**
     * Execute action.
     *
     * @param listener a response listener
     */
    @Override
    protected void doExecute(ActionListener<ReconstructIndexResponse> listener) {
        ((IndicesAdminClient) client).execute(ReconstructIndexAction.INSTANCE, request, listener);
    }
}
