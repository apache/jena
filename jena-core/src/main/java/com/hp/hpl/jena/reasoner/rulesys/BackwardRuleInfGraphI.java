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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This interface collects together those operations that the backchaining
 * engine needs to invoke in the parent InfGraph. This allows different inf graphs
 * to exploit the same core backchaining engine.
 */
public interface BackwardRuleInfGraphI extends SilentAddI, InfGraph {
            
    /**
     * Process a call to a builtin predicate
     * @param clause the term representing the call
     * @param env the BindingEnvironment for this call
     * @param rule the rule which is invoking this call
     * @return true if the predicate succeeds
     */
    public boolean processBuiltin(ClauseEntry clause, Rule rule, BindingEnvironment env);

    /**
     * Match a pattern just against the stored data (raw data, schema,
     * axioms) but no backchaining derivation.
     */
    public ExtendedIterator<Triple> findDataMatches(TriplePattern pattern);

    /**
     * Logger a dervivation record against the given triple.
     */
    public void logDerivation(Triple t, Derivation derivation);

    /**
     * Retrieve or create a bNode representing an inferred property value.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    public Node getTemp(Node instance, Node prop, Node pclass);

    /**
     * Return a version stamp for this graph which can be
     * used to fast-fail concurrent modification exceptions.
     */
    public int getVersion();
    
}
