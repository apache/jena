/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Date ;

import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Overall framework for bulk loading */
public class BulkLoader
{
    boolean showProgress = true ;
    
    public Sink<Triple> loadTriples(DatasetGraphTDB dsg)
    {
        final LoaderNodeTupleTable x = new LoaderNodeTupleTable(null, 
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                true) ;
        Sink<Triple> sink = new Sink<Triple>() {
            public void send(Triple item)
            {
                Node[] nodes = new Node[3] ;
                nodes[0] = item.getSubject() ;
                nodes[1] = item.getPredicate() ;
                nodes[2] = item.getObject() ;
                x.load(item.getSubject(),item.getPredicate(),  item.getObject()) ;
            }

            public void flush() { x.sync() ; }
            public void close() { x.close() ; }
        } ;
        return sink ;
    }
    
    
    // Start
    // data
    // finish
    
    // ---- Misc utilities
    synchronized void printf(String fmt, Object... args)
    {
        if (!showProgress) return ;
        System.out.printf(fmt, args) ;
    }

    synchronized void println()
    {
        if (!showProgress) return ;
        System.out.println() ;
    }

    synchronized void println(String str)
    {
        if (!showProgress) return ;
        System.out.println(str) ;
    }

    synchronized void now(String str)
    {
        if (!showProgress) return ;

        if (str != null)
        {
            System.out.print(str) ;
            System.out.print(" : ") ;
        }
        System.out.println(StringUtils.str(new Date())) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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