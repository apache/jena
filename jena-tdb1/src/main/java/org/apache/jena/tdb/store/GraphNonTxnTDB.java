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

package org.apache.jena.tdb.store;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.Sync;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.impl.TransactionHandlerBase;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction;

/**
 * Non-transactional version of {@link GraphTDB}.
 * Handed out by DatasetGraphTDB when used directly (e.g. by the loader)
 * but not for API or SPARQL usage. 
 * 
 * @see GraphTDB
 * @see GraphTxnTDB
 */
public class GraphNonTxnTDB extends GraphTDB implements Closeable, Sync {
    private final DatasetGraphTDB    dataset;

    public GraphNonTxnTDB(DatasetGraphTDB dataset, Node graphName) {
        super(dataset, graphName);
        this.dataset = dataset;
    }

    @Override
    public DatasetGraphTDB getDatasetGraphTDB() {
        return dataset;
    }

    @Override
    protected DatasetGraphTDB getBaseDatasetGraphTDB() {
        return dataset;
    }
    
    @Override
    public DatasetGraphTransaction getDatasetGraphTransaction() {
        return null;
    }
    
    @Override
    public TransactionHandler getTransactionHandler() {
        return new TransactionHandlerTDBNonTXn(this);
    }
    
    // Transaction handler for non-transactional use.
    // Does not support transactions, but syncs on commit which is the best it
    // can do without being transactional, which is strongly preferred.
    // For backwards compatibility only.
    private static class TransactionHandlerTDBNonTXn extends TransactionHandlerBase //implements TransactionHandler 
    {
        private final GraphTDB graph;

        public TransactionHandlerTDBNonTXn(GraphTDB graph) {
            this.graph = graph ;
        }

        @Override
        public void abort() {
            throw new UnsupportedOperationException("TDB: 'abort' of a transaction not supported") ;
            // log.warn("'Abort' of a transaction not supported - ignored");
        }

        @Override
        public void begin() {}

        @Override
        public void commit() {
            graph.getDatasetGraphTDB().sync();
        }

        @Override
        public boolean transactionsSupported() {
            return false ;
        }
    }
}
