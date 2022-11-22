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

package org.apache.jena.rdfpatch.system;

import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.impl.TransactionHandlerBase;
import org.apache.jena.rdfpatch.RDFChanges;

/** A graph {@link TransactionHandler} that provides monitoring callbacks. */
class TransactionHandlerMonitor extends TransactionHandlerBase {

    private final TransactionHandler handler;
    private final RDFChanges changes;

    public TransactionHandlerMonitor(TransactionHandler handler, RDFChanges changes) {
        this.handler = handler;
        this.changes = changes;
    }

    @Override
    public boolean transactionsSupported() {
        return handler.transactionsSupported();
    }

    @Override
    public void begin() {
        changes.txnBegin();
        handler.begin();
    }

    @Override
    public void commit() {
        // Must be in this order - log the commit, then do it.
        // Recovery from the log wil replay the changes do the log is more important
        // than the graph as the source of truth.
        handler.commit();
        changes.txnCommit();
    }

    @Override
    public void abort() {
        // Must be in this order - record the abort then do it
        // (a crash between will cause an abort).
        changes.txnAbort();
        handler.abort();
    }
}