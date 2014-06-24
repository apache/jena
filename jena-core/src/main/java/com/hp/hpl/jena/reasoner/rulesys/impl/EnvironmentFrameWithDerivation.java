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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.TriplePattern;

import java.util.*;

/**
 * Extension of the normal AND-stack environment frame to support
 * incremental derivation logging.
 */
public class EnvironmentFrameWithDerivation extends EnvironmentFrame {

    /** The initial starting arguments for the call */
    Node[] argVars = new Node[RuleClauseCode.MAX_ARGUMENT_VARS];
        
    /** The set of instantiated subgoals processed so far */
    TriplePattern[] matches;
        
    /** 
     * Constructor 
     * @param clause the compiled code being interpreted by this env frame 
     */
    public EnvironmentFrameWithDerivation(RuleClauseCode clause) {
        super(clause);
        if (clause.getRule() != null) {
            matches = new TriplePattern[clause.getRule().bodyLength()];
        }
    }
    
    /** Instantiate and record a matched subgoal */
    public void noteMatch(TriplePattern pattern, int pc) {
        TriplePattern match = pattern;
        int term = clause.termIndex(pc);   
        if (term >= 0) {                                 
            matches[term] = match;
        }
    }

    /**
     * Return the final instantiated goal given the current binding state.
     */
    public Triple getResult() {
        return new Triple(
                    LPInterpreter.deref(argVars[0]),
                    LPInterpreter.deref(argVars[1]), 
                    LPInterpreter.derefPossFunctor(argVars[2]));
    }
    
    /**
     * Return a safe copy of the list of matched subgoals in this subderivation.
     */
    public List<Triple> getMatchList() {
        ArrayList<Triple> matchList = new ArrayList<>();
        for ( TriplePattern matche : matches )
        {
            matchList.add( LPInterpreter.deref( matche ) );
        }
        return matchList;
    }
    
    /**
     * Create an initial derivation record for this frame, based on the given
     * argument registers.
     */
    public void initDerivationRecord(Node[] args) {
        System.arraycopy(args, 0, argVars, 0, RuleClauseCode.MAX_ARGUMENT_VARS);
    }    
    
}
