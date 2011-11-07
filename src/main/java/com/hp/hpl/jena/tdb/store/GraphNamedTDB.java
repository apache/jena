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

import static com.hp.hpl.jena.sparql.core.Quad.isDefaultGraph ;
import static com.hp.hpl.jena.sparql.core.Quad.isUnionGraph ;

import java.util.Iterator ;

import org.openjena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** A graph implementation that projects a graph from a quad table */
public class GraphNamedTDB extends GraphTDBBase
{
    /*
        Quad.unionGraph
        Quad.defaultGraphIRI
        Quad.defaultGraphNodeGenerated
    */
    private static Logger log = LoggerFactory.getLogger(GraphNamedTDB.class) ;
    
    private final QuadTable quadTable ; 
    private NodeId graphNodeId = null ;

    public GraphNamedTDB(DatasetGraphTDB dataset, Node graphName) 
    {
        super(dataset, graphName) ;

        this.quadTable = dataset.getQuadTable() ;
        
        if ( graphName == null )
            throw new TDBException("GraphNamedTDB: Null graph name") ; 
        if ( ! graphName.isURI() )
            throw new TDBException("GraphNamedTDB: Graph name not a URI") ; 
    }

//    @Override
//    public QueryHandler queryHandler()
//    { return queryHandler ; }
//    
//    @Override
//    public TransactionHandler getTransactionHandler()
//    { return transactionHandler ; }
    
    @Override
    protected PrefixMapping createPrefixMapping()
    {
        return dataset.getPrefixes().getPrefixMapping(graphNode.getURI()) ;
    }

    @Override
    protected boolean _performAdd( Triple t ) 
    { 
        if ( isUnionGraph(graphNode) )
            throw new TDBException("Can't add a triple to the RDF merge of all named graphs") ;
        boolean changed ;
        if ( isDefaultGraph(graphNode) )
            changed = dataset.getTripleTable().add(t) ;
        else 
            changed = dataset.getQuadTable().add(graphNode, t) ;
        
        if ( ! changed )
            duplicate(t) ;
        return changed ; 
    }

 
    @Override
    protected boolean _performDelete( Triple t ) 
    { 
        if ( isUnionGraph(graphNode) )
            throw new TDBException("Can't delete triple from the RDF merge of all named graphs") ;
        
        if ( isDefaultGraph(graphNode) )
            return dataset.getTripleTable().delete(t) ;

        return dataset.getQuadTable().delete(graphNode, t) ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        // Explicitly named default graph
        if ( isDefaultGraph(graphNode) )
            // Default graph.
            return graphBaseFindWorker(getDataset().getTripleTable(), m) ;
        // Includes union graph
        return graphBaseFindWorker(getDataset(), graphNode, m) ;
    }
    
    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        NodeId gn = getGraphNodeId() ;
        Iterator<Tuple<NodeId>> iter = dataset.getQuadTable().getNodeTupleTable().find(gn, null, null, null) ;
//        Tuple<NodeId> t = Tuple.create(gn, null, null, null) ;
//        //Iterator<Tuple<NodeId>> iter = dataset.getQuadTable().getNodeTupleTable().getTupleTable().find(t) ;
//        Iterator<Tuple<NodeId>> iter = dataset.getQuadTable().getNodeTupleTable().getTupleTable().getIndex(0).find(t) ;
        return iter ;
    }
    
    /** Graph node as NodeId */
    public final NodeId getGraphNodeId()
    {
//        if ( graphNodeId == null || graphNodeId == NodeId.NodeDoesNotExist )
//            graphNodeId = dataset.getQuadTable().getNodeTupleTable().getNodeTable().getNodeIdForNode(graphNode) ;
        if ( graphNodeId == null )
            graphNodeId = dataset.getQuadTable().getNodeTupleTable().getNodeTable().getAllocateNodeId(graphNode) ;
        return graphNodeId ;
    }

    @Override
    public Tuple<Node> asTuple(Triple triple)
    {
//        if ( getGraphNode() == null )
//            return Tuple.create(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
//        else
            return Tuple.create(getGraphNode(), triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    @Override
    protected final Logger getLog() { return log ; }
    
    @Override
    public NodeTupleTable getNodeTupleTable()
    {
        // Concrete default graph.
        if ( graphNode == null || Quad.isDefaultGraph(graphNode) )
            return dataset.getTripleTable().getNodeTupleTable() ;
        return dataset.getQuadTable().getNodeTupleTable() ;
    }

    @Override
    final public void close()
    { 
        sync() ;
    }
    
    @Override
    public void sync()
    {
        dataset.sync();
    }
    
    @Override
    public String toString() { return Utils.className(this)+":<"+this.graphNode+">" ; }
}
