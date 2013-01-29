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
import java.util.Map.Entry;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ActionKeyValue;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.hp.hpl.jena.shared.PrefixMapping;

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
     * Creates a prefix map copying prefixes from an existing prefix mapping
     * @param pmap Prefix Mapping
     * @return Prefix Map
     */
    public static PrefixMap fromPrefixMapping(PrefixMapping pmap) {
        PrefixMapStd pm = new PrefixMapStd();
        pm.putAll(pmap);
        return pm;
    }

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

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jena.riot.system.LightweightPrefixMap#getMapping()
     */
    @Override
    public Map<String, IRI> getMapping() {
        return prefixes2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jena.riot.system.LightweightPrefixMap#getMappingCopy()
     */
    @Override
    public Map<String, IRI> getMappingCopy() {
        return new HashMap<String, IRI>(prefixes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jena.riot.system.LightweightPrefixMap#getMappingCopyStr()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#add(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void add(String prefix, String iriString) {
        prefix = canonicalPrefix(prefix);
        IRI iri = IRIFactory.iriImplementation().create(iriString);
        // Check!
        prefixes.put(prefix, iri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#add(java.lang.String,
     * org.apache.jena.iri.IRI)
     */
    @Override
    public void add(String prefix, IRI iri) {
        prefix = canonicalPrefix(prefix);
        prefixes.put(prefix, iri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#putAll(org.apache.jena
     * .riot.system.PrefixMap)
     */
    @Override
    public void putAll(PrefixMap pmap) {
        prefixes.putAll(pmap.getMapping());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#putAll(com.hp.hpl.jena
     * .shared.PrefixMapping)
     */
    @Override
    public void putAll(PrefixMapping pmap) {
        for (Map.Entry<String, String> e : pmap.getNsPrefixMap().entrySet())
            add(e.getKey(), e.getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#delete(java.lang.String)
     */
    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);
        prefixes.remove(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#contains(java.lang.String
     * )
     */
    @Override
    public boolean contains(String prefix) {
        prefix = canonicalPrefix(prefix);
        return _contains(prefix);
    }

    protected boolean _contains(String prefix) {
        return prefixes.containsKey(prefix);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#expand(java.lang.String)
     */
    @Override
    public String expand(String prefixedName) {
        int i = prefixedName.indexOf(':');
        if (i < 0)
            return null;
        return expand(prefixedName.substring(0, i), prefixedName.substring(i + 1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jena.riot.system.LightweightPrefixMap#expand(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String expand(String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        IRI x = prefixes.get(prefix);
        if (x == null)
            return null;
        return x.toString() + localName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        boolean first = true;

        for (Entry<String, IRI> e : prefixes.entrySet()) {
            String prefix = e.getKey();
            IRI iri = e.getValue();
            if (first)
                first = false;
            else
                sb.append(" ,");
            sb.append(prefix);
            sb.append(":=");
            sb.append(iri.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}
