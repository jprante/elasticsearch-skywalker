
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;

/**
 * Skywalker action
 */
public class SkywalkerAction extends Action<SkywalkerRequest, SkywalkerResponse, SkywalkerRequestBuilder> {

    public static final SkywalkerAction INSTANCE = new SkywalkerAction();

    public static final String NAME = "indices/skywalker";

    private SkywalkerAction() {
        super(NAME);
    }

    @Override
    public SkywalkerResponse newResponse() {
        return new SkywalkerResponse();
    }

    @Override
    public SkywalkerRequestBuilder newRequestBuilder(Client client) {
        return new SkywalkerRequestBuilder((InternalGenericClient) client);
    }
}
