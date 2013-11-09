
package org.xbib.elasticsearch.rest.action.skywalker;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.Strings;
import org.xbib.elasticsearch.action.skywalker.SkywalkerAction;
import org.xbib.elasticsearch.action.skywalker.SkywalkerRequest;
import org.xbib.elasticsearch.action.skywalker.SkywalkerResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

import java.io.IOException;

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
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        SkywalkerRequest r = new SkywalkerRequest(Strings.splitStringByCommaToArray(request.param("index")));
        client.execute(SkywalkerAction.INSTANCE, r, new ActionListener<SkywalkerResponse>() {

            @Override
            public void onResponse(SkywalkerResponse response) {
                try {
                    XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
                    builder.startObject();
                    builder.field("ok", true);
                    buildBroadcastShardsHeader(builder, response);
                    builder.field("result", response.getResponse());
                    builder.endObject();
                    channel.sendResponse(new XContentRestResponse(request, OK, builder));
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                try {
                    logger.error(e.getMessage(), e);
                    channel.sendResponse(new XContentThrowableRestResponse(request, e));
                } catch (IOException e1) {
                    logger.error("Failed to send failure response", e1);
                }
            }
        });
    }
}