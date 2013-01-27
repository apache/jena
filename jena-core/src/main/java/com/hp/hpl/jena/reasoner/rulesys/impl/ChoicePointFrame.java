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

import com.hp.hpl.jena.graph.Node;

import java.util.*;

/**
 * Represents a single frame in the LP interpreter's choice point stack,
 * represents the OR part of the search tree.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 */
public class ChoicePointFrame extends GenericChoiceFrame {

    /** The set of argument variables for the call */
    Node[] argVars = new Node[RuleClauseCode.MAX_ARGUMENT_VARS];

    /** Iterator over the clauses being searched */
    Iterator<RuleClauseCode> clauseIterator;
    
    /** Flag that this is a singleton choice point */
    boolean isSingleton = false;
    
    /**
     * Constructor.
     * Initialize a choice point to preserve the current context of the given intepreter 
     * and then call the given set of predicates.
     * @param interpreter the LPInterpreter whose state is to be preserved
     * @param predicateClauses the list of predicates for this choice point
     * @param isSingleton true if this choice should abort after one successful result
     */
    public ChoicePointFrame(LPInterpreter interpreter, List<RuleClauseCode> predicateClauses, boolean isSingleton) {
        init(interpreter, predicateClauses);
        this.isSingleton = isSingleton;
    }

    /**
     * Initialize a choice point to preserve the current context of the given intepreter 
     * and then call the given set of predicates.
     * @param interpreter the LPInterpreter whose state is to be preserved
     * @param predicateClauses the list of predicates for this choice point
     */
    public void init(LPInterpreter interpreter, List<RuleClauseCode> predicateClauses) {
        super.init(interpreter);
        System.arraycopy(interpreter.argVars, 0, argVars, 0, argVars.length);
        clauseIterator = predicateClauses.iterator();
    }
    
    /**
     * Is there another clause in the sequence?
     */
    public boolean hasNext() {
        if (clauseIterator == null) {
            return false;
        } else {
            return clauseIterator.hasNext();
        }
    }
    
    /**
     * Return the next clause in the sequence.
     */
    public RuleClauseCode nextClause() {
        if (clauseIterator == null) return null;
        return clauseIterator.next();
    }

    /**
     * Note successful return from this choice point. This closes
     * the choice point if it is a singleton.
     */
    public void noteSuccess() {
        if (isSingleton) {
            clauseIterator = null;
        }
    }
}
