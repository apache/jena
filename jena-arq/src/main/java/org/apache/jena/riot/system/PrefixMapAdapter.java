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

package org.apache.jena.riot.system;

import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;

/**
 * Provided {@link PrefixMap} for a {@link PrefixMapping}.
 * @see PrefixMappingAdapter
 */
final
public class PrefixMapAdapter extends PrefixMapBase implements PrefixMap {
    private final PrefixMapping prefixMapping;

    public PrefixMapAdapter(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }

    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public String get(String prefix) {
        return prefixMapping.getNsPrefixURI(prefix);
    }

    @Override
    public Map<String, String> getMappingCopy() {
        return prefixMapping.getNsPrefixMap();
    }

    @Override
    public Map<String, String> getMapping() {
        // Actually a copy. There is no direct access in PrefixMapping.
        return prefixMapping.getNsPrefixMap();
    }

    @Override
    public void add(String prefix, String iriString) {
        prefixMapping.setNsPrefix(prefix, iriString);
    }

    @Override
    public void delete(String prefix) {
        prefixMapping.removeNsPrefix(prefix);
    }

    @Override
    public void clear() {
        prefixMapping.clearNsPrefixMap();
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return prefixMapping.getNsPrefixURI(prefix) != null;
    }

    @Override
    public String abbreviate(String uriStr) {
        // PrefixMapiing has a reverse map, PrefixMap does not.
        // Try using the prefix mapping else resort to general purpose library code. 
        String x = prefixMapping.qnameFor(uriStr);
        if ( x != null )
            return x;
        return PrefixLib.abbreviate(this, uriStr);
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        return PrefixLib.abbrev(this, uriStr);
    }

    @Override
    public String expand(String prefix, String localName) {
        String prefixUri = prefixMapping.getNsPrefixURI(prefix);
        if ( prefixUri == null )
            return null;
        return prefixUri+localName;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0 ;
    }

    @Override
    public int size() {
        return prefixMapping.numPrefixes();
    }

    @Override
    public String toString() {
        return Prefixes.toString(this);
    }
}

