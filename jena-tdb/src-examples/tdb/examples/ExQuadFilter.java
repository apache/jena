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

package tdb.examples;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.lib.Tuple ;

import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TDBInternal ;

/** Example of how to filter quads as they are accessed at the lowest level.
 * Can be used to exclude data from specific graphs.   
 * This mechanism is not limited to graphs - it works for properties or anything
 * where the visibility of otherwise is determined by the elements of the quad. 
 * See <a href="http://incubator.apache.org/jena/documentation/tdb/quadfilter.html">QuadFiltering</a>
 * for further details.
 */

public class ExQuadFilter
{
    private static String graphToHide = "http://example/g2" ;

    public static void main(String ... args)
    {
        // This also works for default union graph ....
        TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        
        Dataset ds = setup() ;
        Filter<Tuple<NodeId>> filter = createFilter(ds) ;
        example(ds, filter) ;
    }
    
    /** Example setup - in-memory dataset with two graphs, one triple in each */
    private static Dataset setup()
    {
        Dataset ds = TDBFactory.createDataset() ;
        DatasetGraph dsg = ds.asDatasetGraph() ;
        Quad q1 = SSE.parseQuad("(<http://example/g1> <http://example/s> <http://example/p> <http://example/o1>)") ;
        Quad q2 = SSE.parseQuad("(<http://example/g2> <http://example/s> <http://example/p> <http://example/o2>)") ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        return ds ;
    }
        
    /** Create a filter to exclude the graph http://example/g2 */
    private static Filter<Tuple<NodeId>> createFilter(Dataset ds)
    {
        // Filtering operates at a very low level: 
        // Need to know the internal identifier for the graph name. 
        final NodeId target = TDBInternal.getNodeId(ds, NodeFactory.createURI(graphToHide)) ;

        System.out.println("Hide graph: "+graphToHide+" --> "+target) ;
        
        // Filter for accept/reject as quad as being visible.
        // Return true for "accept", false for "reject"
        Filter<Tuple<NodeId>> filter = new Filter<Tuple<NodeId>>() {
            @Override
            public boolean accept(Tuple<NodeId> item)
            {
                // Reverse the lookup as a demo
                //Node n = TDBInternal.getNode(target) ;
                //System.err.println(item) ;
                if ( item.size() == 4 && item.get(0).equals(target) )
                {
                    //System.out.println("Reject: "+item) ;
                    return false ;
                }
                //System.out.println("Accept: "+item) ;
                return true ;
            } } ;
            
        return filter ;
    }            
        
    private static void example(Dataset ds, Filter<Tuple<NodeId>> filter)
    {
        String[] x = {
            "SELECT * { GRAPH ?g { ?s ?p ?o } }",
            "SELECT * { ?s ?p ?o }",
            // This filter does not hide the graph itself, just the quads associated with the graph.
            "SELECT * { GRAPH ?g {} }"
            } ;
        
        for ( String qs : x )
        {
            example(ds, qs, filter) ;
            example(ds, qs, null) ;
        }
        
    }

    private static void example(Dataset ds, String qs, Filter<Tuple<NodeId>> filter)
    {
        System.out.println() ;
        Query query = QueryFactory.create(qs) ;
        System.out.println(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        // Install filter for this query only.
        if ( filter != null )
        {
            System.out.println("Install quad-level filter") ;
            qExec.getContext().set(SystemTDB.symTupleFilter, filter) ;
        }
        else
            System.out.println("No quad-level filter") ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;

    }
        
}
