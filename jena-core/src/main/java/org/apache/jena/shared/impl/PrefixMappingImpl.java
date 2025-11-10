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

package org.apache.jena.shared.impl;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.util.CollectionFactory ;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.util.XML11Char;

/**
    An implementation of PrefixMapping. The mappings are stored in a pair
    of hash tables, one per direction. The test for a legal prefix is left to
    xerces's XMLChar.isValidNCName() predicate.
*/
public class PrefixMappingImpl implements PrefixMapping
    // See also PrefixMappingBase, PrefixMappingMem
    // in org.apache.jena.sparql.graph for a different implementation

    {
    private Map<String, String> prefixToURI;
    private Map<String, String> URItoPrefix;
    private boolean locked;

    public PrefixMappingImpl() {
        // ConcurrentHashMaps protects against breaking each datastructure
        // but does not protect against inconsistency.
        prefixToURI = new ConcurrentHashMap<>();
        URItoPrefix = new ConcurrentHashMap<>();
    }

    protected void set(String prefix, String uri) {
        prefixToURI.put(prefix, uri);
        URItoPrefix.put(uri, prefix);
    }

    protected String get(String prefix) {
        return prefixToURI.get(prefix);
    }

    protected void remove(String prefix) {
        prefixToURI.remove(prefix);
    }

    @Override
    public PrefixMapping lock() {
        locked = true;
        return this;
    }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        checkUnlocked();
        checkLegalPrefix(prefix);
        if ( !prefix.equals("") )
            checkProperURI(uri);
        if ( uri == null )
            throw new NullPointerException("null URIs are prohibited as arguments to setNsPrefix");
        set(prefix, uri);
        return this;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        checkUnlocked();
        remove(prefix);
        regenerateReverseMapping();
        return this;
    }

    @Override
    public PrefixMapping clearNsPrefixMap() {
        checkUnlocked();
        // Because of subclasses, do a clear by calling remove(prefix) for each prefix.
        // Copy prefixes to avoid possible concurrent modification exceptions
        List<String> prefixes = new ArrayList<>(prefixToURI.keySet());
        prefixes.forEach(p -> remove(p));
        regenerateReverseMapping();
        return this;
    }

    protected void regenerateReverseMapping() {
        URItoPrefix.clear();
        for ( Map.Entry<String, String> e : prefixToURI.entrySet() )
            URItoPrefix.put(e.getValue(), e.getKey());
    }

    protected void checkUnlocked() {
        if ( locked )
            throw new JenaLockedException(this);
    }

    // Is this suitable for use as a prefix.
    // Turtle does not have the XML QName restrictions.
    private void checkProperURI(String uri) {
//        if ( !isNiceURI(uri) )
//            throw new NamespaceEndsWithNameCharException(uri);
    }

    /**
     * Checks that a prefix is "legal" - it must be a valid XML NCName.
     * Jena 5.1.0 loosen the test from XML 1.0 to XML 1.1 NCName
     * which aligns with Turtle.
     */
    private void checkLegalPrefix(String prefix) {
        if ( prefix.length() > 0 && !XML11Char.isXML11ValidNCName(prefix) )
            throw new PrefixMapping.IllegalPrefixException(prefix);
    }

    /**
     * Add the bindings of other to our own. We defer to the general case because we
     * have to ensure the URIs are checked.
     *
     * @param other the PrefixMapping whose bindings we are to add to this.
     */
    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        return setNsPrefixes(other.getNsPrefixMap());
    }

    /**
     * Answer this PrefixMapping after updating it with the <code>(p, u)</code>
     * mappings in <code>other</code> where neither <code>p</code> nor <code>u</code>
     * appear in this mapping.
     */
    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping other) {
        checkUnlocked();
        for ( Entry<String, String> e : other.getNsPrefixMap().entrySet() ) {
            String prefix = e.getKey(), uri = e.getValue();
            if ( getNsPrefixURI(prefix) == null && getNsURIPrefix(uri) == null )
                setNsPrefix(prefix, uri);
        }
        return this;
    }

    /**
     * Add the bindings in the prefixToURI to our own.
     * <p>
     * It will fail with an IllegalPrefixException if any prefix is illegal; we make
     * no guarantees about order or completeness if this happens.
     *
     * @param other the Map whose bindings we are to add to this.
     */
    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> other) {
        checkUnlocked();
        for ( Entry<String, String> e : other.entrySet() )
            setNsPrefix(e.getKey(), e.getValue());
        return this;
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        return get(prefix);
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return CollectionFactory.createHashedMap(prefixToURI);
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return URItoPrefix.get(uri);
    }

    /**
     * Expand a prefixed URI. There's an assumption that any URI of the form
     * Head:Tail is subject to mapping if Head is in the prefix mapping. So, if
     * someone takes it into their heads to define eg "http" or "ftp" we have
     * problems.
     */
    @Override
    public String expandPrefix(String prefixed) {
        int colon = prefixed.indexOf(':');
        if ( colon < 0 )
            return prefixed;
        else {
            String uri = get(prefixed.substring(0, colon));
            return uri == null ? prefixed : uri + prefixed.substring(colon + 1);
        }
    }

    /**
     * Answer a readable (we hope) representation of this prefix mapping.
     */
    @Override
    public String toString() {
        return "pm:" + prefixToURI;
    }

    /**
     * Answer the qname for <code>uri</code> which uses a prefix from this mapping,
     * or null if there isn't one.
     * <p>
     * Relies on <code>splitNamespace</code> to carve uri into namespace and
     * localname components; this ensures that the localname is legal and we just
     * have to (reverse-)lookup the namespace in the prefix table.
     */
    @Override
    public String qnameFor(String uri) {
        int split = SplitIRI.splitXML(uri);
        if ( split == uri.length() )
            return null;
        String ns = uri.substring(0, split);
        String local = uri.substring(split);
        String prefix = URItoPrefix.get(ns);
        return prefix == null ? null : prefix + ":" + local;
    }

    /**
     * Compress the URI using the prefix mapping.
     */
    @Override
    public String shortForm(String uri) {
        // This version of the code looks through all the maplets and checks each
        // candidate prefix URI for being a leading substring of the argument URI.
        Entry<String, String> e = findMapping(uri, true);
        return e == null ? uri : e.getKey() + ":" + uri.substring((e.getValue()).length());
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return other instanceof PrefixMappingImpl ? equals((PrefixMappingImpl)other) : equalsByMap(other);
    }

    @Override
    public boolean hasNoMappings() {
        return prefixToURI.isEmpty();
    }

    @Override
    public int numPrefixes() {
        return prefixToURI.size();
    }

    protected boolean equals(PrefixMappingImpl other) {
        return other.sameAs(this);
    }

    protected boolean sameAs(PrefixMappingImpl other) {
        return prefixToURI.equals(other.prefixToURI);
    }

    protected final boolean equalsByMap(PrefixMapping other) {
        return getNsPrefixMap().equals(other.getNsPrefixMap());
    }

    /**
     * Answer a prefixToURI entry in which the value is an initial substring of
     * <code>uri</code>. If <code>partial</code> is false, then the value must equal
     * <code>uri</code>. Does a linear search of the entire prefixToURI, so not
     * terribly efficient for large maps.
     *
     * @param uri the value to search for
     * @param partial true if the match can be any leading substring, false for exact
     *     match
     * @return some entry (k, v) such that uri starts with v [equal for
     *     partial=false]
     */
    private Entry<String, String> findMapping(String uri, boolean partial) {
        for ( Entry<String, String> e : prefixToURI.entrySet() ) {
            String ss = e.getValue();
            if ( uri.startsWith(ss) && (partial || ss.length() == uri.length()) )
                return e;
        }
        return null;
    }
}
