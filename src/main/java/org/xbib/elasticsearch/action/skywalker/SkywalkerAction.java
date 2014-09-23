
package org.xbib.elasticsearch.action.skywalker;

import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

/**
 * Skywalker action
 */
public class SkywalkerAction extends ClusterAction<SkywalkerRequest, SkywalkerResponse, SkywalkerRequestBuilder> {

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
    public SkywalkerRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new SkywalkerRequestBuilder(client);
    }
}
