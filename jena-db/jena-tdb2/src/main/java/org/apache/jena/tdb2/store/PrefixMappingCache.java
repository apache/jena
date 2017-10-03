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

package org.apache.jena.tdb2.store;

import java.util.Map ;
import java.util.Optional ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.shared.PrefixMapping ;

// Unsafe. Non-transactional cache.
public class PrefixMappingCache implements PrefixMapping {

    private final PrefixMapping other ;
    private Cache<String, String> prefixToUri = CacheFactory.createCache(100) ;
    private Cache<String, String> uriToPrefix = CacheFactory.createCache(100) ;

    public PrefixMappingCache(PrefixMapping other) {
        this.other = other ;
    }
    
    private void add(String prefix, String uri) {
        prefixToUri.put(prefix, uri) ; 
        uriToPrefix.put(uri, prefix);
    }
    
    private void remove(String prefix, String uri) {
        prefixToUri.remove(prefix) ; 
        uriToPrefix.remove(uri);
    }

    private void clear() {
        prefixToUri.clear() ; 
        uriToPrefix.clear() ;
    }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        other.setNsPrefix(prefix, uri) ;
        add(prefix, uri);
        return this ;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        String uri = getNsPrefixURI(prefix) ;
        if ( uri != null )
            remove(prefix, uri);
        other.removeNsPrefix(prefix) ;
        return this ;
    }

    @Override
    public PrefixMapping clearNsPrefixMap() {
        prefixToUri.clear() ;
        uriToPrefix.clear();
        other.clearNsPrefixMap() ;
        return this ;
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping pmap) {
        setNsPrefixes(pmap.getNsPrefixMap()) ;
        return this ;
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        map.entrySet().forEach(entry->{
            setNsPrefix(entry.getKey(), entry.getValue()) ;
        });
        return this ;
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        other.withDefaultMappings(map) ;
        clear() ;
        return this ;
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        String x = prefixToUri.getIfPresent(prefix) ;
        if ( x == null ) {
            x = other.getNsPrefixURI(prefix) ;
            if ( x != null )
                prefixToUri.put(prefix, x); 
        }
        return x ;
    }

    @Override
    public String getNsURIPrefix(String uri) {
        String x = uriToPrefix.getIfPresent(uri) ;
        if ( x == null ) {
            x = other.getNsURIPrefix(uri) ;
            if ( x != null )
                uriToPrefix.put(uri, x); 
        }
        return x ;
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        // Ignore cache - get everything from the provider.
        return other.getNsPrefixMap() ;
    }

    // From PrefixMappingImpl
    // Libraryize?
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
        int split = Util.splitNamespaceXML(uri) ;
        String ns = uri.substring(0, split);
        String local = uri.substring(split) ;
        if ( local.equals("") )
            return null ;
        String prefix = getNsURIPrefix(ns) ;
        return prefix == null ? null : prefix + ":" + local ;
    }
    
    @Override
    public String shortForm(String uri) {
        Optional<Entry<String, String>> e = findMapping(uri, true) ;
        if ( ! e.isPresent() )
            return uri ;
        return e.get().getKey() + ":" + uri.substring((e.get().getValue()).length()) ;
    }

    private Optional<Entry<String, String>> findMapping( String uri, boolean partial ) {
        return getNsPrefixMap().entrySet().stream().sequential().filter(e->{
            String ss = e.getValue();
            if (uri.startsWith( ss ) && (partial || ss.length() == uri.length())) 
                return true;
            return false ;
        }).findFirst() ;
    }    

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return other.samePrefixMappingAs(other) ;
    }

    @Override
    public PrefixMapping lock() {
        return this ;
    }

    @Override 
    public boolean hasNoMappings() {
        return other.hasNoMappings() ;
    }
    
    @Override
    public int numPrefixes() {
        return other.numPrefixes() ;
    }

    private static String str(PrefixMapping pmap) {
        return pmap.getNsPrefixMap().toString();
    }
    
    @Override
    public String toString() {
        // Problem : only prints the cache.
        String x = Iter.iter(prefixToUri.keys()).map(k->k+"->"+prefixToUri.getIfPresent(k)).asString(", ");
        return "pm cache: ["+prefixToUri.size()+"] "+x+" : " + str(other);
    }
}

