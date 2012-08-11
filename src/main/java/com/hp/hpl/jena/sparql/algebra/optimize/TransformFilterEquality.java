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

package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.Set ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.E_Equals ;
import com.hp.hpl.jena.sparql.expr.E_SameTerm ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprFunction2 ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.ExprVars ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class TransformFilterEquality extends TransformCopy
{
    // E_OneOf -- done by expansion earlier.
    
    // TODO (Carefully) transformPlainStrings
    // Aggressive on strings goes for efficient over exactlness of xsd:string/plain literal.
    
    private boolean stringsAsTerms ;
    
    public TransformFilterEquality(boolean stringsAsTerms)
    {
        // XSD string is not a simple literal
        // Inactive.
        // What about numbers? 
        this.stringsAsTerms = stringsAsTerms ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op subOp)
    { 
        ExprList exprs = opFilter.getExprs() ;

        if ( ! safeToTransform(exprs, subOp) )
            return super.transform(opFilter, subOp) ;
        
        Op op = subOp ;
        // Variables set
        Set<Var> patternVars = OpVars.patternVars(op) ;
        
        // Any assignments must go inside filters so the filters see the assignments.
        // For each filter in the expr list ...
        
        ExprList exprs2 = new ExprList() ;      // Unchanged filters.  Put around the result. 
        
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
    
    private static boolean safeToTransform(Expr expr, Op op)
    {
        return safeToTransform(new ExprList(expr), op) ;
    }
    
    private static boolean safeToTransform(ExprList exprs, Op op)
    {
        if ( op instanceof OpBGP || op instanceof OpQuadPattern ) return true ;
        
        // This will be applied also in sub-calls of the Transform but queries 
        // are very rarely so deep that it matters. 
        if ( op instanceof OpSequence )
        {
            OpN opN = (OpN)op ;
            for ( Op subOp : opN.getElements() )
            {
                if ( ! safeToTransform(exprs, subOp) )
                    return false ;
            }
            return true ; 
        }
        
        if ( op instanceof OpJoin || op instanceof OpUnion)
        {
            Op2 op2 = (Op2)op ;
            return safeToTransform(exprs, op2.getLeft()) && safeToTransform(exprs, op2.getRight()) ; 
        }

        // Not safe unless filter variables are mentioned on the LHS. 
        if ( op instanceof OpConditional || op instanceof OpLeftJoin )
        {
            Op2 opleftjoin = (Op2)op ;
            
            if ( ! safeToTransform(exprs, opleftjoin.getLeft()) || 
                 ! safeToTransform(exprs, opleftjoin.getRight()) )
                return false ;
            
            Op opLeft = opleftjoin.getLeft() ;
            // ?? Slightly stronger condition that OpConditional transformation.
            Set<Var> x = OpVars.patternVars(opLeft) ;
            Set<Var> y = ExprVars.getVarsMentioned(exprs) ;
            if ( x.containsAll(y) )
                return true ;
            return false ;
        }
        
        if ( op instanceof OpGraph )
        {
            OpGraph opg = (OpGraph)op ;
            return safeToTransform(exprs, opg.getSubOp()) ;
        }
        
        return false ;
    }
    
    // ++ called by TransformFilterDisjunction
    /** Return null for "no change" */
    public static Op processFilter(Expr e, Op subOp)
    {
        if ( ! safeToTransform(e, subOp) )
            return null ;
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
            // XXX Optimize here.
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
        // Insert LET(var2 := var1)
        return OpAssign.assign(op, var2.asVar(), var1) ;
    }

}
