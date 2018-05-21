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

package org.apache.jena.tdb2.loader.parallel;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/** Per-thread TransactionCoordinator helpers. */
/*package*/ public class CoLib {
    
    /*package*/ public static TransactionCoordinator newCoordinator() {
        Journal journal = Journal.create(Location.mem());
        return new TransactionCoordinator(journal);
    }
    
    /*package*/ public static void add(TransactionCoordinator coordinator, NodeTable nodeTable) {
        coordinator.add(LoaderOps.ntDataFile(nodeTable));
        coordinator.add(LoaderOps.ntBPTree(nodeTable));
    }
    
    /*package*/ public static void add(TransactionCoordinator coordinator, TupleIndex... indexes) {
        for ( TupleIndex pIdx : indexes ) {
            coordinator.add(LoaderOps.idxBTree(pIdx));
        }
    }

    /*package*/ public static void start(TransactionCoordinator coordinator) {
        coordinator.start();
    }
    
    /*package*/ public static void finish(TransactionCoordinator coordinator) {
        // Donot do this - it will shutdown the TrasnactionComponents as well.
        //coordinator.shutdown();
    }

}
