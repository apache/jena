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

package org.apache.jena.tdb2.graph;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.TDBInternal;

public class TestGraphsTDB2_B extends AbstractTestGraphsTDB2
{
    // Transactional.
    @BeforeEach
    public void before() {
        getDataset().begin(ReadWrite.READ);
    }

    @AfterEach
    public void after() {
        getDataset().end();
        TDBInternal.expel(getDataset().asDatasetGraph());
    }

    @Override
    protected void fillDataset(Dataset dataset) {
        Txn.executeWrite(dataset, ()->{
            super.fillDataset(dataset);
        });
    }

    @Override
    protected Dataset createDataset() {
        return TDB2Factory.createDataset();
    }
}
