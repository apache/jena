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

package org.openjena.riot.system;

import java.util.Collections ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;

/** Lightweight, prefix mapping for parsers.  No XML rules, no reverse lookup. */
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
    
    /** return a copy of the underlying mapping */
    public Map<String, IRI> getMappingCopy() { return new HashMap<String, IRI>(prefixes) ; }

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
    
    /** Add a prefix, overwites any existing association */
    public void putAll(PrefixMap pmap)
    {
        prefixes.putAll(pmap.prefixes) ; 
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
    
    /** Abbreviate an IRI or return null */
    public String abbreviate(String uriStr)
    {
        for ( Entry<String, IRI> e : prefixes.entrySet())
        {
            String prefix = e.getValue().toString() ;
            
            if ( uriStr.startsWith(prefix) )
            {
                String ln = uriStr.substring(prefix.length()) ;
                if ( strSafeFor(ln, '/') && strSafeFor(ln, '#') && strSafeFor(ln, ':') )
                    return e.getKey()+":"+ln ;
            }
        }
        return null ;
    }
    
    private static boolean strSafeFor(String str, char ch) { return str.indexOf(ch) == -1 ; } 
    
    
    /** Abbreviate an IRI or retrn null */
    public Pair<String, String> abbrev(String uriStr)
    {
        for ( Entry<String, IRI> e : prefixes.entrySet())
        {
            String uriForPrefix = e.getValue().toString() ;
            
            if ( uriStr.startsWith(uriForPrefix) )
                return Pair.create(e.getKey(), uriStr.substring(uriForPrefix.length())) ;
        }
        return null ;
    }

    /** Expand a prefix named, return null if it can't be expanded */
    public String expand(String prefixedName) 
    { 
        int i = prefixedName.indexOf(':') ;
        if ( i < 0 ) return null ;
        return expand(prefixedName.substring(0,i),
                      prefixedName.substring(i+1)) ;
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
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append("{ ") ;
        boolean first = true ;
        
        for ( Entry<String, IRI> e : prefixes.entrySet())
        {
            String prefix = e.getKey() ;
            IRI iri = e.getValue() ;
            if ( first )
                first = false ;
            else
                sb.append(" ,") ;
            sb.append(prefix) ;
            sb.append(":=") ;
            sb.append(iri.toString()) ;
        }
        sb.append(" }") ;
        return sb.toString() ; 
    }
}
