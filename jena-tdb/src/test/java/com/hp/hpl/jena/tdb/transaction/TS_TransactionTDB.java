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

package com.hp.hpl.jena.tdb.transaction;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TestJournal.class
    , TestTransIterator.class
    , TestObjectFileTransMem.class
    , TestObjectFileTransStorage.class
    , TestNodeTableTransMem.class
    , TestNodeTableTransDisk.class
    , TestTransMem.class
    , TestTransDiskDirect.class
    , TestTransDiskMapped.class
    , TestTransRestart.class
    , TestTransactionTDB.class
    , TestTransactionUnionGraph.class
})
public class TS_TransactionTDB
{
    static Level level ;
    @BeforeClass static public void beforeClass()
    {
        level = Logger.getLogger("com.hp.hpl.jena.tdb.transaction").getLevel() ;
        Logger.getLogger("com.hp.hpl.jena.tdb.transaction").setLevel(Level.INFO) ;
    }
    
    @AfterClass static public void afterClass()
    {
        Logger.getLogger("com.hp.hpl.jena.tdb.transaction").setLevel(level) ;
    }
}
