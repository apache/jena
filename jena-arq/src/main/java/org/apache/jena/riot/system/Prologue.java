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

package org.apache.jena.riot.system;

import org.apache.jena.iri.IRI ;
import org.apache.jena.shared.PrefixMapping ;

public class Prologue
{
    protected boolean seenBaseURI = false ;     // Implicit or set.
//    protected String baseURI = null ;

    protected PrefixMap prefixMap = null ;
    protected IRIResolver resolver = null ;

    public static Prologue create(String base, PrefixMapping pmapping)
    {
        PrefixMap pmap = new PrefixMapStd() ;
        if ( pmapping != null )
            // Copy to isolate
            pmap.putAll(pmapping);
        IRIResolver resolver = null ;
        if ( base != null )
            resolver = IRIResolver.create(base) ;
        return new Prologue(pmap, resolver) ;
    }

    public Prologue()
    {
        this.prefixMap = PrefixMapFactory.create() ;
        this.resolver = null ;
    }

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
        PrefixMap prefixMap = PrefixMapFactory.create(this.prefixMap) ;
        return new Prologue(prefixMap, resolver) ;
    }

    public void usePrologueFrom(Prologue other)
    {
        // Copy.
        prefixMap = PrefixMapFactory.create(other.prefixMap) ;
        seenBaseURI = false ;
        if ( other.resolver != null )
            resolver = IRIResolver.create(other.resolver.getBaseIRIasString()) ;
    }

    public Prologue sub(PrefixMap newMappings)  { return sub(newMappings, null) ; }
    public Prologue sub(String base)            { return sub(null, base) ; }

    public Prologue sub(PrefixMap newMappings, String base) {
        // New prefix mappings
        PrefixMap ext = getPrefixMap() ;
        if ( newMappings != null )
            ext.putAll(newMappings);
        // New base.
        IRIResolver r = resolver ;
        if ( base != null )
            r = IRIResolver.create(base) ;
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
        return resolver.getBaseIRIasString();
    }
    /**
     * @param baseURI The baseURI to set.
     */
    public void setBaseURI(String baseURI)
    {
        this.seenBaseURI = true ;
        this.resolver = IRIResolver.create(baseURI) ;
    }

    public void setBaseURI(IRI iri)
    {
        this.seenBaseURI = true ;
        this.resolver = IRIResolver.create(iri) ;
    }

    /**
     * @param resolver IRI resolver
     */
    public void setBaseURI(IRIResolver resolver)
    {
        this.seenBaseURI = true ;
        this.resolver = resolver ;
    }

    // ---- Query prefixes

    /** Set a prefix for this query */
    public void setPrefix(String prefix, String expansion)
    {
        prefixMap.add(prefix, expansion) ;
    }

    /** Return the prefix map from the parsed query */
    public PrefixMap getPrefixMap() { return prefixMap ; }

    /** Get the IRI resolver */
    public IRIResolver getResolver() { return resolver ; }

    /** Set the IRI resolver */
    public void setResolver(IRIResolver resolver) { this.resolver = resolver; }
}
