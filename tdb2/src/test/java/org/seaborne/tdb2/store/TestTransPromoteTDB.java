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

package org.seaborne.tdb2.store;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.log4j.Logger;
import org.seaborne.dboe.transaction.txn.Transaction;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator;
import org.seaborne.dboe.transaction.txn.TransactionException;
import org.seaborne.tdb2.DatabaseMgr ;

public class TestTransPromoteTDB extends AbstractTestTransPromoteTDB2 {

    public TestTransPromoteTDB() {
        super(getLoggers());
    }

    private static Logger[] getLoggers() {
        return new Logger[]{ Logger.getLogger(Transaction.class) } ;
    }
    
    @Override
    protected void setPromotion(boolean b) {
        TransactionCoordinator.promotion = b ;
    }

    @Override
    protected boolean getPromotion() {
        return TransactionCoordinator.promotion ;
    }

    @Override
    protected void setReadCommitted(boolean b) {
        TransactionCoordinator.readCommittedPromotion = b ;
    }

    @Override
    protected boolean getReadCommitted() {
        return TransactionCoordinator.readCommittedPromotion ;
    }

    @Override
    protected Class<? extends Exception> getTransactionExceptionClass() {
        return TransactionException.class;
    }

    @Override
    protected DatasetGraph create() {
        return DatabaseMgr.createDatasetGraph();
    }

    @Override
    protected boolean supportsReadCommitted() {
        return true;
    }
}
