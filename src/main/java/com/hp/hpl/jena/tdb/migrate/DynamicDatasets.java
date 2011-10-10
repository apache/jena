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

package com.hp.hpl.jena.tdb.migrate;

import java.util.Collection ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphMap ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class DynamicDatasets
{
//    // See QueryEngineTDB.dynamicDatasetOp
//    public static Op dynamicDatasetOp(Query query, Op op)
//    {
    // problems with paths and property functions? which access the active graph. 
//        return TransformDynamicDataset.transform(query, op) ;
//    }
    
    /** Given a DatasetGraph and a query, form a DatasetGraph that 
     * is the dynamic dataset from the query.
     * Returns the original DatasetGraph if the query has no dataset description.
     */ 
    public static DatasetGraph dynamicDataset(Query query, DatasetGraph dsg, boolean defaultUnionGraph)
    {
        if ( query.hasDatasetDescription() )
        {
            Set<Node> defaultGraphs = NodeUtils2.convertToNodes(query.getGraphURIs()) ; 
            Set<Node> namedGraphs = NodeUtils2.convertToNodes(query.getNamedGraphURIs()) ;
            return dynamicDataset(defaultGraphs, namedGraphs, dsg, defaultUnionGraph) ; 
        }
        return dsg ;
    }
    
    
    /** Given a Dataset and a query, form a Dataset that 
     * is the dynamic dataset from the query.
     * Returns the original Dataset if the query has no dataset description.
     */ 
    public static Dataset dynamicDataset(Query query, Dataset ds, boolean defaultUnionGraph)
    {
        DatasetGraph dsg = ds.asDatasetGraph() ;
        DatasetGraph dsg2 = dynamicDataset(query, dsg, defaultUnionGraph) ;
        if ( dsg == dsg2 )
            return ds ;
        return DatasetFactory.create(dsg2) ;
    }

    /** Given a DatasetGraph and a query, form a DatasetGraph that 
     * is the dynamic dataset from the collection of graphs from the dataset
     * that go to make up the default graph (union) and named graphs.  
     */
    public static DatasetGraph dynamicDataset(Collection<Node> defaultGraphs, Collection<Node> namedGraphs, DatasetGraph dsg, boolean defaultUnionGraph)
    {
        Graph dft = new GraphUnionRead(dsg, defaultGraphs) ;
        DatasetGraph dsg2 = new DatasetGraphMap(dft) ;
        
        // The named graphs.
        for ( Node gn : namedGraphs )
        {
            Graph g = GraphOps.getGraph(dsg, gn) ;
            if ( g != null )
                dsg2.addGraph(gn, g) ;
        }
        
        if ( dsg.getContext() != null )
            dsg2.getContext().putAll(dsg.getContext()) ;

        if ( defaultUnionGraph && defaultGraphs.size() == 0 )
        {
            // Create a union graph - there were no defaultGraphs explicitly named.
            Graph unionGraph = new GraphUnionRead(dsg, namedGraphs) ;
            dsg2.setDefaultGraph(unionGraph) ;
        }

        // read-only, <urn:x-arq:DefaultGraph> and <urn:x-arq:UnionGraph> processing.
        dsg2 = new DynamicDatasetGraph(dsg2) ;
        return dsg2 ;
    }
    
    public static class DynamicDatasetGraph extends DatasetGraphReadOnly
    {
        public DynamicDatasetGraph(DatasetGraph dsg)
        {
            super(dsg) ;
        }

        private Graph unionGraph = null ;
        
        // See also the GraphOps
        @Override
        public Graph getGraph(Node graphNode)
        {
            if ( Quad.isUnionGraph(graphNode) )
            {
                if ( unionGraph == null )
                    unionGraph = GraphOps.unionGraph(super.getWrapped()) ;
                return unionGraph ;
            }
            if ( Quad.isDefaultGraphExplicit(graphNode))
                return getDefaultGraph() ;
            
            return super.getGraph(graphNode) ;
        }
    }
}
