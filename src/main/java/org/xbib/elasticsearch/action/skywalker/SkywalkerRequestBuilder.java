
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;

/**
 * A request to skywalk one or more indices.
 */
public class SkywalkerRequestBuilder extends ActionRequestBuilder<SkywalkerRequest, SkywalkerResponse, SkywalkerRequestBuilder, ClusterAdminClient> {

    /**
     * Constructor
     *
     * @param client
     */
    public SkywalkerRequestBuilder(ClusterAdminClient client) {
        super(client, new SkywalkerRequest());
    }

    /**
     * Execute Skywalker action.
     *
     * @param listener a response listener
     */
    @Override
    protected void doExecute(ActionListener<SkywalkerResponse> listener) {
        client.execute(SkywalkerAction.INSTANCE, request, listener);
    }
}
