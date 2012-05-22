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

package com.hp.hpl.jena.sparql.graph;

import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;

/** View of a dataset's prefixes for a particular graph */

public class GraphPrefixesProjection extends PrefixMappingImpl
{
    // Own cache and complete replace PrefixMappingImpl?

    private String graphName ;
    private DatasetPrefixStorage prefixes ; 

    public GraphPrefixesProjection(String graphName, DatasetPrefixStorage prefixes)
    { 
        this.graphName = graphName ;
        this.prefixes = prefixes ;
    }

    //@Override protected void regenerateReverseMapping() {}

    @Override
    public String getNsURIPrefix( String uri )
    {
        String x = super.getNsURIPrefix(uri) ;
        if ( x !=  null )
            return x ;
        // Do a reverse read.
        x = prefixes.readByURI(graphName, uri) ;
        if ( x != null )
            super.set(x, uri) ;
        return x ;
    }


    @Override 
    public Map<String, String> getNsPrefixMap()
    {
        Map<String, String> m =  prefixes.readPrefixMap(graphName) ;
        // Force into the cache
        for ( Entry<String, String> e : m.entrySet() ) 
            super.set(e.getKey(), e.getValue()) ;
        return m ;
    }


    @Override
    protected void set(String prefix, String uri)
    {
        // Delete old one if present and different.
        String x = get(prefix) ;
        if ( x != null )
        {
            if(x.equals(uri))
                // Already there - no-op (thanks to Eric Diaz for pointing this out)
                return;
            // Remove from cache.
            prefixes.removeFromPrefixMap(graphName, prefix) ;
        }
        // Persist
        prefixes.insertPrefix(graphName, prefix, uri) ;
        // Add to caches. 
        super.set(prefix, uri) ;
    }

    @Override
    protected String get(String prefix)
    {
        String x = super.get(prefix) ;
        if ( x != null )
            return x ;
        // In case it has been updated.
        x = prefixes.readPrefix(graphName, prefix) ;
        if ( x != null )
            super.set(prefix, x) ;
        return x ;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix)
    {
        String uri = super.getNsPrefixURI(prefix) ;
        if ( uri != null )
            prefixes.removeFromPrefixMap(graphName, prefix) ;
        super.removeNsPrefix(prefix) ;
        return this ; 
    }
}
