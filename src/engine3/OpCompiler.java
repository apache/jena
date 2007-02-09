/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.core.ARQNotImplemented;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding0;
import com.hp.hpl.jena.query.engine.binding.BindingImmutable;
import com.hp.hpl.jena.query.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.query.engine2.op.*;
import com.hp.hpl.jena.query.engine2.table.TableUnit;

import engine3.iterators.*;

public class OpCompiler
{
    /* XXX Tests needed:
     *  { :x :p ?v . OPTIONAL { FILTER(?v = 1) } } -- Algebra test needed
     *  { :x :p ?v . { :y :q ?v OPTIONAL { FILTER(?v = 1) } } } -- Algebra test needed
     *  Filter placement (internal tests)
     *  Classifier J and LJ - internal
     */
    // And filter placement in groups?
    // TODO property function detemination by general tree rewriting
    //   precursor to pattern replacement?
    //     But that's "Op => Op" so need extension Ops
    //   OpExtBase requires eval() but need better extensibility?
    //   Special case is bottom nodes - adapters and wrappers - and that is OpBGP
    //   ==> Make OpBGP case special and easy
    //    Stages.
    // TODO OpFilter to have a single expression and allow nesting.
    //     E_logicalAnd != OpFilter(OpFilter()) 
    // TODO Non-reorganising AlgebraCompiler mode.
    //
    //
    // Remove "Op" from classes?
    // Package structure:
    //   .core => .core,  .syntax for Element* .describe => .core
    //   .engine, includes the reference engine or .engine.ref/.engine.algebra/
    //   .engine.http or .engineHTTP ?
    //   .engine.engine => normal engine .engine.impl, .engin/std
    //   .engine.engineplan => old engine
    //   .shared
    // Compiler options and globals ; 
    //   Compiler class like ARQConstants or just use ARQ.
    //   Delete .extension

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

    private ExecutionContext execCxt ;
    private CompilerDispatch dispatcher = null ;
    private FilterPlacement filterPlacement ;

    private OpCompiler(ExecutionContext execCxt)
    { 
        this.execCxt = execCxt ;
        dispatcher = new CompilerDispatch(this) ;
        filterPlacement = new FilterPlacement(this, execCxt);
    }

    QueryIterator compileOp(Op op)
    {
        return compileOp(op, null) ;
    }

    QueryIterator compileOp(Op op, QueryIterator input)
    {
        return dispatcher.compile(op, input) ;
    }
        
    QueryIterator compile(OpBGP opBGP, QueryIterator input)
    {
        BasicPattern pattern = opBGP.getPattern() ;
        return StageProcessor.compile(pattern, input, execCxt) ;
    }

    QueryIterator compile(OpQuadPattern quadPattern, QueryIterator input)
    {
        // Turn into a OpGraph/OpBGP.
        // First, separate out OpBGP's dependency on ElementBasicGraphPattern.
        throw new ARQNotImplemented("compile/OpQuadPattern") ;
    }

    QueryIterator compile(OpJoin opJoin, QueryIterator input)
    {
        // Look one level in for any filters with out-of-scope variables.
        boolean canDoLinear = JoinClassifier.isLinear(opJoin) ;
        
        QueryIterator left = compileOp(opJoin.getLeft(), input) ;
        
        if ( canDoLinear )
        {
            // Pass left into right for streamed evaluation
            QueryIterator right = compileOp(opJoin.getRight(), left) ;
            return right ;
        }
        
        // Input may be null?
        // Can't do purely indexed (a filter referencing a variable out of scope is in the way)
        // To consider: partial substitution for improved performance (but does it occur for real?)
        QueryIterator right = compileOp(opJoin.getRight(), root()) ;
        QueryIterator qIter = new QueryIterJoin(left, right, execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpLeftJoin opLeftJoin, QueryIterator input)
    {
        QueryIterator left = compileOp(opLeftJoin.getLeft(), input) ;
        // Do an indexed substitute into the right if possible.
        boolean canDoLinear = LeftJoinClassifier.isLinear(opLeftJoin) ;

        if ( canDoLinear )
        {
            // Pass left into right for substitution before right side evaluation.
            QueryIterator qIter = new QueryIterOptionalIndex(left, opLeftJoin.getRight(), opLeftJoin.getExpr(), execCxt) ;
            return  qIter ;
        }

        // Do it by sub-evaluation of left and right then left join.
        // Can be expensive if RHS returns a lot.
        // To consider: partial substitution for improved performance (but does it occur for real?)

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
        Op base = opFilter ;
        
        // Tidyy: Extract nested filters to find all expressions and the base op. 
        List exprs = new ArrayList() ;
        while ( base instanceof OpFilter )
        {
            OpFilter f = (OpFilter)base ;
            exprs.add(f.getExpr()) ;
            base = f.getSubOp() ;
        }

        if ( base instanceof OpBGP )
            return filterPlacement.placeFilters(exprs, ((OpBGP)base).getPattern(), input) ;

//        if ( sub instanceof OpQuadPattern )
//            return filterPlacement.placeFilter(opFilter.getExpr(), (OpQuadPattern)base, input) ;
        
        // Tidy up.
        if ( base instanceof OpJoin )
        {
            // Look for a join chain (i.e. the left is also a join)
            List joinElts = new ArrayList() ;
            joins(base, joinElts) ;
            //PrintUtils.printList(System.out, joinElts, ":") ;
            //System.out.println(joinElts) ;
            return filterPlacement.placeFilters(exprs, joinElts, input) ;
            // And compress BGPs? Blank node caveats!
            // Do as a separate transform.
        }

        return filterPlacement.buildOpFilter(exprs, base, input) ;
    }

    private static void joins(Op base, List joinElts)
    {
        while ( base instanceof OpJoin )
        {
            OpJoin join = (OpJoin)base ;
            Op right = join.getRight() ; 
            joins(right, joinElts) ;
            base = join.getLeft() ;
        }
        // Not a join - add it.
        joinElts.add(base) ;
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
        throw new ARQNotImplemented("OpTable: not unit table") ;
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
        qIter = new QueryIterSlice(qIter, opSlice.getStart(), opSlice.getLength(), execCxt) ;
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