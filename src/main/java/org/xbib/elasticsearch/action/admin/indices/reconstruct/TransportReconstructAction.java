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

import org.apache.lucene.index.IndexReader;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse;
import org.elasticsearch.action.support.broadcast.TransportBroadcastOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.GroupShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.shard.service.InternalIndexShard;
import org.elasticsearch.indices.IndicesService;
import org.xbib.elasticsearch.skywalker.reconstruct.DocumentReconstructor;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;


/**
 *  Transport reconstruct index action
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TransportReconstructAction extends TransportBroadcastOperationAction<ReconstructIndexRequest, ReconstructIndexResponse, ShardReconstructIndexRequest, BroadcastShardOperationResponse> {

    private final IndicesService indicesService;

    @Inject
    public TransportReconstructAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
                                      TransportService transportService, IndicesService indicesService) {
        super(settings, threadPool, clusterService, transportService);
        this.indicesService = indicesService;
    }

    @Override
    protected String transportAction() {
        return ReconstructIndexAction.NAME;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.GET;
    }

    @Override
    protected ReconstructIndexRequest newRequest() {
        return new ReconstructIndexRequest();
    }

    @Override
    protected ReconstructIndexResponse newResponse(ReconstructIndexRequest reconstructIndexRequest, AtomicReferenceArray shardsResponses, ClusterState clusterState) {
        int successfulShards = 0;
        int failedShards = 0;
        List<ShardOperationFailedException> shardFailures = null;
        final List<ShardReconstructIndexResponse> shards = Lists.newArrayList();
        for (int i = 0; i < shardsResponses.length(); i++) {
            Object shardResponse = shardsResponses.get(i);
            if (shardResponse == null) {
                // a non active shard, ignore...
            } else if (shardResponse instanceof BroadcastShardOperationFailedException) {
                failedShards++;
                if (shardFailures == null) {
                    shardFailures = Lists.newArrayList();
                }
                shardFailures.add(new DefaultShardOperationFailedException((BroadcastShardOperationFailedException) shardResponse));
            } else {
                shards.add((ShardReconstructIndexResponse)shardResponse);
                successfulShards++;
            }
        }
        return new ReconstructIndexResponse(shards, shardsResponses.length(), successfulShards, failedShards, shardFailures);
    }

    @Override
    protected ShardReconstructIndexRequest newShardRequest() {
        return new ShardReconstructIndexRequest();
    }

    @Override
    protected ShardReconstructIndexRequest newShardRequest(ShardRouting shardRouting, ReconstructIndexRequest reconstructIndexRequest) {
        return new ShardReconstructIndexRequest(shardRouting.index(), shardRouting.id(), reconstructIndexRequest);
    }

    @Override
    protected BroadcastShardOperationResponse newShardResponse() {
        return new ShardReconstructIndexResponse();
    }

    @Override
    protected ShardReconstructIndexResponse shardOperation(ShardReconstructIndexRequest request) throws ElasticSearchException {
        IndexService indexService = indicesService.indexService(request.index());
        InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(request.shardId());
        IndexReader reader = indexShard.engine().searcher().reader();
        DocumentReconstructor dr = new DocumentReconstructor(reader);
        try {
            return new ShardReconstructIndexResponse(true, dr.reconstruct(request.shardId()));
        } catch (IOException e) {
            throw new ElasticSearchException("failed to reconstruct index", e);
        }
    }

    @Override
    protected GroupShardsIterator shards(ClusterState clusterState, ReconstructIndexRequest reconstructIndexRequest, String[] concreteIndices) {
        return clusterState.routingTable().allActiveShardsGrouped(concreteIndices, true);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, ReconstructIndexRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, ReconstructIndexRequest reconstructIndexRequest, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
    }

}
