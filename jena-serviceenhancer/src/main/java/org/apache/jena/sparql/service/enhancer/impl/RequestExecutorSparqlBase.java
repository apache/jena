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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterator;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterators;
import org.apache.jena.system.TxnOp;

/** Adaption of the generic {@link RequestExecutorBase} base class to Jena's QueryIterator machinery. */
public abstract class RequestExecutorSparqlBase
    extends RequestExecutorBase<Node, Binding, Binding>
{
    protected ExecutionContext execCxt;

    public RequestExecutorSparqlBase(
            Granularity granularity,
            AbortableIterator<GroupedBatch<Node, Long, Binding>> batchIterator,
            int maxConcurrentTasks,
            long concurrentSlotReadAheadCount,
            ExecutionContext execCxt
    ) {
        super(execCxt.getCancelSignal(), granularity, batchIterator, maxConcurrentTasks, concurrentSlotReadAheadCount);
        this.execCxt = Objects.requireNonNull(execCxt);
    }

    @Override
    protected void checkCanExecInNewThread() {
        DatasetGraph dataset = execCxt.getDataset();
        if (dataset.supportsTransactions()) {
            if (dataset.isInTransaction()) {
                ReadWrite txnMode = dataset.transactionMode();
                if (ReadWrite.WRITE.equals(txnMode)) {
                    throw new IllegalStateException("Cannot create concurrent tasks when in a write transaction.");
                }
            }
        }
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        out.println(Lib.className(this)) ;
//        out.incIndent() ;
//        for ( QueryIterator qIter : execCxt.listAllIterators() )
//        {
//            qIter.output( out, sCxt );
//        }
//        out.decIndent() ;
        out.ensureStartOfLine() ;
    }

    @Override
    protected boolean isCancelled() {
        AtomicBoolean cancelSignal = execCxt.getCancelSignal();
        return (cancelSignal != null && cancelSignal.get()) || Thread.interrupted();
    }

    @Override
    protected Binding detachOutput(Binding item, boolean isInNewThread) {
        Binding result = isInNewThread ? item.detach() : item;
        return result;
    }

    /** Factory method for iterators. May be invoked from different threads. */
    protected abstract AbortableIterator<Binding> buildIterator(boolean runsOnNewThread, Node groupKey, List<Binding> inputs, List<Long> reverseMap, ExecutionContext batchExecCxt);

    @Override
    protected IteratorCreator<Binding> processBatch(boolean runsOnNewThread, Node groupKey, List<Binding> inputs, List<Long> reverseMap) {
        IteratorCreator<Binding> result;
        if (!runsOnNewThread) {
            result = new IteratorCreator<>() {
                @Override
                public  AbortableIterator<Binding> create() {
                    return buildIterator(runsOnNewThread, groupKey, inputs, reverseMap, execCxt);
                }
            };
        } else {
            ExecutionContext isolatedExecCxt = ExecutionContext.fromFunctionEnv(execCxt);
            // TODO Check that fromFunctionEnv is a suitable replacement for the deprecated ctor below:
            // ExecutionContext isolatedExecCxt = new ExecutionContext(execCxt.getContext(), execCxt.getActiveGraph(), execCxt.getDataset(), execCxt.getExecutor());
            result = new IteratorCreatorWithTxn<>(isolatedExecCxt, TxnType.READ) {
                @Override
                protected AbortableIterator<Binding> createIterator() {
                    // Note: execCxt in here is assigned to isolatedExecCxt!
                    return buildIterator(runsOnNewThread, groupKey, inputs, reverseMap, execCxt);
                }
            };
        }
        return result;
    }

    static abstract class IteratorCreatorWithTxn<T>
        implements IteratorCreator<T>
    {
        protected ExecutionContext execCxt;
        protected TxnType txnType;

        public IteratorCreatorWithTxn(ExecutionContext execCxt, TxnType txnType) {
            super();
            this.execCxt = execCxt;
            this.txnType = txnType;
        }

        @Override
        public final AbortableIterator<T> create() {
            begin();
            AbortableIterator<T> it = createIterator();
            return AbortableIterators.onClose(it, this::end);
        }

        protected abstract AbortableIterator<T> createIterator();

        protected void begin() {
            DatasetGraph dsg = execCxt.getDataset();
            txnBegin(dsg, txnType);
        }

        protected void end() {
            DatasetGraph dsg = execCxt.getDataset();
            txnEnd(dsg);
        }
    }

    protected static void txnBegin(Transactional txn, TxnType txnType) {
        boolean b = txn.isInTransaction();
        if ( b )
            TxnOp.compatibleWithPromote(txnType, txn);
        else
            txn.begin(txnType);
    }

    protected static void txnEnd(Transactional txn) {
        boolean b = txn.isInTransaction();
        if ( !b ) {
            if ( txn.isInTransaction() )
                // May have been explicit commit or abort.
                txn.commit();
            txn.end();
        }
    }
}
