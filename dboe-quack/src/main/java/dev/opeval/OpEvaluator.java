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

package dev.opeval ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.atlas.logging.Log ;
import org.seaborne.dboe.engine.general.OpExecLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDistinguishedVars ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.ref.Evaluator ;

/**
 * Evaluator as a Tree.
 * 
 * The reference query engine materializes to tables at every step.
 * This class is very similar but based on streams.  
 * 
 * @see OpExecutor -- stream based execution.
 * @see Evaluator  -- the reference query engine 
 */

// Use RowList<> as the return ?
// OpEvaluator<X>, EvaluatorDispatch<X>

public class OpEvaluator
{
    /** Public interface */
    static QueryIterator execute(Op op, ExecutionContext execCxt) {
        OpEvaluator exec = createOpEvaluator(execCxt) ;
        QueryIterator q = exec.exec(op) ;
        return q ;
    }

    // -------- The object starts here --------

    private static OpEvaluator createOpEvaluator(ExecutionContext execCxt2) {
        return null ;
    }

    protected ExecutionContext     execCxt ;
    protected EvaluatorDispatch    dispatcher = null ;
    protected static final int     TOP_LEVEL  = 0 ;
    protected int                  level      = TOP_LEVEL - 1 ;
    private final boolean          hideBNodeVars ;

    protected OpEvaluator(ExecutionContext execCxt)
    {
        this.execCxt = execCxt ;
        this.dispatcher = new EvaluatorDispatch(this) ;
        this.hideBNodeVars = execCxt.getContext().isTrue(ARQ.hideNonDistiguishedVariables) ;
    }

    // Public interface 
    /*public*/ QueryIterator executeOp(Op op, QueryIterator input) {
        // input?
        if ( input instanceof QueryIterRoot )
            input.close() ;
        else
            throw new NotImplemented("Non-root QueryIterator input") ;
        return exec(op) ;
    }
    
    // ---- The recursive step.
    protected QueryIterator exec(Op op) {
        level++ ;
        QueryIterator qIter = dispatcher.eval(op) ;
        // Intentionally not try/finally so exceptions leave some evidence
        // around.
        level-- ;
        return qIter ;
    }

    // ---- All the cases

    protected QueryIterator execute(OpBGP opBGP) {
        BasicPattern pattern = opBGP.getPattern() ;
        QueryIterator qIter = OpExecLib.solvePattern(execCxt.getActiveGraph(), pattern, execCxt) ;
        if (hideBNodeVars)
            qIter = new QueryIterDistinguishedVars(qIter, execCxt) ;
        return qIter ;
    }

    protected QueryIterator execute(OpTriple opTriple) {
        return execute(opTriple.asBGP()) ;
    }
    
    // ----
    
//
    protected QueryIterator execute(OpGraph opGraph) { 
        QueryIterator qIter = specialcase(opGraph.getNode(), opGraph.getSubOp()) ;
        if (qIter != null)
            return qIter ;
        Node gn = opGraph.getNode() ;
        return null ;
    }
//    protected QueryIterator execute(OpGraph opGraph, QueryIterator input) {
//        QueryIterator qIter = specialcase(opGraph.getNode(), opGraph.getSubOp(), input) ;
//        if (qIter != null)
//            return qIter ;
//        return new QueryIterGraph(input, opGraph, execCxt) ;
//    }
//
    private QueryIterator specialcase(Node gn, Op subOp) {
        // This is a placeholder for code to specially handle explicitly named
        // default graph and union graph.

        if (Quad.isDefaultGraph(gn)) {
            // Get factory.
            ExecutionContext cxt2 = new ExecutionContext(execCxt, execCxt.getDataset().getDefaultGraph()) ;
            OpEvaluator sub = new OpEvaluator(cxt2) ;
            // Or push-pop ExecutionContext
            return sub.exec(subOp) ;
        }

        if ( Quad.isUnionGraph(gn) )
            Log.warn(this, "Not implemented yet: union default graph in general OpExecutor") ;

        // Bad news -- if ( Lib.equals(gn, Quad.tripleInQuad) ) {}
        return null ;
    }
    protected QueryIterator execute(OpQuad opQuad) {
        return execute(opQuad.asQuadPattern()) ;
    }

    protected QueryIterator execute(OpQuadPattern quadPattern) {
        // Convert to BGP forms to execute in this graph-centric engine.
        if (quadPattern.isDefaultGraph() && execCxt.getActiveGraph() == execCxt.getDataset().getDefaultGraph()) {
            // Note we tested that the containing graph was the dataset's
            // default graph.
            // Easy case.
            OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
            return execute(opBGP) ;
        }
        // Not default graph - (graph .... )
        OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
        OpGraph op = new OpGraph(quadPattern.getGraphNode(), opBGP) ;
        return exec(op) ;
    }
    
    protected QueryIterator execute(OpQuadBlock quadBlock) {
        Op op = quadBlock.convertOp() ;
        return exec(op) ;
    }

    protected QueryIterator execute(OpPath opPath) { return null ; }
//    protected QueryIterator execute(OpPath opPath, QueryIterator input) {
//        return new QueryIterPath(opPath.getTriplePath(), input, execCxt) ;
//    }
//
    protected QueryIterator execute(OpProcedure opProc) { return null ; }
//    protected QueryIterator execute(OpProcedure opProc, QueryIterator input) {
//        Procedure procedure = ProcEval.build(opProc, execCxt) ;
//        QueryIterator qIter = exec(opProc.getSubOp(), input) ;
//        // Delay until query starts executing.
//        return new QueryIterProcedure(qIter, procedure, execCxt) ;
//    }
//
    protected QueryIterator execute(OpPropFunc opPropFunc) { return null ; }
//    protected QueryIterator execute(OpPropFunc opPropFunc, QueryIterator input) {
//        Procedure procedure = ProcEval.build(opPropFunc.getProperty(), opPropFunc.getSubjectArgs(),
//                                             opPropFunc.getObjectArgs(), execCxt) ;
//        QueryIterator qIter = exec(opPropFunc.getSubOp(), input) ;
//        return new QueryIterProcedure(qIter, procedure, execCxt) ;
//    }
//
    protected QueryIterator execute(OpJoin opJoin) { return null ; }
//    protected QueryIterator execute(OpJoin opJoin, QueryIterator input) {
//        // Need to clone input into left and right.
//        // Do by evaling for each input case, the left and right and concat'ing
//        // the results.
//
//        if (false) {
//            // If needed, applies to OpDiff and OpLeftJoin as well.
//            List<Binding> a = all(input) ;
//            QueryIterator qIter1 = new QueryIterPlainWrapper(a.iterator(), execCxt) ;
//            QueryIterator qIter2 = new QueryIterPlainWrapper(a.iterator(), execCxt) ;
//
//            QueryIterator left = exec(opJoin.getLeft(), qIter1) ;
//            QueryIterator right = exec(opJoin.getRight(), qIter2) ;
//            QueryIterator qIter = new QueryIterJoin(left, right, execCxt) ;
//            return qIter ;
//        }
//        QueryIterator left = exec(opJoin.getLeft(), input) ;
//        QueryIterator right = exec(opJoin.getRight(), root()) ;
//        QueryIterator qIter = new QueryIterJoin(left, right, execCxt) ;
//        return qIter ;
//    }
//
//    // Pass iterator from one step directly into the next.
    protected QueryIterator execute(OpSequence opSequence) { return null ; }
//    protected QueryIterator execute(OpSequence opSequence, QueryIterator input) {
//        QueryIterator qIter = input ;
//
//        for (Iterator<Op> iter = opSequence.iterator(); iter.hasNext();) {
//            Op sub = iter.next() ;
//            qIter = exec(sub, qIter) ;
//        }
//        return qIter ;
//    }
//
    protected QueryIterator execute(OpLeftJoin opLeftJoin) { return null ; }
//    protected QueryIterator execute(OpLeftJoin opLeftJoin, QueryIterator input) {
//        QueryIterator left = exec(opLeftJoin.getLeft(), input) ;
//        QueryIterator right = exec(opLeftJoin.getRight(), root()) ;
//        QueryIterator qIter = new QueryIterLeftJoin(left, right, opLeftJoin.getExprs(), execCxt) ;
//        return qIter ;
//    }
//
    protected QueryIterator execute(OpConditional opCondition) { return null ; }
//    protected QueryIterator execute(OpConditional opCondition, QueryIterator input) {
//        QueryIterator left = exec(opCondition.getLeft(), input) ;
//        QueryIterator qIter = new QueryIterOptionalIndex(left, opCondition.getRight(), execCxt) ;
//        return qIter ;
//    }
//
    protected QueryIterator execute(OpDiff opDiff) { return null ; }
//    protected QueryIterator execute(OpDiff opDiff, QueryIterator input) {
//        QueryIterator left = exec(opDiff.getLeft(), input) ;
//        QueryIterator right = exec(opDiff.getRight(), root()) ;
//        return new QueryIterDiff(left, right, execCxt) ;
//    }
//
    protected QueryIterator execute(OpMinus opMinus) { return null ; }
//    protected QueryIterator execute(OpMinus opMinus, QueryIterator input) {
//        Op lhsOp = opMinus.getLeft() ;
//        Op rhsOp = opMinus.getRight() ;
//
//        QueryIterator left = exec(lhsOp, input) ;
//        QueryIterator right = exec(rhsOp, root()) ;
//
//        Set<Var> commonVars = OpVars.visibleVars(lhsOp) ;
//        commonVars.retainAll(OpVars.visibleVars(rhsOp)) ;
//
//        return new QueryIterMinus(left, right, commonVars, execCxt) ;
//    }
//
    protected QueryIterator execute(OpDisjunction opDisjunction) { return null ; }
//    protected QueryIterator execute(OpDisjunction opDisjunction, QueryIterator input) {
//        QueryIterator cIter = new QueryIterUnion(input, opDisjunction.getElements(), execCxt) ;
//        return cIter ;
//    }
//
    protected QueryIterator execute(OpUnion opUnion) { return null ; }
//    protected QueryIterator execute(OpUnion opUnion, QueryIterator input) {
//        List<Op> x = flattenUnion(opUnion) ;
//        QueryIterator cIter = new QueryIterUnion(input, x, execCxt) ;
//        return cIter ;
//    }
//
//    // Based on code from Olaf Hartig.
//    protected List<Op> flattenUnion(OpUnion opUnion) {
//    protected List<Op> flattenUnion(OpUnion opUnion) {
//        List<Op> x = new ArrayList<Op>() ;
//        flattenUnion(x x ; return null ; }
//    }
//
//		protected void flattenUnion(List<Op> acc) { return null ; }
//    protected void flattenUnion(List<Op> acc, OpUnion opUnion) {
//        if (opUnion.getLeft() instanceof OpUnion)
//            flattenUnion(acc, (OpUnion)opUnion.getLeft()) ;
//        else
//            acc.add(opUnion.getLeft()) ;
//
//        if (opUnion.getRight() instanceof OpUnion)
//            flattenUnion(acc, (OpUnion)opUnion.getRight()) ;
//        else
//            acc.add(opUnion.getRight()) ;
//    }
//
		protected QueryIterator execute(OpFilter opFilter) { return null ; }
//    protected QueryIterator execute(OpFilter opFilter, QueryIterator input) {
//        ExprList exprs = opFilter.getExprs() ;
//
//        Op base = opFilter.getSubOp() ;
//        QueryIterator qIter = exec(base, input) ;
//
//        for (Expr expr : exprs)
//            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpService opService) { return null ; }
//    protected QueryIterator execute(OpService opService, QueryIterator input) {
//        return new QueryIterService(input, opService, execCxt) ;
//    }
//
//    // Quad form, "GRAPH ?g {}" Flip back to OpGraph.
//    // Normally quad stores override this.
		protected QueryIterator execute(OpDatasetNames dsNames) { return null ; }
//    protected QueryIterator execute(OpDatasetNames dsNames, QueryIterator input) {
//        if (false) {
//            OpGraph op = new OpGraph(dsNames.getGraphNode(), new OpBGP()) ;
//            return execute(op, input) ;
//        }
//        throw new ARQNotImplemented("execute/OpDatasetNames") ;
//    }
//
		protected QueryIterator execute(OpTable opTable) { return null ; }
//    protected QueryIterator execute(OpTable opTable, QueryIterator input) {
//        if (opTable.isJoinIdentity())
//            return input ;
//        if (input instanceof QueryIterRoot) {
//            input.close() ;
//            return opTable.getTable().iterator(execCxt) ;
//        }
//        QueryIterator qIterT = opTable.getTable().iterator(execCxt) ;
//        QueryIterator qIter = new QueryIterJoin(input, qIterT, execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpExt opExt) { return null ; }
//    protected QueryIterator execute(OpExt opExt, QueryIterator input) {
//        try {
//            QueryIterator qIter = opExt.eval(input, execCxt) ;
//            if (qIter != null)
//                return qIter ;
//        } catch (UnsupportedOperationException ex) {}
//        // null or UnsupportedOperationException
//        throw new QueryExecException("Encountered unsupported OpExt: " + opExt.getName()) ;
//    }
//
		protected QueryIterator execute(OpLabel opLabel) { return null ; }
//    protected QueryIterator execute(OpLabel opLabel, QueryIterator input) {
//        if (!opLabel.hasSubOp())
//            return input ;
//
//        return exec(opLabel.getSubOp(), input) ;
//    }
//
		protected QueryIterator execute(OpNull opNull) { return null ; }
//    protected QueryIterator execute(OpNull opNull, QueryIterator input) {
//        // Loose the input.
//        input.close() ;
//        return QueryIterNullIterator.create(execCxt) ;
//    }
//
		protected QueryIterator execute(OpList opList) { return null ; }
//    protected QueryIterator execute(OpList opList, QueryIterator input) {
//        return exec(opList.getSubOp(), input) ;
//    }
//
		protected QueryIterator execute(OpOrder opOrder) { return null ; }
//    protected QueryIterator execute(OpOrder opOrder, QueryIterator input) {
//        QueryIterator qIter = exec(opOrder.getSubOp(), input) ;
//        qIter = new QueryIterSort(qIter, opOrder.getConditions(), execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpTopN opTop) { return null ; }
//    protected QueryIterator execute(OpTopN opTop, QueryIterator input) {
//        QueryIterator qIter = null ;
//        // We could also do (reduced) here as well.
//        // but it's detected in TransformTopN and turned into (distinct)
//        // there so that code catches that already.
//        // We leave this to do the strict case of (top (distinct ...))
//        if (opTop.getSubOp() instanceof OpDistinct) {
//            OpDistinct opDistinct = (OpDistinct)opTop.getSubOp() ;
//            qIter = exec(opDistinct.getSubOp(), input) ;
//            qIter = new QueryIterTopN(qIter, opTop.getConditions(), opTop.getLimit(), true, execCxt) ;
//        } else {
//            qIter = exec(opTop.getSubOp(), input) ;
//            qIter = new QueryIterTopN(qIter, opTop.getConditions(), opTop.getLimit(), false, execCxt) ;
//        }
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpProject opProject) { return null ; }
//    protected QueryIterator execute(OpProject opProject, QueryIterator input) {
//        // This may be under a (graph) in which case we need to operate
//        // on the active graph.
//
//        // More intelligent QueryIterProject needed.
//
//        if (input instanceof QueryIterRoot) {
//            QueryIterator qIter = exec(opProject.getSubOp(), input) ;
//            qIter = new QueryIterProject(qIter, opProject.getVars(), execCxt) ;
//            return qIter ;
//        }
//        // Nested projected : need to ensure the input is seen.
//        QueryIterator qIter = new QueryIterProjectMerge(opProject, input, this, execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpSlice opSlice) { return null ; }
//    protected QueryIterator execute(OpSlice opSlice, QueryIterator input) {
//        QueryIterator qIter = exec(opSlice.getSubOp(), input) ;
//        qIter = new QueryIterSlice(qIter, opSlice.getStart(), opSlice.getLength(), execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpGroup opGroup) { return null ; }
//    protected QueryIterator execute(OpGroup opGroup, QueryIterator input) {
//        QueryIterator qIter = exec(opGroup.getSubOp(), input) ;
//        qIter = new QueryIterGroup(qIter, opGroup.getGroupVars(), opGroup.getAggregators(), execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpDistinct opDistinct) { return null ; }
//    protected QueryIterator execute(OpDistinct opDistinct, QueryIterator input) {
//        QueryIterator qIter = exec(opDistinct.getSubOp(), input) ;
//        qIter = new QueryIterDistinct(qIter, execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpReduced opReduced) { return null ; }
//    protected QueryIterator execute(OpReduced opReduced, QueryIterator input) {
//        QueryIterator qIter = exec(opReduced.getSubOp(), input) ;
//        qIter = new QueryIterReduced(qIter, execCxt) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpAssign opAssign) { return null ; }
//    protected QueryIterator execute(OpAssign opAssign, QueryIterator input) {
//        QueryIterator qIter = exec(opAssign.getSubOp(), input) ;
//        qIter = new QueryIterAssign(qIter, opAssign.getVarExprList(), execCxt, false) ;
//        return qIter ;
//    }
//
		protected QueryIterator execute(OpExtend opExtend) { return null ; }
//    protected QueryIterator execute(OpExtend opExtend, QueryIterator input) {
//        // We know (parse time checking) the variable is unused so far in
//        // the query so we can use QueryIterAssign knowing that it behaves
//        // the same as extend. The boolean should only be a check.
//        QueryIterator qIter = exec(opExtend.getSubOp(), input) ;
//        qIter = new QueryIterAssign(qIter, opExtend.getVarExprList(), execCxt, true) ;
//        return qIter ;
//    }
//
    public static QueryIterator createRootQueryIterator(ExecutionContext execCxt) {
        return QueryIterRoot.create(execCxt) ;
    }

    protected QueryIterator root() { return createRootQueryIterator(execCxt) ; }
}

