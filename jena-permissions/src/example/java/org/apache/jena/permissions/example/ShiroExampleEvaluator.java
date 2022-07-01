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
package org.apache.jena.permissions.example;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to use Shiro to provide credentials.
 * 
 * An example evaluator that only provides access to messages in the graph that
 * are from or to the principal.
 *
 */
public class ShiroExampleEvaluator implements SecurityEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(ShiroExampleEvaluator.class);
    // the model that contains the messages.
    private Model model;
    private RDFNode msgType = ResourceFactory.createResource("http://example.com/msg");
    private Property pTo = ResourceFactory.createProperty("http://example.com/to");
    private Property pFrom = ResourceFactory.createProperty("http://example.com/from");

    /**
     * 
     * @param model The graph we are going to evaluate against.
     */
    public ShiroExampleEvaluator(Model model) {
        this.model = model;
    }

    /**
     * We allow any action on the graph itself, so this is always true.
     */
    @Override
    public boolean evaluate(Object principal, Action action, Node graphIRI) {
        // we allow any action on a graph.
        return true;
    }

    /**
     * This is our internal check to see if the user may access the resource. This
     * method is called from the evaluate(Object,Node) method. A user may only
     * access the resource if they are authenticated, and are either the sender or
     * the recipient. Additionally the admin can always see the messages.
     * 
     * @param principalObj
     * @param r
     * @return
     */
    private boolean evaluate(Object principalObj, Resource r) {
        // cast to the Subject because we know that it comes from Shiro and that
        // our getPrincipal() method returns a Subject.
        Subject subject = (Subject) principalObj;
        if (!subject.isAuthenticated()) {
            // we could throw an AuthenticationRequiredException but
            // in our case we just return false.
            LOG.info("User not authenticated");
            return false;
        }
        // a message is only available to sender or recipient
        LOG.debug("checking {}", subject.getPrincipal());
        Object principal = subject.getPrincipal();

        // We put the admin check here but it could have been done much earlier.
        if ("admin".equals(principal.toString())) {
            return true;
        }
        // if we are looking at a message object then check the restrictions.
        if (r.hasProperty(RDF.type, msgType)) {
            return r.hasProperty(pTo, subject.getPrincipal().toString())
                    || r.hasProperty(pFrom, subject.getPrincipal().toString());
        }
        // otherwise user can see the object.
        return true;
    }

    /**
     * Check that the user can see a specific node.
     * 
     * @param principal
     * @param node
     * @return
     */
    private boolean evaluate(Object principal, Node node) {
        // Access to wild card is false -- this forces checks to the acutal nodes
        // to be returned.
        // we could have checked for admin access here and returned true since the admin
        // can see any node.
        if (node.equals(Node.ANY)) {
            return false;
        }

        // URI nodes and blank nodes are retrieved from the model and evaluated
        if (node.isURI() || node.isBlank()) {
            Resource r = model.getRDFNode(node).asResource();
            return evaluate(principal, r);
        }
        // anything else (literals) can be seen.
        return true;
    }

    /**
     * Evaluate if the user can see the triple.
     * 
     * @param principal
     * @param triple
     * @return
     */
    private boolean evaluate(Object principal, Triple triple) {
        // we could have checked here to see if the principal was the admin and
        // just returned true since the admin can perform any operation on any triple.
        return evaluate(principal, triple.getSubject()) && evaluate(principal, triple.getObject())
                && evaluate(principal, triple.getPredicate());
    }

    /**
     * As per our design, users can do anything with triples they have access to, so
     * we just ignore the action parameter. If we were to implement rules restricted
     * access based upon action this method would sort those out appropriately.
     */
    @Override
    public boolean evaluate(Object principal, Action action, Node graphIRI, Triple triple) {
        // we could have checked here to see if the principal was the admin and
        // just returned true since the admin can perform any operation on any triple.
        return evaluate(principal, triple);
    }

    /**
     * As per our design, users can access any graph. If we were to implement rules
     * that restricted user access to specific graphs, those checks would be here
     * and we would return <code>false</code> if they were not allowed to access the
     * graph. Note that this method is checking to see that the user may perform ALL
     * the actions in the set on the graph.
     */
    @Override
    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI) {
        return true;
    }

    /**
     * As per our design, users can access any triple from a message that is from or
     * to them. Since we don't have restrictions on actions this is no different
     * then checking access for a single action.
     */
    @Override
    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
        return evaluate(principal, triple);
    }

    /**
     * As per our design, users can access any graph. If we were to implement rules
     * that restricted user access to specific graphs, those checks would be here
     * and we would return <code>false</code> if they were not allowed to access the
     * graph. Note that this method is checking to see that the user may perform ANY
     * of the actions in the set on the graph.
     */
    @Override
    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI) {
        return true;
    }

    /**
     * As per our design, users can access any triple from a message that is from or
     * to them. Since we don't have restrictions on actions this is no different
     * then checking access for a single action.
     */
    @Override
    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI, Triple triple) {
        return evaluate(principal, triple);
    }

    /**
     * As per our design, users can access any triple from a message that is from or
     * to them. So for an update they can only change triples they have access to
     * into other triples they have access to. (e.g. they can not remove themselves
     * from the message).
     */
    @Override
    public boolean evaluateUpdate(Object principal, Node graphIRI, Triple from, Triple to) {
        return evaluate(principal, from) && evaluate(principal, to);
    }

    /**
     * Return the Shiro subject. This is the subject that Shiro currently has logged
     * in.
     */
    @Override
    public Object getPrincipal() {
        return SecurityUtils.getSubject();
    }

    /**
     * Verify the Shiro subject is authenticated.
     */
    @Override
    public boolean isPrincipalAuthenticated(Object principal) {
        if (principal instanceof Subject) {
            return ((Subject) principal).isAuthenticated();
        }
        return false;
    }

}
