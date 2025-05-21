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

import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.*;
import org.apache.jena.rdf.model.*;

/**
 * Implementation of SecuredStatement to be used by a SecuredItemInvoker proxy.
 */
@SuppressWarnings("removal")
public class SecuredStatementTermImpl extends SecuredRDFNodeImpl implements SecuredStatementTerm {
    /**
     * get a SecuredStatement
     *
     * @param securedModel     The secured model that provides the security context
     * @param stmtTerm         The statement term to secure.
     * @return the SecuredStatement
     */
    public static SecuredStatementTerm getInstance(final SecuredModel securedModel, final StatementTerm stmtTerm) {
        if (securedModel == null) {
            throw new IllegalArgumentException("Secured securedModel may not be null");
        }
        if (stmtTerm == null) {
            throw new IllegalArgumentException("StatemenTerm may not be null");
        }

        final ItemHolder<StatementTerm, SecuredStatementTerm> holder = new ItemHolder<>(stmtTerm);

        final SecuredStatementTermImpl checker = new SecuredStatementTermImpl(securedModel, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (stmtTerm instanceof SecuredStatementTerm) {
            if (checker.isEquivalent((SecuredStatement) stmtTerm)) {
                return (SecuredStatementTerm) stmtTerm;
            }
        }
        return holder.setSecuredItem(new SecuredItemInvoker(holder.getBaseItem().getClass(), checker));
    }

    // the item holder that contains this SecuredStatement.
    private final ItemHolder<StatementTerm, SecuredStatementTerm> holder;

    private final SecuredModel securedModel;

    /**
     * Constructor.
     *
     * @param securityEvaluator The security evaluator to use.
     * @param graphIRI          the graph IRI to verify against.
     * @param holder            The item holder that will contain this
     *                          SecuredStatement.
     */
    private SecuredStatementTermImpl(final SecuredModel securedModel,
            final ItemHolder<StatementTerm, SecuredStatementTerm> holder) {
        super(securedModel, holder);
        this.holder = holder;
        this.securedModel = securedModel;
    }

    @Override
    public SecuredLiteral asLiteral() {
        checkRead();
        throw new LiteralRequiredException(asNode());
    }

    @Override
    public SecuredResource asResource() {
        checkRead();
        throw new ResourceRequiredException(asNode());
    }

    @Override
    public SecuredStatementTerm asStatementTerm() {
        checkRead();
        return this;
    }

    @Override
    public Statement getStatement() {
        checkRead();
        return SecuredStatementImpl.getInstance(securedModel, holder.getBaseItem().getStatement());
    }

    @Override
    public Object visitWith(RDFVisitor rv) {
        return rv.visitStmt(this, this.getStatement());
    }
}
