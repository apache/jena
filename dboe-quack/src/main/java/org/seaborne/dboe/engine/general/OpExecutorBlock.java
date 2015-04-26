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
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.expr.ExprList ;

/** Execution framework for extension by adding matching for
 *  graphNode (inclued variables) basic graph pattern or a particular graph and a basic graph pattern.
 *  This class does not take any care of with placing filters.  
 *  @see OpExecutorBlockFilter  
 *  @see OpExecutorStage
 */
public abstract class OpExecutorBlock extends OpExecutorBlockFilter
{
    public OpExecutorBlock(ExecutionContext execCxt) {
        super(execCxt) ;
    }

    @Override
    final
    protected QueryIterator evaluateBlockFilter(Node graphNode, BasicPattern bgp, ExprList exprs, QueryIterator input)  {
        QueryIterator qIter = evaluateBlock(graphNode, bgp, input) ;
        return OpExecLib.filter(qIter, exprs, execCxt) ;
    }

    /** Evaluate for a graphNode/basicGraphPattern.
     * 
     * @param graphNode
     * @param bgp
     * @param input
     * @return QueryIterator
     */
    protected abstract QueryIterator evaluateBlock(final Node graphNode, final BasicPattern bgp, QueryIterator input) ;

    @Override
    final
    protected QueryIterator evaluateBlockFilter(Graph graph, BasicPattern bgp, ExprList exprs, QueryIterator input)  {
        QueryIterator qIter = evaluateBlock(graph, bgp, input) ;
        return OpExecLib.filter(qIter, exprs, execCxt) ;
    }

    /** Evaluate for a specific graph.
     * @param graph
     * @param bgp
     * @param input
     * @return
     */
    protected abstract QueryIterator evaluateBlock(Graph graph, BasicPattern bgp, QueryIterator input) ;
}
