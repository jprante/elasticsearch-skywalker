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
package org.xbib.elasticsearch.action.skywalker.support;

import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;

/**
 * Metadata to XContent
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class MetaDataToXContent implements ToXContent {

    MetaData metadata;

    public MetaDataToXContent metadata(MetaData metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field("version", metadata.version());
        builder.field("numberOfShards", metadata.numberOfShards());
        builder.startArray("concreteIndices");
        for (String index : metadata.concreteAllIndicesAsSet()) {
            builder.value(index);
        }
        builder.endArray();
        ImmutableMap<String, ImmutableMap<String, AliasMetaData>> aliases = metadata.aliases ();
        builder.startArray("aliases");
        for (String alias : aliases.keySet()) {
            builder.startObject(alias);
            builder.startArray("aliasMetadata");
            for (String s : aliases.get(alias).keySet()) {
                builder.startObject();
                AliasMetaData amd = aliases.get(alias).get(s);
                builder.field("alias", amd.getAlias());
                builder.field("filter", amd.getFilter().string());
                builder.field("indexRouting", amd.getIndexRouting());
                builder.field("searchRouting", amd.getSearchRouting());
                builder.endObject();
            }
            builder.endArray();
            builder.endObject();
        }
        builder.endArray();
        builder.startArray("indexes");
        ImmutableMap<String,IndexMetaData> indices = metadata.indices();
        for (String s : indices.keySet()) {
            IndexMetaData imd = indices.get(s);
            builder.startObject();
            builder.field("index", imd.getIndex());
            builder.field("state", imd.getState().name());
            builder.field("numberOfReplicas", imd.getNumberOfReplicas());
            builder.field("numberOfShards", imd.getNumberOfShards());
            builder.field("totalNumberOfShards", imd.getTotalNumberOfShards());
            builder.field("version", imd.getVersion());
            builder.field("settings", imd.getSettings().getAsMap());
            ImmutableMap<String, MappingMetaData> m = imd.getMappings();
            builder.endObject();
        }
        builder.endArray();
        builder.startArray("templates");
        ImmutableMap<String,IndexTemplateMetaData> templates = metadata.templates();
        for (String s : templates.keySet()) {
            IndexTemplateMetaData itmd = templates.get(s);
            itmd.getName();
            itmd.getOrder();
            itmd.getTemplate();
            itmd.getSettings();
            itmd.getMappings();
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }
}
