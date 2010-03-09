/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.shared.PrefixMapping;


public class Prologue
{
    protected boolean seenBaseURI = false ;     // Implicit or set.
//    protected String baseURI = null ;

    protected PrefixMap prefixMap = null ;
    protected IRIResolver resolver = null ;
    
    public static Prologue create(String base, PrefixMapping pmapping)
    {
        PrefixMap pmap = null ;
        if ( pmapping != null )
        {
            pmap = new PrefixMap() ;
            Map<String, String> x =  pmapping.getNsPrefixMap() ;
            for ( Entry<String, String> e : x.entrySet() )
                pmap.add(e.getKey(), e.getValue()) ;
        }
        
        IRIResolver resolver = null ;
        if ( base != null )
            resolver = new IRIResolver(base) ;
        return new Prologue(pmap, resolver) ;
    }
    
    public Prologue() { prefixMap = new PrefixMap() ; }
    
//    public Prologue2(PrefixMapping pmap)
//    { 
//        this.prefixMap = pmap.getNsPrefixMap() ; 
//        this.resolver = null ;
//    }
//    
//    public Prologue2(PrefixMapping pmap, String base)
//    { 
//        this.prefixMap = pmap ;
//        setBaseURI(base) ;
//    }
//    
    public Prologue(PrefixMap pmap, IRIResolver resolver)
    {
        this.prefixMap = pmap ; 
        this.resolver = resolver ;
    }
    
    public Prologue(Prologue other)
    {
        this.prefixMap = other.prefixMap ; 
        this.resolver = other.resolver ;
    }

    public Prologue copy()
    {
        PrefixMap prefixMap = new PrefixMap(this.prefixMap) ;
        return new Prologue(prefixMap, resolver) ;
    }
    
    public void usePrologueFrom(Prologue other)
    {
        prefixMap = new PrefixMap(other.prefixMap) ;
        seenBaseURI = false ;
        if ( other.resolver != null )
            resolver = new IRIResolver(other.resolver.getBaseIRI()) ;
    }
    
    public Prologue sub(PrefixMap newMappings) { return sub(newMappings, null) ; }
    public Prologue sub(String base) { return sub(null, base) ; }
    
    public Prologue sub(PrefixMap newMappings, String base)
    {
        // New prefix mappings
        PrefixMap ext = getPrefixMap() ;
        if ( newMappings != null )
            ext = new PrefixMap2(ext) ;
        // New base.
        IRIResolver r = resolver ;
        if ( base != null )
            r = new IRIResolver(base) ;
        return new Prologue(ext, r) ;
    }
    
    /**
     * @return True if the query has an explicitly set base URI. 
     */
    public boolean explicitlySetBaseURI() { return seenBaseURI ; }

    /**
     * @return Returns the baseURI, if set.
     */
    public String getBaseURI()
    {
        if ( resolver == null )
            return null ;
        
//        if ( baseURI == null )
//            setDefaultBaseIRI() ;
        return resolver.getBaseIRI();
    }
    /**
     * @param baseURI The baseURI to set.
     */
    public void setBaseURI(String baseURI)
    {
        this.seenBaseURI = true ;
        this.resolver = new IRIResolver(baseURI) ; 
    }
    
    /**
     * @param resolver IRI resolver
     */
    public void setBaseURI(IRIResolver resolver)
    {
        this.seenBaseURI = true ;
        this.resolver = resolver ; 
    }
    
//    protected void setDefaultBaseIRI() { setDefaultBaseIRI(null) ; }
//    
//    protected void setDefaultBaseIRI(String base)
//    {
//        if ( baseURI != null )
//            return ;
//        
//        baseURI = IRIResolver.chooseBaseURI(base) ;
//    }
    
    // ---- Query prefixes
    
    /** Set a prefix for this query */
    public void setPrefix(String prefix, String expansion)
    {
        prefixMap.add(prefix, expansion) ;
    }   

    /** Return the prefix map from the parsed query */ 
    public PrefixMap getPrefixMap() { return prefixMap ; }
    /** Set the mapping */
    public void setPrefixMapping(PrefixMap pmap ) { prefixMap = pmap ; }

//    /** Reverse lookup of a URI to get a prefix */
//    public String getPrefix(String uriStr)
//    {
//        return prefixMap.getPrefix(uriStr) ;
//    }

    /** Get the IRI resolver */
    public IRIResolver getResolver() { return resolver ; }
    
    /** Set the IRI resolver */
    public void setResolver(IRIResolver resolver) { this.resolver = resolver; }
    
//    /** Expand prefixed name 
//     * 
//     * @param qname  The prefixed name to be expanded
//     * @return URI, or null if not expanded.
//     */
//
//    public String expandPrefixedName(String qname)
//    {
//        // Split qname.
//        
//        String s = prefixMap.expand(qname) ;
//        if ( s.equals(qname) )
//            return null ;
//        return s ;
//    }
    
//    /** Use the prefix map to turn a URI into a qname, or return the original URI */
//    
//    public String shortForm(String uri)
//    {
//        return prefixMap.shortForm(uri) ;
//    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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