/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.core.ARQNotImplemented;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.iterator.QueryIterFilterExpr;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.OpBGP;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.util.Symbol;
import com.hp.hpl.jena.query.util.VarUtils;

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
    
    public QueryIterator placeFilter(List exprs, OpBGP sub, QueryIterator input)
    {
        if ( doSafePlacement )
            return safe(exprs, sub, input) ;

        if ( exprs.size() != 1 )
            // TODO relax this
            throw new ARQNotImplemented("Multiple filters") ;   
        
        Expr expr = (Expr)exprs.get(0) ;
        
        // Ignores special implications of dispatched blank nodes.
        // TODO Filter placement WRT bnode variables.
        Set exprVars = expr.getVarsMentioned() ;    // ExprVars.getVarsMentioned(expr) ;
        
        BasicPattern pattern = sub.getPattern() ;
        Set patternVars = new HashSet() ;
        BasicPattern accPattern = new BasicPattern() ;
        
        for ( Iterator iter = pattern.iterator() ; iter.hasNext() ; )
        {
            // Can we insert here?
            // If the test is second, then FILTER(true) goes after the first triple
            // which is bizaar even if correct.
            if ( patternVars.containsAll(exprVars) )
            {
                // Put all remaining triples in a separate BasicPattern 
                BasicPattern trailer = new BasicPattern() ;
                for ( ; iter.hasNext() ; )
                    trailer.add((Triple)iter.next()) ;
                
                // Put the filter here
                return build(expr, accPattern, trailer, input) ;
            }
            
            // No - move on one triple.
            Triple triple = (Triple)iter.next() ;
//            if ( ! iter.hasNext() )     // Skip last - no point.
//                break ;
            
            accPattern.add(triple) ;
            VarUtils.addVarsFromTriple(patternVars, triple) ;
        }
        
        // Nothing better.
        return safe(exprs, sub, input) ;
    }

    public QueryIterator safe(List exprs, Op sub, QueryIterator input)
    {
        QueryIterator qIter = compiler.compileOp(sub, input) ;

        for ( Iterator iter = exprs.iterator() ; iter.hasNext(); )
        {
            Expr expr = (Expr)iter.next() ;
            qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        }
        return qIter ;
    }
    
    public QueryIterator safe(Expr expr, Op sub, QueryIterator input)
    {
        QueryIterator qIter = compiler.compileOp(sub, input) ;
        qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        return qIter ;
    }

    private QueryIterator build(Expr expr, BasicPattern first, BasicPattern second, QueryIterator input)
    {
        Op op1 = new OpBGP(first) ;

        if ( second.size() == 0 )
            return safe(expr, op1, input) ;
        
        Op op2 = new OpBGP(second) ;
        
        QueryIterator qIter = compiler.compileOp(op1, input) ;
        qIter = new QueryIterFilterExpr(qIter, expr, execCxt) ;
        qIter = compiler.compileOp(op2, qIter) ;
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