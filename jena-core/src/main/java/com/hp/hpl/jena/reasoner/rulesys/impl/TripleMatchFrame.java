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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Frame on the choice point stack used to represent the state of a direct
 * graph triple match.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 */
public class TripleMatchFrame extends GenericTripleMatchFrame {
    
    /** An iterator over triples matching a goal */
    ExtendedIterator<Triple> matchIterator;
    
    /**
     * Constructor.
     * Initialize the triple match to preserve the current context of the given
     * LPInterpreter and search for the match defined by the current argument registers
     * @param interpreter the interpreter instance whose env, trail and arg values are to be preserved
     */
    public TripleMatchFrame(LPInterpreter interpreter) {
        init(interpreter);
    }

    /**
     * Find the next result triple and bind the result vars appropriately.
     * @param interpreter the calling interpreter whose trail should be used
     * @return false if there are no more matches in the iterator.
     */
    public boolean nextMatch(LPInterpreter interpreter) {
        while (matchIterator.hasNext()) {
            if (bindResult(matchIterator.next(), interpreter)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Initialize the triple match to preserve the current context of the given
     * LPInterpreter and search for the match defined by the current argument registers
     * @param interpreter the interpreter instance whose env, trail and arg values are to be preserved
     */
    @Override public void init(LPInterpreter interpreter) {
        super.init(interpreter);
        this.matchIterator = interpreter.getEngine().getInfGraph().findDataMatches(goal);
    }
    
    /**
     * Override close method to reclaim the iterator.
     */
    @Override public void close() {
        if (matchIterator != null) matchIterator.close();
    }
    
}
