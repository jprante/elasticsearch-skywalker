
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;

/**
 * A request to skywalk one or more indices.
 */
public class SkywalkerRequestBuilder extends BroadcastOperationRequestBuilder<SkywalkerRequest, SkywalkerResponse, SkywalkerRequestBuilder> {

    /**
     * Constructor
     *
     * @param client
     */
    public SkywalkerRequestBuilder(InternalGenericClient client) {
        super(client, new SkywalkerRequest());
    }

    /**
     * Execute Skywalker action.
     *
     * @param listener a response listener
     */
    @Override
    protected void doExecute(ActionListener<SkywalkerResponse> listener) {
        ((Client) client).execute(SkywalkerAction.INSTANCE, request, listener);
    }
}
