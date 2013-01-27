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
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Represents a single frame in the LP interpreter's environment stack. The
 * environment stack represents the AND part of the search tree - it is a sequence
 * of nested predicate calls.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 */
public class EnvironmentFrame extends FrameObject {

    /** The set of permanent variables Y(i) in use by this frame.  */
    Node[] pVars;
    
    /** The code the the clause currently being processed */
    RuleClauseCode clause;
    
    /** The continuation program counter offet in the parent clause's byte code */
    int cpc;
    
    /** The continuation argument counter offset in the parent clause's arg stream */
    int cac;
    
     /** 
     * Constructor 
     * @param clause the compiled code being interpreted by this env frame 
     */
    public EnvironmentFrame(RuleClauseCode clause) {
        this.clause = clause;
    }
        
    /**
     * Allocate a vector of permanent variables for use in the rule execution.
     */
    public void allocate(int n) {
            pVars = new Node[n];
    }
           
    /**
     * Return the rule associated with this environment, null if no such rule.
     */
    public Rule getRule() {
        if (clause != null) {
            return clause.rule;
        } else {
            return null;
        }
    }
    
    /**
     * Printable string for debugging.
     */
    @Override
    public String toString() {
        if (clause == null || clause.rule == null) {
            return "[anon]";
        } else {
            return "[" + clause.rule.toShortString() + "]";
        }
    }
}
