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
package org.apache.jena.permissions.utils;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AuthenticationRequiredException;

/**
 * A filter for to filter ExtendedIterators on Statements. This filter removes
 * any triple that the user can not perform all the actions on.
 */
public class PermStatementFilter implements Predicate<Statement> {
    private final SecurityEvaluator evaluator;
    private final Node modelNode;
    private final Set<Action> actions;
    private final Object principal;

    /**
     * Creates a filter that requires that the user have all the permissions listed
     * in the actions parameter
     * 
     * @param action      The action the user must be permitted to perform.
     * @param securedItem The secured item that secures this iterator.
     */
    public PermStatementFilter(final Action action, final SecuredItem securedItem) {
        this.modelNode = securedItem.getModelNode();
        this.actions = SecurityEvaluator.Util.asSet(new Action[] { action });
        this.evaluator = securedItem.getSecurityEvaluator();
        this.principal = evaluator.getPrincipal();
    }

    /**
     * Creates a filter that requires that the user have all the permissions listed
     * in the actions parameter
     * 
     * @param action      The action the user must be permitted to perform.
     * @param securedItem The secured item that secures this iterator.
     * @param evaluator   The security evaluator to evaluate the security queries.
     */
    public PermStatementFilter(final Action action, final SecuredItem securedItem, final SecurityEvaluator evaluator) {
        this.modelNode = securedItem.getModelNode();
        this.actions = SecurityEvaluator.Util.asSet(new Action[] { action });
        this.evaluator = evaluator;
        this.principal = evaluator.getPrincipal();
    }

    /**
     * Creates a filter that requires that the user have all the permissions listed
     * in the actions parameter
     * 
     * @param actions     The actions the user must be permitted to perform.
     * @param securedItem The secured item that secures this iterator.
     */
    public PermStatementFilter(final Action[] actions, final SecuredItem securedItem) {
        this.modelNode = securedItem.getModelNode();
        this.actions = SecurityEvaluator.Util.asSet(actions);
        this.evaluator = securedItem.getSecurityEvaluator();
        this.principal = evaluator.getPrincipal();
    }

    /**
     * Creates a filter that requires that the user have all the permissions listed
     * in the actions parameter
     * 
     * @param actions     The actions the user must be permitted to perform.
     * @param securedItem The secured item that secures this iterator.
     * @param evaluator   The security evaluator to evaluate the security queries.
     */
    public PermStatementFilter(final Action[] actions, final SecuredItem securedItem,
            final SecurityEvaluator evaluator) {
        this.modelNode = securedItem.getModelNode();
        this.actions = SecurityEvaluator.Util.asSet(actions);
        this.evaluator = evaluator;
        this.principal = evaluator.getPrincipal();
    }

    /**
     * Creates a filter that requires that the user have all the permissions listed
     * in the actions parameter
     * 
     * @param actions     The actions the user must be permitted to perform.
     * @param securedItem The secured item that secures this iterator.
     */
    public PermStatementFilter(final Collection<Action> actions, final SecuredItem securedItem) {
        this.modelNode = securedItem.getModelNode();
        this.actions = SecurityEvaluator.Util.asSet(actions);
        this.evaluator = securedItem.getSecurityEvaluator();
        this.principal = evaluator.getPrincipal();
    }

    /**
     * Creates a filter that requires that the user have all the permissions listed
     * in the actions parameter
     * 
     * @param actions     The actions the user must be permitted to perform.
     * @param securedItem The secured item that secures this iterator.
     * @param evaluator   The security evaluator to evaluate the security queries.
     */
    public PermStatementFilter(final Collection<Action> actions, final SecuredItem securedItem,
            final SecurityEvaluator evaluator) {
        this.modelNode = securedItem.getModelNode();
        this.actions = SecurityEvaluator.Util.asSet(actions);
        this.evaluator = evaluator;
        this.principal = evaluator.getPrincipal();
    }

    @Override
    public boolean test(final Statement s) throws AuthenticationRequiredException {
        return evaluator.evaluateAny(principal, actions, modelNode, s.asTriple());
    }

}