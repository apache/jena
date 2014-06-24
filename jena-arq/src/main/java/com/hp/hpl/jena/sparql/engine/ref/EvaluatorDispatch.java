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

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.Iterator ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.http.Service ;

/**  Class to provide type-safe eval() dispatch using the visitor support of Op */

public class EvaluatorDispatch implements OpVisitor
{
    private Deque<Table> stack = new ArrayDeque<>() ;
    protected Evaluator evaluator ;
    
    public EvaluatorDispatch(Evaluator evaluator)
    {
        this.evaluator = evaluator ;
    }

    protected Table eval(Op op)
    {
        op.visit(this) ;
        return pop() ;
    }
    
    Table getResult()
    {
        if ( stack.size() != 1 )
            Log.warn(this, "Warning: getResult: stack size = "+stack.size()) ;
        
        Table table = pop() ;
        return table ;
    }
    
    @Override
    public void visit(OpBGP opBGP)
    {
        Table table = evaluator.basicPattern(opBGP.getPattern()) ;
        push(table) ;
    }

    @Override
    public void visit(OpQuadPattern quadPattern)
    {
        push(Eval.evalQuadPattern(quadPattern, evaluator)) ;
    }

    @Override
    public void visit(OpQuadBlock quadBlock)
    {
        push(eval(quadBlock.convertOp())) ;
        //push(Eval.evalQuadPattern(quadBlock, evaluator)) ;
    }

    @Override
    public void visit(OpTriple opTriple)
    {
        visit(opTriple.asBGP()) ;
    }

    @Override
    public void visit(OpQuad opQuad)
    {
        visit(opQuad.asQuadPattern()) ;
    }
    @Override
    public void visit(OpPath opPath)
    {
        Table table = evaluator.pathPattern(opPath.getTriplePath()) ;
        push(table) ;
    }

    @Override
    public void visit(OpProcedure opProc)
    {
        Table table = eval(opProc.getSubOp()) ;
        table = evaluator.procedure(table, opProc.getProcId(), opProc.getArgs()) ;
        push(table) ;
    }

    @Override
    public void visit(OpPropFunc opPropFunc)
    {
        Table table = eval(opPropFunc.getSubOp()) ;
        table = evaluator.propertyFunction(table, opPropFunc.getProperty(), opPropFunc.getSubjectArgs(), opPropFunc.getObjectArgs()) ;
        push(table) ;
    }

    @Override
    public void visit(OpJoin opJoin)
    {
        Table left = eval(opJoin.getLeft()) ;
        Table right = eval(opJoin.getRight()) ;
        Table table = evaluator.join(left, right) ;
        push(table) ;
    }
    
    @Override
    public void visit(OpSequence opSequence)
    {
        // Evaluation is as a sequence of joins.
        Table table = TableFactory.createUnit() ;
        
        for ( Iterator<Op> iter = opSequence.iterator() ; iter.hasNext() ; )
        {
            Op op = iter.next() ;
            Table eltTable = eval(op) ;
            table = evaluator.join(table, eltTable) ;
        }
        push(table) ;
    }

    @Override
    public void visit(OpDisjunction opDisjunction)
    {
        // Evaluation is as a concatentation of alternatives 
        Table table = TableFactory.createEmpty() ;
        
        for ( Iterator<Op> iter = opDisjunction.iterator() ; iter.hasNext() ; )
        {
            Op op = iter.next() ;
            Table eltTable = eval(op) ;
            table = evaluator.union(table, eltTable) ;
        }
        push(table) ;
    }
    
    @Override
    public void visit(OpLeftJoin opLeftJoin)
    {
        Table left = eval(opLeftJoin.getLeft()) ;
        Table right = eval(opLeftJoin.getRight()) ;
        Table table = evaluator.leftJoin(left, right, opLeftJoin.getExprs()) ;
        push(table) ;
    }

    @Override
    public void visit(OpDiff opDiff)
    {
        Table left = eval(opDiff.getLeft()) ;
        Table right = eval(opDiff.getRight()) ;
        Table table = evaluator.diff(left, right) ;
        push(table) ;
    }

    @Override
    public void visit(OpMinus opMinus)
    {
        Table left = eval(opMinus.getLeft()) ;
        Table right = eval(opMinus.getRight()) ;
        Table table = evaluator.minus(left, right) ;
        push(table) ;
    }

    @Override
    public void visit(OpUnion opUnion)
    {
        Table left = eval(opUnion.getLeft()) ;
        Table right = eval(opUnion.getRight()) ;
        Table table = evaluator.union(left, right) ;
        push(table) ;
    }

    @Override
    public void visit(OpConditional opCond)
    {
        Table left = eval(opCond.getLeft()) ;
        // Ref engine - don;'t care about efficiency
        Table right = eval(opCond.getRight()) ;
        Table table = evaluator.condition(left, right) ;
        push(table) ;
    }
    
    @Override
    public void visit(OpFilter opFilter)
    {
        Table table = eval(opFilter.getSubOp()) ;
        table = evaluator.filter(opFilter.getExprs(), table) ;
        push(table) ;
    }

    @Override
    public void visit(OpGraph opGraph)
    {
        push(Eval.evalGraph(opGraph, evaluator)) ;
    }

    @Override
    public void visit(OpService opService)
    {
        QueryIterator qIter = Service.exec(opService, ARQ.getContext()) ;
        Table table = TableFactory.create(qIter) ;
        push(table) ;
    }

    @Override
    public void visit(OpDatasetNames dsNames)
    {
        push(Eval.evalDS(dsNames, evaluator)) ;
    }

    @Override
    public void visit(OpTable opTable)
    {
        push(opTable.getTable()) ;
    }

    @Override
    public void visit(OpExt opExt)
    { throw new QueryExecException("Encountered OpExt during execution of reference engine") ; }

    @Override
    public void visit(OpNull opNull)
    { 
        push(TableFactory.createEmpty()) ;
    }
    
    @Override
    public void visit(OpLabel opLabel)
    {
        if ( opLabel.hasSubOp() )
            push(eval(opLabel.getSubOp())) ;
        else
            // No subop.
            push(TableFactory.createUnit()) ;
    }

    @Override
    public void visit(OpList opList)
    {
        Table table = eval(opList.getSubOp()) ;
        table = evaluator.list(table) ;
        push(table) ;
    }

    @Override
    public void visit(OpOrder opOrder)
    {
        Table table = eval(opOrder.getSubOp()) ;
        table = evaluator.order(table, opOrder.getConditions()) ;
        push(table) ;
    }

    @Override
    public void visit(OpTopN opTop)
    {
        Table table = eval(opTop.getSubOp()) ;
        //table = evaluator.topN(table, opTop.getLimti(), opTop.getConditions()) ;
        table = evaluator.order(table, opTop.getConditions()) ;
        table = evaluator.slice(table, 0, opTop.getLimit()) ;
        push(table) ;
    }

    @Override
    public void visit(OpProject opProject)
    {
        Table table = eval(opProject.getSubOp()) ;
        table = evaluator.project(table, opProject.getVars()) ;
        push(table) ;
    }

    @Override
    public void visit(OpDistinct opDistinct)
    {
        Table table = eval(opDistinct.getSubOp()) ;
        table = evaluator.distinct(table) ;
        push(table) ;
    }

    @Override
    public void visit(OpReduced opReduced)
    {
        Table table = eval(opReduced.getSubOp()) ;
        table = evaluator.reduced(table) ;
        push(table) ;
    }

    @Override
    public void visit(OpSlice opSlice)
    {
        Table table = eval(opSlice.getSubOp()) ;
        table = evaluator.slice(table, opSlice.getStart(), opSlice.getLength()) ;
        push(table) ;
    }

    @Override
    public void visit(OpAssign opAssign)
    {
        Table table = eval(opAssign.getSubOp()) ;
        table = evaluator.assign(table, opAssign.getVarExprList()) ;
        push(table) ;
    }

    @Override
    public void visit(OpExtend opExtend)
    {
        Table table = eval(opExtend.getSubOp()) ;
        table = evaluator.extend(table, opExtend.getVarExprList()) ;
        push(table) ;
    }

    @Override
    public void visit(OpGroup opGroup)
    {
        Table table = eval(opGroup.getSubOp()) ;
        table = evaluator.groupBy(table, opGroup.getGroupVars(), opGroup.getAggregators()) ;
        push(table) ;
    }

    protected void push(Table table)  { stack.push(table) ; }
    protected Table pop()
    { 
        if ( stack.size() == 0 )
            Log.warn(this, "Warning: pop: empty stack") ;
        return stack.pop() ;
    }

}
