/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openjena.atlas.lib.Pair ;


import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

/** Lightweight, prefixmapping for parsers.  No XML rules, no reverse lookup. */
public class PrefixMap
{
    private final Map<String, IRI> prefixes = new HashMap<String, IRI>() ;
    private final Map<String, IRI> prefixes2 = Collections.unmodifiableMap(prefixes) ;
    
    public PrefixMap() {}
    
    public PrefixMap(PrefixMap other)
    {
        prefixes.putAll(other.prefixes) ;
    }
    
    /** return the underlying mapping - do not modify */
    public Map<String, IRI> getMapping() { return prefixes2 ; }
    
    /** Add a prefix, overwites any existing association */
    public void add(String prefix, String iriString)
    { 
        prefix = canonicalPrefix(prefix) ;
        IRI iri = IRIFactory.iriImplementation().create(iriString) ;
        // Check!
        prefixes.put(prefix, iri);
    }
    
    /** Add a prefix, overwites any existing association */
    public void add(String prefix, IRI iri)
    { 
        prefix = canonicalPrefix(prefix) ;
        prefixes.put(prefix, iri) ;
    }
    
    /** Delete a prefix */
    public void delete(String prefix)
    { 
        prefix = canonicalPrefix(prefix) ;
        prefixes.remove(prefix) ;
    }
    
    public boolean contains(String prefix)
    { 
        prefix = canonicalPrefix(prefix) ;
        return _contains(prefix) ;
    }
    
    protected boolean _contains(String prefix)
    { 
        return prefixes.containsKey(prefix) ;
    }
    
    /** Abbrevaite an IRI or retrn null */
    public String abbreviate(String uriStr)
    {
        for ( Entry<String, IRI> e : prefixes.entrySet())
        {
            String prefix = e.getValue().toString() ;
            
            if ( uriStr.startsWith(prefix) )
                return e.getKey()+":"+uriStr.substring(prefix.length()) ;
        }
        return null ;
    }
    
    /** Abbrevaite an IRI or retrn null */
    public Pair<String, String> abbrev(String uriStr)
    {
        for ( Entry<String, IRI> e : prefixes.entrySet())
        {
            String prefix = e.getValue().toString() ;
            
            if ( uriStr.startsWith(prefix) )
                return new Pair<String, String>(e.getKey(), uriStr.substring(prefix.length())) ;
        }
        return null ;
    }

    
    /** Expand a prefix, return null if it can't be expanded */
    public String expand(String prefix, String localName) 
    { 
        prefix = canonicalPrefix(prefix) ;
        IRI x = prefixes.get(prefix) ;
        if ( x == null )
            return null ;
        return x.toString()+localName ;
    }

    protected static String canonicalPrefix(String prefix)
    {
        if ( prefix.endsWith(":") )
            return prefix.substring(0, prefix.length()-1) ;
        return prefix ;
    }
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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