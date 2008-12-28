/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */