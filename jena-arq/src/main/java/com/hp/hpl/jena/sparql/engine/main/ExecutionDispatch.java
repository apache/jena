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

package com.hp.hpl.jena.sparql.engine.main;

import java.util.ArrayDeque ;
import java.util.Deque ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;

/**  Class to provide type-safe execution dispatch using the visitor support of Op */ 

class ExecutionDispatch implements OpVisitor
{
    private Deque<QueryIterator> stack = new ArrayDeque<>() ;
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

    @Override
    public void visit(OpBGP opBGP)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opBGP, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpQuadPattern quadPattern)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(quadPattern, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpQuadBlock quadBlock)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(quadBlock, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpTriple opTriple)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opTriple, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpQuad opQuad)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opQuad, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpPath opPath)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opPath, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpProcedure opProc)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opProc, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpPropFunc opPropFunc)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opPropFunc, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpJoin opJoin)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opJoin, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpSequence opSequence)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opSequence, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpDisjunction opDisjunction)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opDisjunction, input) ;
        push(qIter) ;
    }
    

    @Override
    public void visit(OpLeftJoin opLeftJoin)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opLeftJoin, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpDiff opDiff)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opDiff, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpMinus opMinus)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opMinus, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpUnion opUnion)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opUnion, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpConditional opCondition)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opCondition, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpFilter opFilter)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opFilter, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpGraph opGraph)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opGraph, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpService opService)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opService, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpDatasetNames dsNames)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(dsNames, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpTable opTable)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opTable, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpExt opExt)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opExt, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpNull opNull)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opNull, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpLabel opLabel)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opLabel, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpList opList)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opList, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpOrder opOrder)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opOrder, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpProject opProject)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opProject, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpDistinct opDistinct)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opDistinct, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpReduced opReduced)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opReduced, input) ;
        push(qIter) ;
    }

    @Override
    public void visit(OpAssign opAssign)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opAssign, input) ;
        push(qIter) ;
    }
    
    @Override
    public void visit(OpExtend opExtend)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opExtend, input) ;
        push(qIter) ;
    }
    
    @Override
    public void visit(OpSlice opSlice)
    {
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opSlice, input) ;
        push(qIter) ;
    }
    
    @Override
    public void visit(OpGroup opGroup)
    { 
        QueryIterator input = pop() ;
        QueryIterator qIter = opExecutor.execute(opGroup, input) ;
        push(qIter) ;
    }
    
    @Override
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
