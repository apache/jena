/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

/**  Class to provide type-safe compile() dispatch using the visitor support of Op */ 

class CompilerDispatch implements OpVisitor
{
    private static Log log = LogFactory.getLog(CompilerDispatch.class) ;
    
    private Stack stack = new Stack() ;
    private OpCompiler opCompiler ;
    
    CompilerDispatch(OpCompiler compiler)
    {
        opCompiler = compiler ;
    }
    
    QueryIterator compile(Op op, QueryIterator input)
    {
        push(input) ;
        int x = stack.size() ; 
        op.visit(this) ;
        int y = stack.size() ;
        if ( x != y )
            log.warn("Possible stack misalignment") ;
        QueryIterator qIter = pop() ;
        return qIter ;
    }

    public void visit(OpBGP opBGP)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opBGP, input) ;
        push(qIter) ;
    }

    public void visit(OpQuadPattern quadPattern)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(quadPattern, input) ;
        push(qIter) ;
    }

    public void visit(OpJoin opJoin)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opJoin, input) ;
        push(qIter) ;
    }

    public void visit(OpLeftJoin opLeftJoin)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opLeftJoin, input) ;
        push(qIter) ;
    }

    public void visit(OpDiff opDiff)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opDiff, input) ;
        push(qIter) ;
    }

    public void visit(OpUnion opUnion)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opUnion, input) ;
        push(qIter) ;
    }

    public void visit(OpFilter opFilter)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opFilter, input) ;
        push(qIter) ;
    }

    public void visit(OpGraph opGraph)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opGraph, input) ;
        push(qIter) ;
    }

    public void visit(OpService opService)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opService, input) ;
        push(qIter) ;
    }

    public void visit(OpDatasetNames dsNames)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(dsNames, input) ;
        push(qIter) ;
    }

    public void visit(OpTable opTable)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opTable, input) ;
        push(qIter) ;
    }

    public void visit(OpExt opExt)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opExt, input) ;
        push(qIter) ;
    }

    public void visit(OpNull opNull)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opNull, input) ;
        push(qIter) ;
    }

    public void visit(OpList opList)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opList, input) ;
        push(qIter) ;
    }

    public void visit(OpOrder opOrder)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opOrder, input) ;
        push(qIter) ;
    }

    public void visit(OpProject opProject)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opProject, input) ;
        push(qIter) ;
    }

    public void visit(OpDistinct opDistinct)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opDistinct, input) ;
        push(qIter) ;
    }

    public void visit(OpReduced opReduced)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opReduced, input) ;
        push(qIter) ;
    }

    public void visit(OpSlice opSlice)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opCompiler.compile(opSlice, input) ;
        push(qIter) ;
    }
    
    public void visit(OpGroupAgg opGroupAgg)
    { throw new ARQNotImplemented("Compile OpGroupAgg") ; }
    
    private void push(QueryIterator qIter)  { stack.push(qIter) ; }
    private QueryIterator pop()
    { 
        if ( stack.size() == 0 )
            log.warn("Warning: pop: empty stack") ;
        return (QueryIterator)stack.pop() ;
    }
}
/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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