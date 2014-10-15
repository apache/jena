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

package com.hp.hpl.jena.sdb.test.misc;

import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestRegistry
{
    String[] databaseNames = {  "derby", "HSQLDB",
                                "MySQL", "PostgreSQL", 
                                "SQLServer", "Oracle", "DB2", "SAP" } ;
    
    String[] layoutNames = {    "layout2/hash" , "layout2", 
                                "layout2/index", 
                                "layout1" } ;
    
    @Test public void reg_database_1()
    {
        // Tests default configuration.
        for ( String s : databaseNames )
            assertNotNull(DatabaseType.fetch(s)) ;
    }

    @Test public void reg_database_2()
    {
        for ( String s : DatabaseType.allNames() )
            assertNotNull(DatabaseType.fetch(s)) ;
    }
    
    @Test public void reg_database_3()
    {
        for ( DatabaseType t : DatabaseType.allTypes() )
            assertNotNull(DatabaseType.fetch(t.getName())) ;
    }

    @Test public void reg_layout_1()
    {
        // Tests default configuration.
        for ( String s : layoutNames )
            assertNotNull(LayoutType.fetch(s)) ;
    }

    @Test public void reg_layout_2()
    {
        for ( String s : LayoutType.allNames() )
            assertNotNull(LayoutType.fetch(s)) ;
    }
    
    @Test public void reg_layout_3()
    {
        for ( LayoutType t : LayoutType.allTypes() )
            assertNotNull(LayoutType.fetch(t.getName())) ;
    }
}
