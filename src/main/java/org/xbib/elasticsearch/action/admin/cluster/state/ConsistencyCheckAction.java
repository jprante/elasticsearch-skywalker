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
package org.xbib.elasticsearch.action.admin.cluster.state;

import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.client.ClusterAdminClient;

/**
 * Consistency check action
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ConsistencyCheckAction extends ClusterAction<ConsistencyCheckRequest, ConsistencyCheckResponse, ConsistencyCheckRequestBuilder> {

    public static final ConsistencyCheckAction INSTANCE = new ConsistencyCheckAction();
    public static final String NAME = "cluster/state/consistencycheck";

    private ConsistencyCheckAction() {
        super(NAME);
    }

    @Override
    public ConsistencyCheckResponse newResponse() {
        return new ConsistencyCheckResponse();
    }

    @Override
    public ConsistencyCheckRequestBuilder newRequestBuilder(ClusterAdminClient client) {
        return new ConsistencyCheckRequestBuilder(client);
    }
}
