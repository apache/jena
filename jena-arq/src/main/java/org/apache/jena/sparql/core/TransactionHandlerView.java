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

package org.apache.jena.sparql.core;

import org.apache.jena.graph.impl.TransactionHandlerBase;
import org.apache.jena.query.TxnType;

/** A graph TransactionHandler that for a graph view of a {@link DatasetGraph}*/
public class TransactionHandlerView extends TransactionHandlerBase
{
    private final DatasetGraph dsg;

    public TransactionHandlerView(DatasetGraph dsg) {
        this.dsg = dsg;
    }

    protected DatasetGraph getDSG() { return dsg; }

    @Override
    public void begin() {
        getDSG().begin(TxnType.READ_PROMOTE);
    }

    @Override
    public void abort() {
        getDSG().abort();
        getDSG().end();
    }

    @Override
    public void commit() {
        getDSG().commit();
        getDSG().end();
    }

    @Override
    public boolean transactionsSupported() {
        // Abort required.
        return getDSG().supportsTransactionAbort();
    }
}
