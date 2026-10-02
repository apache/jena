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

package org.apache.jena.sparql.engine.iterator;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

/**
 * Eagerly consumes a sub-execution on a worker thread while retaining normal
 * {@link org.apache.jena.sparql.engine.QueryIterator} cancellation semantics.
 */
public class QueryIterThreadedSubExecution extends QueryIter {
    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(10);

    private record Item(Binding binding, Throwable throwable, boolean end) {}

    private final Creator<? extends QueryExec> queryExecCreator;
    private final BlockingQueue<Item> items = new LinkedBlockingQueue<>();
    private final Object queryExecLock = new Object();
    private final Thread thread;
    private Item peekedItem;
    private boolean producerJoined = false;
    private boolean cancelRequested = false;
    private volatile QueryExec activeQueryExec;
    private volatile Throwable terminalFailure;

    public QueryIterThreadedSubExecution(ExecutionContext execCxt, Creator<? extends QueryExec> queryExecCreator) {
        super(execCxt);
        this.queryExecCreator = queryExecCreator;
        this.thread = new Thread(this::run, "jena-query-sub-execution");
        this.thread.start();
    }

    @Override
    protected boolean hasNextBinding() {
        ensurePeekedItem();
        awaitProducer();
        throwTerminalFailure();
        return !peekedItem.end();
    }

    @Override
    protected Binding moveToNextBinding() {
        ensurePeekedItem();
        awaitProducer();
        throwTerminalFailure();

        if ( peekedItem.end() )
            throw new NoSuchElementException();

        Binding result = peekedItem.binding();
        peekedItem = null;
        return result;
    }

    @Override
    protected void requestCancel() {
        QueryExec queryExec;
        synchronized (queryExecLock) {
            cancelRequested = true;
            queryExec = activeQueryExec;
        }
        if ( queryExec != null )
            queryExec.abort();
        thread.interrupt();
    }

    @Override
    protected void closeIterator() {
        requestCancel();
        try {
            thread.join(CLOSE_TIMEOUT.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new QueryCancelledException(ex);
        }
        if ( thread.isAlive() )
            throw new ARQException("Abandoned sub-execution thread which did not terminate within " + CLOSE_TIMEOUT);
    }

    private void ensurePeekedItem() {
        if ( peekedItem != null )
            return;
        try {
            peekedItem = items.take();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new QueryCancelledException(ex);
        }
        if ( peekedItem.throwable() != null )
            throwFailure(peekedItem.throwable());
    }

    private void awaitProducer() {
        if ( producerJoined )
            return;
        try {
            thread.join();
            producerJoined = true;
        } catch (InterruptedException ex) {
            thread.interrupt();
            Thread.currentThread().interrupt();
            throw new QueryCancelledException(ex);
        }
    }

    private void throwTerminalFailure() {
        if ( terminalFailure != null )
            throwFailure(terminalFailure);
    }

    private static void throwFailure(Throwable throwable) {
        if ( throwable instanceof InterruptedException || throwable instanceof QueryCancelledException )
            throw new QueryCancelledException(throwable);
        if ( throwable instanceof HttpException ex )
            throw QueryExceptionHTTP.rewrap(ex);
        if ( throwable instanceof QueryException ex )
            throw ex;
        if ( throwable instanceof Error error )
            throw error;
        throw new QueryException(throwable);
    }

    private void run() {
        ExecutionContext execCxt = getExecContext();
        try {
            execCxt.checkCancelSignal();
            try (QueryExec queryExec = queryExecCreator.create()) {
                boolean cancelled;
                synchronized (queryExecLock) {
                    activeQueryExec = queryExec;
                    cancelled = cancelRequested;
                }
                if ( cancelled ) {
                    queryExec.abort();
                    throw new QueryCancelledException();
                }
                try {
                    execCxt.checkCancelSignal();
                    RowSet rowSet = queryExec.select();
                    while ( rowSet.hasNext() ) {
                        execCxt.checkCancelSignal();
                        items.add(new Item(rowSet.next(), null, false));
                    }
                    execCxt.checkCancelSignal();
                } catch (QueryCancelledException ex) {
                    queryExec.abort();
                    throw ex;
                }
            }
            items.add(new Item(null, null, true));
        } catch (Throwable throwable) {
            if ( !(throwable instanceof QueryCancelledException)
                    && (isCancelRequested() || Thread.currentThread().isInterrupted()) )
                throwable = new QueryCancelledException(throwable);
            terminalFailure = throwable;
            items.add(new Item(null, throwable, true));
        } finally {
            synchronized (queryExecLock) {
                activeQueryExec = null;
            }
        }
    }

    private boolean isCancelRequested() {
        synchronized (queryExecLock) {
            return cancelRequested;
        }
    }
}
