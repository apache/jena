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

package com.hp.hpl.jena.sparql.engine.main ;

import java.util.Set ;

import org.apache.jena.atlas.lib.SetUtils ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.Var ;

public class JoinClassifier
{
    static /*final*/ public  boolean print = false ;

    static public boolean isLinear(OpJoin join) {
        if ( print )
            System.err.println(join) ;
        return isLinear(join.getLeft(), join.getRight()) ;
    }

    static public boolean isLinear(Op _left, Op _right) {
        // Modifers that we can push substitution through whether left or right:
        //   OpDistinct, OpReduced, OpList, OpProject
        // Modifiers that we don't touch
        //   OpSlice, OpTopN, OpOrder (which gets lost - could remove it!)
        // (These could be first and top - i.e. in call once position, and be safe)
        
        Op left = effectiveOp(_left) ;
        Op right = effectiveOp(_right) ;

        if ( right instanceof OpExtend )    return false ;
        if ( right instanceof OpAssign )    return false ;
        if ( right instanceof OpGroup )     return false ;
        if ( right instanceof OpDiff )      return false ;
        if ( right instanceof OpMinus )     return false ;
        
        if ( right instanceof OpSlice )     return false ;
        if ( right instanceof OpTopN )      return false ;
        if ( right instanceof OpOrder )     return false ;

        // Assume something will not commute these later on.
        return check(left, right) ;
    }

    // Check left can stream into right
    static private boolean check(Op leftOp, Op rightOp) {
        if ( print ) {
            System.err.println(leftOp) ;
            System.err.println(rightOp) ;
        }

        // Need only check left/rght.
        VarFinder vfLeft = new VarFinder(leftOp) ;
        Set<Var> vLeftFixed = vfLeft.getFixed() ;
        Set<Var> vLeftOpt = vfLeft.getOpt() ;
        // Set<Var> vLeftFilter = vfLeft.getFilter() ;
        if ( print )
            System.err.println("Left/fixed:    " + vLeftFixed) ;
        if ( print )
            System.err.println("Left/opt:      " + vLeftOpt) ;
        // if (print) System.err.println("Left/filter:   " + vLeftFilter) ;

        VarFinder vfRight = new VarFinder(rightOp) ;
        Set<Var> vRightFixed = vfRight.getFixed() ;
        Set<Var> vRightOpt = vfRight.getOpt() ;
        Set<Var> vRightFilter = vfRight.getFilter() ;
        Set<Var> vRightAssign = vfRight.getAssign() ;

        if ( print )
            System.err.println("Right/fixed:   " + vRightFixed) ;
        if ( print )
            System.err.println("Right/opt:     " + vRightOpt) ;
        if ( print )
            System.err.println("Right/filter:  " + vRightFilter) ;
        if ( print )
            System.err.println("Right/assign:  " + vRightAssign) ;

        // Step 1 : remove any variable definitely fixed from the floating sets
        // because the nature of the "join" will deal with that.
        vLeftOpt = SetUtils.difference(vLeftOpt, vLeftFixed) ;
        vRightOpt = SetUtils.difference(vRightOpt, vRightFixed) ;

        // And also assign/filter variables in the RHS which are always defined
        // in the
        // RHS. Leaves any potentially free variables in RHS filter.
        vRightFilter = SetUtils.difference(vRightFilter, vRightFixed) ;
        vRightAssign = SetUtils.difference(vRightAssign, vRightFixed) ;

        if ( print )
            System.err.println() ;
        if ( print )
            System.err.println("Left/opt:      " + vLeftOpt) ;
        if ( print )
            System.err.println("Right/opt:     " + vRightOpt) ;
        if ( print )
            System.err.println("Right/filter:  " + vRightFilter) ;
        if ( print )
            System.err.println("Right/assign:  " + vRightAssign) ;

        // Step 2 : check whether any variables in the right are optional or
        // filter vars which are also optional in the left side.

        // Two cases to consider::
        // Case 1 : a variable in the RHS is optional
        // (this is a join we are classifying).
        // Check no variables are optional on right if bound on the left (fixed
        // or optional)
        // Check no variables are optional on the left side, and optional on the
        // right.
        boolean r11 = SetUtils.intersectionP(vRightOpt, vLeftFixed) ;

        boolean r12 = SetUtils.intersectionP(vRightOpt, vLeftOpt) ;

        // What about rightfixed, left opt?

        boolean bad1 = r11 || r12 ;

        if ( print )
            System.err.println("Case 1 = " + bad1) ;

        // Case 2 : a filter in the RHS is uses a variable from the LHS (whether
        // fixed or optional)
        // Scoping means we must hide the LHS value form the RHS
        // Could mask (??). For now, we stop linearization of this join.
        // (we removed fixed variables of the right side, so right/filter is
        // unfixed vars)

        boolean bad2 = SetUtils.intersectionP(vRightFilter, vLeftFixed) ;
        if ( print )
            System.err.println("Case 2 = " + bad2) ;

        // Case 3 : an assign in the RHS uses a variable not introduced
        // Scoping means we must hide the LHS value from the RHS

        // TODO Think this may be slightly relaxed, using variables in an
        // assign on the RHS is in principal fine if they're also available on
        // the RHS
        // vRightAssign.removeAll(vRightFixed);
        // boolean bad3 = vRightAssign.size() > 0;
        boolean bad3 = SetUtils.intersectionP(vRightAssign, vLeftFixed) ;
        if ( print )
            System.err.println("Case 3 = " + bad3) ;

        // Linear if all conditions are false
        return !bad1 && !bad2 && !bad3 ;
    }

    /** Find the "effective op" - ie. the one that may be sensitive to linearization */
    private static Op effectiveOp(Op op) {
        if ( op instanceof OpExt )
            op = ((OpExt)op).effectiveOp() ;
        while (safeModifier(op))
            op = ((OpModifier)op).getSubOp() ;
        return op ;
    }

    /** Helper - test for "safe" modifiers */
    private static boolean safeModifier(Op op) {
        if ( !(op instanceof OpModifier) )
            return false ;
        return op instanceof OpDistinct || op instanceof OpReduced || op instanceof OpProject || op instanceof OpList ;
    }
}
