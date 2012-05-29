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
package org.elasticsearch.skywalker.reconstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Fieldable;

public class Reconstructed {

    private Map<String, Fieldable[]> storedFields;
    private Map<String, GrowableStringArray> reconstructedFields;

    public Reconstructed() {
        storedFields = new HashMap();
        reconstructedFields = new HashMap();
    }

    /**
     * Construct an instance of this class using existing field data.
     *
     * @param storedFields field data of stored fields
     * @param reconstructedFields field data of unstored fields
     */
    public Reconstructed(Map<String, Fieldable[]> storedFields,
            Map<String, GrowableStringArray> reconstructedFields) {
        this.storedFields = storedFields;
        this.reconstructedFields = reconstructedFields;
    }

    /**
     * Get an alphabetically sorted list of field names.
     */
    public List<String> getFieldNames() {
        HashSet<String> names = new HashSet();
        names.addAll(storedFields.keySet());
        names.addAll(reconstructedFields.keySet());
        ArrayList<String> res = new ArrayList(names.size());
        res.addAll(names);
        Collections.sort(res);
        return res;
    }

    public boolean hasField(String name) {
        return storedFields.containsKey(name) || reconstructedFields.containsKey(name);
    }

    /**
     * @return the storedFields
     */
    public Map<String, Fieldable[]> getStoredFields() {
        return storedFields;
    }

    /**
     * @return the reconstructedFields
     */
    public Map<String, GrowableStringArray> getReconstructedFields() {
        return reconstructedFields;
    }
}