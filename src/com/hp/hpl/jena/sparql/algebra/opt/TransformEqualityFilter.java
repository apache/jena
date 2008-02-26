/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpSubstitute;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_SameTerm;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class TransformEqualityFilter extends TransformCopy
{
    public Op transform(OpFilter opFilter, Op subOp)
    { 
        // Safe for BGPs (and unions and joins of BGPs)
        // Optionals - be careful.
        // Optionals+bound - be very careful.
        if ( !(subOp instanceof OpBGP) && ! (subOp instanceof OpQuadPattern))
            return super.transform(opFilter, subOp) ;
        
        ExprList exprs = opFilter.getExprs() ;
        Op op = subOp ;
        // Any assignments must go inside filters so the filters see the assignments.
        ExprList exprs2 = new ExprList() ;
        
        for ( Iterator iter = exprs.getList().iterator() ; iter.hasNext() ; )
        {
            Expr e = (Expr) iter.next() ;
            Op op2 = processFilter(e, op) ;
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
    
    // Return null for "no change"
    private Op processFilter(Expr e, Op subOp)
    {
        // Rewrites: 
        // FILTER ( ?x = :x ) for IRIs and bNodes, not literals 
        //    (to preserve value testing in the filter, and not in the graph). 
        // FILTER ( sameTerm(?x, :x ) etc
        
        if ( !(e instanceof E_Equals) && !(e instanceof E_SameTerm) )
            return null ;

        ExprFunction2 eq = (ExprFunction2)e ;
        Expr left = eq.getArg1() ;
        Expr right = eq.getArg2() ;
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

        // Final check for "="  
        if ( e instanceof E_Equals )
        {
            // Value based?
            if ( constant.isLiteral() )
                return null ;
        }

        return subst(subOp, var, constant) ;
    }
    
    private static Op subst(Op subOp , Var var, NodeValue nv)
    {
        Op op = OpSubstitute.substitute(subOp, var, nv.asNode()) ;
        return OpAssign.assign(op, var, nv) ;
    }
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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