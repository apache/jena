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

package org.apache.jena.sparql.engine.main.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.SingletonIterator ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpGraph ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterAssignVarValue ;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.iterator.QueryIterSub ;
import org.apache.jena.sparql.engine.main.QC ;

public class QueryIterGraph extends QueryIterRepeatApply
{
    /*
     * A note on the strange case of GRAPH ?g { ... ?g ... }
     * 
     * The inner pattern is solved, then the outer ?g=... is added. This happens
     * because the outer ?g is added by QueryIterAssignVarValue which tests a
     * variable is the same as the binding and drops the binding if not. (which
     * is a specialised form of join.
     */
    protected OpGraph opGraph ;
    
    public QueryIterGraph(QueryIterator input, OpGraph opGraph, ExecutionContext context)
    {
        super(input, context) ;
        this.opGraph = opGraph ;
    }
    
    @Override
    protected QueryIterator nextStage(Binding outerBinding) {
        DatasetGraph ds = getExecContext().getDataset() ;
        // Is this closed?
        Iterator<Node> graphNameNodes = makeSources(ds, outerBinding, opGraph.getNode());
        
//        List<Node> x = Iter.toList(graphNameNodes) ;
//        graphNameNodes = x.iterator() ;
//        System.out.println(x) ;
        
        QueryIterator current = new QueryIterGraphInner(outerBinding, graphNameNodes, opGraph, getExecContext()) ;
        return current ;
    }

    /** Bound value or null */
    private static Node resolve(Binding b, Node n) {
        if ( ! n.isVariable() ) return n ;
        return b.get(Var.alloc(n)) ;
    }

    protected static Iterator<Node> makeSources(DatasetGraph data, Binding b, Node graphVar) {
        Node n2 = resolve(b, graphVar) ;
        if ( n2 != null && n2.isLiteral() ) 
            // Literal possible after resolving
            return Iter.nullIterator() ;
        
        // n2 is a URI or null.
        if ( n2 == null )
            // Do all submodels.
            return data.listGraphNodes() ;
        return new SingletonIterator<>(n2) ;
    }
    

    protected static class QueryIterGraphInner extends QueryIterSub
    {
        protected final Binding parentBinding ;
        protected final Iterator<Node> graphNames ;
        protected final OpGraph opGraph ;
        protected final Op opSubstituted ;

        protected QueryIterGraphInner(Binding parent, Iterator<Node> graphNames, OpGraph opGraph, ExecutionContext execCxt) {
            super(null, execCxt) ;
            this.parentBinding = parent ;
            this.graphNames = graphNames ;
            this.opGraph = opGraph ;
            this.opSubstituted = Substitute.substitute(opGraph.getSubOp(), parent) ;
        }

        @Override
        protected boolean hasNextBinding() {
            for ( ;; ) {
                if ( iter == null )
                    iter = nextIterator();

                if ( iter == null )
                    return false;

                if ( iter.hasNext() )
                    return true;

                iter.close();
                iter = nextIterator();
                if ( iter == null )
                    return false;
            }
        }

        @Override
        protected Binding moveToNextBinding() {
            if ( iter == null )
                throw new NoSuchElementException(Lib.className(this) + ".moveToNextBinding");

            return iter.nextBinding();
        }

        // Proceed to the next iterator.
        protected QueryIterator nextIterator() {
            while( graphNames.hasNext() ) {
                Node gn = graphNames.next() ;
                QueryIterator iter = buildIterator(gn);
                if ( iter != null )
                    return iter;
            }
            return null;
        }
        
        // Build iterator or return null for if there can't be any results.
        private QueryIterator buildIterator(Node gn) {
            QueryIterator qIter = buildIterator(parentBinding, gn, opSubstituted, getExecContext()) ;
            if ( qIter == null )
                // Known to be nothing (e.g. graph does not exist). 
                // try again.
                return null ;
            
            if ( Var.isVar(opGraph.getNode()) ) {
                // This is the join of the graph node variable to the sub-pattern solution.
                // Do after the subpattern so that the variable is not visible to the
                // subpattern.
                Var v = Var.alloc(opGraph.getNode()) ;
                qIter = new QueryIterAssignVarValue(qIter, v, gn, getExecContext()) ;
            }
            return qIter ;
        }
        
        // Create the iterator for a cycle of one node - or return null if there can't be any results.
        protected static QueryIterator buildIterator(Binding binding, Node graphNode, Op opExec, ExecutionContext outerCxt) {
            if ( !graphNode.isURI() && !graphNode.isBlank() )
                // e.g. variable bound to a literal or blank node.
                throw new ARQInternalErrorException("QueryIterGraphInner.buildIterator: Not a URI or blank node: "+graphNode) ;
            
            // We can't just use DatasetGraph.getGraph because it may 
            // "auto-create" graphs. Use the containsGraph function.
            boolean syntheticGraph = ( Quad.isDefaultGraph(graphNode) || Quad.isUnionGraph(graphNode) ) ;
            if ( ! syntheticGraph && ! outerCxt.getDataset().containsGraph(graphNode) )
                return null ;

            Graph g = outerCxt.getDataset().getGraph(graphNode) ;
            // And the contains was true??!!!!!!
            if ( g == null )
                return null ;
                //throw new ARQInternalErrorException(".containsGraph was true but .getGraph is null") ;
            
            ExecutionContext cxt2 = new ExecutionContext(outerCxt, g) ;
            QueryIterator subInput = QueryIterSingleton.create(binding, cxt2) ;
            return QC.execute(opExec, subInput, cxt2) ;
        }

        @Override
        protected void requestSubCancel()
        {}

        @Override
        protected void closeSubIterator()
        {}
    }
}
