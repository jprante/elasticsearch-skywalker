package org.xbib.elasticsearch.action.admin.indices.reconstruct;

import org.apache.lucene.index.IndexReader;
import org.elasticsearch.ElasticsearchException;
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
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.shard.service.InternalIndexShard;
import org.elasticsearch.indices.IndicesService;
import org.xbib.elasticsearch.skywalker.reconstruct.DocumentReconstructor;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.elasticsearch.common.collect.Lists.newArrayList;

/**
 *  Transport reconstruct index action
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
        final List<ShardReconstructIndexResponse> shards = newArrayList();
        for (int i = 0; i < shardsResponses.length(); i++) {
            Object shardResponse = shardsResponses.get(i);
            if (shardResponse == null) {
                // a non active shard, ignore...
            } else if (shardResponse instanceof BroadcastShardOperationFailedException) {
                failedShards++;
                if (shardFailures == null) {
                    shardFailures = newArrayList();
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
    protected ShardReconstructIndexResponse shardOperation(ShardReconstructIndexRequest request) throws ElasticsearchException {
        IndexService indexService = indicesService.indexService(request.index());
        InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(request.shardId());
        Engine.Searcher searcher = indexShard.engine().acquireSearcher("transport_reconstruct");
        IndexReader reader = searcher.reader();
        DocumentReconstructor dr = new DocumentReconstructor(reader);
        try {
            return new ShardReconstructIndexResponse(true, dr.reconstruct(request.shardId()));
        } catch (IOException e) {
            throw new ElasticsearchException("failed to reconstruct index", e);
        } finally {
            searcher.release();
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
