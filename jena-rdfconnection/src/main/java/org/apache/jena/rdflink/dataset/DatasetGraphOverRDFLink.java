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

package org.apache.jena.rdflink.dataset;

import java.util.Optional;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.dataset.todelete.QueryExecWrapperCloseRDFLink;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.adapter.DatasetGraphSPARQL;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

/**
 * DatasetGraph implementation that implements all methods
 * against an RDFLink.
 * All returned iterators are backed by a fresh RDFLink instance.
 * The iterators must be closed to free the resources.
 */
public class DatasetGraphOverRDFLink
    extends DatasetGraphSPARQL
{
    private Creator<RDFLink> rdfLinkCreator;

    private boolean supportsTransactions;
    private boolean supportsTransactionAbort;

    private final TransactionalOverRDFLink transactional;

    public DatasetGraphOverRDFLink(Creator<RDFLink> rdfLinkCreator) {
        this(rdfLinkCreator, false, false);
    }

    public DatasetGraphOverRDFLink(Creator<RDFLink> rdfLinkCreator, boolean supportsTransactions, boolean supportsTransactionAbort) {
        super();
        this.rdfLinkCreator = rdfLinkCreator;
        this.supportsTransactions = supportsTransactions;
        this.supportsTransactionAbort = supportsTransactionAbort;

        this.transactional = new TransactionalOverRDFLink(rdfLinkCreator);
    }

    @Override
    protected Transactional getTransactional() {
        return transactional;
    }

    @Override
    public boolean supportsTransactions() {
        return supportsTransactions;
    }

    @Override
    public boolean supportsTransactionAbort() {
        return supportsTransactionAbort;
    }

    /** This method can be overridden. */
    public RDFLink newLink() {
        RDFLink link = rdfLinkCreator.create();
        return link;
    }

    protected Optional<RDFLink> activeLink() {
        return transactional.activeLink();
    }

    @Override
    protected QueryExec query(Query query) {
        QueryExec result;
        RDFLink activeLink = activeLink().orElse(null);
        if (activeLink != null) {
            result = activeLink.query(query);
        } else {
            RDFLink link = newLink();
            QueryExec base = link.query(query);
            result = new QueryExecWrapperCloseRDFLink(base, link);
        }
        return result;
    }

    @Override
    protected UpdateExec update(UpdateRequest update) {
        RDFLink activeLink = activeLink().orElse(null);
        if (activeLink != null) {
            return new UpdateExecOverRDFLink(() -> activeLink, false, null, null, false, update, null);
        }

        return new UpdateExecOverRDFLink(this::newLink, true, null, null, false, update, null);
    }
}

/**
 * Transactional implementation that creates a thread-local fresh
 * RDFLink whenever a connection a begun.
 */
class TransactionalOverRDFLink
    implements Transactional
{
    private Creator<RDFLink> rdfLinkCreator;
    private ThreadLocal<RDFLink> activeTxn = new ThreadLocal<>();

    public TransactionalOverRDFLink(Creator<RDFLink> rdfLinkCreator) {
        super();
        this.rdfLinkCreator = rdfLinkCreator;
    }

    public Optional<RDFLink> activeLink() {
        return Optional.ofNullable(activeTxn.get());
    }

    public RDFLink requireLink() {
        RDFLink result = activeTxn.get();
        if (result == null) {
            throw new JenaTransactionException("Not in a transaction");
        }
        return result;
    }

    @Override
    public void begin(TxnType type) {
        RDFLink tmp = activeTxn.get();
        if (tmp != null) {
            throw new JenaTransactionException("Transactions cannot be nested");
        }
        RDFLink result = rdfLinkCreator.create();
        activeTxn.set(result);
        result.begin(type);
    }

    @Override
    public boolean promote(Promote mode) {
        return requireLink().promote(mode);
    }

    @Override
    public void commit() {
        requireLink().commit();
    }

    @Override
    public void abort() {
        activeLink().ifPresent(RDFLink::abort);
    }

    @Override
    public void end() {
        try {
            activeLink().ifPresent(RDFLink::end);
        } finally {
            activeTxn.set(null);
        }
    }

    @Override
    public ReadWrite transactionMode() {
        return activeLink().map(RDFLink::transactionMode).orElse(null);
    }

    @Override
    public TxnType transactionType() {
        return activeLink().map(RDFLink::transactionType).orElse(null);
    }

    @Override
    public boolean isInTransaction() {
        RDFLink link = activeLink().orElse(null);
        if (link != null) {
             if (!link.isInTransaction()) {
                 // This code block should only be reached when called during .end()
                 // Otherwise, activeLink() is either non-null and in a transaction, or null and not in a transaction.
                 return false;
             }
             return true;
        }
        return false;
    }
}
