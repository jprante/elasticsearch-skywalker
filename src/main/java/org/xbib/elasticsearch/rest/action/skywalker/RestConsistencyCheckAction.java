/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.elasticsearch.rest.action.skywalker;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.joda.time.Instant;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.SizeUnit;
import org.elasticsearch.common.unit.SizeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;
import org.xbib.elasticsearch.action.admin.cluster.state.ConsistencyCheckAction;
import org.xbib.elasticsearch.action.admin.cluster.state.ConsistencyCheckRequest;
import org.xbib.elasticsearch.action.admin.cluster.state.ConsistencyCheckResponse;

import java.io.File;
import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

/**
 * REST consistency check action
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class RestConsistencyCheckAction extends BaseRestHandler {

    @Inject
    public RestConsistencyCheckAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(GET, "/_cluster/consistency", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        ConsistencyCheckRequest r = new ConsistencyCheckRequest();
        client.admin().cluster().execute(ConsistencyCheckAction.INSTANCE, r, new ActionListener<ConsistencyCheckResponse>() {

            @Override
            public void onResponse(ConsistencyCheckResponse response) {
                try {
                    XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
                    builder.startObject();
                    builder.field("ok", true);
                    builder.startObject("state");
                    response.getState().toXContent(builder, ToXContent.EMPTY_PARAMS);
                    builder.startArray("files");
                    for (File file : response.getFiles()) {
                        Instant instant = new Instant(file.lastModified());
                        builder.startObject()
                            .field("path", file.getAbsolutePath())
                            .field("lastmodified", instant.toDateTime().toString())
                            .field("size", new SizeValue(file.length(), SizeUnit.SINGLE).toString())
                            .field("totalspace", new SizeValue(file.getTotalSpace(), SizeUnit.SINGLE).toString())
                            .field("usablespace", new SizeValue(file.getUsableSpace(), SizeUnit.SINGLE).toString())
                            .field("freespace", new SizeValue(file.getFreeSpace(), SizeUnit.SINGLE).toString())
                        .endObject();
                    }
                    builder.endArray();
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