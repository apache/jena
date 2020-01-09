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

package org.apache.jena.dboe.transaction;

import java.nio.ByteBuffer;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.SysTransState;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionalComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging for transaction steps. This class is stateless in the transaction.
 * When operations are called in the {@link TransactionalComponent} lifecycle,
 * it logs the event.
 */

public class TransLogger implements TransactionalComponent {
    private final Logger log;
    private final boolean everyEvent;

    /** Create a logger for begin-commit/abort-end */
    public TransLogger() {
        this(null, false);
    }

    /** Create a logger, either just  begin-commit/abort-end or all steps */
    public TransLogger(Logger logger) {
        this(logger, false);
    }

    /** Create a logger, either just  begin-commit/abort-end or all steps */
    public TransLogger(Logger logger, boolean all) {
        if ( logger == null )
            logger = LoggerFactory.getLogger(TransLogger.class);
        this.log = logger;
        this.everyEvent = all;
    }

    @Override
    public ComponentId getComponentId() {
//        if ( everyEvent )
//            log.info("getComponentId");
        return null;
    }

    @Override
    public void startRecovery() {
        if ( everyEvent )
            log.info("startRecovery");
    }

    @Override
    public void recover(ByteBuffer ref) {
        // Is not called because this compoent vener writes a redo/undo action to the log.
        if ( everyEvent )
            log.info("recover");
    }

    @Override
    public void finishRecovery() {
        if ( everyEvent )
            log.info("finishRecovery");
    }

    @Override
    public void cleanStart() {
        if ( everyEvent )
            log.info("cleanStart");
    }

    @Override
    public void begin(Transaction transaction) {
        txnStep("begin", transaction);
    }

    @Override
    public boolean promote(Transaction transaction) {
        txnStep("promote", transaction);
        return true;
    }

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        if ( everyEvent )
            txnStep("commitPrepare", transaction);
        return null;
    }

    @Override
    public void commit(Transaction transaction) {
        txnStep("commit", transaction);
    }

    @Override
    public void commitEnd(Transaction transaction) {
        if ( everyEvent )
            txnStep("commitEnd", transaction);
    }

    @Override
    public void abort(Transaction transaction) {
        txnStep("abort", transaction);
    }

    @Override
    public void complete(Transaction transaction) {
        txnStep("complete", transaction);
    }

    @Override
    public SysTransState detach() {
        log.info("detach");
        return null;
    }

    @Override
    public void attach(SysTransState systemState) {
        log.info("attach");
    }

    @Override
    public void shutdown() {
        if ( everyEvent )
            log.info("shutdown");
    }

    private void txnStep(String opName, Transaction transaction) {
        FmtLog.info(log, "%-8s %s", transaction.getTxnId(), opName);
    }

}

