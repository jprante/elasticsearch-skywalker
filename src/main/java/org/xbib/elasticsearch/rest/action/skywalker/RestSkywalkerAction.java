
package org.xbib.elasticsearch.rest.action.skywalker;

import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.support.RestResponseListener;
import org.xbib.elasticsearch.action.skywalker.SkywalkerAction;
import org.xbib.elasticsearch.action.skywalker.SkywalkerRequest;
import org.xbib.elasticsearch.action.skywalker.SkywalkerResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.support.RestActions.buildBroadcastShardsHeader;

/**
 *  REST skywalker action
 */
public class RestSkywalkerAction extends BaseRestHandler {

    @Inject
    public RestSkywalkerAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(POST, "/_skywalker", this);
        controller.registerHandler(POST, "/{index}/_skywalker", this);
        controller.registerHandler(GET, "/_skywalker", this);
        controller.registerHandler(GET, "/{index}/_skywalker", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        SkywalkerRequest r = new SkywalkerRequest(Strings.splitStringByCommaToArray(request.param("index")));
        client.admin().cluster().execute(SkywalkerAction.INSTANCE, r, new RestResponseListener<SkywalkerResponse>(channel) {
            @Override
            public RestResponse buildResponse(SkywalkerResponse response) throws Exception {
                XContentBuilder builder = jsonBuilder();
                builder.startObject();
                builder.field("ok", true);
                buildBroadcastShardsHeader(builder, response);
                builder.field("result", response.getResponse());
                builder.endObject();
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}
