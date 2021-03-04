/**
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
package org.apache.jena.permissions.example.readonly;

import java.util.Set;
import java.util.HashSet;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.shared.AuthenticationRequiredException;

/**
 * An example of a security evaluator that creates read-only graphs and models.
 * 
 * This evaluator does this by only allowing the Action.READ action, all others
 * are denied.
 *
 */
public class ReadOnlyEval implements SecurityEvaluator {

    @Override
    public boolean evaluate(Object principal, Action action, Node graphIRI) throws AuthenticationRequiredException {
        return Action.Read.equals(action);
    }

    @Override
    public boolean evaluate(Object principal, Action action, Node graphIRI, Triple triple)
            throws AuthenticationRequiredException {
        return Action.Read.equals(action);
    }

    @Override
    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI)
            throws AuthenticationRequiredException {
        Set<Action> s = new HashSet<Action>(actions);
        s.remove(Action.Read);
        return s.isEmpty();
    }

    @Override
    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI, Triple triple)
            throws AuthenticationRequiredException {
        Set<Action> s = new HashSet<Action>(actions);
        s.remove(Action.Read);
        return s.isEmpty();
    }

    @Override
    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI)
            throws AuthenticationRequiredException {
        return actions.contains(Action.Read);
    }

    @Override
    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI, Triple triple)
            throws AuthenticationRequiredException {
        return actions.contains(Action.Read);
    }

    /**
     * No updated are allowed.
     */
    @Override
    public boolean evaluateUpdate(Object principal, Node graphIRI, Triple from, Triple to)
            throws AuthenticationRequiredException {
        return false;
    }

    /**
     * We really don't need a system principal so we just create a dummy one.
     */
    @Override
    public Object getPrincipal() {
        return "ReadOnlyResourcePrincipal";
    }

    /**
     * Our dummy principal is never authenticated.
     */
    @Override
    public boolean isPrincipalAuthenticated(Object principal) {
        return false;
    }

}
