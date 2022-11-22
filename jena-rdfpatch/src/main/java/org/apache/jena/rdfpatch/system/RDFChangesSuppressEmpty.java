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

import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.changes.RDFChangesWrapper;

/** Note whether a change has happened and call different operations on txnCommit
 *  A change is a call to one of the dataset-changing operations,
 *  {@code add}, {@code delete}, {@code addPrefix}, {@code deletePrefix}.
 *  Setting headers is not consided a change.
 *  <p>
 *  If a change has been made, {@link #txnChangeCommit()} is called, otherwise
 *  {@link #txnNoChangeCommit()} is called.  The implementation of these methods must call
 *  {@link #doCommit()}, to accept the change or {@link #doAbort()} to cancel it.
 *  <p>
 *  A common case is to suppress empty transactions.
 *  To do this, call {@link #doAbort()} in {@link #txnNoChangeCommit()}
 */
public class RDFChangesSuppressEmpty extends RDFChangesWrapper {
    // TODO Add RDFChanges.cancel.

    public RDFChangesSuppressEmpty(RDFChanges other) {
        super(other);
    }

    private boolean changeHappened = false;
    protected boolean changeHappened()  { return changeHappened; }

    private void markChanged() {
        changeHappened = true;
    }

//    @Override
//    public void start() {}
//
//    @Override
//    public void finish() {}

    // Headers do not count as "changes".
    // A patch must have certain headers for use with a patch log - an id, and a prev.
    @Override
    public void header(String field, Node value) {
        super.header(field, value);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        markChanged();
        super.add(g, s, p, o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        markChanged();
        super.delete(g, s, p, o);
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        markChanged();
        super.addPrefix(gn, prefix, uriStr);
    }

    @Override
    public final void deletePrefix(Node gn, String prefix) {
        markChanged();
        super.deletePrefix(gn, prefix);
    }

    @Override
    public final void txnBegin() {
        changeHappened = false;
        super.txnBegin();
    }

    @Override
    public final void txnCommit() {
        if ( changeHappened )
            txnChangeCommit();
        else
            txnNoChangeCommit();
        changeHappened = false;
    }

    @Override
    public final void txnAbort() {
        txnAborting();
        changeHappened = false;
    }

    /**
     * Called when commit called and there were no changes made (no calls to add/delete
     * operations of quads or prefixes).
     * <p>
     * The default implementation is to call
     * {@link #doAbort()}.
     * <p>
     * If this is a call to {@link #doCommit()}
     * then the process is a no-op.
     */
    protected void txnNoChangeCommit() {
        doAbort();
    }

    /**
     * Called when commit called and there were were changes made.
     * The default implementation is to call {@link #doCommit()}.
     */
    protected void txnChangeCommit() {
        doCommit();
    }

    /** Called when abort called. */
    protected void txnAborting() {
        doAbort();
    }

    // Let subclasses call the wrapped txnCommit()
    protected void doCommit() {
        super.txnCommit();
    }

    // Let subclasses call the wrapped txnAbort()
    protected void doAbort() {
        super.txnAbort();
    }

}
