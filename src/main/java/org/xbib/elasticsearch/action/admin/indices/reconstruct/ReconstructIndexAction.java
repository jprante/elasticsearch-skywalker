
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.client.IndicesAdminClient;

/**
 * Reconstruct document action
 */
public class ReconstructIndexAction extends IndicesAction<ReconstructIndexRequest, ReconstructIndexResponse, ReconstructIndexRequestBuilder> {

    public static final ReconstructIndexAction INSTANCE = new ReconstructIndexAction();
    public static final String NAME = "indices/reconstruct";

    private ReconstructIndexAction() {
        super(NAME);
    }

    @Override
    public ReconstructIndexResponse newResponse() {
        return new ReconstructIndexResponse();
    }

    @Override
    public ReconstructIndexRequestBuilder newRequestBuilder(IndicesAdminClient client) {
        return new ReconstructIndexRequestBuilder(client);
    }
}
