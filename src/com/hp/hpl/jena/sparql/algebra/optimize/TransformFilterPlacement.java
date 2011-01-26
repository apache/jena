/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

/* Contains code submitted in email to jena-user@incubator.apache.org 
 * so software grant and relicensed under the Apache Software License. 
 *    transformFilterConditional
 */

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.util.VarUtils ;

/** Rewrite an algebra expression to put filters as close to their bound variables in a BGP.
 *  Works on (filter (BGP ...) ) */

public class TransformFilterPlacement extends TransformCopy
{
    static boolean doFilterPlacement = true ;
    
    public static Op transform(ExprList exprs, BasicPattern bgp)
    {
        if ( ! doFilterPlacement )
            return OpFilter.filter(exprs, new OpBGP(bgp)) ;
        
        Op op = transformFilterBGP(exprs, new HashSet<Var>(), bgp) ;
        // Remaining filters? e.g. ones mentioning var s not used anywhere. 
        op = buildFilter(exprs, op) ;
        return op ;
    }
    
    public static Op transform(ExprList exprs, Node graphNode, BasicPattern bgp)
    {
        if ( ! doFilterPlacement )
            return OpFilter.filter(exprs, new OpQuadPattern(graphNode, bgp)) ;
        Op op =  transformFilterQuadPattern(exprs, new HashSet<Var>(), graphNode, bgp);
        op = buildFilter(exprs, op) ;
        return op ;
    }
    

    public TransformFilterPlacement()
    { }
    
    @Override
    public Op transform(OpFilter opFilter, Op x)
    {
        if ( ! doFilterPlacement )
            return super.transform(opFilter, x) ;
        
        // Destructive use of exprs - copy it.
        ExprList exprs = new ExprList(opFilter.getExprs()) ;
        Set<Var> varsScope = new HashSet<Var>() ;
        
        Op op = transform(exprs, varsScope, x) ;
        if ( op == x )
            // Didn't do anything.
            return super.transform(opFilter, x) ;
        
        // Remaining exprs
        op = buildFilter(exprs, op) ;
        return op ;
    }
        
    private static Op transform(ExprList exprs, Set<Var> varsScope, Op x)
    {
        // TODO Dispatch by visitor
        if ( x instanceof OpBGP )
            return transformFilterBGP(exprs, varsScope, (OpBGP)x) ;

        if ( x instanceof OpSequence )
            return transformFilterSequence(exprs, varsScope, (OpSequence)x) ;
        
        if ( x instanceof OpQuadPattern )
            return transformFilterQuadPattern(exprs, varsScope, (OpQuadPattern)x) ;
        
        if ( x instanceof OpSequence )
            return transformFilterSequence(exprs, varsScope, (OpSequence)x) ;
        
        if ( x instanceof OpConditional )
            return transformFilterConditional(exprs, varsScope, (OpConditional)x) ;
        
        // Not special - advance the variable scope tracking. 
        OpVars.patternVars(x, varsScope) ;
        return x ;
    }
    
    private static Op transformFilterBGP(ExprList exprs, Set<Var> patternVarsScope, OpBGP x)
    {
        return  transformFilterBGP(exprs, patternVarsScope, x.getPattern()) ;
    }

    private static Op transformFilterBGP(ExprList exprs, Set<Var> patternVarsScope, BasicPattern pattern)
    {
        // Any filters that depend on no variables. 
        Op op = insertAnyFilter(exprs, patternVarsScope, null) ;
        
        for ( Triple triple : pattern )
        {
            OpBGP opBGP = getBGP(op) ;
            if ( opBGP == null )
            {
                // Last thing was not a BGP (so it likely to be a filter)
                // Need to pass the results from that into the next triple.
                // Which is a join and sequence is a special case of join
                // which always evaluates by passing results of the early
                // part into the next element of the sequence.
                
                opBGP = new OpBGP() ;    
                op = OpSequence.create(op, opBGP) ;
            }
            
            opBGP.getPattern().add(triple) ;
            // Update variables in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
            
            // Attempt to place any filters
            op = insertAnyFilter(exprs, patternVarsScope, op) ;
        } 
        // Leave any remaining filter expressions - don't wrap up any as somethign else may take them.
        return op ;
    }
    
    /** Find the current OpBGP, or return null. */ 
    private static OpBGP getBGP(Op op)
    {
        if ( op instanceof OpBGP )
            return (OpBGP)op ;
        
        if ( op instanceof OpSequence )
        {
            // Is last in OpSequence an BGP?
            OpSequence opSeq = (OpSequence)op ;
            List<Op> x = opSeq.getElements() ;
            if ( x.size() > 0 )
            {                
                Op opTop = x.get(x.size()-1) ;
                if ( opTop instanceof OpBGP )
                    return (OpBGP)opTop ;
                // Drop through
            }
        }
        // Can't find.
        return null ;
    }
    
    private static Op transformFilterQuadPattern(ExprList exprs, Set<Var> patternVarsScope, OpQuadPattern pattern)
    {
        return transformFilterQuadPattern(exprs, patternVarsScope, pattern.getGraphNode(), pattern.getBasicPattern()) ;
    }
    
    private static Op transformFilterQuadPattern(ExprList exprs, Set<Var> patternVarsScope, Node graphNode, BasicPattern pattern) 
    {
        // Any filters that depend on no variables. 
        Op op = insertAnyFilter(exprs, patternVarsScope, null) ;
        // Any filters that depend on just the graph node.
        if ( Var.isVar(graphNode) )
        {
            patternVarsScope.add(Var.alloc(graphNode)) ;
            op = insertAnyFilter(exprs, patternVarsScope, op) ;
        }
        

        for ( Triple triple : pattern )
        {
            OpQuadPattern opQuad = getQuads(op) ;
            if ( opQuad == null )
            {
                opQuad = new OpQuadPattern(graphNode, new BasicPattern()) ;    
                op = OpSequence.create(op, opQuad) ;
            }
            
            opQuad.getBasicPattern().add(triple) ;
            // Update varaibles in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
            // Attempt to place any filters
            op = insertAnyFilter(exprs, patternVarsScope, op) ;
        } 
        return op ;
    }
    
    /** Find the current OpQuadPattern, or return null. */ 
    private static OpQuadPattern getQuads(Op op)
    {
        if ( op instanceof OpQuadPattern )
            return (OpQuadPattern)op ;
        
        if ( op instanceof OpSequence )
        {
            // Is last in OpSequence an BGP?
            OpSequence opSeq = (OpSequence)op ;
            List<Op> x = opSeq.getElements() ;
            if ( x.size() > 0 )
            {                
                Op opTop = x.get(x.size()-1) ;
                if ( opTop instanceof OpQuadPattern )
                    return (OpQuadPattern)opTop ;
                // Drop through
            }
        }
        // Can't find.
        return null ;
    }

    private static Op transformFilterSequence(ExprList exprs, Set<Var> varScope, OpSequence opSequence)
    {
        List<Op> ops = opSequence.getElements() ;
        
        // Any filters that depend on no variables. 
        Op op = insertAnyFilter(exprs, varScope, null) ;
        
        for ( Iterator<Op> iter = ops.iterator() ; iter.hasNext() ; )
        {
            Op seqElt = iter.next() ;
            // Process the sequence element.  This may insert filters (sequence or BGP)
            seqElt = transform(exprs, varScope, seqElt) ;
            // Merge into sequence.
            op = OpSequence.create(op, seqElt) ;
            // Place any filters now ready.
            op = insertAnyFilter(exprs, varScope, op) ;
        }
        return op ;
    }
    
    // Modularize.
    private static Op transformFilterConditional(ExprList exprs, Set<Var> varScope, OpConditional opConditional)
    {
        // Any filters that depend on no variables. 
        Op op = insertAnyFilter(exprs, varScope, null) ;
        Op left = opConditional.getLeft();
        left = transform(exprs, varScope, left);
        Op right = opConditional.getRight();
        op = new OpConditional(left, right);
        op = insertAnyFilter(exprs, varScope, op);
        return op;
     }
    
    // ---- Utilities
    
    /** For any expression now in scope, wrap the op with a filter */
    private static Op insertAnyFilter(ExprList exprs, Set<Var> patternVarsScope, Op op)
    {
        for ( Iterator<Expr> iter = exprs.iterator() ; iter.hasNext() ; )
        {
            Expr expr = iter.next() ;
            // Cache
            Set<Var> exprVars = expr.getVarsMentioned() ;
            if ( patternVarsScope.containsAll(exprVars) )
            {
                if ( op == null )
                    op = OpTable.unit() ;
                op = OpFilter.filter(expr, op) ;
                iter.remove() ;
            }
        }
        return op ;
    }
    
    /** Place expressions around an Op */ 
    private static Op buildFilter(ExprList exprs, Op op)
    {
        if ( exprs.isEmpty() )
            return op ;
    
        for ( Iterator<Expr> iter = exprs.iterator() ; iter.hasNext() ; )
        {
            Expr expr = iter.next() ;
            if ( op == null )
                op = OpTable.unit() ;
            op = OpFilter.filter(expr, op) ;
            iter.remove();
        }
        return op ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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