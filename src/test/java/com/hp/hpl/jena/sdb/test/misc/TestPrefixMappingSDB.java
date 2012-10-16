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
import static org.junit.Assert.assertNotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.graph.PrefixMappingSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestPrefixMappingSDB
{
    static final String hsql = "jdbc:hsqldb:mem:aname" ;
    static final String defaultPrefixURI  = "" ; //"urn:x-sdb:prefifdefault" ;

//    public static junit.framework.Test suite() { 
//        return new JUnit4TestAdapter(TestPrefixMappingSDB.class); 
//    }
//    
    static SDBConnection sdb = null ;
    
    @BeforeClass
    public static void setUpClass()
    {
        JDBC.loadDriverHSQL() ;
        //SDB.init() ;
        sdb = SDBFactory.createConnection(hsql, "sa", "");
        StoreDesc desc = new StoreDesc("Layout2", "HSQLDB", null) ;
        Store store = SDBFactory.connectStore(sdb, desc) ;
        store.getTableFormatter().format() ;
        // Make a store and format it.
    }

    @AfterClass
    public static void teardownClass()
    {
        if ( sdb != null )
            sdb = null ;
    }
    
    
    
    @Test public void prefix1()
    {
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
    }
    
    @Test public void prefix2()
    {
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        pmap.setNsPrefix("ex", "http://example/") ;
        assertNotNull(pmap.getNsPrefixURI("ex")) ;
    }
    
    @Test public void prefix3()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        PrefixMapping pmap2 = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        String x = pmap2.getNsPrefixURI("ex") ;
        
        assertNotNull(x) ;
        assertEquals(uri,x) ;
    }
        
    @Test public void prefix4()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex", pmap.getNsURIPrefix("http://example/")) ;
    }
        
    @Test public void prefix5()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals(uri+"foo", pmap.expandPrefix("ex:foo")) ;
    }

    @Test public void prefix6()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex:foo", pmap.qnameFor("http://example/foo")) ;
    }

    @Test public void prefix7()
    {
        String uri1 = "http://example/" ;
        String uri2 = "http://example/ns#" ;
        
        PrefixMapping pmap = new PrefixMappingSDB(defaultPrefixURI, sdb) ;
        pmap.setNsPrefix("ex1", uri1) ;
        pmap.setNsPrefix("ex2", uri2) ;
        assertEquals("ex2:foo", pmap.qnameFor("http://example/ns#foo")) ;
    }        
}
