/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.Date ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.StringUtils ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableI ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Overall framework for bulk loading */
public class BulkLoader
{
    boolean showProgress = true ;
    
    LoaderNodeTupleTable[] tupleTables ;
    
    public Destination<Triple> loadTriples(DatasetGraphTDB dsg)
    {
        return loadTriples(dsg, dsg.getTripleTable().getNodeTupleTable()) ;
    }
    
    private Destination<Triple> loadTriples(DatasetGraphTDB dsg, NodeTupleTableI nodeTupleTable)
    {
        final LoaderNodeTupleTable x = new LoaderNodeTupleTable(null, 
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                true) ;
        tupleTables = new LoaderNodeTupleTable[]{x} ;
        
        Destination<Triple> sink = new Destination<Triple>() {
            public void start()
            {
                tupleTables[0].loadStart() ;
            }
            public void send(Triple triple)
            {
                x.load(triple.getSubject(), triple.getPredicate(),  triple.getObject()) ;
            }

            public void flush() { }
            public void close() {tupleTables[0].loadFinish() ; }

        } ;
        return sink ;
    }

    public Destination<Triple> loadTriples(DatasetGraphTDB dsg, Node graphName)
    {
        NodeTupleTableI ntt = dsg.getQuadTable().getNodeTupleTable() ;
        NodeTupleTableI ntt2 = null ;
        // mask /project to quads[graphName]<->triple
        return loadTriples(dsg, ntt2) ;
    }
    
    public Destination<Quad> loadQuads(DatasetGraphTDB dsg)
    {
        final LoaderNodeTupleTable x1 = new LoaderNodeTupleTable(null, 
                                                                dsg.getTripleTable().getNodeTupleTable(),
                                                                true) ;
        final LoaderNodeTupleTable x2 = new LoaderNodeTupleTable(null, 
                                                                 dsg.getQuadTable().getNodeTupleTable(),
                                                                 true) ;
        
        tupleTables = new LoaderNodeTupleTable[]{x1,x2} ;
        
        Destination<Quad> sink = new Destination<Quad>() {
            public void start()
            {
                for ( LoaderNodeTupleTable ntt : tupleTables )
                    ntt.loadStart() ;
            }
            
            public void send(Quad quad)
            {
                
                //x.load(quad.getGraph(), quad.getSubject(), quad.getPredicate(),  quad.getObject()) ;
            }

            public void flush() { }

            public void close()
            {
                for (LoaderNodeTupleTable ntt : tupleTables)
                    ntt.loadFinish() ;
            }
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