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

package org.seaborne.jena.engine.general;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.NotImplemented ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.* ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterCommonParent ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.graph.GraphUnionRead ;

/** Execution framework for extension by matching a basic graph pattern;
 *  everything else is taken care of by the framework.
 */
public abstract class OpExecutorStage extends OpExecutorBlock
{
    public OpExecutorStage(ExecutionContext execCxt) {
        super(execCxt) ;
    }

    @Override
    protected QueryIterator evaluateBlock(final Node graphNode, final BasicPattern bgp, QueryIterator input) {
        // XXX Deal with variables, union graphs
        // OpGraph probably did variables.
        QueryIterator qIter = new QueryIterRepeatApply(input, execCxt) {
            @Override
            protected QueryIterator nextStage(Binding binding) {
                BasicPattern bgp2 = Substitute.substitute(bgp, binding) ;
                Node graphNode2 = Substitute.substitute(graphNode, binding) ;
                Graph graph = chooseGraph(dataset, graphNode2) ;
                QueryIterator qIter = match(graph, bgp2) ;
                qIter = new QueryIterCommonParent(qIter, binding, execCxt) ;
                return qIter ;
            }
        };
        return qIter ;
    }

    @Override
    protected QueryIterator evaluateBlock(final Graph graph, final BasicPattern bgp, QueryIterator input) {
        QueryIterator qIter = new QueryIterRepeatApply(input, execCxt) {
            @Override
            protected QueryIterator nextStage(Binding binding) {
                BasicPattern bgp2 = Substitute.substitute(bgp, binding) ;
                QueryIterator qIter = match(graph, bgp2) ;
                qIter = new QueryIterCommonParent(qIter, binding, execCxt) ;
                return qIter ;
            }
        };
        return qIter ;
    }

    /** Choose a graph from a DatasetGraph.
     * If it's the union, provide a union graph (not always the best way to deal with union).
     * @param dataset
     * @param graphNode
     * @return Graph
     */
    protected static Graph chooseGraph(DatasetGraph dataset, Node graphNode) {
        if ( graphNode == null ) 
            return dataset.getDefaultGraph() ;
        else if ( Var.isVar(graphNode) )
            throw new NotImplemented("Choosing a graph OpExecutorStage.executeBlockFilter[Variable]") ;
        else if ( graphNode == Node.ANY )
            throw new NotImplemented("OpExecutorMain.executeBlockFilter[Node.ANY]") ;
        else if ( Quad.isUnionGraph(graphNode) ) {
            // TODO Check this!  Work needed here to consolidate union graph handling.
            List<Node> graphs = Iter.toList(dataset.listGraphNodes()) ;
            return new GraphUnionRead(dataset, graphs) ;
        }
        else
            return dataset.getGraph(graphNode) ;
    }

    /** Execute a basic graph pattern, for graph {@code graphNode}.
     * The given basic graph pattern has been substituted for input bindings
     * from earlier in the query execution and so this may be called repeated
     * for one point in the query with different partial groundings
     * for thet point in the query.
     */
    protected abstract QueryIterator match(Graph graph, BasicPattern bgp) ;

    @Override
    protected boolean isForThisExecutor(DatasetGraph dsg, Graph activeGraph, ExecutionContext execCxt) {
        return false ;
    }
}
