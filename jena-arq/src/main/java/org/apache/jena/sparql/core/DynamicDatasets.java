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

package org.apache.jena.sparql.core;

import java.util.Collection ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.graph.GraphOps ;
import org.apache.jena.sparql.graph.GraphUnionRead ;
import org.apache.jena.sparql.util.NodeUtils ;

public class DynamicDatasets
{
    /** Given a Dataset and a query, form a Dataset that 
     * is the dynamic dataset from the query.
     */ 
    public static Dataset dynamicDataset(DatasetDescription description, Dataset ds, boolean defaultUnionGraph)
    {
        DatasetGraph dsg = ds.asDatasetGraph() ;
        DatasetGraph dsg2 = dynamicDataset(description, dsg, defaultUnionGraph) ;
        if ( dsg == dsg2 )
            return ds ;
        return DatasetFactory.wrap(dsg2) ;
    }

    /** Given a DatasetGraph and a query, form a DatasetGraph that 
     * is the dynamic dataset from the query.
     * Returns the original DatasetGraph if the dataset description is null or empty.
     */ 
    public static DatasetGraph dynamicDataset(DatasetDescription description, DatasetGraph dsg, boolean defaultUnionGraph)
    {
        if ( description == null )
            return dsg ;    
    	//An empty description means leave the dataset as-is
    	if (description.isEmpty() )
    		return dsg;
    	
        Collection<Node> defaultGraphs = NodeUtils.convertToNodes(description.getDefaultGraphURIs()) ; 
        Collection<Node> namedGraphs = NodeUtils.convertToNodes(description.getNamedGraphURIs()) ;
        return dynamicDataset(defaultGraphs, namedGraphs, dsg, defaultUnionGraph) ;
    }
    
    /** Given a DatasetGraph and a query, form a DatasetGraph that 
     * is the dynamic dataset from the collection of graphs from the dataset
     * that go to make up the default graph (union) and named graphs.  
     */
    public static DatasetGraph dynamicDataset(Collection<Node> defaultGraphs, Collection<Node> namedGraphs, DatasetGraph dsg, boolean defaultUnionGraph)
    {
        Graph dft = new GraphUnionRead(dsg, defaultGraphs) ;
        DatasetGraph dsg2 = new DatasetGraphMapLink(dft) ;
        
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
        // Record what we've done.
        // c.f. "ARQConstants.sysDatasetDescription" which is used to pass in a  DatasetDescription
        dsg2.getContext().set(ARQConstants.symDatasetDefaultGraphs, defaultGraphs) ;
        dsg2.getContext().set(ARQConstants.symDatasetNamedGraphs,   namedGraphs) ;
        return dsg2 ;
    }
    
    public static class DynamicDatasetGraph extends DatasetGraphReadOnly
    {
        public DynamicDatasetGraph(DatasetGraph dsg) {
            super(dsg) ;
        }

        private Graph unionGraph = null ;

        @Override
        public boolean containsGraph(Node graphNode) {
            if ( Quad.isUnionGraph(graphNode) ) return true ;
            if ( Quad.isDefaultGraphExplicit(graphNode)) return true ;
            return super.containsGraph(graphNode) ;
        }

        // See also the GraphOps
        @Override
        public Graph getGraph(Node graphNode)
        {
            if ( Quad.isUnionGraph(graphNode) ) {
                if ( unionGraph == null )
                    unionGraph = GraphOps.unionGraph(super.getWrapped()) ;
                return unionGraph ;
            }
            if ( Quad.isDefaultGraph(graphNode))
                return getDefaultGraph() ;

            return super.getGraph(graphNode) ;
        }
    }
}
