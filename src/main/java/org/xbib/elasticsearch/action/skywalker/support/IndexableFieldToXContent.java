
package org.xbib.elasticsearch.action.skywalker.support;

import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 *  Indexable field to XContent
 */
public class IndexableFieldToXContent implements ToXContent {

    private IndexableField field;

    public IndexableFieldToXContent field(IndexableField field) {
        this.field = field;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field("name", field.name());
        if (field.binaryValue() != null) {
            builder.field("type", "binary")
                    .field("value", field.binaryValue());
        } else if (field.stringValue() != null) {
            builder.field("type", "string")
                    .field("value", field.stringValue());
        } else if (field.numericValue() != null) {
            if (field instanceof IntField) {
                builder.field("type", "integer")
                        .field("value", field.numericValue().intValue());
            } else if (field instanceof LongField) {
                builder.field("type", "long")
                        .field("value", field.numericValue().longValue());
            } else if (field instanceof FloatField) {
                builder.field("type", "float")
                        .field("value", field.numericValue().floatValue());
            } else if (field instanceof DoubleField) {
                builder.field("type", "double")
                        .field("value", field.numericValue().doubleValue());
            }
        }
        builder.field("stored", field.fieldType().stored());
        builder.field("indexed", field.fieldType().indexed());
        builder.field("omitNorms", field.fieldType().omitNorms());
        builder.field("storeTermVectors", field.fieldType().storeTermVectors());
        builder.field("storeTermVectorOffsets", field.fieldType().storeTermVectorOffsets());
        builder.field("storeTermVectorPayloads", field.fieldType().storeTermVectorPayloads());
        builder.field("storeTermVectorPositions", field.fieldType().storeTermVectorPositions());
        builder.field("boost", field.boost());
        builder.endObject();
        return builder;
    }
}
