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

package com.hp.hpl.jena.sparql.engine.main;

import java.util.HashSet;
import java.util.Set ;

import org.apache.jena.atlas.lib.SetUtils ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.core.Var ;

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

    static public boolean isLinear(OpLeftJoin op)
    {
        return isLinear(op.getLeft(), op.getRight()) ;
    }
    static public boolean isLinear(Op left, Op right)
    {
        left = effectiveOp(left) ;
        right = effectiveOp(right) ;
        
        // Subquery with modifier.  Substitution does not apply.
        // With SELECT *, it's as if the subquery were just the pattern.
        if ( right instanceof OpModifier )
            return false ;
        
        Set<Var> leftVars = OpVars.visibleVars(left) ;
        VarFinder vf = new VarFinder(right) ;
        
        Set<Var> optRight = vf.getOpt() ;
        Set<Var> fixedRight = vf.getFixed() ;
        Set<Var> filterVarsRight = vf.getFilter() ; 
        Set<Var> assignVarsRight = vf.getAssign() ;
        
        if (print) {
            System.err.println("Left/visible: " + leftVars) ;
            System.err.println("Right/fixed:  " + fixedRight) ;
            System.err.println("Right/opt:    " + optRight) ;
            System.err.println("Right/filter: " + filterVarsRight) ;
            System.err.println("Right/assign: " + assignVarsRight) ;
        }
        
        // Case 1
        // A variable is nested in an optional on the RHS and on the LHS
        // Cannot linearize as we must preserve scope
        boolean b1 = SetUtils.intersectionP(leftVars, optRight) ;
        if (print) System.err.println("Case 1 - " + b1);
        
        // Case 2
        // A variable mentioned in a filter within the RHS already exists on the LHS
        // Cannot linearize as would change filter evaluation
        boolean b2 = SetUtils.intersectionP(leftVars, filterVarsRight) ;
        if (print) System.err.println("Case 2 - " + b2);
        
        // Case 3
        // A variable mentioned in the assign is not introduced on the RHS
        // Cannot linearize as would change bind evaluation
        Set<Var> unsafeAssign = new HashSet<>(assignVarsRight);
        unsafeAssign.removeAll(fixedRight);
        boolean b3 = unsafeAssign.size() > 0 ;
        if (print) System.err.println("Case 3 - " + b3);

        // Linear if all conditions are false
        return ! b1 && ! b2 && ! b3 ;
    }
    
    static public Set<Var> nonLinearVars(OpLeftJoin op)
    {
        Op left = effectiveOp(op.getLeft()) ;
        Op right = effectiveOp(op.getRight()) ;
        Set<Var> leftVars = OpVars.visibleVars(left) ;
        Set<Var> optRight = VarFinder.optDefined(right) ;

        return SetUtils.intersection(leftVars, optRight) ;
    }
    
    private static Op effectiveOp(Op op)
    {
        if (op instanceof OpExt)
            op = ((OpExt) op).effectiveOp() ;
        return op ;
    }

}
