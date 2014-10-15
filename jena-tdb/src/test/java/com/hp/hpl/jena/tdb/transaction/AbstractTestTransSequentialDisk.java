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

import java.io.File ;

import org.junit.After ;
import org.junit.Before ;

import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;

/** Basic tests and tests of ordering (single thread) */
public abstract class AbstractTestTransSequentialDisk extends AbstractTestTransSeq
{
    protected String DIR = null ;
    
    // Subclasses must implement.
//    @BeforeClass public static void beforeClass() {}
//    @AfterClass  public static void afterClass()  {}
    
    @Before public void before()
    {
        StoreConnection.reset() ;
        DIR = ConfigTest.getCleanDir() ;
        File d = new File(DIR) ;
    }

    @After public void after() {} 

    @Override
    protected StoreConnection getStoreConnection()
    {
        return StoreConnection.make(DIR) ;
    }
}
