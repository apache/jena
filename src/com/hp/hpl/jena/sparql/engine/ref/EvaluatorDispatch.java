/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.Iterator;
import java.util.Stack;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.query.QueryExecException;

/**  Class to provide type-safe eval() dispatch using the visitor support of Op */

public class EvaluatorDispatch implements OpVisitor
{
    // TODO Clean up: OpGraph, OpDatasetNames (needed?)
    
    private Stack<Table> stack = new Stack<Table>() ;
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
            ALog.warn(this, "Warning: getResult: stack size = "+stack.size()) ;
        
        Table table = pop() ;
        return table ;
    }
    
    public void visit(OpBGP opBGP)
    {
        Table table = evaluator.basicPattern(opBGP.getPattern()) ;
        push(table) ;
    }

    public void visit(OpQuadPattern quadPattern)
    {
        push(Eval.evalQuadPattern(quadPattern, evaluator)) ;
    }

    public void visit(OpTriple opTriple)
    {
        visit(opTriple.asBGP()) ;
    }

    public void visit(OpPath opPath)
    {
        Table table = evaluator.pathPattern(opPath.getTriplePath()) ;
        push(table) ;
    }

    public void visit(OpProcedure opProc)
    {
        Table table = eval(opProc.getSubOp()) ;
        table = evaluator.procedure(table, opProc.getProcId(), opProc.getArgs()) ;
        push(table) ;
    }

    public void visit(OpPropFunc opPropFunc)
    {
        Table table = eval(opPropFunc.getSubOp()) ;
        table = evaluator.propertyFunction(table, opPropFunc.getProperty(), opPropFunc.getSubjectArgs(), opPropFunc.getObjectArgs()) ;
        push(table) ;
    }

    public void visit(OpJoin opJoin)
    {
        Table left = eval(opJoin.getLeft()) ;
        Table right = eval(opJoin.getRight()) ;
        Table table = evaluator.join(left, right) ;
        push(table) ;
    }
    
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
    
    public void visit(OpLeftJoin opLeftJoin)
    {
        Table left = eval(opLeftJoin.getLeft()) ;
        Table right = eval(opLeftJoin.getRight()) ;
        Table table = evaluator.leftJoin(left, right, opLeftJoin.getExprs()) ;
        push(table) ;
    }

    public void visit(OpDiff opDiff)
    {
        Table left = eval(opDiff.getLeft()) ;
        Table right = eval(opDiff.getRight()) ;
        Table table = evaluator.diff(left, right) ;
        push(table) ;
    }

    public void visit(OpMinus opMinus)
    {
        Table left = eval(opMinus.getLeft()) ;
        Table right = eval(opMinus.getRight()) ;
        Table table = evaluator.minus(left, right) ;
        push(table) ;
    }

    public void visit(OpUnion opUnion)
    {
        Table left = eval(opUnion.getLeft()) ;
        Table right = eval(opUnion.getRight()) ;
        Table table = evaluator.union(left, right) ;
        push(table) ;
    }

    public void visit(OpConditional opCond)
    {
        Table left = eval(opCond.getLeft()) ;
        // Ref engine - don;'t care about efficiency
        Table right = eval(opCond.getRight()) ;
        Table table = evaluator.condition(left, right) ;
        push(table) ;
    }
    
    public void visit(OpFilter opFilter)
    {
        Table table = eval(opFilter.getSubOp()) ;
        table = evaluator.filter(opFilter.getExprs(), table) ;
        push(table) ;
    }

    public void visit(OpGraph opGraph)
    {
        push(Eval.evalGraph(opGraph, evaluator)) ;
    }

    public void visit(OpService opService)
    {
        QueryIterator qIter = Service.exec(opService) ;
        Table table = TableFactory.create(qIter) ;
        push(table) ;
    }

    public void visit(OpDatasetNames dsNames)
    {
        push(Eval.evalDS(dsNames, evaluator)) ;
    }

    public void visit(OpTable opTable)
    {
        push(opTable.getTable()) ;
    }

    public void visit(OpExt opExt)
    { throw new QueryExecException("Encountered OpExt during execution of reference engine") ; }

    public void visit(OpNull opNull)
    { 
        push(TableFactory.createEmpty()) ;
    }
    
    public void visit(OpLabel opLabel)
    {
        if ( opLabel.hasSubOp() )
            push(eval(opLabel.getSubOp())) ;
        else
            // No subop.
            push(TableFactory.createUnit()) ;
    }

    public void visit(OpList opList)
    {
        Table table = eval(opList.getSubOp()) ;
        table = evaluator.list(table) ;
        push(table) ;
    }

    public void visit(OpOrder opOrder)
    {
        Table table = eval(opOrder.getSubOp()) ;
        table = evaluator.order(table, opOrder.getConditions()) ;
        push(table) ;
    }

    public void visit(OpProject opProject)
    {
        Table table = eval(opProject.getSubOp()) ;
        table = evaluator.project(table, opProject.getVars()) ;
        push(table) ;
    }

    public void visit(OpDistinct opDistinct)
    {
        Table table = eval(opDistinct.getSubOp()) ;
        table = evaluator.distinct(table) ;
        push(table) ;
    }

    public void visit(OpReduced opReduced)
    {
        Table table = eval(opReduced.getSubOp()) ;
        table = evaluator.reduced(table) ;
        push(table) ;
    }

    public void visit(OpSlice opSlice)
    {
        Table table = eval(opSlice.getSubOp()) ;
        table = evaluator.slice(table, opSlice.getStart(), opSlice.getLength()) ;
        push(table) ;
    }

    public void visit(OpAssign opAssign)
    {
        Table table = eval(opAssign.getSubOp()) ;
        table = evaluator.assign(table, opAssign.getVarExprList()) ;
        push(table) ;
    }

    public void visit(OpGroupAgg opGroupAgg)
    {
        Table table = eval(opGroupAgg.getSubOp()) ;
        table = evaluator.groupBy(table, opGroupAgg.getGroupVars(), opGroupAgg.getAggregators()) ;
        push(table) ;
    }

    protected void push(Table table)  { stack.push(table) ; }
    protected Table pop()
    { 
        if ( stack.size() == 0 )
            ALog.warn(this, "Warning: pop: empty stack") ;
        return stack.pop() ;
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
