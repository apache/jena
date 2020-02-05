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
import org.apache.jena.shared.PrefixMapping;

/** Provided {@link PrefixMapping} for a {@link PrefixMap}. */
public class PrefixMappingAdapter extends PrefixMappingBase {

    private final PrefixMap pmap;
    
    public PrefixMappingAdapter(PrefixMap pmap) {
        this.pmap = pmap;
    }
    
    @Override
    protected void add(String prefix, String uri) {
        pmap.add(prefix, uri);
    }

    @Override
    protected void remove(String prefix) {
        pmap.delete(prefix);
    }

    @Override
    protected void clear() {
        pmap.clear();
    }

    @Override
    protected boolean isEmpty() {
        return pmap.isEmpty();
    }

    @Override
    protected int size() {
        return pmap.size();
    }

    @Override
    protected String prefixToUri(String prefix) {
        return pmap.getMapping().get(prefix);
    }

    @Override
    protected String uriToPrefix(String uri) {
       return pmap.getMapping().entrySet().stream()
           .filter(e->Objects.equals(uri, e.getValue().toString()))
           .map(Entry::getKey)
           .findFirst()
           .orElse(null);
    }

    @Override
    protected Map<String, String> asMap() {
        return pmap.getMapping();
    }

    @Override
    protected Map<String, String> asMapCopy() {
        return pmap.getMappingCopy();
    }

    @Override
    protected void apply(BiConsumer<String, String> action) {
        pmap.forEach(action);
    }
}
