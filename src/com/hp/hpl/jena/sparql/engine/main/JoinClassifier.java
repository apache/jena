/**
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

import org.openjena.atlas.lib.SetUtils ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpDiff ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpMinus ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.core.Var ;

public class JoinClassifier
{
    static /*final*/ public  boolean print = false ;

    static public boolean isLinear(OpJoin join)
    {
        if (print) System.err.println(join) ;
        return isLinear(join.getLeft(),join.getRight()) ;
    }

    static public boolean isLinear(Op left, Op right)
    {
        left = effectiveOp(left) ;
        right = effectiveOp(right) ;

        // Old: Subquery with modifier. Substitution does not apply.
        // Renaming should make this work.
        // With SELECT *, it's as if the subquery were just the pattern.

        if (right instanceof OpModifier) return false ;
        if (right instanceof OpDiff) return false ;
        if (right instanceof OpMinus) return false ;

        // Assume something will not commute these later on.
        return check(left, right) ;
    }

    // Check left can stream into right
    static private boolean check(Op leftOp, Op rightOp)
    {
        // This is probably overly cautious.
        if (print)
        {
            System.err.println(leftOp) ;
            System.err.println(rightOp) ;
        }

        // Need only check left/rght.
        VarFinder vfLeft = new VarFinder(leftOp) ;
        Set<Var> vLeftFixed = vfLeft.getFixed() ;
        Set<Var> vLeftOpt = vfLeft.getOpt() ;
        //Set<Var> vLeftFilter = vfLeft.getFilter() ;
        if (print) System.err.println("Left/fixed:    " + vLeftFixed) ;
        if (print) System.err.println("Left/opt:      " + vLeftOpt) ;
        //if (print) System.err.println("Left/filter:   " + vLeftFilter) ;

        VarFinder vfRight = new VarFinder(rightOp) ;
        Set<Var> vRightFixed = vfRight.getFixed() ;
        Set<Var> vRightOpt = vfRight.getOpt() ;
        Set<Var> vRightFilter = vfRight.getFilter() ;

        if (print) System.err.println("Right/fixed:   " + vRightFixed) ;
        if (print) System.err.println("Right/opt:     " + vRightOpt) ;
        if (print) System.err.println("Right/filter:  " + vRightFilter) ;

        // Step 1 : remove any variable definitely fixed from the floating sets
        // because the nature of the "join" will deal with that.
        vLeftOpt = SetUtils.difference(vLeftOpt, vLeftFixed) ;
        vRightOpt = SetUtils.difference(vRightOpt, vRightFixed) ;

        // And also filter variables in the RHS which are always defined in the
        // RHS.  Leaves any potentially free variables in RHS filter. 
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
        
        // What about rightfixed, left opt?

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
