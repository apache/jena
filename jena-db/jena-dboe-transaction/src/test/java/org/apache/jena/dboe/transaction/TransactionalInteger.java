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

import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionalBase;

/**
 * A Transactional (unit of begin/commit) of a single integer component.
 * Testing support.  Use {@link TransInteger} for application code.
 * @see TransInteger
 */
public class TransactionalInteger extends TransactionalBase {
    final private TransInteger integer;

    public TransactionalInteger(TransactionCoordinator coord, long v) {
        super(coord);
        integer = new TransInteger(v);
        coord.add(integer);
    }

    public void inc() {
        integer.inc();
    }

    /** Return the current value.
     * If inside a transaction, return the tarnsaction view of the value.
     * If not in a transaction return the state value (effectively
     * a fast read transaction).
     */
    public long get() {
        return integer.get();
    }

    public void set(long v) {
        integer.set(v);
    }

    /** Return the currently commited value */
    public long value() {
        return integer.value();
    }
}

