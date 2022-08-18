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

package org.apache.jena.sparql.graph;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.util.XMLChar;

/**
 * Framework for implementing {@link PrefixMapping}. It is stateless (unlike
 * {@code PrefixMappingImpl}) and implements the contract of {@link PrefixMapping},
 * providing the key algorithm and delegating storage to the subclasses.
 * <p>
 * Reverse mapping, looking up a URI to find a prefix is complex because there may be several
 * possibilities. Applications should not rely on every implementation being consistent
 * when there is a choice of which prefix to use to shorten a URI.
 */
public abstract class PrefixMappingBase implements PrefixMapping {
    /* Reverse mappings.
     * The strict contract of PrefixMapping requires a separate reverse mapping to be stored and manipulated,
     * which in turn adds complexity to the storage implementations.
     * However, applications removing prefixes is unusual so we end up with a lot of complexity with little value.
     * 
     * Beware of the details of removing a mapping when there is another to the same URI.
     * If we had:
     * 
     *  Add (pref1, U)
     *  Add (pref2, U)
     * 
     * so that {@code U} reverse maps ({@link #getNsURIPrefix}) to {@code pref2} (it was
     * done second) then
     * 
     *  Remove (pref2)
     * 
     * it causes {@code U} to reverse map to {@code pref1}.
     * 
     * This feature is quite a burden on implementations and should be regarded as "legacy" -
     * an implementation may not support this complex effect.
     * 
     * PrefixMappingMem does.
     * Database backed ones typically don't.
     */

    protected PrefixMappingBase() {}
    
    // The storage operations of an implementation.
    
    /** Add prefix */
    abstract protected void add(String prefix, String uri);
    
    /** Remove prefix. */
    abstract protected void remove(String prefix);
    
    /** Clear all mappings */
    abstract protected void clear();

    abstract protected boolean isEmpty();
    
    abstract protected int size();

    /** Return the URI that the prefix maps to. */ 
    abstract protected String prefixToUri(String prefix);
    
    /** Return a prefix that maps to the URI.
     * There may be several; the answer is any one of them. 
     */ 
    abstract protected String uriToPrefix(String uri);
    
    /** Return as a map. This map is only used within this class.
     * It can be as efficient as possible.
     * It will not be modified.
     * It will not be returned to a caller of {@code PrefixMappingBase}.
     */
    abstract protected Map<String, String> asMap();

    /** 
     * Return as a map. The map return is not connected to the prefix mapping implementation,
     * does not reflect subsequent prefix mapping changes.
     */
    abstract protected Map<String, String> asMapCopy();

    /** Apply the {@link BiConsumer} to each (prefix, uri) pair. */
    abstract protected void apply(BiConsumer<String, String> action);
    
    /**
     * This part of the subclass API and may be overridden if an implementation can do
     * better This general implementation is based on asMap() which may be a copy or may
     * be a view of the storage directly.
     */
    protected Optional<Entry<String, String>> findMapping( String uri, boolean partial ) {
        return asMap().entrySet().stream().sequential().filter(e->{
            String ss = e.getValue();
            if (uri.startsWith( ss ) && (partial || ss.length() == uri.length())) 
                return true;
            return false;
        }).findFirst();
    }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        checkLegalPrefix(prefix); 
        add(prefix, uri);
        return this;
    }
    
    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        remove(prefix);
        return this;
    }
    
    @Override
    public PrefixMapping clearNsPrefixMap() {
        clear();
        return this;
    }

    /**
     * Checks that a prefix is "legal" - it must be a valid XML NCName or "". XML rules
     * for RDF/XML output.
     * <p>
     * This is a recurring user question - why does {@code Resource.getNamespace},
     * {@code Resource.getLocalname} not abbreviate when it is legal Turtle.
     * <p>
     * Answer - legacy for RDF/XML.
     * <p>
     * See also {@link #qnameFor}.
     */
    public static void checkLegalPrefix(String prefix) {
        if ( prefix == null )
            throw new PrefixMapping.IllegalPrefixException("null for prefix");
        if ( prefix.length() > 0 && !XMLChar.isValidNCName(prefix) )
            throw new PrefixMapping.IllegalPrefixException(prefix);
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping pmap) {
        if ( pmap instanceof PrefixMappingBase ) {
            PrefixMappingBase pmap2 = (PrefixMappingBase)pmap;
            pmap2.apply((p,u)->setNsPrefix(p, u));
            return this;
        }
        // Need to create as a map (a copy) and then add. 
        setNsPrefixes(pmap.getNsPrefixMap());
        return this;
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        map.forEach(this::setNsPrefix);
        return this;
    }

    /* Not javadoc.
        This is the unusual contract as defined by the interface:
      Update this PrefixMapping with the bindings in <code>map</code>, only
      adding those (p, u) pairs for which neither p nor u appears in this mapping.
     */
    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping pmap) {
        if ( pmap instanceof PrefixMappingBase ) {
            // Direct. No intermediate Map<> object created,
            PrefixMappingBase pmap2 = (PrefixMappingBase)pmap;
            pmap2.apply(this::addWith);
            return this;
        }
        // Creates a Map<>.
        Map<String, String> map = pmap.getNsPrefixMap();
        map.forEach(this::addWith);
        return this;
    }
    
    private void addWith(String p, String u) {
        if ( prefixToUri(p) == null && uriToPrefix(u) == null ) 
            add(p,u);
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        return prefixToUri(prefix);
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return uriToPrefix(uri);
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return asMapCopy();
    }

    @Override
    public String expandPrefix(String prefixed) {
        int colon = prefixed.indexOf(':');
        if ( colon < 0 )
            return prefixed;
        else {
            String prefix = prefixed.substring(0, colon);
            String uri = getNsPrefixURI(prefix);
            return uri == null ? prefixed : uri + prefixed.substring(colon + 1);
        }
    }

    @Override
    public String qnameFor(String uri) {
        // Turtle.  SplitIRI.splitpoint(uri);
        int split = SplitIRI.splitXML(uri);
        String ns = uri.substring(0, split);
        String local = uri.substring(split);
        if ( local.equals("") )
            return null;
        String prefix = getNsURIPrefix(ns);
        return prefix == null ? null : prefix + ":" + local;
    }
    
    @Override
    public String shortForm(String uri) {
        Optional<Entry<String, String>> e = findMapping(uri, true);
        if ( ! e.isPresent() )
            return uri;
        return e.get().getKey() + ":" + uri.substring((e.get().getValue()).length());
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        if ( numPrefixes() != other.numPrefixes() )
            return false;
        return getNsPrefixMap().equals(other.getNsPrefixMap());
    }

    @Override
    public PrefixMapping lock() {
        return this;
    }

    @Override 
    public boolean hasNoMappings() {
        return isEmpty();
    }
    
    @Override
    public int numPrefixes() {
        return size();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", "); 
        apply((p,u)->sj.add(p+"->"+u));
        return "pm:["+numPrefixes()+"]{"+sj.toString()+"}";
    }
}
