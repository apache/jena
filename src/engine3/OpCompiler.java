/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.core.ARQNotImplemented;
import com.hp.hpl.jena.query.core.ElementBasicGraphPattern;
import com.hp.hpl.jena.query.engine.Binding0;
import com.hp.hpl.jena.query.engine.BindingImmutable;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterDistinct;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterLimitOffset;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterProject;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterSort;
import com.hp.hpl.jena.query.engine1.plan.PlanBasicGraphPattern;
import com.hp.hpl.jena.query.engine2.op.*;
import com.hp.hpl.jena.query.engine2.table.TableUnit;

import engine3.iterators.*;

public class OpCompiler
{
    // TODO Get working like engine1 - then do OpLeftJoin
    // TODO Filter placement
    // TODO Sort out iterators

    static QueryIterator compile(Op op, ExecutionContext execCxt)
    {
        return compile(op, root(execCxt), execCxt) ;
    }
    
    static QueryIterator compile(Op op, QueryIterator qIter, ExecutionContext execCxt)
    {
        OpCompiler compiler = new OpCompiler(execCxt) ;
        QueryIterator q = compiler.compileOp(op, qIter) ;
        return q ;
    }

    ExecutionContext execCxt ;
    CompilerDispatch dispatcher = null ;

    private OpCompiler(ExecutionContext execCxt)
    { 
        this.execCxt = execCxt ;
        dispatcher = new CompilerDispatch(this) ;
    }

    private QueryIterator compileOp(Op op)
    {
        return compileOp(op, null) ;
    }

    private QueryIterator compileOp(Op op, QueryIterator input)
    {
        return dispatcher.compile(op, input) ;
    }
        
    QueryIterator compile(OpBGP opBGP, QueryIterator input)
    {
        ElementBasicGraphPattern bgp = new ElementBasicGraphPattern() ; 
        bgp.getTriples().addAll(opBGP.getPattern()) ;

        // Turn into a real PlanBasicGraphPattern (with property function sorting out)
        PlanElement planElt = PlanBasicGraphPattern.make(execCxt.getContext(), bgp) ;
        QueryIterator qIter = planElt.build(input, execCxt) ;
        return qIter ;
    }

    // Zero inputs.
    QueryIterator compile(OpQuadPattern quadPattern, QueryIterator input)
    {
        throw new ARQNotImplemented("compile/OpQuadPattern") ;
    }

    QueryIterator compile(OpJoin opJoin, QueryIterator input)
    {
        QueryIterator left = compileOp(opJoin.getLeft(), input) ;
        // Pass left into right for streamed evaluation
        QueryIterator right = compileOp(opJoin.getRight(), left) ;
        return right ;
    }


    QueryIterator compile(OpLeftJoin opLeftJoin, QueryIterator input)
    {
        QueryIterator left = compileOp(opLeftJoin.getLeft(), input) ;
        // Do an indexed substitute into the right if possible
        boolean canDoLinear = true ; 

        if ( canDoLinear )
        {
            // Pass left into right for substitution before right side evaluation.
            QueryIterator qIter = new QueryIterOptionalIndex(left, opLeftJoin.getRight(), opLeftJoin.getExpr(), execCxt) ;
            return  qIter ;
        }

        // Do it by sub-evaluation of left and right then left join.
        // Can be expensive if RHS returns a lot.
        // Do better? Allow substitution of the safe vars??

        QueryIterator right = compileOp(opLeftJoin.getRight(), root()) ;
        QueryIterator qIter = new QueryIterLeftJoin(left, right, opLeftJoin.getExpr(), execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpUnion opUnion, QueryIterator input)
    {
        List x = new ArrayList() ;
        x.add(opUnion.getLeft()) ;

        // Merge a casaded union
        while (opUnion.getRight() instanceof OpUnion)
        {
            Op opUnionNext = (OpUnion)opUnion.getRight() ;
            x.add(opUnionNext) ;
        }
        x.add(opUnion.getRight()) ;
        QueryIterator cIter = new QueryIterSplit(input, x, execCxt) ;
        return cIter ;
    }

    QueryIterator compile(OpFilter opFilter, QueryIterator input)
    {
        Op sub = opFilter.getSubOp() ;

        
        
        // Put filter in best place
        // Beware of 
        // { _:a ?p ?v .  FILTER(true) . [] ?q _:a }
        // making sure the right amount is dispatched as the BGP.
        // Only affects SPARQl extensions.
        
        if ( sub instanceof OpBGP )
        {}

        if ( sub instanceof OpQuadPattern )
        {}

        QueryIterator qIter = compileOp(sub, input) ;
        qIter = new QueryIterFilterExpr(qIter, opFilter.getExpr(), execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpGraph opGraph, QueryIterator input)
    { 
        return new QueryIterGraph(input, opGraph, execCxt) ;
    }
    
    QueryIterator compile(OpDatasetNames dsNames, QueryIterator input)
    { throw new ARQNotImplemented("OpDatasetNames") ; }

    QueryIterator compile(OpTable opTable, QueryIterator input)
    { 
        // Works for all the wrong reasons!
        // opTable is UnitTable.
        
        if ( opTable.getTable() instanceof TableUnit )
            return input ;
        throw new ARQNotImplemented("OpTable: no tunit table") ;
        //return opTable.getTable().iterator(execCxt) ;
    }

    QueryIterator compile(OpExt opExt, QueryIterator input)
    { throw new ARQNotImplemented("OpExt") ; }

    QueryIterator compile(OpOrder opOrder, QueryIterator input)
    { 
        QueryIterator qIter = compileOp(opOrder.getSubOp(), input) ;
        qIter = new QueryIterSort(qIter, opOrder.getConditions(), execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpProject opProject, QueryIterator input)
    {
        QueryIterator  qIter = compileOp(opProject.getSubOp(), input) ;
        qIter = new QueryIterProject(qIter, opProject.getVars(), execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpSlice opSlice, QueryIterator input)
    { 
        QueryIterator qIter = compileOp(opSlice.getSubOp(), input) ;
        qIter = new QueryIterLimitOffset(qIter, opSlice.getStart(), opSlice.getLength(), execCxt) ;
        return qIter ;
        }

    QueryIterator compile(OpDistinct opDistinct, QueryIterator input)
    {
        QueryIterator qIter = compileOp(opDistinct.getSubOp(), input) ;
        qIter = BindingImmutable.create(opDistinct.getVars(), qIter, execCxt) ;
        qIter = new QueryIterDistinct(qIter, execCxt) ;
        return qIter ;
    }

    static private QueryIterator root(ExecutionContext execCxt)
    {
        return new QueryIterSingleton(new Binding0(), execCxt) ;
    }

    private QueryIterator root()
    { return root(execCxt) ; }
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