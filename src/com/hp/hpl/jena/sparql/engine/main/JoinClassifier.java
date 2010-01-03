/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved. [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main ;

import java.util.Set ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.SetUtils ;

public class JoinClassifier
{
    static final boolean print = false ;

    static public boolean isLinear(OpJoin join)
    {
        if (print) System.err.println(join) ;
        return isLinear(join.getLeft(),join.getRight()) ;
    }

    static public boolean isLinear(Op left, Op right)
    {
         left = effectiveOp(left) ;
         right = effectiveOp(right) ;

        // Subquery with modifier. Substitution does not apply.
        // With SELECT *, it's as if the subquery were just the pattern.

        if (right instanceof OpModifier) return false ;

        // Assume something will not commute these later on.
        return check(left, right) ;
    }

    // Check left can stream into right
    static private boolean check(Op op, Op other)
    {
        // This is probably overly cautious.
        if (print)
        {
            System.err.println(op) ;
            System.err.println(other) ;
        }

        // Need only check left/rght.
        VarFinder vfLeft = new VarFinder(op) ;
        Set<Var> vLeftFixed = vfLeft.getFixed() ;
        Set<Var> vLeftOpt = vfLeft.getOpt() ;
        if (print) System.err.println("Left/fixed:    " + vLeftFixed) ;
        if (print) System.err.println("Left/opt:      " + vLeftOpt) ;

        VarFinder vfRight = new VarFinder(other) ;
        Set<Var> vRightFixed = vfRight.getFixed() ;
        Set<Var> vRightOpt = vfRight.getOpt() ;
        Set<Var> vRightFilter = vfRight.getFilter() ;

        if (print) System.err.println("Right/fixed:   " + vRightFixed) ;
        if (print) System.err.println("Right/opt:     " + vRightOpt) ;
        if (print) System.err.println("Right/filter:  " + vRightFilter) ;

        // Step 1 : remove any variable definitely fixed from the floating sets
        // because the nature of the "join" will dela with that.
        vLeftOpt = SetUtils.difference(vLeftOpt, vLeftFixed) ;
        vRightOpt = SetUtils.difference(vRightOpt, vRightFixed) ;

        // And also filter variables in the RHS which are always defined in the
        // RHS.
        vRightFilter = SetUtils.difference(vRightFilter, vRightFixed) ;

        if (print) System.err.println() ;
        if (print) System.err.println("Left/opt:      " + vLeftOpt) ;
        if (print) System.err.println("Right/opt:     " + vRightOpt) ;
        if (print) System.err.println("Right/filter:  " + vRightFilter) ;

        // Step 2 : check whether any variables in the right are optional or
        // filter vars
        // which are also optional in the left side.

        // Two cases to consider::
        // Case 1 : a variable in the RHS is optional 
        //          (this is a join we are classifying).
        // Check no variables are optional on right if bound on the left (fixed
        // or optional)
        // Check no variables are optional on the left side, and optional on the
        // right.
        boolean r11 = SetUtils.intersectionP(vRightOpt, vLeftFixed) ;

        boolean r12 = SetUtils.intersectionP(vRightOpt, vLeftOpt) ;

        boolean bad1 = r11 || r12 ;

        if (print) System.err.println("bad1 = " + bad1) ;

        // Case 2 : a filter in the RHS is uses a variable from the LHS (whether
        // fixed or optional)
        // Scoping means we must hide the LHS value form the RHS
        // Could mask (??). For now, we stop linearization of this join.
        // (we removed fixed variables of the right side, so right/filter is
        // unfixed vars)

        boolean bad2 = SetUtils.intersectionP(vRightFilter, vLeftFixed) ;
        if (print) System.err.println("bad2 = " + bad2) ;

        // Linear if both intersections are empty.
        return !bad1 && !bad2 ;
    }

    static public Op effectiveOp(Op op)
    {
        if (op instanceof OpExt) op = ((OpExt) op).effectiveOp() ;
        return op ;
    }

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */