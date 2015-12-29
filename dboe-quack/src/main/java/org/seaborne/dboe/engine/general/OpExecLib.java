/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.general;

import java.util.Iterator ;
import java.util.List ;
import java.util.function.Function ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.* ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowList ;
import org.seaborne.dboe.engine.Slot ;

/** Helper functions related to OpExecutor implementation */ 
public class OpExecLib {
    /**
     * Is this a node referring to the default graph?
     * @param graphNode
     */
    public static boolean isDefaultGraph(Node graphNode) {
        return graphNode == null || Quad.isDefaultGraph(graphNode) ; 
    }

    /**
     * Is this a node referring to the union of named graphs?
     * @param graphNode
     */
    public static boolean isUnionGraph(Node graphNode) {
        return Quad.isUnionGraph(graphNode) ; 
    }

    /** Handle special graph node names and return the Node needed for a match:<br/>
     *  Returns null for default graph in storage.<br/>
     *  Returns Node.ANY for the unionGraph (needs duplicate supression).
     */
    
    public static Node decideGraphNode(Node gn, ExecutionContext execCxt) {
        // ---- Graph names with special meaning.
        // Graph names with special meaning:
        //  Quad.defaultGraphIRI
        //    -- the IRI used in GRAPH <> to mean the default graph.
        //  Quad.defaultGraphNodeGenerated
        //    -- the internal marker node used for the quad form of queries.
        //  Quad.unionGraph
        ///   -- the IRI used in GRAPH <> to mean the union of named graphs
    
        if ( isDefaultGraphStorage(gn) )
            return null ;
        if ( isUnionGraph(gn) )
            return Node.ANY ;
        return gn ;
    }

    /** Is this a node referring to the real default graph in the storage (as might be in a 3-tuple table).*/
    public static boolean isDefaultGraphStorage(Node gn) {
        if ( gn == null )
            return true ;
        // Is it the implicit name for default graph.
        if ( Quad.isDefaultGraph(gn) )
            return true ;
        return false ;
    }

    /** Is this the root QueryIterator? It is known to be one row, no variables. */
    public static boolean isRootInput(QueryIterator input) {
        return (input instanceof QueryIterRoot) ;
    }
    
    
    /** Wrap a QueryIterator in some filter expressions
     * @param qIter 
     *  QueryIterator
     * @param exprs
     *  Expression list, or null.
     * @param execCxt
     *  Execution context, fortrackign iterators.
     * @return QueryIterator
     */
    public static QueryIterator filter(QueryIterator qIter, ExprList exprs, ExecutionContext execCxt) {
        if ( exprs != null && ! exprs.isEmpty() ) {
            for (Expr expr : exprs)
                qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        }
        return qIter ;
    }
    


    /** Solve a pattern on the given graph; no reordering of the basic graph pattern is done. */
    /*public*/private static QueryIterator solvePattern$(final Graph graph, final BasicPattern bgp, QueryIterator input, final ExecutionContext execCxt) {
        //  See solvePattern - which calls QueryIterBlockTriples.create
        // Presumed less efficient than QueryIterBlockTriples
        QueryIterator qIter = new QueryIterRepeatApply(input, execCxt) {
            @Override
            protected QueryIterator nextStage(Binding binding) {
                BasicPattern bgp2 = Substitute.substitute(bgp, binding) ;
                QueryIterator qIter = solvePattern(graph, bgp2, execCxt) ;
                qIter = new QueryIterCommonParent(qIter, binding, execCxt) ;
                return qIter ;
            }
        };
        return qIter ;
    }
    
    /** Solve a pattern on the given graph; no reordering of the basic graph pattern is done.
     *  Better to use method that includes input.
     */
    public static QueryIterator solvePattern(Graph graph, BasicPattern bgp) {
        return solvePattern(graph, bgp, null, null) ;
    }

    /** Solve a pattern on the given graph; no reordering of the basic graph pattern is done.
     *  Not necessarily the most efficient way but will work. */
    public static QueryIterator solvePattern(Graph graph, BasicPattern bgp, ExecutionContext execCxt) {
        return solvePattern(graph, bgp, null, execCxt) ;
    }

    public static QueryIterator solvePattern(Graph graph, BasicPattern bgp, QueryIterator input) {
        ExecutionContext execCxt = null ;
        if ( input instanceof QueryIter )
            execCxt = ((QueryIter)input).getExecContext() ;
        return QueryIterBlockTriples.create(input, bgp, execCxt) ;
    }

    public static QueryIterator solvePattern(Graph graph, BasicPattern bgp, QueryIterator input, ExecutionContext execCxt) {
        if ( execCxt == null )
            execCxt = new ExecutionContext(ARQ.getContext(), graph, null, null) ;
        if ( input == null )
            input = QueryIterRoot.create(execCxt) ;
        return QueryIterBlockTriples.create(input, bgp, execCxt) ;
    }
    
    private static Function<Row<Node>, Binding> rowToBinding = new Function<Row<Node>, Binding>(){
        @Override
        public Binding apply(Row<Node> row) {
            return new BindingRowNode(row) ;
//            BindingMap b = BindingFactory.create() ;
//            for ( Var v : row.vars() ) {
//                Node n = row.get(v) ;
//                b.add(v, n); 
//            }
//            return b ;
        }
    } ;
    
    public static QueryIterator apply(RowList<Node> results, ExecutionContext execCxt) {
        Iterator<Binding> bIter = Iter.map(results.iterator(), rowToBinding) ;
        return new QueryIterPlainWrapper(bIter, execCxt) ;
    }
    
    /** Triples to tuples */ 
    public static List<Tuple<Node>> convertTriplesToTuples(List<Triple> iter)  {
        return iter.stream().map(tripleToTuple).collect(Collectors.toList()) ;
    }
    
    public static Iterator<Tuple<Node>> convertTriplesToTuples(Iterator<Triple> iter)  {
        return Iter.map(iter, tripleToTuple) ;
    }
    
    /** Quads to tuples */ 
    public static List<Tuple<Node>> convertQuadsToTuples(List<Quad> iter)  {
        return iter.stream().map(quadToTuple).collect(Collectors.toList()) ;
    }
    
    /** Quads to tuples */
    public static Iterator<Tuple<Node>> convertQuadsToTuples(Iterator<Quad> iter)  {
        return Iter.map(iter, quadToTuple) ;
    }

    /** Triples to tuples of slots */
    public static List<Tuple<Slot<Node>>> convertTriplesToSlots(List<Triple> iter)  {
        return iter.stream().map(tripleToTupleSlot).collect(Collectors.toList()) ;
    }

    /** Triples to tuples of slots */
    public static Iterator<Tuple<Slot<Node>>> convertTriplesToSlots(Iterator<Triple> iter)  {
        return Iter.map(iter, tripleToTupleSlot) ;
    }
    
    /** Quads to tuples of slots */
    public static List<Tuple<Slot<Node>>> convertQuadsToSlots(List<Quad> iter)  {
        return iter.stream().map(quadToTupleSlot).collect(Collectors.toList()) ;
    }

    /** Quads to tuples of slots */
    public static Iterator<Tuple<Slot<Node>>> convertQuadsToSlots(Iterator<Quad> iter)  {
        return Iter.map(iter, quadToTupleSlot) ;
    }
    
    private static Slot<Node> nodeToSlot(Node n) {
        if ( Var.isVar(n)) 
            return Slot.createVarSlot(Var.alloc(n)) ;
        else
            return Slot.createTermSlot(n) ;
    }

    private static Function<Quad, Tuple<Node>> quadToTuple = (quad) -> TupleFactory.tuple(quad.getGraph(),
                                                                                          quad.getSubject() ,
                                                                                          quad.getPredicate(),
                                                                                          quad.getObject()) ;


    private static Function<Triple, Tuple<Node>> tripleToTuple = (triple) -> TupleFactory.tuple(triple.getSubject(),
                                                                                                triple.getPredicate(),
                                                                                                triple.getObject());

    private static Function<Triple, Tuple<Slot<Node>>> tripleToTupleSlot = (triple) -> TupleFactory.tuple(nodeToSlot(triple.getSubject()),
                                                                                                          nodeToSlot(triple.getPredicate()),
                                                                                                          nodeToSlot(triple.getObject()));

    private static Function<Quad, Tuple<Slot<Node>>> quadToTupleSlot = (quad) -> TupleFactory.tuple(nodeToSlot(quad.getGraph()),
                                                                                                    nodeToSlot(quad.getSubject()),
                                                                                                    nodeToSlot(quad.getPredicate()),
                                                                                                    nodeToSlot(quad.getObject()));

    private static Function<Tuple<Node>, Triple> tupleToTriple = new Function<Tuple<Node>, Triple>(){
        @Override
        public Triple apply(Tuple<Node> tuple) {
            if ( tuple.len() != 3 )
                throw new ARQInternalErrorException("Attmpt to convert a tuple of length "+tuple.len()+" to a triple") ;
            return Triple.create(tuple.get(0), tuple.get(1), tuple.get(2)) ; 
        }
    } ;
    
    private static Function<Tuple<Node>, Quad> tupleToQuad = new Function<Tuple<Node>, Quad>(){
        @Override
        public Quad apply(Tuple<Node> tuple) {
            if ( tuple.len() != 4 )
                throw new ARQInternalErrorException("Attmpt to convert a tuple of length "+tuple.len()+" to a quad") ;
            return Quad.create(tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3)) ; 
        }
    } ;


}
