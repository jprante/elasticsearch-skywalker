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

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.support.master.TransportMasterNodeOperationAction;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.Index;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.skywalker.Skywalker;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.cluster.ClusterState.newClusterStateBuilder;

/**
 *  Transport consistency check action
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TransportConsistencyCheckAction extends TransportMasterNodeOperationAction<ConsistencyCheckRequest, ConsistencyCheckResponse> {

    private final ClusterName clusterName;

    private final NodeEnvironment nodeEnv;

    @Inject
    public TransportConsistencyCheckAction(Settings settings, TransportService transportService, ClusterService clusterService, ThreadPool threadPool,
                                           ClusterName clusterName, NodeEnvironment nodeEnvironment) {
        super(settings, transportService, clusterService, threadPool);
        this.clusterName = clusterName;
        this.nodeEnv = nodeEnvironment;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.GENERIC;
    }

    @Override
    protected String transportAction() {
        return ConsistencyCheckAction.NAME;
    }

    @Override
    protected ConsistencyCheckRequest newRequest() {
        return new ConsistencyCheckRequest();
    }

    @Override
    protected ConsistencyCheckResponse newResponse() {
        return new ConsistencyCheckResponse();
    }

    @Override
    protected boolean localExecute(ConsistencyCheckRequest request) {
        return true;
    }

    @Override
    protected ConsistencyCheckResponse masterOperation(ConsistencyCheckRequest request, ClusterState state) throws ElasticSearchException {
        ClusterState.Builder builder = newClusterStateBuilder();
        List<File> files = new ArrayList();
        builder.metaData(Skywalker.loadState(files, nodeEnv));
        return new ConsistencyCheckResponse(clusterName, builder.build(), files);
    }

}
