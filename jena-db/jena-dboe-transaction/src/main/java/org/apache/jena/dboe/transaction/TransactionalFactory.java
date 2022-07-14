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

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionalBase;
import org.apache.jena.dboe.transaction.txn.TransactionalComponent;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.dboe.transaction.txn.journal.Journal;

/** Helper operations for creating a {@link Transactional}.
 * The operations capture some common patterns.
 */
public class TransactionalFactory {

    /** Create, and start, management of a number of {@link TransactionalComponent}s */
    public static Transactional createTransactional(Location location, TransactionalComponent ... elements) {
        TransactionCoordinator coord = TransactionCoordinator.create(location);
        return createTransactional(coord, elements);
    }

    /** Create, and start, management of a number of {@link TransactionalComponent}s */
    public static Transactional createTransactional(Journal journal, TransactionalComponent ... elements) {
        TransactionCoordinator coord = new TransactionCoordinator(journal);
        return createTransactional(coord, elements);
    }

    private static Transactional createTransactional(TransactionCoordinator coord, TransactionalComponent... elements) {
        for ( TransactionalComponent tc : elements ) {
            coord.add(tc);
        }
        TransactionalBase base = new TransactionalBase(coord);
        coord.start();
        return base;
    }

    /** Create, but do not start, a {@link TransactionalSystem} from a {@link TransactionCoordinator} */
    public static TransactionalSystem createTransactionalSystem(TransactionCoordinator coord) {
        return new TransactionalBase(coord);
    }

}

