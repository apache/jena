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

package org.apache.jena.sparql.engine.iterator;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFatalException;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.PrintSerializableBase;

/**
 * This class provides the general machinery for iterators.  This includes
 * <ul>
 * <li>autoclose when the iterator runs out</li>
 * <li>async cancellation</li>
 * <li>final {@link #hasNext()} and {@link #next()} to check for abort.
       Implementation provide {@link #hasNextBinding()}, {@link #moveToNextBinding()}</li>
 * </ul>
 */

public abstract class QueryIteratorBase
    extends PrintSerializableBase
    implements QueryIterator
{
    private boolean finished = false;

    // === Cancellation

    // .cancel() can be called asynchronously with iterator execution.
    // It causes notification to cancellation to be made, once, by calling .requestCancel()
    // which is called synchronously with .cancel() and asynchronously with iterator execution.
    private final AtomicBoolean requestingCancel;
    private volatile boolean cancelOnce = false;
    private Object cancelLock = new Object();

    public QueryIteratorBase() {
        // No async cancellation.
        this(null);
    }

    /** Argument : shared flag for cancellation. */
    public QueryIteratorBase(AtomicBoolean cancelSignal) {
        if ( cancelSignal == null )
            // Allows for direct cancel (not timeout).
            cancelSignal = new AtomicBoolean(false);
        requestingCancel = cancelSignal;
    }

    private boolean requestingCancel() {
        return requestingCancel != null && requestingCancel.get() ;
    }

    private void haveCancelled() {}

    // -------- The contract with the subclasses

    /** Implement this, not hasNext() */
    protected abstract boolean hasNextBinding();

    /** Implement this, not next() or nextBinding()
        Returning null is turned into NoSuchElementException
        Does not need to call hasNext (can presume it is true) */
    protected abstract Binding moveToNextBinding();

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
    public final Binding next() {
        return nextBinding();
    }

    /** final - subclasses implement moveToNextBinding() */
    @Override
    public final Binding nextBinding() {
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

            Binding obj = moveToNextBinding();
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
    public void close() {
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
    protected static void performClose(QueryIterator iter) {
        if ( iter == null )
            return;
        iter.close();
    }

    /** cancel an iterator */
    protected static void performRequestCancel(QueryIterator iter) {
        if ( iter == null )
            return;
        iter.cancel();
    }
}
