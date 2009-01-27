/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.Map;
import java.util.Iterator;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/** A prefix mapping based on global and local mappings.
 *  Updates go to the local (second) copy only.
 *  Lookup looks in the local copy before the global copy.
 * 
 * @author Andy Seaborne
 */

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
    public PrefixMapping setNsPrefix(String prefix, String uri)
    {
        pmapLocal.setNsPrefix(prefix, uri) ;
        return this ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#removeNsPrefix(java.lang.String) */
    public PrefixMapping removeNsPrefix(String prefix)
    {
        pmapLocal.removeNsPrefix(prefix) ;
        if ( pmapGlobal != null && pmapGlobal.getNsPrefixURI(prefix) != null )
            throw new UnsupportedOperationException("PrefixMapping2: prefix '"+prefix+"' in the immutable map") ;
        return this ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(com.hp.hpl.jena.shared.PrefixMapping) */
    public PrefixMapping setNsPrefixes(PrefixMapping other)
    {
       pmapLocal.setNsPrefixes(other) ;
       return this ;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(java.util.Map) */
    public PrefixMapping setNsPrefixes(Map<String, String> map)
    {
        pmapLocal.setNsPrefixes(map) ;
        return this;
    }

    /** @see com.hp.hpl.jena.shared.PrefixMapping#getNsPrefixURI(java.lang.String) */
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
    public PrefixMapping lock()
    {
        pmapLocal.lock() ;
        return this;
    }

    public PrefixMapping withDefaultMappings(PrefixMapping map)
    {
        Iterator<Map.Entry<String, String>> it = map.getNsPrefixMap().entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry<String, String> e = it.next();
            String prefix = e.getKey();
            String uri = e.getValue();
            if (getNsPrefixURI( prefix ) == null && getNsURIPrefix( uri ) == null)
                setNsPrefix( prefix, uri );
            }
        return this;
    }

    public boolean samePrefixMappingAs(PrefixMapping other)
    {
        if ( other == null )
            return false ;
        
        if ( ! ( other instanceof PrefixMapping2 ) )
            return false ;
        
        PrefixMapping2 other2 = (PrefixMapping2)other ;
        
        return this.pmapGlobal.samePrefixMappingAs(other2.pmapGlobal) && 
               this.pmapLocal.samePrefixMappingAs(other2.pmapLocal) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */