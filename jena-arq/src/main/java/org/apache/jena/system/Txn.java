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

package org.apache.jena.system;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

import java.util.function.Supplier;

/**
 * Application utilities for executing code in transactions.
 * <p>
 *
 * @deprecated Use {@link Transactional } class directly instead
 * <p>
 * Nested transaction are not supported but calling inside an existing transaction,
 * which must be compatible, (i.e. a write needs a WRITE transaction).
 * causes the existing transaction to be used.
 */
@Deprecated
public class Txn {
    /**
     * Execute in a "read" transaction that can promote to "write".
     * <p>
     * Such a transaction may abort if an update is executed
     * by another thread before this one is promoted to "write" mode.
     * If so, the data protected by {@code txn} is unchanged.
     * <p>
     * If the application knows updates will be needed, consider using {@link #executeWrite}
     * which starts in "write" mode.
     * <p>
     * The application code can call {@link Transactional#promote} to attempt to
     * change from "read" to "write"; the {@link Transactional#promote promote} method
     * returns a boolean indicating whether the promotion was possible or not.
     * <p>
     *
     * @deprecated Use {@link Transactional#exec} instead
     */
    @Deprecated
    public static <T extends Transactional> void execute(T txn, Runnable r) {
        txn.exec(TxnType.READ_PROMOTE, r);
    }

    /**
     * Execute in a "read" transaction that can promote to "write" and return some calculated value.
     * <p>
     * Such a transaction may abort if an update is executed
     * by another thread before this one is promoted to "write" mode.
     * If so, the data protected by {@code txn} is unchanged.
     * <p>
     * If the application knows updates will be needed, consider using {@link #executeWrite}
     * which starts in "write" mode.
     * <p>
     * The application code can call {@link Transactional#promote} to attempt to
     * change from "read" to "write"; the {@link Transactional#promote promote} method
     * returns a boolean indicating whether the promotion was possible or not.
     * <p>
     *
     * @deprecated Use {@link Transactional#calculate} instead
     */
    @Deprecated
    public static <T extends Transactional, X> X calculate(T txn, Supplier<X> r) {
        return calc(txn, TxnType.READ_PROMOTE, r);
    }

    /**
     * Execute application code in a transaction with the given {@link TxnType trasnaction type}.
     * <p>
     *
     * @deprecated Use {@link Transactional#exec} instead
     */
    public static <T extends Transactional> void exec(T txn, TxnType txnType, Runnable r) {
        txn.exec(txnType, r);
    }

    /**
     * Execute and return a value in a transaction with the given {@link TxnType transaction type}.
     * <p>>
     *
     * @deprecated Use {@link Transactional#calc} instead
     */
    public static <T extends Transactional, X> X calc(T txn, TxnType txnType, Supplier<X> r) {
        return txn.calc(txnType, r);
    }

    /**
     * Execute in a read transaction
     * <p>
     *
     * @deprecated Use {@link Transactional#executeRead} instead
     */
    public static <T extends Transactional> void executeRead(T txn, Runnable r) {
        txn.exec(TxnType.READ, r);
    }

    /**
     * Execute and return a value in a read transaction
     * <p>
     *
     * @deprecated Use {@link Transactional#calculateRead} instead
     */
    public static <T extends Transactional, X> X calculateRead(T txn, Supplier<X> r) {
        return calc(txn, TxnType.READ, r);
    }

    /**
     * Execute the Runnable in a write transaction
     * <p>
     *
     * @deprecated Use {@link Transactional#executeWrite} instead
     */
    public static <T extends Transactional> void executeWrite(T txn, Runnable r) {
        txn.exec(TxnType.WRITE, r);
    }

    /**
     * Execute and return a value in a write transaction.
     * <p>
     *
     * @deprecated Use {@link Transactional#calculateWrite} instead
     */
    public static <T extends Transactional, X> X calculateWrite(T txn, Supplier<X> r) {
        return txn.calc(TxnType.WRITE, r);
    }
}
