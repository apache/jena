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

import java.util.Collections ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;

/**
 * Default implementation of a {@link PrefixMap}, this implementation
 * is best suited to use for input.
 * <p>
 * @see FastAbbreviatingPrefixMap which may offer much better abbreviation performance.
 */
public class PrefixMapStd extends PrefixMapBase {
    // Expansion map
    final Map<String, IRI> prefixes = new HashMap<>();

    // Immutable view of prefixes 
    private final Map<String, IRI> prefixes2 = Collections.unmodifiableMap(prefixes);
    
    // Abbreviation map used for common cases.
    // This keeps the URI->prefix mappings for a computed guess at the anser, before
    // resorting to a full search. See abbrev(String) below.s 
    final Map<String, String> uriToPrefix = new HashMap<>();

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
    public void add(String prefix, String iriString) {
        prefix = canonicalPrefix(prefix);
        IRI iri = IRIFactory.iriImplementation().create(iriString);
        prefixes.put(prefix, iri);
        uriToPrefix.put(iriString, prefix) ;
    }

    @Override
    public void add(String prefix, IRI iri) {
        prefix = canonicalPrefix(prefix);
        prefixes.put(prefix, iri);
        uriToPrefix.put(iri.toString(), prefix) ;
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
        Pair<String, String> p = abbrev(uriStr);
        if (p == null)
            return null;
        return p.getLeft() + ":" + p.getRight();
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        // Look for a prefix by URI ending "#" or "/"
        // then look for that as a known prefix.
        String candidate = getPossibleKey(uriStr) ;
        String uriForPrefix = uriToPrefix.get(candidate) ;
        if ( uriForPrefix != null )
        {
            // Fast track.
            String ln = uriStr.substring(candidate.length());
            if ( isSafeLocalPart(ln))
                return Pair.create(uriForPrefix, ln); 
        }
        // Not in the uri -> prefix map.  Crunch it.
        return abbrev(this.prefixes, uriStr, true);
    }

    /**
     * Takes a guess for the IRI string to use in abbreviation.
     * 
     * @param iriString IRI string
     * @return String or null
     */
    protected static String getPossibleKey(String iriString) {
        int index = iriString.lastIndexOf('#');
        if (index > -1)
            return iriString.substring(0, index + 1);
        index = iriString.lastIndexOf('/');
        if (index > -1)
            return iriString.substring(0, index + 1);
        return null;
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
