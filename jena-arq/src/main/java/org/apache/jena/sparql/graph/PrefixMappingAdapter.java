/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.graph;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapAdapter;
import org.apache.jena.shared.PrefixMapping;

/**
 * Provided {@link PrefixMapping} for a {@link PrefixMap}.
 *
 * @see PrefixMapAdapter
 */
final
public class PrefixMappingAdapter extends PrefixMappingBase {

    private final PrefixMap prefixMap;

    public PrefixMappingAdapter(PrefixMap pmap) {
        this.prefixMap = pmap;
    }

    public PrefixMap getPrefixMap() {
        return prefixMap;
    }

    @Override
    protected void add(String prefix, String uri) {
        prefixMap.add(prefix, uri);
    }

    @Override
    protected void remove(String prefix) {
        prefixMap.delete(prefix);
    }

    @Override
    protected void clear() {
        prefixMap.clear();
    }

    @Override
    protected boolean isEmpty() {
        return prefixMap.isEmpty();
    }

    @Override
    protected int size() {
        return prefixMap.size();
    }

    @Override
    protected String prefixToUri(String prefix) {
        return prefixMap.getMapping().get(prefix);
    }

    @Override
    protected String uriToPrefix(String uri) {
       return prefixMap.getMapping().entrySet().stream()
           .filter(e->Objects.equals(uri, e.getValue().toString()))
           .map(Entry::getKey)
           .findFirst()
           .orElse(null);
    }

    @Override
    protected Map<String, String> asMap() {
        return prefixMap.getMapping();
    }

    @Override
    protected Map<String, String> asMapCopy() {
        return prefixMap.getMappingCopy();
    }

    @Override
    protected void apply(BiConsumer<String, String> action) {
        prefixMap.forEach(action);
    }
}
