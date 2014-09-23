
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;

/**
 * A request builder for reconstructing deleted documents.
 */
public class ReconstructIndexRequestBuilder extends ActionRequestBuilder<ReconstructIndexRequest, ReconstructIndexResponse, ReconstructIndexRequestBuilder, IndicesAdminClient> {

    /**
     * Constructor
     *
     * @param client
     */
    public ReconstructIndexRequestBuilder(IndicesAdminClient client) {
        super(client, new ReconstructIndexRequest());
    }

    /**
     * Execute action.
     *
     * @param listener a response listener
     */
    @Override
    protected void doExecute(ActionListener<ReconstructIndexResponse> listener) {
        client.execute(ReconstructIndexAction.INSTANCE, request, listener);
    }
}
