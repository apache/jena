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
import java.util.Set ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent ;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import org.apache.jena.sparql.expr.ExprList ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.AccessRows ;
import org.seaborne.dboe.engine.access.AccessorGraph ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** OpExecutor in Node space using Quack and Rows.*/
public class OpExecutorRowsMain extends OpExecutorBlockFilter {
    
    /*
     * 1 - Library code for statics and commented code.
     * 2 - Extract the stage case => OpExecutorMain3
     * 3 - this can do better - all in Row space?
     */
    
    public final static Logger log = LoggerFactory.getLogger(OpExecutorRowsMain.class) ;

    public static final OpExecutorFactory factoryRowsMain = 
        new OpExecutorFactory() {
            @Override
            public OpExecutor create(ExecutionContext execCxt) {
                return new OpExecutorRowsMain(execCxt) ;
            }
        } ;
        
    public OpExecutorRowsMain(ExecutionContext execCxt) {
        super(execCxt) ;
    }
    
    @Override
    protected boolean isForThisExecutor(DatasetGraph dsg, Graph activeGraph, ExecutionContext execCxt) {
        return true ;
    }

//    @Override
//    protected QueryIterator exec(Op op, QueryIterator input) {
//        System.out.println(op) ;
//        return super.exec(op, input) ;
//    }
    
    @Override
    protected QueryIterator evaluateBlockFilter(Node graphNode, BasicPattern bgp, ExprList exprs, QueryIterator input) {
        // XXX Do as quads.
        Graph graph ;
        // chooseGraph
        if ( graphNode == null ) 
            graph = dataset.getDefaultGraph() ;
        else if ( Var.isVar(graphNode) )
            throw new NotImplemented("OpExecutorMain.executeBlockFilter[Variable]") ;
        else if ( graphNode == Node.ANY )
            throw new NotImplemented("OpExecutorMain.executeBlockFilter[Node.ANY]") ;
        else
            graph = dataset.getGraph(graphNode) ;
        
        return evaluateBlockFilter(graph, bgp, exprs, input);
    }
    
    @Override
    protected QueryIterator evaluateBlockFilter(final Graph graph, BasicPattern bgp, ExprList exprs, QueryIterator input) {
        // passed a graph specifically (e.g. part of a mixed dataset) or doing non-quad execution. 
        QueryIterator qIter = executeBlockTriples(graph, bgp, input) ;
        return OpExecLib.filter(qIter, exprs, execCxt) ;
    }

    private QueryIterator executeBlockTriples(final Graph graph, final BasicPattern bgp, QueryIterator input) {
        // XXX Switch from RepeatApply to translating the whole thing to Row-space
        // including QueryIterator input.
        // Two possibilities here.
        final AccessRows<Node> accessor = new AccessorGraph(graph) ;
        QueryIterator qIter = new QueryIterRepeatApply(input, execCxt) {
            @Override
            protected QueryIterator nextStage(Binding binding) {
                BasicPattern bgp2 = Substitute.substitute(bgp, binding) ;
                QueryIterator qIter = execute(accessor, bgp2) ;
                qIter = new QueryIterCommonParent(qIter, binding, execCxt) ;
                return qIter ;
            }
        };
        return qIter ;
    }
    
    // ---- Node space impl stops here and uses a direct solver.
    
    // Pull out Accessor, work in Row space?
    private QueryIterator execute(AccessRows<Node> accessor, BasicPattern bgp) {
        bgp = ReorderLib.fixed().reorder(bgp) ;
        List<Tuple<Slot<Node>>> pattern = OpExecLib.convertTriplesToSlots(bgp.getList()) ;
        return execute(pattern, accessor) ;
    }

    protected QueryIterator execute(List<Tuple<Slot<Node>>> pattern, AccessRows<Node> accessor) {
        PhysicalPlan<Node> plan = new PhysicalPlan<Node>() ;
        boolean first = true ;
        for ( Tuple<Slot<Node>> pat : pattern ) {
            Step<Node> step ;
            if ( first ) {
                Set<Var> vars = EngLib.vars(pat) ;
                step = new StepOne(accessor, pat, vars) ;
            } else {
                final RowBuilder<Node> builder = new RowBuilderBase<>() ;
                //step = new StepSubstitutionJoin<>(pattern, accessor, builder) ; 
                //step = new StepInnerLoopJoin<>(pattern, accessor, builder) ;
                step = new StepHashJoin<>(pat, accessor, builder) ;
            }
            first = false ;
            plan.add(step);
        }
        
        // Execute the plan.
        RowList<Node> identity = RowLib.identityRowList() ;
        RowList<Node> results = plan.execute(identity) ;
        QueryIterator qIter = OpExecLib.apply(results, execCxt) ;
        return qIter ;
    }

    static class StepOne implements Step<Node> {
        final private AccessRows<Node> accessor ;
        final private Tuple<Slot<Node>> pattern ;
        final private Set<Var> vars ;
        
        StepOne(AccessRows<Node> accessor, Tuple<Slot<Node>> pattern, Set<Var> vars) {
            this.accessor = accessor ;
            this.pattern = pattern ;
            this.vars = vars ;
        }
        @Override
        public RowList<Node> execute(RowList<Node> input) {
            Iterator<Row<Node>> iter = accessor.accessRows(pattern) ;
            return RowLib.createRowList(vars, iter) ;
        }
    }
}
