/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.data;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.VarAlloc;
import com.hp.hpl.jena.sdb.core.compiler.BlockBGP;
import com.hp.hpl.jena.sdb.store.StoreCustomizer;
import com.hp.hpl.jena.vocabulary.RDFS;

/** Rewrite a query based on sub/super property */

public class CustomizeProperty implements StoreCustomizer
{
    static final Node RDFS_subPropertyOf = RDFS.subPropertyOf.asNode() ;

    // s p o ==> s ?p o . ?p subPropertyOf p
    // Assumes  p subPropertyOf p
    // This may get rewritten later in the process to special SQL (e.g. a subproperty table)   
    
    public BlockBGP modify(BlockBGP block)
    {
        block = block.copy() ;
        for ( int i = 0 ; i < block.getTriples().size() ; i++ )
        {
            Triple t = block.getTriples().get(i) ;
            // Test to see if it's a propeerty we wish to 
            if ( t.getPredicate() != null )
            {
                Node v = VarAlloc.getVarAllocator().allocVarNode() ;
                Node property = t.getPredicate() ;
                // Assumes that <type> rdfs:subClassOf <type> 
                Triple t1 = new Triple(t.getSubject(), v, t.getObject()) ;
                Triple t2 = new Triple(v, RDFS_subPropertyOf, t.getPredicate()) ;
                block.getTriples().set(i, t1) ;
                block.getTriples().add(i+1, t2) ;
                // Skip the indexer over the extra triple.
                i++ ;
            }
        }
        return block ;
    }
    
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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