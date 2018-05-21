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
import java.util.Map.Entry ;
import java.util.Optional ;

import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.DatasetPrefixStorage ;

public class GraphPrefixesProjectionTDB implements PrefixMapping {
    // Despite the name "TDB" this is general replacement for
    // PrefixMapping over DatasetPrefixStorage.
    private final String graphName ;
    private final DatasetPrefixStorage prefixes ; 

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
    public PrefixMapping clearNsPrefixMap() {
        prefixes.removeAllFromPrefixMap(graphName);
        return this ;
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        setNsPrefixes(other.getNsPrefixMap()) ;
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
    public PrefixMapping withDefaultMappings(PrefixMapping other) {
        other.getNsPrefixMap().entrySet().forEach(entry->{
            String prefix = entry.getKey() ;
            String uri = entry.getValue();
            if (getNsPrefixURI( prefix ) == null && getNsURIPrefix( uri ) == null)
                setNsPrefix( prefix, uri );
        }) ;
        return this ;
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        return prefixes.readPrefix(graphName, prefix) ;
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return prefixes.readByURI(graphName, uri) ;
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return prefixes.readPrefixMap(graphName) ;
    }

    // From PrefixMappingImpl
    @Override
    public String expandPrefix(String prefixed) {
        {
            int colon = prefixed.indexOf(':') ;
            if ( colon < 0 )
                return prefixed ;
            else {
                String prefix = prefixed.substring(0, colon) ;
                String uri = prefixes.readPrefix(graphName, prefix) ;
                return uri == null ? prefixed : uri + prefixed.substring(colon + 1) ;
            }
        }
    }

    @Override
    public String qnameFor(String uri) {
        int split = Util.splitNamespaceXML(uri) ;
        String ns = uri.substring(0, split); 
        String local = uri.substring(split) ;
        if ( local.equals("") )
            return null ;
        String prefix = prefixes.readByURI(graphName, ns) ;
        return prefix == null ? null : prefix + ":" + local ;
    }
    
    @Override
    public String shortForm(String uri) {
        Optional<Entry<String, String>> e = findMapping(uri, true) ;
        if ( ! e.isPresent() )
            return uri ;
        return e.get().getKey() + ":" + uri.substring((e.get().getValue()).length()) ;
    }

    // Do better?
    
    @Override
    public boolean hasNoMappings() { return getNsPrefixMap().isEmpty() ; }
    
    @Override
    public int numPrefixes() {
        return getNsPrefixMap().size() ;
    }

    private Optional<Entry<String, String>> findMapping( String uri, boolean partial )
    {
        return getNsPrefixMap().entrySet().stream().sequential().filter(e->{
            String ss = e.getValue();
            if (uri.startsWith( ss ) && (partial || ss.length() == uri.length())) 
                return true;
            return false ;
        }).findFirst() ;
    }    
    
    @Override
    public PrefixMapping lock() {
        return this ;
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return this.getNsPrefixMap().equals(other.getNsPrefixMap()) ;
    }

}

