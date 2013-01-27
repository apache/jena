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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This interface collects together the operations on the InfGraph which
 * are needed to support the forward rule engine. 
 */
public interface ForwardRuleInfGraphI extends InfGraph, SilentAddI {
    
    /**
     * Return true if tracing should be acted on - i.e. if traceOn is true
     * and we are past the bootstrap phase.
     */
    public boolean shouldTrace();
        
    /**
     * Adds a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void addBRule(Rule brule);
        
    /**
     * Deletes a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void deleteBRule(Rule brule);
    
    /**
     * Return the Graph containing all the static deductions available so far.
     * Triggers a prepare if the graph has not been prepared already.
     */
    @Override
    public Graph getDeductionsGraph();
    
    /**
     * Return the Graph containing all the static deductions available so far.
     * Does not trigger a prepare action.
     */
    public Graph getCurrentDeductionsGraph();
    
    /**
     * Add a new deduction to the deductions graph.
     */
    public void addDeduction(Triple t);
    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
     */
    public ExtendedIterator<Triple> findDataMatches(Node subject, Node predicate, Node object);

    /**
     * Return true if derivation logging is enabled.
     */
    public boolean shouldLogDerivations();
    
    /**
     * Logger a dervivation record against the given triple.
     */
    public void logDerivation(Triple t, Derivation derivation);
    
    /**
     * Set to true to cause functor-valued literals to be dropped from rule output.
     * Default is true.
     */
    public void setFunctorFiltering(boolean param) ;

}
