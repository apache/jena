/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import java.util.Iterator;

import org.openjena.atlas.lib.Tuple ;



import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.GraphTDBBase;
import com.hp.hpl.jena.tdb.store.NodeId;

public class BulkUpdateHandlerTDB extends SimpleBulkUpdateHandler implements BulkUpdateHandler
{
    GraphTDBBase graphTDB ;
    
    public BulkUpdateHandlerTDB(GraphTDBBase graph)
    {
        super(graph) ;
        this.graphTDB = graph ;
    }

//    @Override
//    public void add(Triple[] triples)
//    {}
//
//    @Override
//    public void add(List triples)
//    { }
//
//    @Override
//    public void add(Iterator it)
//    {}
//
//    @Override
//    public void add(Graph g)
//    {}
//
//    @Override
//    public void add(Graph g, boolean withReifications)
//    {}
//
//    @Override
//    public void delete(Triple[] triples)
//    {}
//
//    @Override
//    public void delete(List triples)
//    {}
//
//    @Override
//    public void delete(Iterator it)
//    {}
//
//    @Override
//    public void delete(Graph g)
//    {}
//
//    @Override
//    public void delete(Graph g, boolean withReifications)
//    {}
//
    
    // Testcases needed
    @Override
    public void remove(Node s, Node p, Node o)
    {
        s = fix(s) ;
        p = fix(p) ;
        o = fix(o) ;
        removeWorker(s,p,o) ;
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) );
    }

    private static Node fix(Node n) { return (n!=null)? n : Node.ANY ; }
    
    @Override
    public void removeAll()
    {
         removeWorker(null, null, null) ;
         notifyRemoveAll(); 
    }
    
    private static final int sliceSize = 1000 ;
    
    private void removeWorker(Node s, Node p, Node o)
    {
        graphTDB.startUpdate() ;
        
        // Delete in batches.
        // That way, there is no active iterator when a delete 
        // from the indexes happens.
        
        NodeTupleTable t = graphTDB.getNodeTupleTable() ;
        Node gn = graphTDB.getGraphNode() ;
        
        @SuppressWarnings("unchecked")
        Tuple<NodeId>[] array = (Tuple<NodeId>[])new Tuple<?>[sliceSize] ;
        
        while (true)
        {
            // Convert/cache s,p,o?
            // The Node Cache will catch these so don't worry unduely. 
            Iterator<Tuple<NodeId>> iter = null ;
            if ( gn == null )
                iter = t.findAsNodeIds(s, p, o) ;
            else
                iter = t.findAsNodeIds(gn, s, p, o) ;
            
            if ( iter == null )
                // Finished?
                return ;
            
            //Arrays.fill(array, null) ;
            
            // Get the first sliceSize
            int len = 0 ;
            for ( ; len < sliceSize ; len++ )
            {
                if ( !iter.hasNext() ) break ;
                array[len] = iter.next() ;
            }
            // Delete them.
            for ( int i = 0 ; i < len ; i++ )
            {
                t.getTupleTable().delete(array[i]) ;
                array[i] = null ;
            }
            // Finished?
            if ( len < sliceSize )
                break ;
        }
        
        graphTDB.finishUpdate() ;

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