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

import java.util.Objects;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

/**
 * Txn variant for use with try-with-resources. Allows raising
 * checked exceptions in an idiomatic way. Closing the TxnCtl
 * instance will abort the transaction unless it
 * has been manually committed.
 * <p>
 *
 * Usage example:
 * <pre>
 * public void myMethod() throws IOException {
 *   try (TxnCtl txn = TxnCtl.begin(dataset, TxnType.WRITE)) {
 *     // Do work
 *     if (someError) {
 *         throw new IOException();
 *     }
 *     // Must manually call commit on success.
 *     txn.commit();
 *   }
 * }
 * </pre>
 */
public class TxnCtl
    implements AutoCloseable
{
    private Transactional txn;
    private boolean b;

    private TxnCtl(Transactional txn, boolean b) {
        super();
        this.txn = txn;
        this.b = b;
    }

    public static TxnCtl begin(Transactional txn, ReadWrite readWrite) {
        return begin(txn, TxnType.convert(readWrite));
    }

    public static TxnCtl begin(Transactional txn, TxnType txnType) {
        Objects.requireNonNull(txn);
        Objects.requireNonNull(txnType);
        boolean b = txn.isInTransaction();
        if ( b )
            TxnOp.compatibleWithPromote(txnType, txn);
        else
            txn.begin(txnType);
        return new TxnCtl(txn, b);
    }

    public void commit() {
        if ( txn.isInTransaction() ) {

            // May have been explicit commit or abort.
            txn.commit();
        }
    }

    @Override
    public void close() {
        if ( !b ) {
            if ( txn.isInTransaction() )
                // May have been explicit commit or abort.
                txn.abort();
            txn.end();
        }
    }
}
