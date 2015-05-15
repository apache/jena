/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.store;

import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.core.DatasetPrefixStorage ;

public class GraphPrefixesProjectionTDB implements PrefixMapping {
    // Despite the name "TDB" this is general replacement for PrefixMapping
    // over DatasetPrefixStorage that manages the caching better.
    private String graphName ;
    private DatasetPrefixStorage prefixes ; 

    public GraphPrefixesProjectionTDB(String graphName, DatasetPrefixStorage prefixes)
    { 
        this.graphName = graphName ;
        this.prefixes = prefixes ;
    }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        prefixes.insertPrefix(graphName, prefix, uri); 
        return this ;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        prefixes.removeFromPrefixMap(graphName, prefix);
        return this ;
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        // TODO
        // Loop/add
        return this ;
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        // TODO
        // Loop/add
        return this ;
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        // TODO
        return this ;
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        // TODO
        return null ;
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return null ;
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return null ;
    }

    @Override
    public String expandPrefix(String prefixed) {
        return null ;
    }

    @Override
    public String shortForm(String uri) {
        return null ;
    }

    @Override
    public String qnameFor(String uri) {
        return null ;
    }

    @Override
    public PrefixMapping lock() {
        return null ;
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return false ;
    }

//    //@Override protected void regenerateReverseMapping() {}
//
//    @Override
//    public String getNsURIPrefix( String uri )
//    {
//        String x = super.getNsURIPrefix(uri) ;
//        if ( x !=  null )
//            return x ;
//        // Do a reverse read.
//        x = prefixes.readByURI(graphName, uri) ;
//        if ( x != null )
//            super.set(x, uri) ;
//        return x ;
//    }
//
//
//    @Override 
//    public Map<String, String> getNsPrefixMap()
//    {
//        Map<String, String> m =  prefixes.readPrefixMap(graphName) ;
//        // Force into the cache
//        for ( Entry<String, String> e : m.entrySet() ) 
//            super.set(e.getKey(), e.getValue()) ;
//        return m ;
//    }
//
//
//    @Override
//    protected void set(String prefix, String uri)
//    {
//        // Delete old one if present and different.
//        String x = get(prefix) ;
//        if ( x != null )
//        {
//            if(x.equals(uri))
//                // Already there - no-op (thanks to Eric Diaz for pointing this out)
//                return;
//            // Remove from cache.
//            prefixes.removeFromPrefixMap(graphName, prefix) ;
//        }
//        // Persist
//        prefixes.insertPrefix(graphName, prefix, uri) ;
//        // Add to caches. 
//        super.set(prefix, uri) ;
//    }
//
//    @Override
//    protected String get(String prefix)
//    {
//        String x = super.get(prefix) ;
//        if ( x != null )
//            return x ;
//        // In case it has been updated.
//        x = prefixes.readPrefix(graphName, prefix) ;
//        if ( x != null )
//            super.set(prefix, x) ;
//        return x ;
//    }
//
//    @Override
//    public PrefixMapping removeNsPrefix(String prefix)
//    {
//        String uri = super.getNsPrefixURI(prefix) ;
//        if ( uri != null )
//            prefixes.removeFromPrefixMap(graphName, prefix) ;
//        super.removeNsPrefix(prefix) ;
//        return this ; 
//    }
}

