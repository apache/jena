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

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.lib.Lib2.printAbbrev ;

import java.util.* ;

import org.apache.jena.atlas.iterator.* ;
import org.apache.jena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.sys.TDBInternal ;

/** Utilities used within the TDB BGP solver : local TDB store */
public class SolverLib
{
    private static Logger log = LoggerFactory.getLogger(SolverLib.class) ; 
    
    /** Non-reordering execution of a basic graph pattern, given a iterator of bindings as input */ 
    public static QueryIterator execute(GraphTDB graph, BasicPattern pattern, 
                                        QueryIterator input, Filter<Tuple<NodeId>> filter,
                                        ExecutionContext execCxt)
    {
        // Maybe default graph or named graph.
        NodeTupleTable ntt = graph.getNodeTupleTable() ;
        return execute(ntt, graph.getGraphName(), pattern, input, filter, execCxt) ;
    }
    
    /** Non-reordering execution of a quad pattern, given a iterator of bindings as input.
     *  GraphNode is Node.ANY for execution over the union of named graphs.
     *  GraphNode is null for execution over the real default graph.
     */ 
    public static QueryIterator execute(DatasetGraphTDB ds, Node graphNode, BasicPattern pattern,
                                        QueryIterator input, Filter<Tuple<NodeId>> filter,
                                        ExecutionContext execCxt)
    {
        NodeTupleTable ntt = ds.chooseNodeTupleTable(graphNode) ;
        return execute(ntt, graphNode, pattern, input, filter, execCxt) ;
    }
    
    public static Iterator<BindingNodeId> convertToIds(Iterator<Binding> iterBindings, NodeTable nodeTable)
    { return Iter.map(iterBindings, convFromBinding(nodeTable)) ; }
    
    /** Convert from Iterator<BindingNodeId> to Iterator<Binding>, conversion "on demand" 
     * (in convToBinding(BindingNodeId, NodeTable)
     */
    public static Iterator<Binding> convertToNodes(Iterator<BindingNodeId> iterBindingIds, NodeTable nodeTable)
    { return Iter.map(iterBindingIds, convToBinding(nodeTable)) ; }
    
    // The worker.  Callers choose the NodeTupleTable.  
    //     graphNode may be Node.ANY, meaning we should make triples unique.
    //     graphNode may be null, meaning default graph

    private static QueryIterator execute(NodeTupleTable nodeTupleTable, Node graphNode, BasicPattern pattern, 
                                         QueryIterator input, Filter<Tuple<NodeId>> filter,
                                         ExecutionContext execCxt)
    {
        if ( Quad.isUnionGraph(graphNode) )
            graphNode = Node.ANY ;
        if ( Quad.isDefaultGraph(graphNode) )
            graphNode = null ;
        
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
        List<Abortable> killList = new ArrayList<>() ;
        
        for ( Triple triple : triples )
        {
            Tuple<Node> tuple = null ;
            if ( graphNode == null )
                // 3-tuples
                tuple = Tuple.createTuple(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
            else
                // 4-tuples.
                tuple = Tuple.createTuple(graphNode, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
            chain = solve(nodeTupleTable, tuple, anyGraph, chain, filter, execCxt) ;
            chain = makeAbortable(chain, killList) ; 
        }
        
        // DEBUG POINT
        if ( false )
        {
            if ( chain.hasNext())
                chain = Iter.debug(chain) ;
            else
                System.out.println("No results") ;
        }
        
        // Timeout wrapper ****
        // QueryIterTDB gets called async.
        // Iter.abortable?
        // Or each iterator has a place to test.
        // or pass in a thing to test?
        
        
        // Need to make sure the bindings here point to parent.
        Iterator<Binding> iterBinding = convertToNodes(chain, nodeTable) ;
        
        // "input" will be closed by QueryIterTDB but is otherwise unused.
        // "killList" will be aborted on timeout.
        return new QueryIterTDB(iterBinding, killList, input, execCxt) ;
    }
    
    /** Create an abortable iterator, storing it in the killList.
     *  Just return the input iterator if kilList is null. 
     */
    static <T> Iterator<T> makeAbortable(Iterator<T> iter, List<Abortable> killList)
    {
        if ( killList == null )
            return iter ;
        IterAbortable<T> k = new IterAbortable<>(iter) ;
        killList.add(k) ;
        return k ;
    }
    
    /** Iterator that adds an abort operation which can be called
     *  at any time, including from another thread, and causes the
     *  iterator to throw an exception when next touched (hasNext, next).  
     */
    static class IterAbortable<T> extends IteratorWrapper<T> implements Abortable
    {
        volatile boolean abortFlag = false ;
        
        public IterAbortable(Iterator<T> iterator)
        {
            super(iterator) ;
        }
        
        /** Can call asynchronously at anytime */
        @Override
        public void abort() { 
            abortFlag = true ;
        }
        
        @Override
        public boolean hasNext()
        {
            if ( abortFlag )
                throw new QueryCancelledException() ;
            return iterator.hasNext() ; 
        }
        
        @Override
        public T next()
        {
            if ( abortFlag )
                throw new QueryCancelledException() ;
            return iterator.next() ; 
        }
    }
    
    public static Iterator<BindingNodeId> solve(NodeTupleTable nodeTupleTable, 
                                                Tuple<Node> tuple,
                                                boolean anyGraph,
                                                Iterator<BindingNodeId> chain, Filter<Tuple<NodeId>> filter,
                                                ExecutionContext execCxt)
    {
        return new StageMatchTuple(nodeTupleTable, chain, tuple, anyGraph, filter, execCxt) ;
    }
    
    // Transform : BindingNodeId ==> Binding
    private static Transform<BindingNodeId, Binding> convToBinding(final NodeTable nodeTable) {
        return new Transform<BindingNodeId, Binding>() {
            @Override
            public Binding convert(BindingNodeId bindingNodeIds) {
                return convToBinding(bindingNodeIds, nodeTable) ;
            }
        } ;
    }

    public static Binding convToBinding(BindingNodeId bindingNodeIds, NodeTable nodeTable) {
        if ( true )
            return new BindingTDB(bindingNodeIds, nodeTable) ;
        else {
            // Makes nodes immediately. Causing unnecessary NodeTable accesses
            // (e.g. project)
            BindingMap b = BindingFactory.create() ;
            for (Var v : bindingNodeIds) {
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
            public BindingNodeId convert(Binding binding) {
                return SolverLib.convert(binding, nodeTable) ;
            }
        } ;
    }
    
    /** Binding ==> BindingNodeId, given a NodeTable */
    public static BindingNodeId convert(Binding binding, NodeTable nodeTable) 
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
            b.put(v, id) ;
        }
        return b ;
    }
    
    /** Find whether a specific graph name is in the quads table. */
    public static QueryIterator testForGraphName(DatasetGraphTDB ds, Node graphNode, QueryIterator input,
                                                 Filter<Tuple<NodeId>> filter, ExecutionContext execCxt) {
        NodeId nid = TDBInternal.getNodeId(ds, graphNode) ;
        boolean exists = !NodeId.isDoesNotExist(nid) ;
        if ( exists ) {
            // Node exists but is it used in the quad position?
            NodeTupleTable ntt = ds.getQuadTable().getNodeTupleTable() ;
            // Don't worry about abortable - this iterator should be fast
            // (with normal indexing - at least one G???).
            // Either it finds a starting point, or it doesn't.  We are only 
            // interested in the first .hasNext.
            Iterator<Tuple<NodeId>> iter1 = ntt.find(nid, NodeId.NodeIdAny, NodeId.NodeIdAny, NodeId.NodeIdAny) ;
            if ( filter != null )
                iter1 = Iter.filter(iter1, filter) ;
            exists = iter1.hasNext() ;
        }

        if ( exists )
            return input ;
        else {
            input.close() ;
            return QueryIterNullIterator.create(execCxt) ;
        }
    }

    /** Find all the graph names in the quads table. */
    public static QueryIterator graphNames(DatasetGraphTDB ds, Node graphNode, QueryIterator input,
                                           Filter<Tuple<NodeId>> filter, ExecutionContext execCxt) {
        List<Abortable> killList = new ArrayList<>() ;
        Iterator<Tuple<NodeId>> iter1 = ds.getQuadTable().getNodeTupleTable().find(NodeId.NodeIdAny, NodeId.NodeIdAny,
                                                                                   NodeId.NodeIdAny, NodeId.NodeIdAny) ;
        if ( filter != null )
            iter1 = Iter.filter(iter1, filter) ;

        Iterator<NodeId> iter2 = Tuple.project(0, iter1) ;
        // Project is cheap - don't brother wrapping iter1
        iter2 = makeAbortable(iter2, killList) ;

        Iterator<NodeId> iter3 = Iter.distinct(iter2) ;
        iter3 = makeAbortable(iter3, killList) ;

        Iterator<Node> iter4 = NodeLib.nodes(ds.getQuadTable().getNodeTupleTable().getNodeTable(), iter3) ;

        final Var var = Var.alloc(graphNode) ;
        Transform<Node, Binding> bindGraphName = new Transform<Node, Binding>() {
            @Override
            public Binding convert(Node node) {
                return BindingFactory.binding(var, node) ;
            }
        } ;

        Iterator<Binding> iterBinding = Iter.map(iter4, bindGraphName) ;
        // Not abortable.
        return new QueryIterTDB(iterBinding, killList, input, execCxt) ;
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
        Set<NodeId> graphIds = new HashSet<>() ;
        NodeTable nt = dataset.getQuadTable().getNodeTupleTable().getNodeTable() ;
        for ( Node n : nodes )
            graphIds.add(nt.getNodeIdForNode(n)) ;
        return graphIds ;
    }

    public static Iterator<Tuple<NodeId>> unionGraph(NodeTupleTable ntt)
    {
        Iterator<Tuple<NodeId>> iter = ntt.find((NodeId)null, null, null, null) ;
        iter = Iter.operate(iter, quadsToAnyTriples) ;
        //iterMatches = Iter.distinct(iterMatches) ;
        
        // This depends on the way indexes are choose and
        // the indexing pattern. It assumes that the index 
        // chosen ends in G so same triples are adjacent 
        // in a union query.
        /// See TupleTable.scanAllIndex that ensures this.
        iter = Iter.distinctAdjacent(iter) ;
        return iter ;
    }
    
    // -- Mutating "transform in place"
    private static Action<Tuple<NodeId>> quadsToAnyTriples = new Action<Tuple<NodeId>>(){
        @Override
        public void apply(Tuple<NodeId> item)
        { item.tuple()[0] = NodeId.NodeIdAny ; }
    } ;

}
