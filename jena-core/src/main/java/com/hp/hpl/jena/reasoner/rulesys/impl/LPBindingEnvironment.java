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
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * Implementation of the binding environment interface for use in LP
 * backward rules.
 */
public class LPBindingEnvironment implements BindingEnvironment {
    
    /** The interpreter which holds the context for this environment */
    protected LPInterpreter interpreter;
    
    /**
     * Constructor.
     */
    public LPBindingEnvironment(LPInterpreter interpeter) {
        this.interpreter = interpeter;
    }
    
    /**
     * Return the most ground version of the node. If the node is not a variable
     * just return it, if it is a varible bound in this environment return the binding,
     * if it is an unbound variable return the variable.
     */
    @Override
    public Node getGroundVersion(Node node) {
        return LPInterpreter.deref(node);
    }
    
    /**
     * Bind a variable in the current envionment to the given value.
     * Checks that the new binding is compatible with any current binding.
     * @param var a Node_RuleVariable defining the variable to bind
     * @param value the value to bind
     * @return false if the binding fails
     */
    @Override
    public boolean bind(Node var, Node value) {
        Node dvar = var;
        if (dvar instanceof Node_RuleVariable) dvar = ((Node_RuleVariable)dvar).deref();
        if (dvar instanceof Node_RuleVariable) {
            interpreter.bind(dvar, value);
            return true;
        } else {
            return var.sameValueAs(value);
        }

    }
    
     
    /**
     * Instantiate a triple pattern against the current environment.
     * This version handles unbound varibles by turning them into bNodes.
     * @param pattern the triple pattern to match
     * @return a new, instantiated triple
     */
    @Override
    public Triple instantiate(TriplePattern pattern) {
        Node s = getGroundVersion(pattern.getSubject());
        if (s.isVariable()) s = NodeFactory.createAnon();
        Node p = getGroundVersion(pattern.getPredicate());
        if (p.isVariable()) p = NodeFactory.createAnon();
        Node o = getGroundVersion(pattern.getObject());
        if (o.isVariable()) o = NodeFactory.createAnon();
        return new Triple(s, p, o);
    }


}
