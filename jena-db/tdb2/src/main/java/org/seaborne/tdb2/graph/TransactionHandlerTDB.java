/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.graph;

import org.apache.jena.graph.impl.TransactionHandlerBase ;
import org.apache.jena.query.ReadWrite;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator;
import org.seaborne.tdb2.store.DatasetGraphTDB;
import org.seaborne.tdb2.store.GraphTDB ;

public class TransactionHandlerTDB extends TransactionHandlerBase //implements TransactionHandler 
{
    private final GraphTDB graph ;
    private final DatasetGraphTDB dsg;

    public TransactionHandlerTDB(GraphTDB graph) {
        this.graph = graph;
        this.dsg = graph.getDSG();
    }

    @Override
    public void abort() {
        graph.getDSG().abort();
        graph.getDSG().end();
    }

    @Override
    public void begin() {
        if ( TransactionCoordinator.promotion )
            dsg.begin(ReadWrite.READ);
        else
            dsg.begin(ReadWrite.WRITE);
    }

    @Override
    public void commit() {
        dsg.commit();
        dsg.end();
    }

    @Override
    public boolean transactionsSupported() {
        return true;
    }
}
