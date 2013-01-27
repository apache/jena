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
 * A set of constants used to record state information in the
 * backchaining rule interepreter. 
 */
public class StateFlag {
    
    /** Label for printing */
    private String label;

    /** Indicates a goal has failed and return no more answers at this time */
    public static final StateFlag FAIL = new StateFlag("FAIL");
    
    /** Indicates that all currently available results have been returned and
     *  the goal should be suspended into new subgoal results have been generated */
    public static final StateFlag SUSPEND = new StateFlag("SUSPEND");
    
    /** Indicates that the goal remains active */
    public static final StateFlag ACTIVE = new StateFlag("ACTIVE");
    
    /** Indicates a fully satisfied goal */
    public static final StateFlag SATISFIED = new StateFlag("SATISFIED");
    
    /** Constructor */
    private StateFlag(String label) {
        this.label = label;
    }
    
    /** Print string */
    @Override
    public String toString() {
        return label;
    }
}
