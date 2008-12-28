/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/** A persistence layer for a PrefixMapping (not used : TDB DatasetP{refixes is better) */ 

public abstract class PrefixMappingPersistent extends PrefixMappingImpl
{
    private String graphName ; 

    public PrefixMappingPersistent(String graphURI)
    {
        super() ;
        graphName = graphURI ;
        try { readPrefixMapping(graphName) ; }
        catch (Throwable th) { }
    }

//    @Override 
//    public Map<String, String> getNsPrefixMap()
//    { return super.getNsPrefixMap() ; }
    
    @Override
    protected void set(String prefix, String uri)
    {
        super.set(prefix, uri) ;
        insertIntoPrefixMap(graphName, prefix, uri) ;
    }

    @Override
    protected String get(String prefix)
    {
        String x = super.get(prefix) ;
        if ( x !=  null )
            return x ;
        // In case it has been updated.
        return readFromPrefixMap(graphName, prefix) ;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix)
    {
        String uri = super.getNsPrefixURI(prefix) ;
        if ( uri != null )
            removeFromPrefixMap(graphName, prefix, uri) ;
        super.removeNsPrefix(prefix) ;
        return this ; 
    }

    // Abstraction of storage.

    /** Boot strap - preload prefixes */
    protected abstract void readPrefixMapping(String graphName) ;

    /** Read a prefix */
    protected abstract String readFromPrefixMap(String graphName, String prefix) ;
    /** Add or update a prefix */
    protected abstract void insertIntoPrefixMap(String graphName, String prefix, String uri) ;
    /** Remove a  prefix mapping*/
    protected abstract void removeFromPrefixMap(String graphName, String prefix, String uri) ;

    // Always put in a trailing ":" so the prefix is never the empty string.
    // Convenience for database systems that think the empoty string and the
    // null string are the same. 

    private String encode(String prefix)
    { return prefix+":" ; }

    private String decode(String prefix)
    { return prefix.substring(0, prefix.length()-1) ; }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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