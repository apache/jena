/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.Set;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.util.SetUtils;

public class JoinClassifier
{
    static public boolean isLinear(OpJoin join)
    {
        Op left = join.getLeft() ;
        Op right = join.getRight() ;
        if ( left instanceof OpExt )
            left = ((OpExt)left).effectiveOp() ;
        
        if ( right instanceof OpExt )
            right = ((OpExt)right).effectiveOp() ;

        if ( print )
            System.err.println(join) ;
        // return check(left, right) && check(right, left) ;
        
        // New code.
        // Doing the check in reverse means we preserve comunitivity.
        
        //return check2(left, right) && check2(right, left) ;
        return check2(left, right) ;
    }
    
    static final boolean print = false ; 
    
    // Still to do : optional at top level and optional deeper are different.
    // Assumes VarFinder is recursive.
    static private boolean check2(Op op, Op other)
    {
        if ( print )
        {
            System.err.println(op) ;
            System.err.println(other) ;
        }
        
        // Need only check left/rght.
        VarFinder vfLeft = new VarFinder(op) ;
        Set vLeftFixed = vfLeft.getFixed() ;
        Set vLeftOpt = vfLeft.getOpt() ;
        if ( print ) System.err.println("Left/fixed:    "+vLeftFixed) ;
        if ( print ) System.err.println("Left/opt:      "+vLeftOpt) ;
        
        VarFinder vfRight = new VarFinder(other) ;
        Set vRightFixed = vfRight.getFixed() ;
        Set vRightOpt = vfRight.getOpt() ;
        Set vRightFilter = vfRight.getFilter() ;
        
        if ( print ) System.err.println("Right/fixed:   "+vRightFixed) ;
        if ( print ) System.err.println("Right/opt:     "+vRightOpt) ;
        if ( print ) System.err.println("Right/filter:  "+vRightFilter) ;

        // Step 1 : remove any variable definitely fixed from the floating sets
        // because the nature of the "join" will dela with that. 
        vLeftOpt = SetUtils.difference(vLeftOpt, vLeftFixed) ;
        vRightOpt = SetUtils.difference(vRightOpt, vRightFixed) ;
        
        // And also filter variables in the RHS which are always defined in the RHS. 
        vRightFilter = SetUtils.difference(vRightFilter, vRightFixed) ;
        
        if ( print ) System.err.println() ;
        if ( print ) System.err.println("Left/opt:      "+vLeftOpt) ;
        if ( print ) System.err.println("Right/opt:     "+vRightOpt) ;
        if ( print ) System.err.println("Right/filter:  "+vRightFilter) ;

        // Step 2 : check whether any variables in the right are optional or filter vars
        // which are also optional in the left side. 
        
        // Two cases to consider::
        // Case 1 : a variable in the RHS is optional (this is a join we are classifying).
        // Check no variables are optional on right if bound on the left (fixed or optional)   
        // Check no variables are optional on the left side, and optional on the right.
        boolean r11 = 
            SetUtils.intersectionP(vRightOpt, vLeftFixed) ;
        
        boolean r12 = 
            SetUtils.intersectionP(vRightOpt, vLeftOpt) ;
        
        boolean bad1 = r11 || r12 ; 
        
        if ( print ) System.err.println("bad1 = "+bad1) ; 
        
        // Case 2 : a filter in the RHS is uses a variable from the LHS (whether fixed or optional)
        //          Scoping means we must hide the LHS value form the RHS
        //          Could mask (??).  For now, we stop linearization of this join.
        // (we removed fixed variables of the right side, so right/filter is unfixed vars)
        
        boolean bad2 = SetUtils.intersectionP(vRightFilter, vLeftFixed) ;
        if ( print ) System.err.println("bad2 = "+bad2) ;
        if ( print ) System.err.println("bad2 = "+bad2) ;
        
        // Linear if both intersections are empty.
        return !bad1 && !bad2  ;
    }
    
//    static private boolean check(Op op, Op other)
//    {
//        ExprList exprs = null ;
//        Set fixedFilterScope = null ;   // Vars in scope to the filter - fixed
//        Set optFilterScope = null ;     // Vars in scope to the filter - optional
//        
//        if ( op instanceof OpFilter )
//        {
//            OpFilter f = (OpFilter)op ;    
//            op = f.getSubOp() ;
//            exprs = f.getExprs() ;
//            VarFinder vf = new VarFinder(op) ;
//            fixedFilterScope = vf.getFixed() ;
//            optFilterScope = vf.getOpt() ;
//        }
//        
//        if ( op instanceof OpLeftJoin )
//        {
//            OpLeftJoin j = (OpLeftJoin)op ;
//            // Leave op
//            exprs = j.getExprs() ;
//            VarFinder vf1 = new VarFinder(j.getLeft()) ;
//            VarFinder vf2 = new VarFinder(j.getRight()) ;
//            // Both sides of the LeftJoin are in-scope to the filter. 
//            fixedFilterScope = SetUtils.union(vf1.getFixed(), vf2.getFixed()) ;
//            optFilterScope = SetUtils.union(vf1.getOpt(), vf2.getOpt()) ;
//        }
//        
//        if ( exprs == null )
//            return true ;
//        
//        if ( fixedFilterScope == null || optFilterScope == null )
//            throw new ARQInternalErrorException("JoinClassifier: Failed to set up variable sets correctly") ;
//        
//        Set exprVars = exprs.getVarsMentioned() ;
//        
//        // remove variables that are safe:
//        //   fixed mentioned here
//        //   or optional and not mentioned in the other branch
//        
//        // **** If expr from a left join, then safe if filter var in LHS or RHS of LJ.  
//        
//        if ( print ) System.out.println("Expr vars:     "+exprVars) ;
//        if ( print ) System.out.println("Fixed, here:   "+fixedFilterScope) ;
//        if ( print ) System.out.println("Opt, here:     "+optFilterScope) ;
//        
//        Set allVarsOther = OpVars.patternVars(other) ;      // The other side.
//        if ( print ) System.out.println("allVarsOther:  "+allVarsOther) ;
//        
//        // Remove variables in filter that are always set below it (hence safe) 
//        exprVars.removeAll(fixedFilterScope) ;
//        if ( print ) System.out.println("Expr vars:(\\F) "+exprVars) ;
//        if ( exprVars.size() == 0 ) return true ;
//
//        // Ditto safe are any optionals vars IFF they not in other side 
//        // (where they may have a value when they don't in the op below the filter)
//        Set s = SetUtils.difference(optFilterScope, allVarsOther) ;
//        // s is the set of optionals only on this side. 
//        exprVars.removeAll(s) ;
//        if ( exprVars.size() == 0 ) return true ;
//        
//        // At this point, an expr var is "dangerous" if it is mentioned in the other side.
//        if ( print ) System.out.println("Diff           "+s) ;
//        if ( print ) System.out.println("Expr vars:(\\s) "+exprVars) ;
//        
//        // Keep those in the other side : thien, if not empty, it's a dangerous variable.
//        exprVars.retainAll(allVarsOther) ;
//        if ( print ) System.out.println("Expr vars:(!!) "+exprVars) ;
//        
//        // Safe if nothing in the intersection of remaining exprVars and  
//        return exprVars.size() == 0 ;
//    }
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