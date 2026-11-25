/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfpatch.filelog;

import java.io.OutputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.RDFPatchConst;
import org.apache.jena.rdfpatch.filelog.rotate.ManagedOutput;
import org.apache.jena.rdfpatch.system.URNs;
import org.apache.jena.rdfpatch.text.RDFChangesWriterText;
import org.apache.jena.rdfpatch.text.TokenWriterText;

/**
 * Log changes to a {@link ManagedOutput}.
 * Each transaction is written as a {@link ManagedOutput} section, opened lazily
 * when the writer emits its first output or completes the transaction.
 */
public class RDFChangesManagedOutput extends RDFChangesWriterText {

    private final ManagedOutput managedOutput;
    private final ThreadLocal<Boolean> pendingTxnBegin = new ThreadLocal<>();
    private OutputStream currentStream = null;
    private Thread ownerThread = null;
    private Node previous = null;

    public RDFChangesManagedOutput(ManagedOutput output) {
        super(null);
        this.managedOutput = output;
    }

    @Override
    public void txnBegin() {
        pendingTxnBegin.set(Boolean.TRUE);
    }

    @Override
    public synchronized void header(String field, Node value) {
        ensureOutput();
        super.header(field, value);
    }

    @Override
    public synchronized void add(Node g, Node s, Node p, Node o) {
        ensureOutput();
        super.add(g, s, p, o);
    }

    @Override
    public synchronized void delete(Node g, Node s, Node p, Node o) {
        ensureOutput();
        super.delete(g, s, p, o);
    }

    @Override
    public synchronized void addPrefix(Node gn, String prefix, String uriStr) {
        ensureOutput();
        super.addPrefix(gn, prefix, uriStr);
    }

    @Override
    public synchronized void deletePrefix(Node gn, String prefix) {
        ensureOutput();
        super.deletePrefix(gn, prefix);
    }

    @Override
    public synchronized void segment() {
        ensureOutput();
        super.segment();
    }

    @Override
    public synchronized void txnCommit() {
        try {
            ensureOutput();
            super.txnCommit();
        } finally {
            pendingTxnBegin.remove();
            if ( ownerThread == Thread.currentThread() )
                finishOutput();
        }
    }

    @Override
    public synchronized void txnAbort() {
        try {
            ensureOutput();
            super.txnAbort();
        } finally {
            pendingTxnBegin.remove();
            if ( ownerThread == Thread.currentThread() )
                finishOutput();
        }
    }

    @Override
    public synchronized void finish() {
        if ( currentStream == null )
            return;
        if ( ownerThread != Thread.currentThread() )
            throw new PatchException("Concurrent write to managed output: section active on thread " + ownerThread.getName());
        super.finish();
    }

    @Override
    public synchronized void close() {
        if ( currentStream == null ) {
            pendingTxnBegin.remove();
            return;
        }
        if ( ownerThread != Thread.currentThread() )
            throw new PatchException("Concurrent write to managed output: section active on thread " + ownerThread.getName());
        pendingTxnBegin.remove();
        finishOutput();
    }

    private void ensureOutput() {
        Thread current = Thread.currentThread();
        if ( currentStream != null ) {
            if ( ownerThread != current )
                throw new PatchException("Concurrent write to managed output: section active on thread " + ownerThread.getName());
            if ( Boolean.TRUE.equals(pendingTxnBegin.get()) ) {
                pendingTxnBegin.remove();
                super.txnBegin();
            }
            return;
        }
        startOutput(current);
        if ( Boolean.TRUE.equals(pendingTxnBegin.get()) ) {
            pendingTxnBegin.remove();
            super.txnBegin();
        }
    }

    private void startOutput(Thread current) {
        currentStream = managedOutput.output();
        ownerThread = current;
        boolean opened = false;
        try {
            super.tok = TokenWriterText.create(currentStream);
            Node id = URNs.unique();
            super.header(RDFPatchConst.ID, id);
            if ( previous != null )
                super.header(RDFPatchConst.PREV, previous);
            previous = id;
            opened = true;
        } finally {
            if ( !opened )
                releaseOutput();
        }
    }

    private void finishOutput() {
        try {
            if ( super.tok != null )
                super.tok.flush();
        } finally {
            releaseOutput();
        }
    }

    private void releaseOutput() {
        IO.close(currentStream);
        currentStream = null;
        super.tok = null;
        ownerThread = null;
    }
}
