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

import java.util.Set ;

import org.apache.jena.atlas.lib.SetUtils ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier ;
import com.hp.hpl.jena.sparql.core.Var ;

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
