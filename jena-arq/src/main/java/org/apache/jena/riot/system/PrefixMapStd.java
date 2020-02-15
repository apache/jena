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

/**
 * Default implementation of a {@link PrefixMap}.
 */
public class PrefixMapStd extends PrefixMapBase {
    // Expansion map
    final Map<String, String> prefixes = new HashMap<>();

    // Immutable view of prefixes 
    private final Map<String, String> prefixes2 = Collections.unmodifiableMap(prefixes);
    
    // Abbreviation map used for common cases.
    // This keeps the URI->prefix mappings for a computed guess at the answer, before
    // resorting to a full search. See abbrev(String) below. 
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
    public Map<String, String> getMapping() {
        return prefixes2;
    }

    @Override
    public void add(String prefix, String iri) {
        prefix = canonicalPrefix(prefix);
        prefixes.put(prefix, iri);
        uriToPrefix.put(iri.toString(), prefix) ;
    }

    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);
        prefixes.remove(prefix);
        // Remove from the abbreviation map.
        uriToPrefix.values().remove(prefix);
    }

    @Override
    public void clear() {
        prefixes.clear() ; 
    }

    @Override
    public boolean containsPrefix(String prefix) {
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
     * Takes a guess for the String string to use in abbreviation.
     * 
     * @param iriString String string
     * @return String or null
     */
    protected static String getPossibleKey(String iriString) {
        int index = iriString.lastIndexOf('#');
        if (index > -1)
            return iriString.substring(0, index + 1);
        index = iriString.lastIndexOf('/');
        if (index > -1)
            return iriString.substring(0, index + 1);
        // We could add ':' here, it is used as a separator in URNs.
        // But it is a multiple use character and always present in the scheme name.
        // This is a fast-track guess so don't try guessing based on ':'.
        return null;
    }

    @Override
    public String expand(String prefix, String localName) {
        prefix = canonicalPrefix(prefix);
        String x = prefixes.get(prefix);
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
