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

package com.hp.hpl.jena.sdb.test;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.jena.atlas.lib.FileOps ;

/** Not quite all, yet: Does not yet include the model tests which need to be linked to the store descriptions */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
    SDBTestMisc.class,
    SDBQueryTestSuite.class,
    SDBUpdateTestSuite.class
} )

public class SDBTestAll
{ 
    /* Derby needs this before the tests;
        sdbconfig --sdb testing/StoreDescSimple/derby-layout1.ttl --format
        sdbconfig --sdb testing/StoreDesc/derby-hash.ttl  --format
        sdbconfig --sdb testing/StoreDesc/derby-index.ttl  --format
     */

    @BeforeClass public static void beforeClass() {
        FileOps.ensureDir("target") ;
        FileOps.ensureDir("target/Derby-test") ;
        sdb.sdbconfig.main("--sdb=testing/StoreDescSimple/derby-layout1.ttl", "--format") ;
        sdb.sdbconfig.main("--sdb=testing/StoreDesc/derby-hash.ttl", "--format") ;
        sdb.sdbconfig.main("--sdb=testing/StoreDesc/derby-index.ttl", "--format") ;
    }
    
    @AfterClass public static void afterClass() {
        FileOps.deleteSilent("derby.log") ;
    }
}
