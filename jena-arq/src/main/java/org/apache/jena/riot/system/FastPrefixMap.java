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

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ActionKeyValue;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Trie;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A prefix map implementation suited to output heavy workloads
 * <p>
 * This is an alternative implementation of the {@link LightweightPrefixMap}
 * interface, it is broadly similar to the default {@link PrefixMap}
 * implementation but is faster in output heavy workloads. If you are calling
 * the {@link LightweightPrefixMap#expand(String)} or
 * {@link LightweightPrefixMap#expand(String, String)} methods a lot then you
 * should be using this implementation.
 * </p>
 * <p>
 * To improve on output performance this implementation maintains a {@link Trie}
 * mapping namespace IRIs to their prefixes as well as a normal mapping.
 * Therefore IRI abbreviation can be done as an approximately O(1) operation in
 * the worst case (ignoring the vagaries of {@link HashMap} performance since
 * that underpins the {@link Trie} implementation). If you contrast this with
 * the abbreviation performance of the default {@link PrefixMap} which is worst
 * case O(n) then this is a substantial saving in scenarios where you primarily
 * use a prefix map for output.
 * </p>
 * <p>
 * Generally speaking all other operations should be roughly equivalent to the
 * default {@link PrefixMap} though the memory overhead of this implementation
 * will be marginally higher due to the extra information maintained in the
 * {@link Trie}.
 * </p>
 */
public class FastPrefixMap extends LightweightPrefixMapBase {

    private Map<String, IRI> prefixes = new HashMap<String, IRI>();
    private Map<String, IRI> prefixesView = Collections.unmodifiableMap(this.prefixes);
    private Trie<String> abbrevs = new Trie<String>();

    /**
     * Create a new fast prefix map
     */
    public FastPrefixMap() {
    }

    /**
     * Create a new prefix map which copies mappings from an existing map
     * 
     * @param pmap
     *            Prefix Map
     */
    public FastPrefixMap(LightweightPrefixMap pmap) {
        this.putAll(pmap);
    }

    /**
     * Gets the key for abbreviation lookup from an IRI
     * 
     * @param iriString
     *            IRI string
     * @return Key or null
     */
    private String getAbbrevKey(String iriString) {
        int index = iriString.lastIndexOf('#');
        if (index > -1)
            return iriString.substring(0, index + 1);
        index = iriString.lastIndexOf('/');
        if (index > -1)
            return iriString.substring(0, index + 1);
        return null;
    }

    @Override
    public Map<String, IRI> getMapping() {
        return this.prefixesView;
    }

    @Override
    public Map<String, IRI> getMappingCopy() {
        return new HashMap<String, IRI>(this.prefixes);
    }

    @Override
    public Map<String, String> getMappingCopyStr() {
        final Map<String, String> smap = new HashMap<String, String>();
        ActionKeyValue<String, IRI> action = new ActionKeyValue<String, IRI>() {
            @Override
            public void apply(String key, IRI value) {
                String str = value.toString();
                smap.put(key, str);
            }
        };
        Iter.apply(getMapping(), action);
        return smap;
    }

    @Override
    public void add(String prefix, String iriString) {
        this.add(prefix, IRIFactory.iriImplementation().create(iriString));
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
    public void putAll(LightweightPrefixMap pmap) {
        Map<String, IRI> map = pmap.getMapping();
        for (String prefix : map.keySet()) {
            this.add(prefix, map.get(prefix));
        }
    }

    @Override
    public void putAll(PrefixMapping pmap) {
        Map<String, String> map = pmap.getNsPrefixMap();
        for (String prefix : map.keySet()) {
            this.add(prefix, map.get(prefix));
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
    protected Pair<String, String> abbrev(Map<String, IRI> prefixes, String uriStr, boolean turtleSafe) {
        // Try to use trie based lookup
        String abbrevKey = this.getAbbrevKey(uriStr);
        if (abbrevKey != null) {
            // Suitable for trie based lookup
            String prefix = this.abbrevs.get(abbrevKey);
            if (prefix == null)
                return null;

            String ln = uriStr.substring(this.prefixes.get(prefix).toString().length());
            if (!turtleSafe || isTurtleSafe(ln))
                return Pair.create(prefix, ln);
            return null;
        } else {
            // Not suitable for trie based lookup so trie the brute force
            // approach
            return super.abbrev(prefixes, uriStr, turtleSafe);
        }
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

}
