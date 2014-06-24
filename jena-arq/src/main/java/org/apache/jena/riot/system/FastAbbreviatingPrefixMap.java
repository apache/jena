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
import org.apache.jena.atlas.lib.Trie;
import org.apache.jena.iri.IRI;

/**
 * A prefix map implementation suited to output heavy workloads
 * <p>
 * This is an alternative implementation of the {@link PrefixMap}
 * interface, it is broadly similar to the default {@link PrefixMapStd}
 * implementation but is faster in output heavy workloads. If you are calling
 * the {@link PrefixMap#expand(String)} or
 * {@link PrefixMap#expand(String, String)} methods a lot then you
 * should be using this implementation.
 * </p>
 * <p>
 * To improve on output performance this implementation maintains a {@link Trie}
 * mapping namespace IRIs to their prefixes as well as a normal mapping.
 * Therefore IRI abbreviation can be done as an approximately O(1) operation in
 * the worst case . If you contrast this with the abbreviation performance of
 * the default {@code PrefixMap} which is worst case O(n) then this is a
 * substantial saving in scenarios where you primarily use a prefix map for
 * output.
 * </p>
 * <p>
 * Generally speaking all other operations should be roughly equivalent to the
 * default {@code PrefixMap} though the memory overhead of this implementation
 * will be marginally higher due to the extra information maintained in the
 * {@code Trie}.
 * </p>
 */
public class FastAbbreviatingPrefixMap extends PrefixMapBase {

    private Map<String, IRI> prefixes = new HashMap<>();
    private Map<String, IRI> prefixesView = Collections.unmodifiableMap(this.prefixes);
    private Trie<String> abbrevs = new Trie<>();

    /**
     * Create a new fast abbreviating prefix map
     */
    public FastAbbreviatingPrefixMap() {
    }

    /**
     * Create a new fast abbreviating prefix map which copies mappings from an existing map
     * 
     * @param pmap
     *            Prefix Map
     */
    public FastAbbreviatingPrefixMap(PrefixMap pmap) {
        this.putAll(pmap);
    }

    @Override
    public Map<String, IRI> getMapping() {
        return this.prefixesView;
    }

    @Override
    public void add(String prefix, IRI iri) {
        prefix = canonicalPrefix(prefix);

        IRI existing = this.prefixes.get(prefix);
        if (existing != null && existing.equals(iri)) {
            // Same as existing mapping so no-op
            return;
        } else {
            // New/Updated mapping

            if (existing != null) {
                // Delete current abbreviation mapping
                this.abbrevs.remove(existing.toString());
            }

            // Add/Update the mapping
            this.prefixes.put(prefix, iri);

            // Update the abbreviation mapping if possible
            this.abbrevs.add(iri.toString(), prefix);
        }
    }

    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);

        IRI iri = this.prefixes.get(prefix);
        if (iri == null)
            return;

        // Delete the abbreviation mapping
        this.abbrevs.remove(iri.toString());

        // Delete the mapping
        this.prefixes.remove(prefix);
    }

    @Override
    public boolean contains(String prefix) {
        prefix = canonicalPrefix(prefix);
        return this.prefixes.containsKey(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#abbreviate(java.lang
     * .String)
     */
    @Override
    public String abbreviate(String uriStr) {
        Pair<String, String> p = abbrev(this.prefixes, uriStr, true);
        if (p == null)
            return null;
        return p.getLeft() + ":" + p.getRight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#abbrev(java.lang.String)
     */
    @Override
    public Pair<String, String> abbrev(String uriStr) {
        return abbrev(this.prefixes, uriStr, true);
    }

    @Override
    protected Pair<String, String> abbrev(Map<String, IRI> prefixes, String uriStr, boolean checkLocalPart) {
        //Use longest match to find the longest possible match
        String prefix = this.abbrevs.longestMatch(uriStr);
        if (prefix == null)
            return null;

        String ln = uriStr.substring(this.prefixes.get(prefix).toString().length());
        if (!checkLocalPart || isSafeLocalPart(ln))
            return Pair.create(prefix, ln);
        return null;
    }

    @Override
    public String expand(String prefixedName) {
        int i = prefixedName.indexOf(':');
        if (i < 0)
            return null;
        return expand(prefixedName.substring(0, i), prefixedName.substring(i + 1));
    }

    @Override
    public String expand(String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        IRI x = prefixes.get(prefix);
        if (x == null)
            return null;
        return x.toString() + localName;
    }

    @Override
    public boolean isEmpty()
    {
        return prefixes.isEmpty() ;
    }

    @Override
    public int size()
    {
        return prefixes.size() ;
    }
}
