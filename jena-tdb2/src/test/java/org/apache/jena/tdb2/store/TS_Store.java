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

package org.apache.jena.tdb2.store;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.tdb2.sys.LibTestOps;
import org.apache.jena.tdb2.sys.SystemTDB;

@Suite
@SelectClasses({

    TestNodeId.class
    , TestTripleTable.class
    , TestStorageDatasetGraphTests.class
    , TestGraphTDB.class
    , TestGraphNamedTDB.class
    , TestDatasetTDB.class
    , TestDatasetTDBPersist.class
    , Test_SPARQL_TDB.class
    , TestQueryExecTDB.class
    , TestDynamicDatasetTDB.class
    , TestStoreConnectionMem.class
    , TestStoreConnectionDirect.class
    , TestStoreConnectionMapped.class
    , TestStoreConnectionLock.class
    , TestTransactions.class
    , TestTransactionLifecycleTDB.class
    , TestTransPromoteTDB.class
    , TestQuadFilter.class
    , TestGraphView_Prefixes.class
} )
public class TS_Store
{
    static FileMode mode;

    @BeforeAll
    public static void beforeClass()
    {
        mode = SystemTDB.fileMode();
    }

    @AfterAll
    public static void afterClass()
    {
        if ( ! SystemTDB.fileMode().equals(mode) )
            LibTestOps.setFileMode(mode);
    }
}
