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

package org.apache.jena.tdb.transaction;

import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TestJournal.class
    , TestTransIterator.class
    , TestObjectFileTransMem.class
    , TestObjectFileTransStorage.class
    
    // ObjectFileTransComplex is not used by TDB currently.
    , TestObjectFileTransComplexMem.class
    , TestObjectFileTransComplexStorage.class
    
    , TestTransMem.class
    , TestTransDiskDirect.class
    , TestTransDiskMapped.class
    , TestTransRestart.class
    , TestTransactionTDB.class
    , TestTransactionUnionGraph.class
    , TestTransactionMiscTDB.class
    , TestTDBInternal.class
    , TestTransPromoteTDB.class
    , TestTransControl.class
    , TestTransIsolation.class
})
public class TS_TransactionTDB
{}
