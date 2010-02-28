/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.tdb.migrate.DatasetPrefixStorage ;

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

    //@Override
    //protected void regenerateReverseMapping() {}

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
            prefixes.removeFromPrefixMap(graphName, prefix, x) ;
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
            prefixes.removeFromPrefixMap(graphName, prefix, uri) ;
        super.removeNsPrefix(prefix) ;
        return this ; 
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