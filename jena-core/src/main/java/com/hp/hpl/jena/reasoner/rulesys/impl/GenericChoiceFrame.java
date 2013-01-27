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

/**
 * Core properties of choice frames used use to represent the OR state of
 * the backtracking search. Specific variants of this need to preserve additional
 * choice state.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 */
public class GenericChoiceFrame extends FrameObject {

    /** The environment frame describing the state of the AND tree at this choice point */
    EnvironmentFrame envFrame;

    /** The top of the trail stack at the time of the call */
    int trailIndex;
    
    /** The continuation program counter offet in the parent clause's byte code */
    int cpc;
    
    /** The continuation argument counter offset in the parent clause's arg stream */
    int cac;

    /**
     * Initialize a choice point to preserve the current context of the given intepreter 
     * and then call the given set of predicates.
     * @param interpreter the LPInterpreter whose state is to be preserved
     */
    public void init(LPInterpreter interpreter) {
        envFrame = interpreter.envFrame;
        trailIndex = interpreter.trail.size();
    }

    /**
     * Set the continuation point for this frame.
     */
    public void setContinuation(int pc, int ac) {
        cpc = pc;
        cac = ac; 
    }

}
