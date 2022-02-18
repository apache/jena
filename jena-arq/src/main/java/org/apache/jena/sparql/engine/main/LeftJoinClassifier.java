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

package org.apache.jena.sparql.engine.main;

import java.util.HashSet;
import java.util.Set ;

import org.apache.jena.atlas.lib.SetUtils ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.algebra.op.OpExt ;
import org.apache.jena.sparql.algebra.op.OpLeftJoin ;
import org.apache.jena.sparql.algebra.op.OpModifier ;
import org.apache.jena.sparql.core.Var ;

public class LeftJoinClassifier
{
    static /*final*/ public boolean print = false ;

    // Test for the "well-formed" criterion of left joins whereby they can
    // be executed against a current set of bindings.  If not, the left join
    // has to be done by execution of the left, executing the right without
    // the left (so no substitution/additional indexing), then
    // left-join-ed.  AND that can be expensive - luckily, it only occurs
    // in OPTIONALs with a pattern depth of 2 or more.

    // This amounts to testing whether there are any optional variables in the
    // RHS pattern (hence they are nested in someway) that also occur in the LHS
    // of the LeftJoin being considered.

    // Need also worry about filters in the right (not in the LJ condition)
    // which use vars from the left.

    static public boolean isLinear(OpLeftJoin op) {
        return isLinear(op.getLeft(), op.getRight()) ;
    }

    static public boolean isLinear(Op left, Op right) {
        left = effectiveOp(left) ;
        right = effectiveOp(right) ;

        // Subquery with modifier.  Substitution does not apply.
        // With SELECT *, it's as if the subquery were just the pattern.
        if ( right instanceof OpModifier )
            return false ;

        Set<Var> leftVars = OpVars.visibleVars(left) ;
        if ( print ) {
            System.err.println("Left") ;
            System.err.println("  Visible: "+leftVars) ;
        }
        if ( print ) {
            VarFinder vfLeft = VarFinder.process(left) ;
            System.err.println("Left") ;
            vfLeft.print(System.err) ;
//        // Check
//        Set<Var> leftVars2 = new HashSet<>();
//        leftVars.addAll(vfLeft.getFixed());
//        leftVars.addAll(vfLeft.getOpt());
//        leftVars.addAll(vfLeft.getAssign());
//        if ( print ) {
//            System.err.println("  Visible: "+leftVars);
//        }
        }

        VarFinder vf = VarFinder.process(right) ;
        if ( print ) {
            System.err.println("Right") ;
            vf.print(System.err) ;
        }

        // Case 1 : If there are any variables in the LHS that are
        // filter-only or filter-before define, we can't do anything.
        if ( ! vf.getFilterOnly().isEmpty() ) {
            // A tigher condition is to see of any of the getFilterOnly are possible from the
            // left.  If not, then we can still use a sequence.
            // But an outer sequence may push arbitrary here so play safe on the argument
            // this is a relative uncommon case.
            if (print) System.err.println("LJ: Case 1 (true=ok) - " + false);
            return false ;
        }

        if (print) System.err.println("LJ: Case 1 (true=ok)  - " + true);
        Set<Var> optRight = vf.getOpt() ;
        Set<Var> fixedRight = vf.getFixed() ;
        Set<Var> filterVarsRight = vf.getFilter() ;
        Set<Var> assignVarsRight = vf.getAssign() ;
        // Case 2
        // A variable is nested in an optional on the RHS and on the LHS
        // Cannot linearize as we must preserve scope
        boolean b2 = SetUtils.intersectionP(leftVars, optRight) ;
        if (print) System.err.println("LJ: Case 2 (false=ok) - " + b2);

        // Case 3
        // A variable mentioned in a filter within the RHS already exists on the LHS
        // Cannot linearize as would change filter evaluation
        boolean b3 = SetUtils.intersectionP(leftVars, filterVarsRight) ;
        if (print) System.err.println("LJ: Case 3 (false=ok) - " + b3);

        // Case 4
        // A variable mentioned in the assign is not introduced on the RHS
        // Cannot linearize as would change bind evaluation
        Set<Var> unsafeAssign = new HashSet<>(assignVarsRight);
        unsafeAssign.removeAll(fixedRight);
        boolean b4 = unsafeAssign.size() > 0 ;
        if (print) System.err.println("LJ: Case 4 (false=ok) - " + b4);

        if (print) {
            boolean b9 = ! b2 && ! b3 && ! b4 ;
            System.err.println("LJ: Case !2&!3&!4  (true=ok) - " + b9);
        }

        // Linear if all conditions are false
        return ! b2 && ! b3 && ! b4 ;
    }

    static public Set<Var> nonLinearVars(OpLeftJoin op) {
        Op left = effectiveOp(op.getLeft()) ;
        Op right = effectiveOp(op.getRight()) ;
        Set<Var> leftVars = OpVars.visibleVars(left) ;
        Set<Var> optRight = VarFinder.optDefined(right) ;

        return SetUtils.intersection(leftVars, optRight) ;
    }

    private static Op effectiveOp(Op op) {
        if (op instanceof OpExt)
            op = ((OpExt) op).effectiveOp() ;
        return op ;
    }
}
