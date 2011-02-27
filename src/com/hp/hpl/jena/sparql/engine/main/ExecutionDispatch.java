/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.Stack ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import org.openjena.atlas.logging.Log ;

/**  Class to provide type-safe execution dispatch using the visitor support of Op */ 

class ExecutionDispatch implements OpVisitor
{
    private Stack<QueryIterator> stack = new Stack<QueryIterator>() ;
    private OpExecutor opExecutor ;
    
    ExecutionDispatch(OpExecutor exec)
    {
        opExecutor = exec ;
    }
    
    QueryIterator exec(Op op, QueryIterator input)
    {
        push(input) ;
        int x = stack.size() ; 
        op.visit(this) ;
        int y = stack.size() ;
        if ( x != y )
            Log.warn(this, "Possible stack misalignment") ;
        QueryIterator qIter = pop() ;
        return qIter ;
    }

    public void visit(OpBGP opBGP)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opBGP, input) ;
        push(qIter) ;
    }

    public void visit(OpQuadPattern quadPattern)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(quadPattern, input) ;
        push(qIter) ;
    }

    public void visit(OpTriple opTriple)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opTriple, input) ;
        push(qIter) ;
    }

    public void visit(OpPath opPath)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opPath, input) ;
        push(qIter) ;
    }

    public void visit(OpProcedure opProc)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opProc, input) ;
        push(qIter) ;
    }

    public void visit(OpPropFunc opPropFunc)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opPropFunc, input) ;
        push(qIter) ;
    }

    public void visit(OpJoin opJoin)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opJoin, input) ;
        push(qIter) ;
    }

    public void visit(OpSequence opSequence)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opSequence, input) ;
        push(qIter) ;
    }

    public void visit(OpDisjunction opDisjunction)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opDisjunction, input) ;
        push(qIter) ;
    }
    

    public void visit(OpLeftJoin opLeftJoin)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opLeftJoin, input) ;
        push(qIter) ;
    }

    public void visit(OpDiff opDiff)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opDiff, input) ;
        push(qIter) ;
    }

    public void visit(OpMinus opMinus)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opMinus, input) ;
        push(qIter) ;
    }

    public void visit(OpUnion opUnion)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opUnion, input) ;
        push(qIter) ;
    }

    public void visit(OpConditional opCondition)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opCondition, input) ;
        push(qIter) ;
    }

    public void visit(OpFilter opFilter)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opFilter, input) ;
        push(qIter) ;
    }

    public void visit(OpGraph opGraph)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opGraph, input) ;
        push(qIter) ;
    }

    public void visit(OpService opService)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opService, input) ;
        push(qIter) ;
    }

    public void visit(OpDatasetNames dsNames)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(dsNames, input) ;
        push(qIter) ;
    }

    public void visit(OpTable opTable)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opTable, input) ;
        push(qIter) ;
    }

    public void visit(OpExt opExt)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opExt, input) ;
        push(qIter) ;
    }

    public void visit(OpNull opNull)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opNull, input) ;
        push(qIter) ;
    }

    public void visit(OpLabel opLabel)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opLabel, input) ;
        push(qIter) ;
    }

    public void visit(OpList opList)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opList, input) ;
        push(qIter) ;
    }

    public void visit(OpOrder opOrder)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opOrder, input) ;
        push(qIter) ;
    }

    public void visit(OpProject opProject)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opProject, input) ;
        push(qIter) ;
    }

    public void visit(OpDistinct opDistinct)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opDistinct, input) ;
        push(qIter) ;
    }

    public void visit(OpReduced opReduced)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opReduced, input) ;
        push(qIter) ;
    }

    public void visit(OpAssign opAssign)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opAssign, input) ;
        push(qIter) ;
    }
    
    public void visit(OpExtend opExtend)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opExtend, input) ;
        push(qIter) ;
    }
    
    public void visit(OpSlice opSlice)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opSlice, input) ;
        push(qIter) ;
    }
    
    public void visit(OpGroup opGroup)
    { 
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opGroup, input) ;
        push(qIter) ;
    }
    
    public void visit(OpTopN opTop)
    { 
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opTop, input) ;
        push(qIter) ;
    }

    private void push(QueryIterator qIter)  { stack.push(qIter) ; }
    private QueryIterator pop()
    { 
        if ( stack.size() == 0 )
            Log.warn(this, "Warning: pop: empty stack") ;
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