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

import static org.apache.jena.riot.system.PrefixLib.canonicalPrefix;
import static org.apache.jena.riot.system.PrefixLib.isSafeLocalPart;

import java.util.Collections ;
import java.util.Map ;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.lib.Pair ;

/**
 * In-memory implementation of a {@link PrefixMap}.
 * <p>
 * This also provides fast URI to prefix name calculation suitable for output. For
 * output, calculating possible prefix names from a URI happens on every URI so this
 * operations needs to be efficient. Normally, a prefix map is "prefix to URI" and
 * the abbreviation is a reverse lookup, which is a scan of the value of the map.
 * This class keeps a reverse lookup map of URI to prefix which combined with a fast,
 * approximate for determining the split point exploiting the most common use cases,
 * provides efficient abbreviation.
 * <p>
 * Usage for abbreviation: call
 * {@linkplain PrefixMapFactory#createForOutput(PrefixMap)} which copies the argument
 * prefix map into an instance of this class, setting up the reverse lookup. This
 * copy is cheaper than repeated reverse lookups would be.
 */
public class PrefixMapStd extends PrefixMapBase {
    // Expansion map
    final Map<String, String> prefixes = new ConcurrentHashMap<>();

    // Immutable view of prefixes
    private final Map<String, String> prefixes2 = Collections.unmodifiableMap(prefixes);

    // Abbreviation map used for common cases.
    // This keeps the URI->prefix mappings for a computed guess at the answer, before
    // resorting to a full search. See abbrev(String) below.
    private final Map<String, String> uriToPrefix = new ConcurrentHashMap<>();

    /**
     * Creates a new empty prefix mapping
     */
    public PrefixMapStd() {}

    /**
     * Creates a new prefix mapping copied from an existing map
     * @param prefixMap Prefix Map
     */
    public PrefixMapStd(PrefixMap prefixMap) {
        Objects.requireNonNull(prefixMap);
        prefixes.putAll(prefixMap.getMapping());
    }

    @Override
    public Map<String, String> getMapping() {
        return prefixes2;
    }

    @Override
    public String get(String prefix) {
        Objects.requireNonNull(prefix);
        prefix = canonicalPrefix(prefix);
        return prefixes.get(prefix);
    }

    @Override
    public void add(String prefix, String iri) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(iri);
        prefix = canonicalPrefix(prefix);
        String oldURI = prefixes.get(prefix);
        if ( oldURI != null )
            uriToPrefix.remove(oldURI);
        prefixes.put(prefix, iri);
        uriToPrefix.put(iri.toString(), prefix) ;
    }

    @Override
    public void delete(String prefix) {
        Objects.requireNonNull(prefix);
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
        Objects.requireNonNull(prefix);
        prefix = canonicalPrefix(prefix);
        return prefixes.containsKey(prefix);
    }

    @Override
    public String abbreviate(String uriStr) {
        Objects.requireNonNull(uriStr);
        Pair<String, String> p = abbrev(uriStr);
        if (p == null)
            return null;
        return p.getLeft() + ":" + p.getRight();
    }

    // This is thread safe (does not crash) - it is not thread-consistent (answer
    // uncertain if the prefix mappings are in flux).
    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Objects.requireNonNull(uriStr);
        // Fast path.
        // Look for a prefix by URI ending "#" or "/"
        // then look for that as a known prefix.
        String candidate = getPossibleKey(uriStr);
        if ( candidate != null ) {
            String uriForPrefix = uriToPrefix.get(candidate);
            if ( uriForPrefix != null ) {
                // Fast track.
                String ln = uriStr.substring(candidate.length());
                if ( isSafeLocalPart(ln) )
                    return Pair.create(uriForPrefix, ln);
            }
        }
        // Not in the uri -> prefix map. Crunch it.
        return PrefixLib.abbrev(prefixes, uriStr, true);
    }

    /**
     * Takes a guess for the namespace URI string to use in abbreviation.
     * Finds the part of the IRI string before the last '#' or '/'.
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
    public boolean isEmpty() {
        return prefixes.isEmpty();
    }

    @Override
    public int size() {
        return prefixes.size();
    }
}
