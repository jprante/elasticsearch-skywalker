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
package org.elasticsearch.action.skywalker;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexGate;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.ReaderUtil;
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
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.joda.FormatDateTimeFormatter;
import org.elasticsearch.common.joda.Joda;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.shard.service.InternalIndexShard;
import org.elasticsearch.index.store.Store;
import org.elasticsearch.index.store.StoreFileMetaData;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.skywalker.FieldType;
import org.elasticsearch.skywalker.FieldType.Type;
import org.elasticsearch.skywalker.IndexInfo;
import org.elasticsearch.skywalker.TermInfo;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Transport action for Skywalker plugin.
 */
public class TransportSkywalkerAction
        extends TransportBroadcastOperationAction<SkywalkerRequest, SkywalkerResponse, ShardSkywalkerRequest, ShardSkywalkerResponse> {

    private static final String DEFAULT_DATE_TIME_FORMAT = "dateOptionalTime";
    private static final FormatDateTimeFormatter DATE_TIME_FORMATTER = Joda.forPattern(DEFAULT_DATE_TIME_FORMAT);
    private final IndicesService indicesService;
    private final Object mutex = new Object();

    @Inject
    public TransportSkywalkerAction(Settings settings, ThreadPool threadPool,
            ClusterService clusterService, TransportService transportService,
            IndicesService indicesService) {
        super(settings, threadPool, clusterService, transportService);
        this.indicesService = indicesService;
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
                    int shardId = shardResp.shardId();
                    // one map per index
                    Map indexresponse = response.get(index);
                    if (indexresponse == null) {
                        indexresponse = new HashMap();
                    }
                    // merge index-wide fieldInfo into single field
                    indexresponse.put("fieldInfos", shardResp.getResponse().get("fieldInfos"));
                    shardResp.getResponse().remove("fieldInfos");
                    // shard-wise data
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
                Map<String, Object> response = new HashMap();
                Store store = indexShard.store();
                Directory directory = store.directory();
                IndexReader reader = indexShard.searcher().reader();
                IndexInfo indexInfo = new IndexInfo(reader);
                getStoreMetadata(response, store.list());
                response.put("numDocs", reader.numDocs());
                response.put("maxDoc", reader.maxDoc());
                response.put("numDeletedDocs", reader.numDeletedDocs());
                response.put("numTerms", indexInfo.getNumTerms());
                response.put("hasDeletions", reader.hasDeletions());
                response.put("indexversion", indexInfo.getVersion());
                Map indexFormat = new HashMap();
                indexFormat.put("id", indexInfo.getIndexFormat());
                indexFormat.put("genericName", indexInfo.getFormatDetails().genericName);
                indexFormat.put("capabilities", indexInfo.getFormatDetails().capabilities);
                response.put("indexFormat", indexFormat);
                response.put("directoryImpl", indexInfo.getDirImpl());
                List commits = new ArrayList();
                for (IndexCommit ic : reader.listCommits(directory)) {
                    Map commitMap = new HashMap();
                    commitMap.put("segment", ic.getSegmentsFileName());
                    commitMap.put("count", ic.getSegmentCount());
                    commitMap.put("deleted", ic.isDeleted());
                    commitMap.put("files", ic.getFileNames());
                    commitMap.put("userdata", ic.getUserData());
                    commits.add(commitMap);
                }
                response.put("commits", commits);
                List fieldInfos = new ArrayList();
                Map<String, Type> typeMap = new HashMap();
                for (FieldInfo fi : ReaderUtil.getMergedFieldInfos(reader)) {
                    fieldInfos.add(getFieldInfo(indexService, fi, typeMap));
                }
                response.put("fieldInfos", fieldInfos);
                List termList = new ArrayList();
                for (TermInfo ti : indexInfo.getTopTerms()) {
                    Map tiMap = new HashMap();
                    tiMap.put("field", ti.term.field());
                    tiMap.put("text", decode(ti.term, typeMap));
                    tiMap.put("docFreq", ti.docFreq);
                    termList.add(tiMap);
                }
                response.put("topterms", termList);
                return new ShardSkywalkerResponse(request.index(), request.shardId()).setResponse(response);
            } catch (IOException ex) {
                throw new ElasticSearchException(ex.getMessage(), ex);
            }
        }
    }

    @Override
    protected GroupShardsIterator shards(ClusterState clusterState, SkywalkerRequest request, String[] concreteIndices) {
        return clusterState.routingTable().activePrimaryShardsGrouped(concreteIndices, true);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, SkywalkerRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, SkywalkerRequest request, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA, concreteIndices);
    }

    private Map<String, Object> getFieldInfo(IndexService indexService, FieldInfo fieldinfo, Map<String, Type> types) {
        Map<String, Object> info = new HashMap();
        info.put("name", fieldinfo.name);
        info.put("isindexed", fieldinfo.isIndexed);
        info.put("storePayloads", fieldinfo.storePayloads);
        info.put("storeTermVector", fieldinfo.storeTermVector);
        info.put("number", fieldinfo.number);
        info.put("omitNorms", fieldinfo.omitNorms);        
        info.put("options", fieldinfo.indexOptions.name().toString());
        MapperService mapperService = indexService.mapperService();
        if (mapperService == null) {
            return info;
        }
        FieldMapper fieldMapper = mapperService.smartNameFieldMapper(fieldinfo.name);
        if (fieldMapper != null) {
            Map<String, Object> mapper = new HashMap();
            mapper.put("analyzed", fieldMapper.analyzed());
            mapper.put("boost", fieldMapper.boost());
            mapper.put("indexed", fieldMapper.indexed());
            mapper.put("omitNorms", fieldMapper.omitNorms());
            Type type = FieldType.type(fieldMapper.fieldDataType());
            mapper.put("fieldDataType", type.toString());
            types.put(fieldinfo.name, type);
            mapper.put("fullName", fieldMapper.names().fullName());
            mapper.put("indexName", fieldMapper.names().indexName());
            mapper.put("indexNameClean", fieldMapper.names().indexNameClean());
            info.put("mapper", mapper);
        }
        return info;
    }

    private void getStoreMetadata(Map<String, Object> response, ImmutableMap<String, StoreFileMetaData> metadata) {
        List result = new ArrayList();
        long minLastModified = Long.MAX_VALUE;
        long maxLastModified = Long.MIN_VALUE;
        for (String name : metadata.keySet()) {
            StoreFileMetaData m = metadata.get(name);
            Map<String, Object> info = new HashMap();
            info.put("name", name);
            info.put("length", m.length());
            if (m.lastModified() < minLastModified) {
                minLastModified = m.lastModified();
            }
            if (m.lastModified() > maxLastModified) {
                maxLastModified = m.lastModified();
            }
            info.put("lastmodified", DATE_TIME_FORMATTER.printer().print(m.lastModified()));
            info.put("checksum", m.checksum());
            info.put("func", IndexGate.getFileFunction(name));
            result.add(info);
        }
        response.put("store", result);
        response.put("minlastmodified", minLastModified);
        response.put("maxlastmodified", maxLastModified);
    }

    private String decode(Term term, Map<String, Type> typeMap) {
        String value = term.text();
        Type type = typeMap.get(term.field());
        switch (type) {
            case STRING:
                return term.text();
            case BYTE:
                byte[] b = value.getBytes();
                return bytesToHex(b, 0, b.length);
            case INT:
            case SHORT:
                return Integer.toString(NumericUtils.prefixCodedToInt(value));
            case LONG:
                return Long.toString(NumericUtils.prefixCodedToLong(value));
            case FLOAT:
                return Float.toString(NumericUtils.prefixCodedToFloat(value));
            case DOUBLE:
                return Double.toString(NumericUtils.prefixCodedToDouble(value));
            case NULL:
                return "null";

        }
        return "unknown";
    }

    private String bytesToHex(byte bytes[], int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; ++i) {
            if (i > offset) {
                sb.append(" ");
            }
            sb.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1));
        }
        return sb.toString();
    }
}