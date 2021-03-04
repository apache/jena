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

import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.vocabulary.RDF;

/**
 * An RDF List security filter. Filters out nodes the user can not see.
 * 
 * @param <T>
 */
public class RDFListSecFilter<T extends RDFList> implements Predicate<T> {
    private final SecuredItem securedItem;
    private final Set<Action> perms;
    private final Object principal;

    /**
     * Constructor.
     * 
     * @param securedItem The secured item for permission checks.
     * @param perm        the permission that user must have to access the item.
     */
    public RDFListSecFilter(final SecuredItem securedItem, final Action perm) {
        this(securedItem, SecurityEvaluator.Util.asSet(new Action[] { perm }));
    }

    /**
     * Constructor
     * 
     * @param securedItem The secured item for permission checks.
     * @param perms       the set of permissions that user must have to access the
     *                    item.
     */
    public RDFListSecFilter(final SecuredItem securedItem, final Set<Action> perms) {
        this.securedItem = securedItem;
        this.perms = perms;
        this.principal = securedItem.getSecurityEvaluator().getPrincipal();
    }

    /**
     * Returns true if the user has access to the RDFList item.
     */
    @Override
    public boolean test(final RDFList o) throws AuthenticationRequiredException {
        final Statement s = o.getRequiredProperty(RDF.first);
        return securedItem.getSecurityEvaluator().evaluate(principal, perms, securedItem.getModelNode(), s.asTriple());
    }
}
