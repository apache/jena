/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.reorder;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.tdb.TDBException;

public class ReorderProcIndexes implements ReorderProc
{
    private int[] indexes ;

    public ReorderProcIndexes(int[] indexes)
    {
        this.indexes = indexes ;   
    }
    
    /** Return a new basic pattern with the same triples as the input,
     *  but ordered as per the index list of this reorder processor. 
     */ 
    //@Override
    public BasicPattern reorder(BasicPattern bgp)
    {
        if ( indexes.length != bgp.size() )
        {
            String str = String.format("Expected size = %d : actual basic pattern size = %d", indexes.length, bgp.size()) ;
            ALog.fatal(this, str) ;
            throw new TDBException(str) ; 
        }        
        
        BasicPattern bgp2 = new BasicPattern() ; 
        for ( int j = 0 ; j < indexes.length ; j++ )
        {
            int idx = indexes[j] ;
            Triple t = bgp.get(idx) ;
            bgp2.add(t) ;
        }
        return bgp2 ;
    }
    
    @Override
    public String toString()
    {
        String x = "" ;
        String sep = "" ;
        for ( int idx : indexes )
        {
            x = x + sep + idx ;
            sep = ", " ;
        }
        return x;
    }
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