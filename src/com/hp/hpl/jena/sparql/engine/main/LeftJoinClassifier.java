/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.Set;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.SetUtils;

public class LeftJoinClassifier
{
    // Test for the "well-formed" criterion of left joins whereby they can
    // be executed against a current set of bindings.  If not, the left join
    // has to be done by execution of the left, executing the right without
    // the left (so no substitution/additional indexing), then
    // left-join-ed.  AND that can be expensive - luckily, it only occurs
    // in OPTIONALs with a pattern depth of 2 or more. 

    // This amounts to testing whether there are any optional variables in the 
    // RHS pattern (hence they are nested in someway) that also occur in the LHS
    // of the LeftJoin being considered.
    
    // Need also worry about filters in the right (not in the LJ condidtion)
    // which use vars from the left. 

    static public boolean isLinear(OpLeftJoin op)
    {
        return isLinear(op.getLeft(), op.getRight()) ;
    }
    static public boolean isLinear(Op left, Op right)
    {
        left = JoinClassifier.effectiveOp(left) ;
        right = JoinClassifier.effectiveOp(right) ;
        
        // Subquery with modifier.  Substitution does not apply.
        // With SELECT *, it's as if the subquery were just the pattern.
        if ( right instanceof OpModifier )
            return false ;
        
        Set<Var> leftVars = OpVars.patternVars(left) ;
        
        VarFinder vf = new VarFinder(right) ;
        
        Set<Var> optRight = vf.getOpt() ;
        //Set<Var> fixedRight = vf.getFixed() ;
        Set<Var> filterVarsRight = vf.getFilter() ; 
        
        boolean b1 = SetUtils.intersectionP(leftVars, optRight) ;
        boolean b2 = SetUtils.intersectionP(leftVars, filterVarsRight) ;        

        // Safe for linear execution if there are no  
        return ! SetUtils.intersectionP(leftVars, optRight) && ! SetUtils.intersectionP(leftVars, filterVarsRight) ;
    }
    
    static public Set<Var> nonLinearVars(OpLeftJoin op)
    {
        Op left = JoinClassifier.effectiveOp(op.getLeft()) ;
        Op right = JoinClassifier.effectiveOp(op.getRight()) ;
        Set<Var> leftVars = OpVars.patternVars(left) ;
        Set<Var> optRight = VarFinder.optDefined(right) ;

        return SetUtils.intersection(leftVars, optRight) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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