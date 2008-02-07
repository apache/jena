/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class Rewrite
{
    static void main()
    {
        // Aside: Algebra doc
        Query query = QueryFactory.read("Q.rq") ;
        Op op = Algebra.compile(query) ;
        System.out.println(op) ;
        op = Transformer.transform(new Redo(), op) ;
        System.out.println(op) ;
    }
    
    static class Redo extends TransformCopy
    {
        public Op transform(OpFilter opFilter, Op subOp)
        { 
            // Safe for BGPs (and unions and joins of BGPs)
            // Optionals - be careful.
            // Optionals+bound - be very careful.
            if ( !(subOp instanceof OpBGP) )
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
            // FILTER ( ?x = :x ) etc
            if ( e instanceof E_Equals )
            {
                E_Equals eq = (E_Equals)e ;
                Expr left = eq.getArg1() ;
                Expr right = eq.getArg2() ;
                if ( left.isVariable() && right.isConstant() )
                    return subst(subOp, left.asVar(), right.getConstant()) ;
                if ( left.isConstant() && right.isVariable() )
                    return subst(subOp, right.asVar(), left.getConstant()) ;
            }
                
            return null ;
        }
        
        private static Op subst(Op subOp , Var var, NodeValue nv)
        {
            Op op = OpSubstitute.substitute(subOp, var, nv.asNode()) ;
            return OpAssign.assign(op, var, nv) ;
        }
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