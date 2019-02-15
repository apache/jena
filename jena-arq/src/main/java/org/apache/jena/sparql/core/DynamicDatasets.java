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

import static org.apache.jena.sparql.util.NodeUtils.convertToSetNodes;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.graph.GraphOps ;
import org.apache.jena.sparql.graph.GraphUnionRead ;

public class DynamicDatasets
{
    /**
     * Given a DatasetDescription, form a Dataset that is the dynamic dataset over the
     * base dataset. Returns the original Dataset if the dataset description is null or
     * empty.
     */ 
    public static Dataset dynamicDataset(DatasetDescription description, Dataset ds, boolean defaultUnionGraph)
    {
        DatasetGraph dsg = ds.asDatasetGraph() ;
        DatasetGraph dsg2 = dynamicDataset(description, dsg, defaultUnionGraph) ;
        if ( dsg == dsg2 )
            return ds ;
        return DatasetFactory.wrap(dsg2) ;
    }

    /**
     * Given a DatasetDescription, form a Dataset that is the dynamic dataset over the
     * base dataset. Returns the original DatasetGraph if the dataset description is null
     * or empty.
     */
    public static DatasetGraph dynamicDataset(DatasetDescription description, DatasetGraph dsg, boolean defaultUnionGraph)
    {
        if ( description == null )
            return dsg ;    
    	//An empty description means leave the dataset as-is
    	if (description.isEmpty() )
    		return dsg;
    	
        Set<Node> defaultGraphs = convertToSetNodes(description.getDefaultGraphURIs()) ; 
        Set<Node> namedGraphs = convertToSetNodes(description.getNamedGraphURIs()) ;
        return dynamicDataset(defaultGraphs, namedGraphs, dsg, defaultUnionGraph) ;
    }
    
    /**
     * Form a {@link DatasetGraph} that is the dynamic dataset from the collections of
     * graphs from the dataset that go to make up the default graph and named graphs.
     */
    public static DatasetGraph dynamicDataset(Collection<Node> defaultGraphs, Collection<Node> namedGraphs, DatasetGraph dsg, boolean defaultUnionGraph)
   {
        Graph dft;
        if ( defaultUnionGraph || defaultGraphs.contains(Quad.unionGraph) ) {
            if ( defaultGraphs.contains(Quad.defaultGraphIRI) )
                dft = new Union(dsg.getUnionGraph(), dsg.getDefaultGraph());
            else
                // Any other FROM graphs don't matter - they are in the union graph.
                dft = dsg.getUnionGraph();
        } else
            dft = new GraphUnionRead(dsg, defaultGraphs);
        
        DatasetGraph dsg2 = new DatasetGraphMapLink(dft);
        
        // The named graphs.
        for ( Node gn : namedGraphs )
        {
            if ( Quad.isUnionGraph(gn) ) {
                // Special case - don't put an explicitly named union graph into the name
                // graphs because union is an operation over all named graphs ... which
                // includes itself.
                // In practical terms, it can lead to stackoveflow in execution.  
                continue;
            }
            Graph g = GraphOps.getGraph(dsg, gn) ;
            if ( g != null )
                dsg2.addGraph(gn, g) ;
        }
        
        if ( dsg.getContext() != null )
            dsg2.getContext().putAll(dsg.getContext()) ;

        dsg2 = new DynamicDatasetGraph(dsg2, dsg);
        
        // Record what we've done.
        // c.f. "ARQConstants.sysDatasetDescription" which is used to pass in a DatasetDescription
        dsg2.getContext().set(ARQConstants.symDatasetDefaultGraphs, defaultGraphs) ;
        dsg2.getContext().set(ARQConstants.symDatasetNamedGraphs,   namedGraphs) ;
        return dsg2 ;
    }

    public static class DynamicDatasetGraph extends DatasetGraphReadOnly implements DatasetGraphWrapperView {
        private final DatasetGraph projected;

        public DynamicDatasetGraph(DatasetGraph viewDSG, DatasetGraph baseDSG) {
            super(viewDSG, baseDSG.getContext().copy());
            this.projected = baseDSG;
        }
        
        private DatasetGraph getProjected() {
            return projected;
        }
    }
}
