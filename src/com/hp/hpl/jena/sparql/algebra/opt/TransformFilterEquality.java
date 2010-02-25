/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import java.util.Set ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.E_Equals ;
import com.hp.hpl.jena.sparql.expr.E_SameTerm ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprFunction2 ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class TransformFilterEquality extends TransformCopy
{
    // TODO E_OneOf
    
    // TODO (Carefully) Two forms - aggressive and strict
    // Aggressive on strings goes for efficient over exactlness of xsd:string/plain literal.
    // Extend to disjunctions of equalties.
    
    public TransformFilterEquality() {}
    
    @Override
    public Op transform(OpFilter opFilter, Op subOp)
    { 
        // What about filter of OpSequence? 
        
        // Safe for BGPs, quads and GRAPH 
        // (and unions and joins of BGPs) 
        // Optionals - be careful.
        // Optionals+bound - be very careful.
        if ( ! (subOp instanceof OpBGP) &&
             ! (subOp instanceof OpQuadPattern) &&
             ! (subOp instanceof OpGraph) )
            return super.transform(opFilter, subOp) ;
        
        ExprList exprs = opFilter.getExprs() ;
        Op op = subOp ;
        // Variables set
        Set<Var> patternVars = OpVars.patternVars(op) ;
        
        // Any assignments must go inside filters so the filters see the assignments.
        ExprList exprs2 = new ExprList() ;
        
        for (  Expr e : exprs.getList() )
        {
            Op op2 = processFilterWorker(e, op, patternVars) ;
            if ( op2 == null )
                exprs2.add(e) ;
            else
                op = op2 ;
        }

        // Place any filter expressions around the processed sub op. 
        if ( exprs2.size() > 0 )
            op = OpFilter.filter(exprs2, op) ;
        return op ;
    }
    
    /** Return an optimized filter for equality expressions */
    public static Op processFilterOrOpFilter(Expr e, Op subOp)
    {
        Op op2 = processFilterWorker(e, subOp, null) ;
        if ( op2 == null )
            op2 = OpFilter.filter(e, subOp) ;
        return op2 ;
    }
    
    /** Return null for "no change" */
    public static Op processFilter(Expr e, Op subOp)
    {
        return processFilterWorker(e, subOp, null) ;
    }

    private static Op processFilterWorker(Expr e, Op subOp, Set<Var> patternVars)
    {
        if ( patternVars == null )
            patternVars = OpVars.patternVars(subOp) ;
        // Rewrites: 
        // FILTER ( ?x = ?y ) 
        // FILTER ( ?x = :x ) for IRIs and bNodes, not literals 
        //    (to preserve value testing in the filter, and not in the graph). 
        // FILTER ( sameTerm(?x, :x ) ) etc
        
        if ( !(e instanceof E_Equals) && !(e instanceof E_SameTerm) )
            return null ;

        // Corner case: sameTerm is false for string/plain literal, 
        // but true in the graph for graphs with 
        
        ExprFunction2 eq = (ExprFunction2)e ;
        Expr left = eq.getArg1() ;
        Expr right = eq.getArg2() ;

        // This gets cardinality wrong.
//        if ( left.isVariable() && right.isVariable() )
//        {
//            // Both must be used or else.
//            if ( patternVars.contains(left.asVar()) &&
//                 patternVars.contains(right.asVar()) )
//                return subst(subOp, left.getExprVar(), right.getExprVar()) ;
//        }
        
        Var var = null ;
        NodeValue constant = null ;

        if ( left.isVariable() && right.isConstant() )
        {
            var = left.asVar() ;
            constant = right.getConstant() ;
        }
        else if ( right.isVariable() && left.isConstant() )
        {
            var = right.asVar() ;
            constant = left.getConstant() ;
        }

        if ( var == null || constant == null )
            return null ;

        if ( !patternVars.contains(var) )
            return null ;
        
        // Corner case: sameTerm is false for string/plain literal, 
        // but true in the graph for graph matching. 
        if (e instanceof E_SameTerm)
        {
            if ( ! ARQ.isStrictMode() && constant.isString() )
                return null ;
        }
        
        // Final check for "=" where a FILTER = can do value matching when the graph does not.
        if ( e instanceof E_Equals )
        {
            // Value based?
            if ( ! ARQ.isStrictMode() && constant.isLiteral() )
                return null ;
        }

        return subst(subOp, var, constant) ;
    }
    
    private static Op subst(Op subOp , Var var, NodeValue nv)
    {
        Op op = Substitute.substitute(subOp, var, nv.asNode()) ;
        return OpAssign.assign(op, var, nv) ;
    }
    
    private static Op subst(Op subOp , ExprVar var1, ExprVar var2)
    {
        // Replace var2 with var1
        Op op = Substitute.substitute(subOp, var2.asVar(), var1.asVar()) ;
        // Insert LET(var2:=var1)
        return OpAssign.assign(op, var2.asVar(), var1) ;
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