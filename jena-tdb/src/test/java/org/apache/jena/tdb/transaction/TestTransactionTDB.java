/**
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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.transaction.AbstractTestTransactionLifecycle ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class TestTransactionTDB extends AbstractTestTransactionLifecycle
{
    private String DIR = null ; 
    private static String level = null ;
    
    @BeforeClass
    public static void beforeClassLoggingOff() {
        level = LogCtl.getLevel(SystemTDB.errlog.getName());
        LogCtl.setLevel(SystemTDB.errlog.getName(), "OFF");
    }

    @AfterClass
    public static void afterClassLoggingOn() {
        LogCtl.setLevel(SystemTDB.errlog.getName(), level);
    }
    
    @Before
    public void before() {
        TDBInternal.reset();
        DIR = ConfigTest.getCleanDir();
    }

    @After
    public void after() {
        TDBInternal.reset();
        FileOps.clearDirectory(DIR);
    }

    @Override
    protected Dataset create() {
        return TDBFactory.createDataset(DIR);
    }
}

