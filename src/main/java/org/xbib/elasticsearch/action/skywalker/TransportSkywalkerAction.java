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
package org.xbib.elasticsearch.action.skywalker;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.Directory;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.TransportBroadcastOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.GroupShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.engine.Segment;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.shard.service.InternalIndexShard;
import org.elasticsearch.indices.IndicesService;
import org.xbib.elasticsearch.skywalker.stats.FieldTermCount;
import org.xbib.elasticsearch.skywalker.FormatDetails;
import org.xbib.elasticsearch.skywalker.Skywalker;
import org.xbib.elasticsearch.skywalker.stats.TermStats;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.elasticsearch.common.collect.Lists.newArrayList;

/**
 * Transport action for Skywalker plugin.
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TransportSkywalkerAction
        extends TransportBroadcastOperationAction<SkywalkerRequest, SkywalkerResponse, ShardSkywalkerRequest, ShardSkywalkerResponse> {

    private final IndicesService indicesService;

    private final NodeEnvironment nodeEnv;

    private final Object mutex = new Object();

    @Inject
    public TransportSkywalkerAction(Settings settings, ThreadPool threadPool,
                                    ClusterService clusterService,
                                    TransportService transportService,
                                    IndicesService indicesService,
                                    NodeEnvironment nodeEnv) {
        super(settings, threadPool, clusterService, transportService);
        this.indicesService = indicesService;
        this.nodeEnv = nodeEnv;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.MERGE;
    }

    @Override
    protected String transportAction() {
        return SkywalkerAction.NAME;
    }

    @Override
    protected SkywalkerRequest newRequest() {
        return new SkywalkerRequest();
    }

    @Override
    protected boolean ignoreNonActiveExceptions() {
        return true;
    }

    @Override
    protected SkywalkerResponse newResponse(SkywalkerRequest request, AtomicReferenceArray shardsResponses, ClusterState clusterState) {
        int successfulShards = 0;
        int failedShards = 0;
        List<ShardOperationFailedException> shardFailures = null;
        Map<String, Map<String, Map<String, Object>>> response = new HashMap();
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
                successfulShards++;
                if (shardResponse instanceof ShardSkywalkerResponse) {
                    ShardSkywalkerResponse shardResp = (ShardSkywalkerResponse) shardResponse;
                    String index = shardResp.getIndex();
                    int shardId = shardResp.getShardId();
                    // one map per index
                    Map indexresponse = response.get(index);
                    if (indexresponse == null) {
                        indexresponse = new HashMap();
                    }
                    // merge index-wide fieldInfo into single field
                    //indexresponse.put("fieldInfos", shardResp.getResponse().get("fieldInfos"));
                    //shardResp.getResponse().remove("fieldInfos");
                    indexresponse.put(Integer.toString(shardId), shardResp.getResponse());
                    response.put(index, indexresponse);
                }
            }
        }
        return new SkywalkerResponse(shardsResponses.length(), successfulShards, failedShards, shardFailures).setResponse(response);
    }

    @Override
    protected ShardSkywalkerRequest newShardRequest() {
        return new ShardSkywalkerRequest();
    }

    @Override
    protected ShardSkywalkerRequest newShardRequest(ShardRouting shard, SkywalkerRequest request) {
        return new ShardSkywalkerRequest(shard.index(), shard.id(), request);
    }

    @Override
    protected ShardSkywalkerResponse newShardResponse() {
        return new ShardSkywalkerResponse();
    }

    @Override
    protected ShardSkywalkerResponse shardOperation(ShardSkywalkerRequest request) throws ElasticSearchException {
        synchronized (mutex) {
            try {
                IndexService indexService = indicesService.indexServiceSafe(request.index());
                InternalIndexShard indexShard = (InternalIndexShard) indexService.shardSafe(request.shardId());
                MapperService mapperService = indexService.mapperService();
                IndexReader reader = indexShard.searcher().reader();
                Skywalker skywalker = new Skywalker(reader);

                Map<String, Object> response = new HashMap();

                Directory directory = indexShard.store().directory();
                List indexFiles = new ArrayList();
                for (String f : skywalker.getIndexFiles(directory)) {
                    Map indexFile = new HashMap();
                    indexFile.put("name", f);
                    indexFile.put("function", skywalker.getFileFunction(f));
                    indexFiles.add(indexFile);
                }
                response.put("indexFiles", indexFiles);

                skywalker.getStoreMetadata(response, indexShard.store().list());

                response.put("indexVersion", skywalker.getVersion());
                response.put("directoryImpl", skywalker.getDirImpl());
                response.put("numDocs", reader.numDocs());
                response.put("maxDoc", reader.maxDoc());
                response.put("hasDeletions", reader.hasDeletions());
                response.put("numDeletedDocs", reader.numDeletedDocs());

                Set<FieldTermCount> ftc = skywalker.getFieldTermCounts();
                response.put("numTerms", skywalker.getNumTerms());

                Map indexFormatInfo = new HashMap();
                FormatDetails details = skywalker.getFormatDetails();
                indexFormatInfo.put("version", details.getVersion());
                indexFormatInfo.put("genericName", details.getGenericName());
                indexFormatInfo.put("capabilities", details.getCapabilities());
                response.put("indexFormat", indexFormatInfo);

                List commits = new ArrayList();
                Iterator<Segment> it = indexShard.engine().segments().iterator();
                while (it.hasNext()) {
                    Segment segment = it.next();
                    Map m = new HashMap();
                    m.put("segment", segment.getName());
                    m.put("count", segment.getNumDocs());
                    m.put("deleted", segment.getDeletedDocs());
                    m.put("generation", segment.getGeneration());
                    m.put("sizeInBytes", segment.getSizeInBytes());
                    m.put("version", segment.getVersion());
                    m.put("committed", segment.committed);
                    m.put("compound", segment.compound);
                    m.put("size", segment.getSize().toString());
                    commits.add(m);
                }
                response.put("commits", commits);

                List fieldInfos = new ArrayList();
                for (FieldInfo fi : MultiFields.getMergedFieldInfos(reader)) {
                    fieldInfos.add(skywalker.getFieldInfo(mapperService, fi));
                }
                response.put("fieldInfos", fieldInfos);

                List termList = new ArrayList();
                for (TermStats ts : skywalker.getTopTerms(50)) {
                    Map m = new HashMap();
                    m.put("field", ts.field());
                    m.put("text", ts.text());
                    m.put("docFreq", ts.docFreq());
                    termList.add(m);
                }
                response.put("topterms", termList);
                return new ShardSkywalkerResponse(request.index(), request.shardId()).setResponse(response);
            } catch (Exception ex) {
                throw new ElasticSearchException(ex.getMessage(), ex);
            }
        }
    }

    @Override
    protected GroupShardsIterator shards(ClusterState clusterState, SkywalkerRequest request, String[] concreteIndices) {
        return clusterState.routingTable().activePrimaryShardsGrouped(concreteIndices, true);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, SkywalkerRequest request, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.READ, concreteIndices);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, SkywalkerRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
    }

}