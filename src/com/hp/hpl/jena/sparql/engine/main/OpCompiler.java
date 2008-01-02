/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.*;
import com.hp.hpl.jena.sparql.engine.main.iterator.*;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprBuild;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.procedure.ProcEval;
import com.hp.hpl.jena.sparql.procedure.Procedure;

import com.hp.hpl.jena.query.QueryExecException;


public class OpCompiler
{
    // And filter placement in LeftJoins?
    // Is this part of a more general algorithm of pushing the filter down
    // when the vars are known to be fixed?
    
    // TODO property function detemination by general tree rewriting - BGP special first.
    //  By general BGP rewriting.  Stages.
    //  (General tree rewrite is "Op => Op")
    //   OpExtBase requires eval() but need better extensibility?
    
    public static QueryIterator compile(Op op, ExecutionContext execCxt)
    {
        return compile(op, root(execCxt), execCxt) ;
    }
    
    public static QueryIterator compile(Op op, QueryIterator qIter, ExecutionContext execCxt)
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
        return StageBuilder.compile(pattern, input, execCxt) ;
    }

    QueryIterator compile(OpQuadPattern quadPattern, QueryIterator input)
    {
        if ( false )
        {
            if ( quadPattern.isDefaultGraph() )
            {
                // Easy case.
                OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
                return compile(opBGP, input) ;  
            }
        }        
        // Turn into a OpGraph/OpBGP.
        throw new ARQNotImplemented("compile/OpQuadPattern") ;
    }

    QueryIterator compile(OpProcedure opProc, QueryIterator input)
    {
        Procedure procedure = ProcEval.build(opProc, execCxt) ;
        QueryIterator qIter = compileOp(opProc.getSubOp(), input) ;
        // Delay until query startes executing.
        return new QueryIterProcedure(qIter, procedure, execCxt) ;
    }

    QueryIterator compile(OpJoin opJoin, QueryIterator input)
    {
        // TODO Consider building join lists and place filters carefully.  
        // Place by fixed - if none present, place by optional
        
        // Look one level in for any filters with out-of-scope variables.
        boolean canDoLinear = JoinClassifier.isLinear(opJoin) ;

        if ( canDoLinear )
            // Streamed evaluation
            return stream(opJoin.getLeft(), opJoin.getRight(), input) ;
        
        // Input may be null?
        // Can't do purely indexed (a filter referencing a variable out of scope is in the way)
        // To consider: partial substitution for improved performance (but does it occur for real?)
        
        QueryIterator left = compileOp(opJoin.getLeft(), input) ;
        QueryIterator right = compileOp(opJoin.getRight(), root()) ;
        QueryIterator qIter = new QueryIterJoin(left, right, execCxt) ;
        return qIter ;
        // Worth doing anything about join(join(..))?  Probably not.
    }

    QueryIterator compile(OpStage opStage, QueryIterator input)
    {
        return stream(opStage.getLeft(), opStage.getRight(), input) ;
    }
    
    // Pass iterator from left directly into the right.
    private final QueryIterator stream(Op opLeft, Op opRight, QueryIterator input)
    {
        QueryIterator left = compileOp(opLeft, input) ;
        QueryIterator right = compileOp(opRight, left) ;
        return right ;
    }
    
    QueryIterator compile(OpLeftJoin opLeftJoin, QueryIterator input)
    {
        ExprList exprs = opLeftJoin.getExprs() ;
        if ( exprs != null )
            exprs.prepareExprs(execCxt.getContext()) ;

        QueryIterator left = compileOp(opLeftJoin.getLeft(), input) ;
        // Do an indexed substitute into the right if possible.
        boolean canDoLinear = LeftJoinClassifier.isLinear(opLeftJoin) ;
        
        if ( canDoLinear )
        {
            // Pass left into right for substitution before right side evaluation.
            QueryIterator qIter = new QueryIterOptionalIndex(left, opLeftJoin.getRight(), exprs, execCxt) ;
            return  qIter ;
        }

        // Do it by sub-evaluation of left and right then left join.
        // Can be expensive if RHS returns a lot.
        // To consider: partial substitution for improved performance (but does it occur for real?)

        QueryIterator right = compileOp(opLeftJoin.getRight(), root()) ;
        QueryIterator qIter = new QueryIterLeftJoin(left, right, exprs, execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpDiff opDiff, QueryIterator input)
    { 
        QueryIterator left = compileOp(opDiff.getLeft(), input) ;
        QueryIterator right = compileOp(opDiff.getRight(), root()) ;
        return new QueryIterDiff(left, right, execCxt) ;
    }
    
    QueryIterator compile(OpUnion opUnion, QueryIterator input)
    {
        List x = new ArrayList() ;
        x.add(opUnion.getLeft()) ;

        // Merge a casaded union
        while (opUnion.getRight() instanceof OpUnion)
        {
            Op opUnionNext = opUnion.getRight() ;
            x.add(opUnionNext) ;
        }
        x.add(opUnion.getRight()) ;
        QueryIterator cIter = new QueryIterUnion(input, x, execCxt) ;
        return cIter ;
    }

    QueryIterator compile(OpFilter opFilter, QueryIterator input)
    {
        ExprList exprs = opFilter.getExprs() ;
        exprs.prepareExprs(execCxt.getContext()) ;
        
        Op base = opFilter.getSubOp() ;
        
        if ( base instanceof OpBGP )
            // Uncompiled => unsplit
            return filterPlacement.placeFiltersBGP(exprs, ((OpBGP)base).getPattern(), input) ;

        if ( base instanceof OpStage )
            return filterPlacement.placeFiltersStage(exprs, (OpStage)base, input) ;
        
        if ( base instanceof OpGraph )
        {}

//        if ( base instanceof OpQuadPattern )
//            return filterPlacement.placeFilter(opFilter.getExpr(), (OpQuadPattern)base, input) ;
        
        // Tidy up.
        if ( base instanceof OpJoin )
            return filterPlacement.placeFiltersJoin(exprs, (OpJoin)base, input) ;
        
        // There must be a better way.
        if ( base instanceof OpLeftJoin )
        {
            // Can push in if used only on the LHS 
        }
        
        if ( base instanceof OpUnion )
        {}

        // Nothing special.
        return filterPlacement.buildOpFilter(exprs, base, input) ;
    }

    private void prepareExprs(ExprList exprs)
    {
        for ( Iterator iter = exprs.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            ExprWalker.walk(new ExprBuild(execCxt.getContext()), expr) ;
        }
    }
    
    QueryIterator compile(OpGraph opGraph, QueryIterator input)
    { 
        return new QueryIterGraph(input, opGraph, execCxt) ;
    }
    
    QueryIterator compile(OpService opService, QueryIterator input)
    {
        return new QueryIterService(input, opService, execCxt) ;
    }
    
    QueryIterator compile(OpDatasetNames dsNames, QueryIterator input)
    { 
        if ( true ) throw new ARQNotImplemented("OpDatasetNames") ;
        
        // Augment (join) iterator with a table.
        Table t = null ;
        Op left = null ; 
        Op right = OpTable.create(t) ;
        Op opJoin = OpJoin.create(left, right) ;
        return compileOp(opJoin , input) ;    //??
    }

    QueryIterator compile(OpTable opTable, QueryIterator input)
    { 
//        if ( input instanceof QueryIteratorBase )
//        {
//            String x = ((QueryIteratorBase)input).debug();
//            System.out.println(x) ;
//        }
//        
        if ( opTable.isJoinIdentity() )
            return input ;
        if ( input instanceof QueryIterRoot )
        {
            input.close() ;
            return opTable.getTable().iterator(execCxt) ;
        }
        //throw new ARQNotImplemented("Not identity table") ;
        QueryIterator qIterT = opTable.getTable().iterator(execCxt) ;
        //QueryIterator qIterT = root() ;
        QueryIterator qIter = new QueryIterJoin(input, qIterT, execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpExt opExt, QueryIterator input)
    { 
        if ( opExt instanceof OpExtMain )
        {
            OpExtMain op = (OpExtMain)opExt ;
            return op.eval(input, execCxt) ;
        }
        
        throw new QueryExecException("Encountered unsupport OpExt: "+opExt.getName()) ;
    }

    QueryIterator compile(OpNull opNull, QueryIterator input)
    {
        // Loose the input.
        input.close() ;
        return new QueryIterNullIterator(execCxt) ;
    }

    QueryIterator compile(OpList opList, QueryIterator input)
    {
        return compileOp(opList.getSubOp(), input) ;
    }
    
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
    
    QueryIterator compile(OpGroupAgg opGroupAgg, QueryIterator input)
    { 
        QueryIterator qIter = compileOp(opGroupAgg.getSubOp(), input) ;
        qIter = new QueryIterGroup(qIter, opGroupAgg.getGroupVars(), opGroupAgg.getAggregators(), execCxt) ;
        return qIter ;
    }
    
    QueryIterator compile(OpDistinct opDistinct, QueryIterator input)
    {
        QueryIterator qIter = compileOp(opDistinct.getSubOp(), input) ;
        qIter = new QueryIterDistinct(qIter, execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpReduced opReduced, QueryIterator input)
    {
        QueryIterator qIter = compileOp(opReduced.getSubOp(), input) ;
        qIter = new QueryIterReduced(qIter, execCxt) ;
        return qIter ;
    }

    QueryIterator compile(OpAssign opAssign, QueryIterator input)
    {
        QueryIterator qIter = compileOp(opAssign.getSubOp(), input) ;
        qIter = new QueryIterExtend(qIter, opAssign.getVarExprList(), execCxt) ;
        return qIter ;
    }

    static QueryIterator root(ExecutionContext execCxt)
    {
        return QueryIterRoot.create(execCxt) ;
    }

    private QueryIterator root()
    { return root(execCxt) ; }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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