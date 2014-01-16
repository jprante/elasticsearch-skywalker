
package org.xbib.elasticsearch.action.skywalker.support;

import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * Metadata to XContent
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
        for (String index : metadata.concreteAllIndices()) {
            builder.value(index);
        }
        builder.endArray();
        ImmutableOpenMap<String, ImmutableOpenMap<String, AliasMetaData>> aliases = metadata.getAliases();
        builder.startArray("aliases");
        for (ObjectCursor<String> alias : aliases.keys()) {
            builder.startObject(alias.value);
            builder.startArray("aliasMetadata");
            for (ObjectCursor<String> s : aliases.get(alias.value).keys()) {
                builder.startObject();
                AliasMetaData amd = aliases.get(alias.value).get(s.value);
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
        ImmutableOpenMap<String,IndexMetaData> indices = metadata.getIndices();
        for (ObjectCursor<String> s : indices.keys()) {
            IndexMetaData imd = indices.get(s.value);
            builder.startObject();
            builder.field("index", imd.getIndex());
            builder.field("state", imd.getState().name());
            builder.field("numberOfReplicas", imd.getNumberOfReplicas());
            builder.field("numberOfShards", imd.getNumberOfShards());
            builder.field("totalNumberOfShards", imd.getTotalNumberOfShards());
            builder.field("version", imd.getVersion());
            builder.field("settings", imd.getSettings().getAsMap());
            ImmutableOpenMap<String, MappingMetaData> m = imd.getMappings();
            // skip mappings here
            builder.endObject();
        }
        builder.endArray();
        builder.startArray("templates");
        ImmutableOpenMap<String,IndexTemplateMetaData> templates = metadata.getTemplates();
        for (ObjectCursor<String> s : templates.keys()) {
            IndexTemplateMetaData itmd = templates.get(s.value);
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
