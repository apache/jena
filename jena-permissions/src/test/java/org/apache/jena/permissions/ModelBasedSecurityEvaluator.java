/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;

public class ModelBasedSecurityEvaluator implements SecurityEvaluator {

    // private Model model;

    public ModelBasedSecurityEvaluator(Model model) {
        // this.model = model;
    }

    @Override
    public boolean evaluate(final Object principal, Action action, Node graphIRI) {
        return true;
    }

    @Override
    public boolean evaluate(final Object principal, Action action, Node graphIRI, Triple triple) {
        return true;
    }

    @Override
    public boolean evaluate(final Object principal, Set<Action> actions, Node graphIRI) {
        return true;
    }

    @Override
    public boolean evaluate(final Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
        return true;
    }

    @Override
    public boolean evaluateAny(final Object principal, Set<Action> actions, Node graphIRI) {
        return true;
    }

    @Override
    public boolean evaluateAny(final Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
        return true;
    }

    @Override
    public boolean evaluateUpdate(final Object principal, Node graphIRI, Triple from, Triple to) {
        return true;
    }

    @Override
    public Object getPrincipal() {
        return "principal";
    }

    @Override
    public boolean isPrincipalAuthenticated(Object principal) {
        return principal != null;
    }

}
