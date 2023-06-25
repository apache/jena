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
package org.apache.jena.permissions.example;

import java.security.Principal;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.vocabulary.RDF;

/**
 * An example evaluator that only provides access to messages in the graph that
 * are from or to the principal.
 *
 */
public class ExampleEvaluator implements SecurityEvaluator {

    private Principal principal;
    private Model model;
    private RDFNode msgType = ResourceFactory.createResource("http://example.com/msg");
    private Property pTo = ResourceFactory.createProperty("http://example.com/to");
    private Property pFrom = ResourceFactory.createProperty("http://example.com/from");

    /**
     *
     * @param model The graph we are going to evaluate against.
     */
    public ExampleEvaluator(Model model) {
        this.model = model;
    }

    @Override
    public boolean evaluate(Object principal, Action action, Node graphIRI) {
        // we allow any action on a graph.
        return true;
    }

    // not that in this implementation all permission checks flow through
    // this method. We can do this because we have a simple permissions
    // requirement. A more complex set of permissions requirement would
    // require a different strategy.
    private boolean evaluate(Object principalObj, Resource r) {
        Principal principal = (Principal) principalObj;
        // we do not allow anonymous (un-authenticated) reads of data.
        // Another strategy would be to only require authentication if the
        // data being requested was restricted -- but that is a more complex
        // process and not suitable for this simple example.
        if (principal == null) {
            throw new AuthenticationRequiredException();
        }

        // a message is only available to sender or recipient
        if (r.hasProperty(RDF.type, msgType)) {
            return r.hasProperty(pTo, principal.getName()) || r.hasProperty(pFrom, principal.getName());
        }
        return true;
    }

    private boolean evaluate(Object principal, Node node) {
        if (node.equals(Node.ANY)) {
            return false; // all wild cards are false
        }

        if (node.isURI() || node.isBlank()) {
            Resource r = model.getRDFNode(node).asResource();
            return evaluate(principal, r);
        }
        return true;
    }

    private boolean evaluate(Object principal, Triple triple) {
        return evaluate(principal, triple.getSubject()) && evaluate(principal, triple.getObject())
                && evaluate(principal, triple.getPredicate());
    }

    @Override
    public boolean evaluate(Object principal, Action action, Node graphIRI, Triple triple) {
        return evaluate(principal, triple);
    }

    @Override
    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI) {
        return true;
    }

    @Override
    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
        return evaluate(principal, triple);
    }

    @Override
    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI) {
        return true;
    }

    @Override
    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
        return evaluate(principal, triple);
    }

    @Override
    public boolean evaluateUpdate(Object principal, Node graphIRI, Triple from, Triple to) {
        return evaluate(principal, from) && evaluate(principal, to);
    }

    public void setPrincipal(String userName) {
        if (userName == null) {
            principal = null;
        }
        principal = new Principal() {
            @Override
            public String getName() {
                return userName;
            }};
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isPrincipalAuthenticated(Object principal) {
        return principal != null;
    }

}
