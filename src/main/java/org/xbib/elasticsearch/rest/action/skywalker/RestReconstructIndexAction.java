
package org.xbib.elasticsearch.rest.action.skywalker;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.support.RestResponseListener;
import org.xbib.elasticsearch.action.admin.indices.reconstruct.ReconstructIndexAction;
import org.xbib.elasticsearch.action.admin.indices.reconstruct.ReconstructIndexRequest;
import org.xbib.elasticsearch.action.admin.indices.reconstruct.ReconstructIndexResponse;
import org.xbib.elasticsearch.action.admin.indices.reconstruct.ShardReconstructIndexResponse;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

/**
 *  REST action for reconstructing an index
 */
public class RestReconstructIndexAction extends BaseRestHandler {

    @Inject
    public RestReconstructIndexAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(GET, "/{index}/_skywalker/reconstruct", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        ReconstructIndexRequest r = new ReconstructIndexRequest(request.param("index"));
        client.admin().indices().execute(ReconstructIndexAction.INSTANCE, r, new RestResponseListener<ReconstructIndexResponse>(channel) {
            @Override
            public RestResponse buildResponse(ReconstructIndexResponse response) throws Exception {
                XContentBuilder builder = jsonBuilder();
                builder.startObject()
                        .field("ok", true)
                        .field("index", request.param("index"))
                        .startArray("shards");
                for (ShardReconstructIndexResponse r : response.shards()) {
                    XContentParser p = XContentHelper.createParser(r.getReconstructedIndex().bytes());
                    builder.copyCurrentStructure(p);
                }
                builder.endArray();
                builder.endObject();
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}
