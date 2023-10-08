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

package org.apache.jena.tdb1.store ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.graph.Node ;
import org.apache.jena.tdb1.transaction.DatasetGraphTransaction;

/**
 * Transaction-capable version of {@link GraphTDB}.
 * Valid across transactions except where noted (caution: prefix mappings are not).
 * Valid to use when TDB is not transactional.
 *
 * @see GraphTDB
 * @see GraphNonTxnTDB
 */
public class GraphTxnTDB extends GraphTDB implements Closeable, Sync {

    private final DatasetGraphTransaction dataset ;

    public GraphTxnTDB(DatasetGraphTransaction dataset, Node graphName) {
        super(dataset, graphName) ;
        this.dataset = dataset ;
    }

    @Override
    public DatasetGraphTransaction getDatasetGraphTransaction() {
        return dataset;
    }

    @Override
    public DatasetGraphTDB getDatasetGraphTDB() {
        return dataset.getDatasetGraphToQuery() ;
    }

    @Override
    protected DatasetGraphTDB getBaseDatasetGraphTDB() {
        return dataset.getBaseDatasetGraph() ;
    }
}
