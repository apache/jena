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

package com.hp.hpl.jena.tdb.store;

import static com.hp.hpl.jena.sparql.core.Quad.isUnionGraph ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.sys.TDBInternal ;

/** A graph implementation that projects a graph from a quad table */
public class GraphNamedTDB extends GraphTDBBase
{
    // Collapse this into GraphTDBBase and have one class, no interface.
    // This copes with
    //    null for graphName (= default graph).
    //    Quad.unionGraph
    //    Quad.defaultGraphIRI
    //    Quad.defaultGraphNodeGenerated
    private static Logger log = LoggerFactory.getLogger(GraphNamedTDB.class) ;
    
    private NodeId graphNodeId = null ;

    public GraphNamedTDB(DatasetGraphTDB dataset, Node graphName) 
    {
        super(dataset, graphName) ;

        if ( graphName != null && ! graphName.isURI() )
            throw new TDBException("GraphNamedTDB: Graph name not a URI - " + graphName.toString()) ; 
    }

    @Override
    protected PrefixMapping createPrefixMapping()
    {
        if ( isDefaultGraph() )
            return dataset.getPrefixes().getPrefixMapping() ;
        else
            return dataset.getPrefixes().getPrefixMapping(graphNode.getURI()) ;
    }

    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        if ( isDefaultGraph() ) 
            return dataset.getTripleTable().getNodeTupleTable().findAll() ;
        
        NodeId gn = isUnionGraph(graphNode) ? null : getGraphNodeId() ; 
        if ( NodeId.isDoesNotExist(gn) )
            return Iter.nullIterator() ;
        
        // Eliminate this and push all work into find/4. 
        Iterator<Tuple<NodeId>> iter = dataset.getQuadTable().getNodeTupleTable().find(gn, null, null, null) ;
        if ( isUnionGraph(graphNode) )
        {
            iter = Iter.map(iter, project4TupleTo3Tuple) ;
            iter = Iter.distinct(iter) ;
        }
        return iter ;
    }
    
    private static Transform<Tuple<NodeId>, Tuple<NodeId>> project4TupleTo3Tuple = new Transform<Tuple<NodeId>, Tuple<NodeId>>(){
        @Override
        public Tuple<NodeId> convert(Tuple<NodeId> item)
        {
            if ( item.size() != 4 )
                throw new TDBException("Expected a Tuple of 4, got: "+item) ;
            return Tuple.create(item.get(1), item.get(2), item.get(3)) ;
        }} ; 
    
    /** Graph node as NodeId */
    private final NodeId getGraphNodeId()
    {
        // Caution - may not exist.
        if ( graphNodeId == null || graphNodeId == NodeId.NodeDoesNotExist )
        {
            // Don't allocate - we may be in a read transaction.
            NodeId n = TDBInternal.getNodeId(dataset, graphNode) ;
            graphNodeId = n ; 
        }
        
        return graphNodeId ;
    }

    private boolean isDefaultGraph() {
        return isDefaultGraph(graphNode) ;
    }
    
    @Override
    protected final Logger getLog() { return log ; }
    
    @Override
    public NodeTupleTable getNodeTupleTable()
    {
        // Concrete default graph.
        if ( isDefaultGraph() )
            return dataset.getTripleTable().getNodeTupleTable() ;
        return dataset.getQuadTable().getNodeTupleTable() ;
    }

    @Override
    public String toString() { 
        String x = ":defaultGraph" ;
        if ( graphNode != null )
            x = ":<"+this.graphNode+">" ;
        return Utils.className(this)+x ; }
}
