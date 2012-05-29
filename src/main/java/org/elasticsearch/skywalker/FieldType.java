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
package org.elasticsearch.skywalker;

import org.elasticsearch.index.field.data.bytes.ByteFieldDataType;
import org.elasticsearch.index.field.data.doubles.DoubleFieldDataType;
import org.elasticsearch.index.field.data.floats.FloatFieldDataType;
import org.elasticsearch.index.field.data.ints.IntFieldDataType;
import org.elasticsearch.index.field.data.longs.LongFieldDataType;
import org.elasticsearch.index.field.data.shorts.ShortFieldDataType;
import org.elasticsearch.index.field.data.strings.StringFieldDataType;

public class FieldType {

    public enum Type {

        NULL, STRING, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
    }

    FieldType() {
    }

    public static Type type(Object object) {
        if (object instanceof StringFieldDataType) {
            return Type.STRING;
        }
        if (object instanceof ByteFieldDataType) {
            return Type.BYTE;
        }
        if (object instanceof ShortFieldDataType) {
            return Type.SHORT;
        }
        if (object instanceof IntFieldDataType) {
            return Type.INT;
        }
        if (object instanceof LongFieldDataType) {
            return Type.LONG;
        }
        if (object instanceof FloatFieldDataType) {
            return Type.FLOAT;
        }
        if (object instanceof DoubleFieldDataType) {
            return Type.DOUBLE;
        }
        return Type.NULL;
    }
}
