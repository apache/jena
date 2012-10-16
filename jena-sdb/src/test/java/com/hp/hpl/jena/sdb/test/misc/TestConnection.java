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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.test.junit.ParamAllStoreDesc;


@RunWith(Parameterized.class)
public class TestConnection extends ParamAllStoreDesc
{
    java.sql.Connection conn ;
    
    public TestConnection(String name, StoreDesc storeDesc)
    {
        super(name, storeDesc) ;
    }

    @Before public void before()
    {
        conn = SDBFactory.createSqlConnection(storeDesc.connDesc) ;
    }
    
    @Test public void connection_1()
    {
        SDBConnection conn1 = SDBFactory.createConnection(conn) ;
        Store store1 = StoreFactory.create(storeDesc, conn1) ;
        // Reset
        store1.getTableFormatter().format();
        
        SDBConnection conn2 = SDBFactory.createConnection(conn) ;
        Store store2 = StoreFactory.create(storeDesc, conn2) ;
        
        Model model1 = SDBFactory.connectDefaultModel(store1) ;
        Model model2 = SDBFactory.connectDefaultModel(store2) ;
        
        Resource s = model1.createResource() ;
        Property p = model1.createProperty("http://example/p") ;
        
        // These are autocommit so two stores should be OK (but not a good design paradigm)
        model1.add(s, p, "model1") ;
        model2.add(s, p, "model2") ;
        
        assertEquals(2, model1.size()) ;
        assertEquals(2, model2.size()) ;
        assertTrue(model1.isIsomorphicWith(model2)) ;
        
    }
}
