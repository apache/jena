/**
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
