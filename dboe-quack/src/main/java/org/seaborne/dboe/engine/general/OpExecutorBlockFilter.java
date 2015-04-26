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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.algebra.op.OpQuadPattern ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.expr.ExprList ;

/** Framework for writing an OpExecutor.  
 *  This class provides all execution and makes calls to the abstract methods
 *  to evaluate the triple patterns, quad patterns and the same with filtering.
 *  Filters on OpBGPs and OpQuadPattern are handled by passing to the base implementation.
 *  Filters on other expressions are handled by this framework.
 *  Filtered triple patterns and quad patterns provide a good balance
 *  between simpicity of implementation and reasonable performance.   
 *
 *  {@link OpExecutorStage} extends this class to provide execution with a single abstract
 *  operation of "execute this BGP on this graph".
 *  
 *  @see OpExecutorStage
 */
public abstract class OpExecutorBlockFilter extends OpExecutor
{
    protected final boolean isForThisExecutor ;
    protected final DatasetGraph dataset ;
    
    public OpExecutorBlockFilter(ExecutionContext execCxt) {
        super(execCxt) ;
        DatasetGraph dsg = execCxt.getDataset() ;
        Graph graph = execCxt.getActiveGraph() ;
        isForThisExecutor = isForThisExecutor(dsg, graph, execCxt) ;
        if ( isForThisExecutor ) {
            dataset = initExecutor(dsg, graph, execCxt) ;
        } else
            dataset = null ;
    }
    
    /** Test whether this executor is going to process the request.
     *  The answer is usually "yes" if we got here are all but it give flexibility
     *  for an executor in a stack.
     *  If this returns 'false', then general OpExecutor is used. 
     * 
     * @param dsg          DatasetGraph
     * @param activeGraph  Active graph (starting point for query).
     * @param execCxt      ExecutionContext
     * @return
     */
    protected abstract boolean isForThisExecutor(DatasetGraph dsg, Graph activeGraph, ExecutionContext execCxt) ;

    /**
     * A chance to initialize; called after deciding isForThisExecutor.
     * This is not normally needed so it is not abstract. 
     * @param dsg
     * @param graph
     * @param execCxt   ExecutionContext
     */
    protected DatasetGraph initExecutor(DatasetGraph dsg, Graph graph, ExecutionContext execCxt) { return dsg ; }

    @Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForThisExecutor )
            return super.execute(opFilter, input) ;
        
        Op sub = opFilter.getSubOp() ;
        
        // (filter (bgp ...))
        if ( OpBGP.isBGP(sub) )
        {
            OpBGP opBGP = (OpBGP)sub ;
            return evaluateBlockFilter(execCxt.getActiveGraph(), opBGP.getPattern(), opFilter.getExprs(), input) ;
        }
        
        // (filter (quadpattern ...))
        if ( sub instanceof OpQuadPattern )
        {
            OpQuadPattern quadPattern = (OpQuadPattern)sub ;
            return evaluateBlockFilter(quadPattern.getGraphNode(), quadPattern.getBasicPattern(), opFilter.getExprs(), input) ;
        }
        
        // TODO OpTable.
        return super.execute(opFilter, input) ;
    }

    @Override
    protected QueryIterator execute(OpQuadPattern opQuad, QueryIterator input) {
        if ( ! isForThisExecutor )
            return super.execute(opQuad, input) ;
        BasicPattern bgp = opQuad.getBasicPattern() ;
        Node gn = opQuad.getGraphNode() ;
        return evaluateBlockFilter(gn, bgp, null, input) ;
    }

    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
        if ( ! isForThisExecutor )
            return super.execute(opBGP, input) ;
        return evaluateBlockFilter(execCxt.getActiveGraph(), opBGP.getPattern(), null, input) ;
    }

    // May be catchOpGraph+filter/BGP. 
//    @Override
//    protected QueryIterator execute(OpGraph opGraph, QueryIterator input) {
//        if ( ! isForThisExecutor )
//            return super.execute(opGraph, input) ;
//        return super.execute(opGraph, input) ;
//    }

    // -- Core extensions
    // 
    
//    /**
//     * Execute a quad pattern 
//     * 
//     * @param quadPattern
//     *   Quad Pattern
//     * @param exprs
//     *   Any filters to apply to the overall results (where they get applied is an optimization step that the sublcass may perform).  
//     * @param input
//     *   The results so far.  Logically, this executeBlockFilter is joined with that stream
//     *   but the optimizer guarantees that substitution semantics are correct.
//     * @return QueryIterator
//     */
//    protected abstract QueryIterator evalauteBlockFilter(QuadPattern quadPattern, ExprList exprs, QueryIterator input) ;
    
    /**
     * Execute a basic graph pattern on the graph named by graphNode 
     * 
     * @param graphNode 
     *   The node from {@link OpExecLib#decideGraphNode}. null for real default (storage) graph, Node.ANY for union graph.
     * @param bgp
     *   Basic Graph Pattern
     * @param exprs
     *   Any filters to apply to the overall results (where they get applied is an optimization step that the sublcass may perform).  
     * @param input
     *   The results so far.  Logically, this executeBlockFilter is joined with that stream
     *   but the optimizer guarantees that substitution semantics are correct.
     * @return QueryIterator
     */
    protected abstract QueryIterator evaluateBlockFilter(Node graphNode, BasicPattern bgp, ExprList exprs, QueryIterator input) ;
    
    /**
     * Execute a basic graph pattern on the graph named by graphNode. 
     * 
     * @param graphNode 
     *   The node from {@link OpExecLib#decideGraphNode}. null for real default (storage) graph, Node.ANY for union graph.
     * @param bgp
     *   Basic Graph Pattern
     * @param exprs
     *   Any filters to apply to the overall results (where they get applied is an optimization step that the sublcass may perform).  
     * @param input
     *   The results so far.  Logically, this executeBlockFilter is joined with that stream
     *   but the optimizer guarantees that substitution semantics are correct.
     * @return QueryIterator
     */
    protected abstract QueryIterator evaluateBlockFilter(Graph graph, BasicPattern bgp, ExprList exprs, QueryIterator input) ;
}
