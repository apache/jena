/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.lib.Lib2.printAbbrev ;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Utilities used within the TDB BGP solver : local TDB store */
public class SolverLib
{
    private static Logger log = LoggerFactory.getLogger(SolverLib.class) ; 
    
    public interface ConvertNodeIDToNode { 
        public Iterator<Binding> convert(NodeTable nodeTable, Iterator<BindingNodeId> iterBindingIds) ;
    }
    
    /** Change this to change the process of NodeId to Node conversion. 
     * Normally it's this code, which puts a delayed iterator mapping
     * around the BindingNodeId stream. 
     */
    public final static ConvertNodeIDToNode converter = new ConvertNodeIDToNode(){
        @Override
        public Iterator<Binding> convert(NodeTable nodeTable, Iterator<BindingNodeId> iterBindingIds)
        {
            return Iter.map(iterBindingIds, convToBinding(nodeTable)) ;
        }} ;

    /** Non-reordering execution of a basic graph pattern, given a iterator of bindings as input */ 
    public static QueryIterator execute(GraphTDB graph, BasicPattern pattern, 
                                        QueryIterator input, Filter<Tuple<NodeId>> filter,
                                        ExecutionContext execCxt)
    {
        return execute(graph.getNodeTupleTable(), null, pattern, input, filter, execCxt) ;
    }
    
    /** Non-reordering execution of a quad pattern, given a iterator of bindings as input.
     *  GraphNode is Node.ANY for execution over the union of named graphs.
     *  GraphNode is null for execution over the real default graph.
     */ 
    public static QueryIterator execute(DatasetGraphTDB ds, Node graphNode, BasicPattern pattern,
                                        QueryIterator input, Filter<Tuple<NodeId>> filter,
                                        ExecutionContext execCxt)
    {
        return execute(ds.getQuadTable().getNodeTupleTable(), graphNode, pattern, input, filter, execCxt) ;
    }
    
    public static Iterator<BindingNodeId> convertToIds(Iterator<Binding> iterBindings, NodeTable nodeTable)
    { return Iter.map(iterBindings, convFromBinding(nodeTable)) ; }
    
    public static Iterator<Binding> convertToNodes(Iterator<BindingNodeId> iterBindingIds, NodeTable nodeTable)
    { return Iter.map(iterBindingIds, convToBinding(nodeTable)) ; }

    
    // The worker.  Callers choose the NodeTupleTable.  
    //     graphNode may be Node.ANY, meaning we should make triples unique.
    //     graphNode may be null, meaning we should make triples unique.

    private static QueryIterator execute(NodeTupleTable nodeTupleTable, Node graphNode, BasicPattern pattern, 
                                         QueryIterator input, Filter<Tuple<NodeId>> filter,
                                         ExecutionContext execCxt)
    {
        List<Triple> triples = pattern.getList() ;
        boolean anyGraph = (graphNode==null ? false : (Node.ANY.equals(graphNode))) ;
        
        int tupleLen = nodeTupleTable.getTupleTable().getTupleLen() ;
        if ( graphNode == null ) {
            if ( 3 != tupleLen )
                throw new TDBException("SolverLib: Null graph node but tuples are of length "+tupleLen) ;
        } else {
            if ( 4 != tupleLen )
                throw new TDBException("SolverLib: Graph node specified but tuples are of length "+tupleLen) ;
        }
        
        // Convert from a QueryIterator (Bindings of Var/Node) to BindingNodeId
        NodeTable nodeTable = nodeTupleTable.getNodeTable() ;
        
        Iterator<BindingNodeId> chain = Iter.map(input, SolverLib.convFromBinding(nodeTable)) ;
        
        for ( Triple triple : triples )
        {
            Tuple<Node> tuple = null ;
            if ( graphNode == null )
                // 3-tuples
                tuple = Tuple.create(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
            else
                // 4-tuples.
                tuple = Tuple.create(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
            chain = solve(nodeTupleTable, tuple, anyGraph, chain, filter, execCxt) ;
        }
        
        // DEBUG POINT
        if ( false )
        {
            if ( chain.hasNext())
                chain = Iter.debug(chain) ;
            else
                System.out.println("No results") ;
        }
        
        // XXX
        
        // Need to make sure the bindings here point to parent.
        Iterator<Binding> iterBinding = converter.convert(nodeTable, chain) ;
        
        // "input" = wil be closed by QueryIterTDB but is otherwise unused.
        return new QueryIterTDB(iterBinding, input, execCxt) ;
    }
    
    private static Iterator<BindingNodeId> solve(NodeTupleTable nodeTupleTable, 
                                                 Tuple<Node> tuple,
                                                 boolean anyGraph,
                                                 Iterator<BindingNodeId> chain, Filter<Tuple<NodeId>> filter,
                                                 ExecutionContext execCxt)
    {
        return new StageMatchTuple(nodeTupleTable, chain, tuple, anyGraph, filter, execCxt) ;
    }
    
    // Transform : BindingNodeId ==> Binding
    private static Transform<BindingNodeId, Binding> convToBinding(final NodeTable nodeTable)
    {
        return new Transform<BindingNodeId, Binding>()
        {
            @Override
            public Binding convert(BindingNodeId bindingNodeIds)
            {
                return convToBinding(nodeTable, bindingNodeIds) ;
            }
        } ;
    }

    public static Binding convToBinding(NodeTable nodeTable, BindingNodeId bindingNodeIds)
    {
        if ( true )
            return new BindingTDB(bindingNodeIds, nodeTable) ;
        else
        {
            // Makes nodes immediately.  Causing unecessary NodeTable accesses (e.g. project) 
            Binding b = new BindingMap() ;
            for ( Var v : bindingNodeIds )
            {
                NodeId id = bindingNodeIds.get(v) ;
                Node n = nodeTable.getNodeForNodeId(id) ;
                b.add(v, n) ;
            }
            return b ;
        }
    }
    
    // Transform : Binding ==> BindingNodeId
    public static Transform<Binding, BindingNodeId> convFromBinding(final NodeTable nodeTable)
    {
        return new Transform<Binding, BindingNodeId>()
        {
            @Override
            public BindingNodeId convert(Binding binding)
            {
                if ( binding instanceof BindingTDB )
                    return ((BindingTDB)binding).getBindingId() ;
                
                BindingNodeId b = new BindingNodeId(binding) ;
                // and copy over, getting NodeIds.
                Iterator<Var> vars = binding.vars() ;
    
                for ( ; vars.hasNext() ; )
                {
                    Var v = vars.next() ;
                    Node n = binding.get(v) ;  
                    if ( n == null )
                        // Variable mentioned in the binding but not actually defined.
                        // Can occur with BindingProject
                        continue ;
                    
                    // Rely on the node table cache for efficency - we will likely be
                    // repeatedly looking up the same node in different bindings.
                    NodeId id = nodeTable.getNodeIdForNode(n) ;
                    // Even put in "does not exist" for a node now known not to be in the DB.
                    // Removed at TDB 0.8.7: if ( ! NodeId.doesNotExist(id) )
                    b.put(v, id) ;
                }
                return b ;
            }
        } ;
    }

    /** Find all the graph names in the quads table. */ 
    public static QueryIterator graphNames(DatasetGraphTDB ds, Node graphNode,
                                           QueryIterator input, Filter<Tuple<NodeId>> filter,
                                           ExecutionContext execCxt)
    {
        Iterator<Tuple<NodeId>> iter1 = ds.getQuadTable().getNodeTupleTable().find(NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
        if ( filter != null )
            iter1 = Iter.filter(iter1, filter) ;
        Iterator<NodeId> iter2 = Tuple.project(0, iter1) ;
        Iterator<NodeId> iter3 = Iter.distinct(iter2) ;
        Iterator<Node> iter4 = NodeLib.nodes(ds.getQuadTable().getNodeTupleTable().getNodeTable(), iter3) ;
        
        final Var var = Var.alloc(graphNode) ;
        Transform<Node, Binding> bindGraphName = new Transform<Node, Binding>(){
            @Override
            public Binding convert(Node node)
            {
                return BindingFactory.binding(var, node) ;
            }
        } ;
        
        Iterator<Binding> iterBinding = Iter.map(iter4, bindGraphName) ;
        
        return new QueryIterTDB(iterBinding, input, execCxt) ;
    }
    
    /** Turn a BasicPattern into an abbreviated string for debugging */  
    public static String strPattern(BasicPattern pattern)
    {
        List<Triple> triples = pattern.getList() ;
        String x = Iter.asString(triples, "\n  ") ;
        return printAbbrev(x) ; 
    }

    public static Set<NodeId> convertToNodeIds(Collection<Node> nodes, DatasetGraphTDB dataset)
    {
        Set<NodeId> graphIds = new HashSet<NodeId>() ;
        NodeTable nt = dataset.getQuadTable().getNodeTupleTable().getNodeTable() ;
        for ( Node n : nodes )
            graphIds.add(nt.getNodeIdForNode(n)) ;
        return graphIds ;
    }
    

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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