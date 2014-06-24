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

package com.hp.hpl.jena.sparql.util;

import java.util.Map ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;

/** A prefix mapping based on global and local mappings.
 *  Updates go to the local (second) copy only.
 *  Lookup looks in the local copy before the global copy. */

public class PrefixMapping2 implements PrefixMapping
{
    PrefixMapping pmapGlobal ;
    PrefixMapping pmapLocal ;
    
    public PrefixMapping2(PrefixMapping globalMapping, PrefixMapping localMapping)
    {
        pmapGlobal = globalMapping ;
        pmapLocal = localMapping ;
    }
    
    public PrefixMapping2(PrefixMapping globalMapping)
    {
        this(globalMapping, new PrefixMappingImpl()) ;
    }
    
    public PrefixMapping getLocalPrefixMapping() { return pmapLocal ; }
    public PrefixMapping getGlobalPrefixMapping() { return pmapGlobal ; }

    public void setLocalPrefixMapping(PrefixMapping x)  { pmapLocal = x ; }
    public void setGlobalPrefixMapping(PrefixMapping x) { pmapGlobal = x ; }

    
    /** @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefix(java.lang.String, java.lang.String) */
    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri)
    {
        pmapLocal.setNsPrefix(prefix, uri) ;
        return this ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#removeNsPrefix(java.lang.String) */
    @Override
    public PrefixMapping removeNsPrefix(String prefix)
    {
        pmapLocal.removeNsPrefix(prefix) ;
        if ( pmapGlobal != null && pmapGlobal.getNsPrefixURI(prefix) != null )
            throw new UnsupportedOperationException("PrefixMapping2: prefix '"+prefix+"' in the immutable map") ;
        return this ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(com.hp.hpl.jena.shared.PrefixMapping) */
    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other)
    {
       pmapLocal.setNsPrefixes(other) ;
       return this ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(java.util.Map) */
    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map)
    {
        pmapLocal.setNsPrefixes(map) ;
        return this;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#getNsPrefixURI(java.lang.String) */
    @Override
    public String getNsPrefixURI(String prefix)
    {
        String s = pmapLocal.getNsPrefixURI(prefix) ;
        if ( s != null )
            return s ;

        if ( pmapGlobal != null )
            return pmapGlobal.getNsPrefixURI(prefix) ;
        return null ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#getNsURIPrefix(java.lang.String) */
    @Override
    public String getNsURIPrefix(String uri)
    {
        String s = pmapLocal.getNsURIPrefix(uri) ;
        if ( s != null )
            return s ;
        if ( pmapGlobal == null )
            return null ;
        if ( pmapGlobal != null )
            return pmapGlobal.getNsURIPrefix(uri) ;
        return null ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#getNsPrefixMap() */
    @Override
    public Map<String, String> getNsPrefixMap() { return getNsPrefixMap(true) ; }
    
    public Map<String, String> getNsPrefixMap(boolean includeGlobalMap)
    {
        Map<String, String> m1 = pmapLocal.getNsPrefixMap() ;
        if ( pmapGlobal == null )
            return m1 ;
        if ( includeGlobalMap )
        {
            Map<String, String> m2 = pmapGlobal.getNsPrefixMap() ;
            m1.putAll(m2) ;
        }
        return m1 ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#expandPrefix(java.lang.String) */
    @Override
    public String expandPrefix(String prefixed)
    {
        // Returns the unchanged prefixed name if no transformation
        // Helps cope with unusual URIs schemes.
        String s = pmapLocal.expandPrefix(prefixed) ;
        if ( pmapGlobal == null )
            return s ;
        
        if ( s == null || s.equals(prefixed) )
        {
            if ( pmapGlobal != null )
                s = pmapGlobal.expandPrefix(prefixed) ;
        }
        return s ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#shortForm(java.lang.String) */
    @Override
    public String shortForm(String uri)
    {
        String s = pmapLocal.shortForm(uri) ;
        if ( pmapGlobal == null )
            return s ;
        
        if ( s == null || s.equals(uri) )
            s = pmapGlobal.shortForm(uri) ;
        return s ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#qnameFor(java.lang.String) */
    @Override
    public String qnameFor(String uri)
    {
        String s = pmapLocal.qnameFor(uri) ;
        if ( pmapGlobal == null )
            return s ;
        if ( s != null )
            return s ;
        if ( pmapGlobal != null )
            return pmapGlobal.qnameFor(uri) ;
        return null ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#lock() */
    @Override
    public PrefixMapping lock()
    {
        pmapLocal.lock() ;
        return this;
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map)
    {
        for ( Map.Entry<String, String> e : map.getNsPrefixMap().entrySet() )
        {
            String prefix = e.getKey();
            String uri = e.getValue();
            if ( getNsPrefixURI( prefix ) == null && getNsURIPrefix( uri ) == null )
            {
                setNsPrefix( prefix, uri );
            }
        }
        return this;
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other)
    {
        if ( other == null )
            return false ;
        
        if ( other instanceof PrefixMapping2 )
        {
            PrefixMapping2 other2 = (PrefixMapping2)other ;
            
            return this.pmapGlobal.samePrefixMappingAs(other2.pmapGlobal) && 
                   this.pmapLocal.samePrefixMappingAs(other2.pmapLocal) ;
        }
        
        // Do by map copy.
        return getNsPrefixMap().equals( other.getNsPrefixMap() );
    }
}
