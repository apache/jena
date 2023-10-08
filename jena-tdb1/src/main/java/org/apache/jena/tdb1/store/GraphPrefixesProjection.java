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

package org.apache.jena.tdb1.store;

import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixLib;
import org.apache.jena.riot.system.PrefixMapBase;
import org.apache.jena.shared.PrefixMapping;

/** Projection from DatasetPrefixStorage to a single {@link PrefixMapping}. */
public class GraphPrefixesProjection extends PrefixMapBase {
    private final String graphName;
    private final DatasetPrefixStorage prefixes;

    public GraphPrefixesProjection(String graphName, DatasetPrefixStorage prefixes) {
        Objects.requireNonNull(graphName);
        Objects.requireNonNull(prefixes);
        this.graphName = graphName;
        this.prefixes = prefixes;
    }

    @Override
    public String get(String prefix) {
        Objects.requireNonNull(prefix);
        prefix = PrefixLib.canonicalPrefix(prefix);
        return prefixes.readPrefix(graphName, prefix);
    }

//    @Override
//    public String getByURI(String uri) {
//        return prefixes.readByURI(graphName, uri);
//    }

    @Override
    public Map<String, String> getMapping() {
        return prefixes.readPrefixMap(graphName);
    }

    @Override
    public void add(String prefix, String iriString) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(iriString);
        prefix = PrefixLib.canonicalPrefix(prefix);
        prefixes.insertPrefix(graphName, prefix, iriString);
    }

    @Override
    public void delete(String prefix) {
        Objects.requireNonNull(prefix);
        prefix = PrefixLib.canonicalPrefix(prefix);
        prefixes.removeFromPrefixMap(graphName, prefix);
    }

    @Override
    public void clear() {
        prefixes.removeAllFromPrefixMap(graphName);
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return get(prefix) != null;
    }

    @Override
    public String abbreviate(String uriStr) {
        Objects.requireNonNull(uriStr);
        return PrefixLib.abbreviate(this, uriStr);
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Objects.requireNonNull(uriStr);
        return PrefixLib.abbrev(this, uriStr);
    }

    @Override
    public String expand(String prefix, String localName) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(localName);
        prefix = PrefixLib.canonicalPrefix(prefix);
        return PrefixLib.expand(this, prefix, localName);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return getMapping().size();
    }
}

