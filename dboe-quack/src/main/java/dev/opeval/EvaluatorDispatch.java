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

import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;

/** One level visitor dispatch and results as QueryIterator */
public class EvaluatorDispatch implements OpVisitor
{
    private QueryIterator result = null ;
    protected OpEvaluator evaluator ;

    protected EvaluatorDispatch(OpEvaluator evaluator) {
        this.evaluator = evaluator ;
    }

    public static QueryIterator eval(Op op, ExecutionContext execCxt) {
        OpEvaluator eval = new OpEvaluator(execCxt) ;
        EvaluatorDispatch dispatch = new EvaluatorDispatch(eval) ;
        return dispatch.eval(op) ;
    }

    protected QueryIterator eval(Op op) {
        op.visit(this) ;
        if ( result == null )
            throw new ARQInternalErrorException("EvaluatorDispatch result is null") ;
        return result ;
    }

    @Override
    public void visit(OpBGP opBGP) {
        result = evaluator.execute(opBGP) ;
    }

    @Override
    public void visit(OpQuadPattern quadPattern) {
        result = evaluator.execute(quadPattern) ;
    }

    @Override
    public void visit(OpQuadBlock quadBlock) {
        result = evaluator.execute(quadBlock) ;
    }

    @Override
    public void visit(OpTriple opTriple) {
        visit(opTriple.asBGP()) ;
    }

    @Override
    public void visit(OpQuad opQuad) {
        visit(opQuad.asQuadPattern()) ;
    }

    @Override
    public void visit(OpPath opPath) {
        result = evaluator.execute(opPath) ;
    }

    @Override
    public void visit(OpProcedure opProc) {
        result = evaluator.execute(opProc) ;
    }

    @Override
    public void visit(OpPropFunc opPropFunc) {
        result = evaluator.execute(opPropFunc) ;
    }

    @Override
    public void visit(OpJoin opJoin) {
        result = evaluator.execute(opJoin) ;
    }

    @Override
    public void visit(OpSequence opSequence) {
        result = evaluator.execute(opSequence) ;
    }

    @Override
    public void visit(OpDisjunction opDisjunction) {
        result = evaluator.execute(opDisjunction) ;
    }

    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        result = evaluator.execute(opLeftJoin) ;
    }

    @Override
    public void visit(OpDiff opDiff) {
        result = evaluator.execute(opDiff) ;
    }

    @Override
    public void visit(OpMinus opMinus) {
        result = evaluator.execute(opMinus) ;
    }

    @Override
    public void visit(OpUnion opUnion) {
        result = evaluator.execute(opUnion) ;
    }

    @Override
    public void visit(OpConditional opCond) {
        result = evaluator.execute(opCond) ;
    }

    @Override
    public void visit(OpFilter opFilter) {
        result = evaluator.execute(opFilter) ;
    }

    @Override
    public void visit(OpGraph opGraph) {
        result = evaluator.execute(opGraph) ;
    }

    @Override
    public void visit(OpService opService) {
        result = evaluator.execute(opService) ;
    }

    @Override
    public void visit(OpDatasetNames dsNames) {
        result = evaluator.execute(dsNames) ;
    }

    @Override
    public void visit(OpTable opTable) {
        result = evaluator.execute(opTable) ;
    }

    @Override
    public void visit(OpExt opExt) {
        result = evaluator.execute(opExt) ;
    }

    @Override
    public void visit(OpNull opNull) {
        result = evaluator.execute(opNull) ;
    }

    @Override
    public void visit(OpLabel opLabel) {
        result = evaluator.execute(opLabel) ;
    }

    @Override
    public void visit(OpList opList) {
        result = evaluator.execute(opList) ;
    }

    @Override
    public void visit(OpOrder opOrder) {
        result = evaluator.execute(opOrder) ;
    }

    @Override
    public void visit(OpTopN opTop) {
        result = evaluator.execute(opTop) ;
    }

    @Override
    public void visit(OpProject opProject) {
        result = evaluator.execute(opProject) ;
    }

    @Override
    public void visit(OpDistinct opDistinct) {
        result = evaluator.execute(opDistinct) ;
    }

    @Override
    public void visit(OpReduced opReduced) {
        result = evaluator.execute(opReduced) ;
    }

    @Override
    public void visit(OpSlice opSlice) {
        result = evaluator.execute(opSlice) ;
    }

    @Override
    public void visit(OpAssign opAssign) {
        result = evaluator.execute(opAssign) ;
    }

    @Override
    public void visit(OpExtend opExtend) {
        result = evaluator.execute(opExtend) ;
    }

    @Override
    public void visit(OpGroup opGroup) {
        result = evaluator.execute(opGroup) ;
    }
}
