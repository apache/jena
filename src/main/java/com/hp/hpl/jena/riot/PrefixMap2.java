/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.iri.IRI;

// UNUSED
/** Extend a PrefixMap - never alters the partent PrefixMap */
public class PrefixMap2 extends PrefixMap
{
    PrefixMap parent ;
    PrefixMap local ;
    
    public PrefixMap2(PrefixMap parent)
    {
        this.parent = parent ;
        this.local = new PrefixMap() ; 
    }
    
    /** Add a prefix, overwites any existing association */
    @Override
    public void add(String prefix, IRI iri)
    { 
        prefix = canonicalPrefix(prefix) ;
        // Add to local always.
        local.add(prefix, iri) ;
    }
    
    /** Add a prefix, overwites any existing association */
    @Override
    public void delete(String prefix)
    { 
        prefix = canonicalPrefix(prefix) ;
        local.delete(prefix) ;
        if ( parent._contains(prefix) )
            Log.warn(this, "Attempt to delete a prefix in the parent" ) ;
    }
    
    /** Expand a prefix, return null if it can't be expanded */
    @Override
    public String expand(String prefix, String localName) 
    { 
        prefix = canonicalPrefix(prefix) ;
        String x = local.expand(prefix, localName) ;
        if ( x != null )
            return x ;
        return parent.expand(prefix, localName) ;
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