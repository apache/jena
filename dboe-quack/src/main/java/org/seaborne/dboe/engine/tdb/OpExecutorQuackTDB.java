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

package org.seaborne.dboe.engine.tdb;

import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.explain.Explain2 ;
import org.seaborne.dboe.engine.extra_tdb.OpExecutorTDBBase ;
import org.seaborne.dboe.engine.general.OpExecLib ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderProc ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.solver.OpExecutorTDB1 ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

/** Query execution for TDB */ 
public class OpExecutorQuackTDB extends OpExecutorTDBBase
{
    private static OpExecSetup setupPlain = new OpExecSetup() {
        @Override
        public AccessorTDB initAccessor(DatasetGraphTDB dsgtdb) {
            return AccessorTDB.create(new StorageTDB(dsgtdb)) ;
        }

        @Override
        public Planner initPlanner(DatasetGraphTDB dsgtdb, AccessorTDB accessor) {
            return new PlannerSubstitution(accessor) ;
        }
    } ;

    private static OpExecSetup setupPredicateObject = new OpExecSetup() {
        @Override
        public AccessorTDB initAccessor(DatasetGraphTDB dsgtdb) {
            return AccessorTDB.create(new StorageTDB(dsgtdb)) ;
        }

        @Override
        public Planner initPlanner(DatasetGraphTDB dsgtdb, AccessorTDB accessor) {
            return new PlannerPredObjList(accessor) ;
        }
    } ;

    /** TDB execution with the Substitution planner.*/
    public static final OpExecutorFactory factorySubstitute = new OpExecutorFactory() {
        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            Explain2.explain(Quack.quackExec, "OpExecutorQuackTDB/Plain");
            OpExecutorQuackTDB opExec = new OpExecutorQuackTDB(execCxt, setupPlain) ;
            return opExec ;
        }
    } ;
        
    /**TDB execution with the Predicate-Object list planner.*/
    public static final OpExecutorFactory factoryPredicateObject = new OpExecutorFactory() {
        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            Explain2.explain(Quack.quackExec, "OpExecutorQuackTDB/PredObj");
            return new OpExecutorQuackTDB(execCxt, setupPredicateObject) ;
        }
    } ;
        
    /** TDB execution with the old TDB execution engine */
    public static final OpExecutorFactory factoryTDB1 = new OpExecutorFactory() {
        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            Explain2.explain(Quack.quackExec, "Quack-TDB1") ;
            return new OpExecutorTDB1(execCxt) ; 
        }
    } ;

    public interface OpExecSetup {
        public AccessorTDB initAccessor(DatasetGraphTDB dsgtdb ) ;
        public Planner initPlanner(DatasetGraphTDB dsgtdb , AccessorTDB accessor ) ;
    }

    // ---- Object
    
    protected final AccessorTDB accessor ;
    protected final Planner planner ;
    
    public OpExecutorQuackTDB(ExecutionContext execCxt, OpExecSetup setup) {
        super(execCxt) ;
        if ( isForThisExecutor ) {
            accessor = setup.initAccessor(dsgtdb) ;
            planner = setup.initPlanner(dsgtdb, accessor) ;
        } else {
            accessor = null ;
            planner = null ;
        }
    }
    
    @Override
    protected QueryIterator exec(Op op, QueryIterator input) {
        //if ( level < 0 )
            // Print only at top level (and we're called before level++) 
            Explain2.explain(Quack.quackExec, "exec Op =\n%s", op) ;
        return super.exec(op, input) ;
    }

    // XXX Long term - move away from RepeatApply and do more with executing a whole Iterator<Binding> input
    // Issues - (1) variables in the input used in the pattern. (2) passing the right parent to the rows->binding step. 
    
    @Override
    protected QueryIterator evaluateBlockFilter(final DatasetGraphTDB dsgtdb, final Node graphNode, BasicPattern bgp, final ExprList exprs, QueryIterator _input) {
        Iterator<Binding> input = _input ;
        ReorderTransformation reorder = dsgtdb.getReorderTransform() ;
        
        if ( true && ! OpExecLib.isRootInput(_input) ) {
            // The input may help ground the pattern.
            // Substitue the first binding, calculate the reordering from the
            // substitued pattern and apply to the basic graph pattern. 
            PeekIterator<Binding> peek = PeekIterator.create(input) ;
            input = peek ;
            Binding b = peek.peek() ;
            BasicPattern bgp2 = Substitute.substitute(bgp, b) ;
            ReorderProc rproc = reorder.reorderIndexes(bgp2) ;
            // Reorder bgp based on bgp2. 
            final BasicPattern bgp$ = rproc.reorder(bgp) ;
            QueryIterator overall = new QueryIterRepeatApply(new QueryIterPlainWrapper(input, execCxt), execCxt) {
                @Override
                protected QueryIterator nextStage(Binding input1) {
                    return evaluateBlockFilterSub(dsgtdb, graphNode, bgp$, exprs, input1) ;
                }
            };
            // Ensure input is closed properly.
            // This should not be necessary if 'input' is handled properly. 
            //qIter = new QueryIteratorCloseable(qIter, _input) ;
            return overall ;
        } else {
            // Root - do directly.
            bgp = reorder.reorder(bgp) ;
            Binding b = _input.next() ; 
            _input.close(); 
            QueryIterator results = evaluateBlockFilterSub(dsgtdb, graphNode, bgp, exprs, b) ;
            _input.close();
            return results ;
        }
//        
//        // Or RepeatApplyIteator?
//        QueryIterator overall = new QueryIterRepeatApply(new QueryIterPlainWrapper(input, execCxt), execCxt) {
//            @Override
//            protected QueryIterator nextStage(Binding input1) {
//                return evaluateBlockFilterSub(dsgtdb, graphNode, bgp$, exprs, input1) ;
//            }
//        };
//        // Ensure input is closed properly.
//        // This should not be necessary if 'input' is handled properly. 
//        //qIter = new QueryIteratorCloseable(qIter, _input) ;
//        return overall ;
    }

    /** Execute for one input binding */  
    protected QueryIterator evaluateBlockFilterSub(DatasetGraphTDB dsgtdb, Node graphNode, BasicPattern bgp, ExprList exprs, Binding input1) {
        Explain2.explain(Quack.quackExec, "%s, BGP =\n%s", graphNode, bgp) ;  
        PhysicalPlan<NodeId> plan = new PhysicalPlan<>() ;
        bgp = Substitute.substitute(bgp, input1) ;
        
        if ( exprs != null ) {
            Op op ;
            if ( graphNode == null )
                op = TransformFilterPlacement.transform(exprs, bgp) ;
            else
                op = TransformFilterPlacement.transform(exprs, graphNode, bgp) ;
            accumulatePlan(plan, graphNode, op);
        } else {
            accumulatePlan(plan, graphNode, bgp) ;
        }
        
        return executePlan$(plan, input1) ;
    }

    @Override
    protected QueryIterator evaluateBlockFilter(Graph graph, BasicPattern bgp, ExprList exprs, QueryIterator input) {
        // The case of TDB graph in a general dataset or non-quad execution for some reason.
        GraphTDB gt = (GraphTDB)graph ;
        return evaluateBlockFilter(gt.getDSG(), gt.getGraphName(), bgp, null, input) ;
    }

    private QueryIterator executePlan$(PhysicalPlan<NodeId> plan, Binding input) {
        if ( plan.executesToNothing() ) {
            executePlanToNothing(plan) ;
            return QueryIterNullIterator.create(execCxt) ;
        }

        RowList<NodeId> rows ;
        if ( input.isEmpty() )
            rows = RowLib.identityRowList() ;
        else {
            final RowBuilder<NodeId> builder = new RowBuilderBase<NodeId>() ; // Reuse?
            builder.reset() ;
            Row<NodeId> row = ELibTDB.convertToRow(input, accessor.getNodeTable(), builder) ;
            Set<Var> vars = Collections.emptySet() ; // Reuse?
            rows = RowLib.createRowList(vars, Iter.singleton(row)) ;
        }
        
        RowList<NodeId> results = executePlan(plan, rows) ; 
        // And include the input bindings not passed on.
        Iterator<Binding> bIter = convertToBindings(results.iterator(), input, accessor.getNodeTable()) ;
        return new QueryIterPlainWrapper(bIter, execCxt) ;
    }

    /** Convert rows to bindings for a give parent, that may, or may not,
     *  have equivalent bindings of variables in the rows */ 
    private static Iterator<Binding> convertToBindings(Iterator<Row<NodeId>> iter, final Binding parent, final NodeTable nodeTable) {
        Transform<Row<NodeId>, Binding> conv = new Transform<Row<NodeId>, Binding>() {
            @Override
            public Binding convert(Row<NodeId> row) {
                if ( parent.isEmpty() )
                    return new BindingRow(row, nodeTable) ;
                
                // Temporary fix.  Proper fix is to change BindingBase to allow multiple occurrences in a controlled way. 
                BindingMap b = BindingFactory.create() ;
                if ( ! parent.isEmpty() ) {
                    for ( Iterator<Var> vars = parent.vars() ; vars.hasNext() ; ) {
                        Var v = vars.next() ;
                        if ( ! row.contains(v) )
                            b.add(v, parent.get(v)) ;
                    }
                }
                return new BindingRow(b, row, nodeTable) ;
            }
        } ;
        return Iter.map(iter, conv) ;
    }

    private static void executePlanToNothing(PhysicalPlan<NodeId> plan) {
        explainPlan(plan) ;
    }

    /** Execution point - log and do */
    private RowList<NodeId> executePlan(PhysicalPlan<NodeId> plan, RowList<NodeId> rows) {
        explainPlan(plan) ;
        RowList<NodeId> results = plan.execute(rows) ;
        return results ;
    }
    
    private static void explainPlan(PhysicalPlan<NodeId> plan) {
        Explain2.explain(Quack.quackPlan, plan) ;
    }

//    private static List<Tuple<Slot<NodeId>>> substitute(List<Tuple<Slot<NodeId>>> tuples, Row<NodeId> row) {
//        List<Tuple<Slot<NodeId>>> x = new ArrayList<>(tuples.size()) ;
//        for ( int i = 0 ; i < x.size() ; i++ )
//            x.set(i, substitute(tuples.get(i), row)) ;
//        return x ;
//    }

//    private static Tuple<Slot<NodeId>> substitute(Tuple<Slot<NodeId>> tuple, Row<NodeId> row) {
//        @SuppressWarnings("unchecked")
//        Slot<NodeId>[] x = (Slot<NodeId>[])new Slot<?>[tuple.size()] ;
//        for ( Slot<NodeId> slot : tuple ) {
//            NodeId nid = slot.term ;
//            if ( slot.isVar() && row.contains(slot.var) )
//                nid = row.get(slot.var) ;
//        }
//        return Tuple.createTuple(x) ;
//    }

    // Process the outcome of TransformFilterPlacement
    private void accumulatePlan(PhysicalPlan<NodeId> plan, Node graphNode, Op op) {
        if ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            op = f.getSubOp() ;
            accumulatePlan(plan, graphNode, op) ;
            Step<NodeId> step = new StepFilterTDB(f.getExprs(), accessor.getNodeTable(), execCxt) ;
            plan.add(step) ;
            return ;
        }
        
        if ( op instanceof OpBGP ) {
            OpBGP opBGP = (OpBGP)op ;
            accumulatePlan(plan, null, opBGP.getPattern()) ;
            return ;
        }
        
        if ( op instanceof OpQuadPattern ) {
            OpQuadPattern opQuads = (OpQuadPattern)op ;
            accumulatePlan(plan, opQuads.getGraphNode(), opQuads.getBasicPattern()) ;
            return ;
        }

        if ( op instanceof OpSequence ) {
            OpSequence opSeq = (OpSequence)op ;
            for ( Op subOp : opSeq.getElements() ) {
                accumulatePlan(plan, graphNode, subOp) ;
            }
            return ;
        }

        if ( op instanceof OpTable ) {
            OpTable opTable = (OpTable)op ;
            if ( opTable.isJoinIdentity() )
                return ;
            throw new InternalErrorException("Data table") ;
        }
        throw new InternalErrorException("Unknown Op passed to accumulatePlan: "+Utils.className(op) ) ;
    }
    
    private void accumulatePlan(PhysicalPlan<NodeId> plan, Node graphNode, BasicPattern basicPattern) {
        List<Triple> triples = basicPattern.getList() ;
        int N = OpExecLib.isDefaultGraph(graphNode) ? 3 : 4 ;
        List<Tuple<Slot<NodeId>>> tuples ;
        if ( OpExecLib.isDefaultGraph(graphNode) ) 
            tuples = ELibTDB.convertTriples(triples, accessor.getNodeTable()) ;
        else 
            tuples = ELibTDB.convertQuads(graphNode, triples, accessor.getNodeTable()) ;
        
        // Some concrete term was not found so this pattern can not match. 
        if ( tuples == null ) {
            plan.add(new StepNothing<NodeId>()) ;
            return ;
        }
        // accumulate would be clearer
        PhysicalPlan<NodeId> p = generateAccessPlan(tuples) ;
        plan.append(p) ;
    }

    /** Generate a plan for a block of tuples patterns */ 
    protected final PhysicalPlan<NodeId> generateAccessPlan(List<Tuple<Slot<NodeId>>> tuples) {
        return planner.generatePlan(tuples) ;
    }
}