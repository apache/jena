/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterFilterExpr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.VarUtils;

public class FilterPlacement
{
    // Broken out to keep OpCompiler more manageable.
    private OpCompiler compiler ;
    private ExecutionContext execCxt ;
    
    static final Symbol safePlacement = ARQConstants.allocSymbol("safeFilterPlacement") ;
    
    boolean doSafePlacement = true ;

    // Put filter in best place
    // Beware of 
    // { _:a ?p ?v .  FILTER(true) . [] ?q _:a }
    // making sure the right amount is dispatched as the BGP.
    // Only affects SPARQL extensions.

    public FilterPlacement(OpCompiler compiler, ExecutionContext execCxt)
    {
        this.compiler = compiler ;
        this.execCxt = execCxt ;
        doSafePlacement = execCxt.getContext().isTrue(safePlacement) ;
    }
    
    // --------------------------------
    // Basic patterns
    
    public QueryIterator placeFiltersBGP(ExprList exprs, BasicPattern pattern, QueryIterator input)
    {
        QueryIterator qIter = placeFiltersWorker(exprs, pattern, input) ;
        // any remaining filters
        qIter = buildFilter(exprs, qIter) ;
        return qIter ;
    }

    private QueryIterator placeFiltersWorker(ExprList exprs, BasicPattern pattern, QueryIterator input)
    {
        BasicPattern accPattern = new BasicPattern() ;
        Set patternVarsScope = new HashSet() ;
        QueryIterator qIter = input ;
        
        //OpVars.patternVars(op) ;
        
        qIter = insertAnyFilter(exprs, patternVarsScope, accPattern, qIter) ;
        if ( qIter == null )
            qIter = input ;
        
        for ( Iterator iter = pattern.iterator() ; iter.hasNext() ; )
        {
            Triple triple = (Triple)iter.next() ;
            
            accPattern.add(triple) ;
            VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
            
            QueryIterator qIter2 = insertAnyFilter(exprs, patternVarsScope, accPattern, qIter) ;
            if ( qIter2 != null )
            {
                accPattern = new BasicPattern() ;
                qIter = qIter2 ;
            }
        }
        
        // Remaining triples.
        qIter = StageBuilder.compile(accPattern, qIter, execCxt) ;
        return qIter ;
    }
    
    private QueryIterator insertAnyFilter(ExprList exprs, Set patternVarsScope, BasicPattern accPattern, QueryIterator qIter)
    {
        boolean doneSomething = false ;
        for ( Iterator iter = exprs.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            // Cache
            Set exprVars = expr.getVarsMentioned() ;
            if ( patternVarsScope.containsAll(exprVars) )
            {
                QueryIterator qIter2 = buildPatternFilter(expr, accPattern, qIter) ;
                iter.remove() ;
                qIter = qIter2 ;
                doneSomething = true ;
            }
        }
        
        return ( doneSomething ? qIter : null ) ;
    }

    // --------------------------------
    // Placement in joins.
    
    public QueryIterator placeFiltersJoin(ExprList exprs, List ops, QueryIterator input)
    {
        Set varScope = new HashSet() ;
        QueryIterator qIter = input ;
        
        qIter = insertAnyFilter(exprs, varScope, qIter) ;
        if ( qIter == null )
            qIter = input ;
        
        for ( Iterator iter = ops.iterator() ; iter.hasNext() ; )
        {
            Op op = (Op)iter.next() ;
            
            // Push into BGPs if possible.
            if ( op instanceof OpBGP )
            {
                OpBGP bgp = (OpBGP)op ;
                BasicPattern pattern = bgp.getPattern() ;
                qIter = placeFiltersWorker(exprs, pattern, qIter) ;
            }
            else
                qIter = compiler.compileOp(op, qIter) ;
            
            OpVars.patternVars(op, varScope) ;
            qIter = insertAnyFilter(exprs, varScope, qIter) ;
        }
        // Insert any remaining filter expressions regardless.
        qIter = buildFilter(exprs, qIter) ;
        return qIter ;
    }

    private QueryIterator insertAnyFilter(ExprList exprs, Set varScope, QueryIterator qIter)
    {
        for ( Iterator iter = exprs.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            // Cache
            Set exprVars = expr.getVarsMentioned() ;
            // Insert filter if satisified.
            if ( varScope.containsAll(exprVars) )
            {
                qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
                iter.remove() ;
            }
        }
        return qIter ;
    }

    // ----------------
    
    // Build a series of filters around a compile Op
    public QueryIterator buildOpFilter(ExprList exprs, Op sub, QueryIterator input)
    {
        QueryIterator qIter = compiler.compileOp(sub, input) ;

        for ( Iterator iter = exprs.iterator() ; iter.hasNext(); )
        {
            Expr expr = (Expr)iter.next() ;
            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        }
        return qIter ;
    }
    
    private QueryIterator buildOpFilter(Expr expr, Op op, QueryIterator input)
    {
        QueryIterator qIter = compiler.compileOp(op, input) ;
        qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        return qIter ;
    }

    // Build a series of filters around a BasicPattern
    private QueryIterator buildPatternFilter(Expr expr, BasicPattern pattern, QueryIterator input)
    {
        QueryIterator qIter = StageBuilder.compile(pattern, input, execCxt) ;
        qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        return qIter ;
    }

    // Insert filters around a query iterator.
    private QueryIterator buildFilter(ExprList exprs, QueryIterator qIter)
    {
        if ( exprs.isEmpty() )
            return qIter ;
    
        for ( Iterator iter = exprs.iterator() ; iter.hasNext() ; )
        {
            Expr expr = (Expr)iter.next() ;
            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        }
        return qIter ;
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