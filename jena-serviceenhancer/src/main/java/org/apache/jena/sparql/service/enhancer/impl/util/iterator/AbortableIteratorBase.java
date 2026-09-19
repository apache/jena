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

package org.apache.jena.sparql.service.enhancer.impl.util.iterator;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.Printable;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFatalException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.service.enhancer.impl.util.AutoCloseableWithLeakDetectionBase;
import org.apache.jena.sparql.util.QueryOutputUtils;

/** This is a generalization of QueryIterator to a generic type T. */
public abstract class AbortableIteratorBase<T>
    extends AutoCloseableWithLeakDetectionBase
    implements AbortableIterator<T>
{
    private boolean finished = false;

    // === Cancellation

    // .cancel() can be called asynchronously with iterator execution.
    // It causes notification to cancellation to be made, once, by calling .requestCancel()
    // which is called synchronously with .cancel() and asynchronously with iterator execution.
    private final AtomicBoolean requestingCancel;
    private volatile boolean cancelOnce = false;
    private Object cancelLock = new Object();

    /** QueryIteratorBase with no cancellation facility */
    protected AbortableIteratorBase() {
        // No async cancellation.
        this(null);
    }

    /** Argument : shared flag for cancellation. */
    protected AbortableIteratorBase(AtomicBoolean cancelSignal) {
        super(true);
        requestingCancel = (cancelSignal == null)
            ? new AtomicBoolean(false) // Allows for direct cancel (not timeout).
            : cancelSignal;
    }

    private boolean requestingCancel() {
        return (requestingCancel != null && requestingCancel.get()) || Thread.interrupted() ;
    }

    // -------- The contract with the subclasses

    /**
     * Implement this, not hasNext().
     * Do not throw {@link NoSuchElementException}.
     */
    protected abstract boolean hasNextBinding();

    /**
     * Implement this, not next() or nextBinding().
     * Returning null is turned into
     * NoSuchElementException. Does not need to call hasNextBinding.
     */
    protected abstract T moveToNextBinding();

    /** Close the iterator. */
    protected abstract void closeIterator();

    /** Propagates the cancellation request - called asynchronously with the iterator itself */
    protected abstract void requestCancel();

    /* package */ boolean getRequestingCancel() {
        return requestingCancel();
    }

    // -------- The contract with the subclasses

    protected boolean isFinished() { return finished; }

    /** final - subclasses implement hasNextBinding() */
    @Override
    public final boolean hasNext() {
        if ( finished )
            // Even if aborted. Finished is finished.
            return false;

        if ( cancelOnce ) {
            // Try to close first to release resources (in case the user
            // doesn't have a close() call in a finally block)
            close();
            throw new QueryCancelledException();
        }

        // Handles exceptions
        boolean r = hasNextBinding();

        if ( r == false )
            try {
                close();
            } catch (QueryFatalException ex) {
                Log.error(this, "Fatal exception: " + ex.getMessage());
                throw ex;      // And pass on up the exception.
            }
        return r;
    }

    /**
     * final - autoclose and registration relies on it - implement
     * moveToNextBinding()
     */
    @Override
    public final T next() {
        return nextBinding();
    }

    /** final - subclasses implement moveToNextBinding() */
    @Override
    public final T nextBinding() {
        try {
            // Need to make sure to only read this once per iteration
            boolean shouldCancel = requestingCancel();

            if ( shouldCancel ) {
                // Try to close first to release resources (in case the user
                // doesn't have a close() call in a finally block)
                close();
                throw new QueryCancelledException();
            }

            if ( finished )
                throw new NoSuchElementException(Lib.className(this));

            if ( !hasNextBinding() )
                throw new NoSuchElementException(Lib.className(this));

            T obj = moveToNextBinding();
            if ( obj == null )
                throw new NoSuchElementException(Lib.className(this));

            if ( shouldCancel && !finished ) {
                // But .cancel sets both requestingCancel and abortIterator
                // This only happens with a continuing iterator.
                close();
            }

            return obj;
        } catch (QueryFatalException ex) {
            Log.error(this, "QueryFatalException", ex);
            throw ex;
        }
    }

    @Override
    public final void remove() {
        Log.warn(this, "Call to QueryIterator.remove() : " + Lib.className(this) + ".remove");
        throw new UnsupportedOperationException(Lib.className(this) + ".remove");
    }

    @Override
    protected final void closeActual() {
        if ( finished )
            return;
        try {
            closeIterator();
        } catch (QueryException ex) {
            Log.warn(this, "QueryException in close()", ex);
        }
        finished = true;
    }

    /** Cancel this iterator : this is called, possibly asynchronously, to cancel an iterator.*/
    @Override
    public final void cancel() {
        synchronized (cancelLock) {
            if ( ! cancelOnce ) {
                // Need to set the flags before allowing subclasses to handle requestCancel() in order
                // to prevent a race condition. We want to be sure that calls to have hasNext()/nextBinding()
                // will definitely throw a QueryCancelledException in this class and
                // not allow a situation in which a subclass component thinks it is cancelled,
                // while this class does not.
                if ( requestingCancel != null )
                    // Signalling from timeouts
                    requestingCancel.set(true);
                cancelOnce = true;
                this.requestCancel();
            }
        }
    }

    /** close an iterator */
    protected static void performClose(AbortableIterator<?> iter) {
        if ( iter == null )
            return;
        iter.close();
    }

    /**
     * Cancel an iterator. Best-effort for non-blocking concurrent cancel because
     * iter may longer be in-use when cancel() is called on it.
     */
    protected static void performRequestCancel(AbortableIterator<?> iter) {
        if ( iter == null )
            return;
        iter.cancel();
    }

    // --- PrintSerializableBase ---

    @Override
    public String toString(PrefixMapping pmap)
    { return QueryOutputUtils.toString(this, pmap) ; }

    // final stops it being overridden and missing the output() route.
    @Override
    public final String toString()
    { return Printable.toString(this) ; }

    /** Normally overridden for better information */
    @Override
    public void output(IndentedWriter out) {
        out.print(Plan.startMarker);
        out.print(Lib.className(this));
        out.print(Plan.finishMarker);
    }
}
