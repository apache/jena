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
package org.apache.jena.permissions.model.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.rdf.model.RSIterator;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A secured RSIterator implementation
 */
public class SecuredRSIterator implements RSIterator {
    private class PermReifiedStatementFilter implements Predicate<ReifiedStatement> {
        private final SecurityEvaluator evaluator;
        private final Node modelNode;
        private final Set<Action> actions;

        public PermReifiedStatementFilter(final Action[] actions, final SecuredModel securedModel) {
            this.modelNode = securedModel.getModelNode();
            this.actions = SecurityEvaluator.Util.asSet(actions);
            this.evaluator = securedModel.getSecurityEvaluator();
        }

        @Override
        public boolean test(final ReifiedStatement t) {
            return evaluator.evaluateAny(evaluator.getPrincipal(), actions, modelNode, t.getStatement().asTriple());
        }

    }

    private class PermReifiedStatementMap implements Function<ReifiedStatement, ReifiedStatement> {
        private final SecuredModel securedModel;

        public PermReifiedStatementMap(final SecuredModel securedModel) {
            this.securedModel = securedModel;
        }

        @Override
        public ReifiedStatement apply(final ReifiedStatement o) {
            return SecuredReifiedStatementImpl.getInstance(securedModel, o);
        }
    }

    private final ExtendedIterator<ReifiedStatement> iter;

    /**
     * Constructor
     * 
     * @param securedModel The secured model that provides the security context
     * @param wrapped      The wrapped iterator.
     */
    public SecuredRSIterator(final SecuredModel securedModel, final ExtendedIterator<ReifiedStatement> wrapped) {
        final PermReifiedStatementFilter filter = new PermReifiedStatementFilter(new Action[] { Action.Read },
                securedModel);
        final PermReifiedStatementMap map1 = new PermReifiedStatementMap(securedModel);
        iter = wrapped.filterKeep(filter).mapWith(map1);
    }

    @Override
    public <X extends ReifiedStatement> ExtendedIterator<ReifiedStatement> andThen(final Iterator<X> other) {
        return iter.andThen(other);
    }

    @Override
    public void close() {
        iter.close();
    }

    @Override
    public ExtendedIterator<ReifiedStatement> filterDrop(final Predicate<ReifiedStatement> f) {
        return iter.filterDrop(f);
    }

    @Override
    public ExtendedIterator<ReifiedStatement> filterKeep(final Predicate<ReifiedStatement> f) {
        return iter.filterKeep(f);
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public <U> ExtendedIterator<U> mapWith(final Function<ReifiedStatement, U> map1) {
        return iter.mapWith(map1);
    }

    @Override
    public ReifiedStatement next() {
        return iter.next();
    }

    @Override
    public ReifiedStatement nextRS() {
        return next();
    }

    @Override
    public void remove() {
        iter.remove();
    }

    @Override
    public ReifiedStatement removeNext() {
        return iter.removeNext();
    }

    @Override
    public List<ReifiedStatement> toList() {
        return iter.toList();
    }

    @Override
    public Set<ReifiedStatement> toSet() {
        return iter.toSet();
    }
}
