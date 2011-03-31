/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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