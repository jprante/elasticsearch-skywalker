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
package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.internal.InternalClient;

/**
 * A request builder for reconstructing deleted documents.
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ReconstructIndexRequestBuilder extends BroadcastOperationRequestBuilder<ReconstructIndexRequest, ReconstructIndexResponse, ReconstructIndexRequestBuilder> {

    /**
     * Constructor
     *
     * @param client
     */
    public ReconstructIndexRequestBuilder(IndicesAdminClient client) {
        super((InternalClient) client, new ReconstructIndexRequest());
    }

    /**
     * Execute action.
     *
     * @param listener a response listener
     */
    @Override
    protected void doExecute(ActionListener<ReconstructIndexResponse> listener) {
        ((IndicesAdminClient) client).execute(ReconstructIndexAction.INSTANCE, request, listener);
    }
}
