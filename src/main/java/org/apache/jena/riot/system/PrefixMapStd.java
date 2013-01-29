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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;

/**
 * Default implementation of a {@code LightweightPrefixMap}, this implementation
 * is best suited to use for input.
 * <p>
 * If you are using this primarily to abbreviate URIs for output consider using
 * the {@link FastAbbreviatingPrefixMap} instead which offers much better abbreviation
 * performance.
 */
public class PrefixMapStd extends PrefixMapBase {
    final Map<String, IRI> prefixes = new HashMap<String, IRI>();
    private final Map<String, IRI> prefixes2 = Collections.unmodifiableMap(prefixes);

    /**
     * Creates a new empty prefix mapping
     */
    public PrefixMapStd() {
    }

    /**
     * Creates a new prefix mapping copied from an existing map
     * @param prefixMap Prefix Map
     */
    public PrefixMapStd(PrefixMap prefixMap) {
        prefixes.putAll(prefixMap.getMapping());
    }

    @Override
    public Map<String, IRI> getMapping() {
        return prefixes2;
    }

    @Override
    public void add(String prefix, IRI iri) {
        prefix = canonicalPrefix(prefix);
        prefixes.put(prefix, iri);
    }

    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);
        prefixes.remove(prefix);
    }

    @Override
    public boolean contains(String prefix) {
        prefix = canonicalPrefix(prefix);
        return prefixes.containsKey(prefix);
    }

    @Override
    public String abbreviate(String uriStr) {
        Pair<String, String> p = abbrev(this.prefixes, uriStr, true);
        if (p == null)
            return null;
        return p.getLeft() + ":" + p.getRight();
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        return abbrev(this.prefixes, uriStr, true);
    }

    @Override
    public String expand(String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        IRI x = prefixes.get(prefix);
        if (x == null)
            return null;
        return x.toString() + localName;
    }
}
