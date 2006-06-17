/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import junit.framework.TestCase;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.graph.PrefixMappingSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestPrefixMappingSDB extends TestCase
{
    static final String hsql = "jdbc:hsqldb:mem:aname" ;

    SDBConnection sdb = null ;
    
    // Cheap and nasty TestSuite initialization
    {
        System.err.println("Not working yet") ;
        JDBC.loadDriverHSQL() ;
        //SDB.init() ;
        sdb = SDBFactory.createConnection(hsql, "sa", "");
    }
    
    public void test_prefix1()
    {
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
    }
    
    public void test_prefix2()
    {
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
        pmap.setNsPrefix("ex", "http://example/") ;
        assertNotNull(pmap.getNsPrefixURI("ex")) ;
    }
    
    public void test_prefix3()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        PrefixMapping pmap2 = new PrefixMappingSDB(sdb) ;
        String x = pmap2.getNsPrefixURI("ex") ;
        
        assertNotNull(x) ;
        assertEquals(uri,x) ;
    }
        
    public void test_prefix4()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals(uri, pmap.getNsURIPrefix("http://example/")) ;
    }
        
    public void test_prefix5()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals(uri+"foo", pmap.expandPrefix("ex:foo")) ;
    }

    public void test_prefix6()
    {
        String uri = "http://example/" ;
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
        pmap.setNsPrefix("ex", uri) ;
        
        assertEquals("ex:foo", pmap.qnameFor("http://example/foo")) ;
    }

    public void test_prefix7()
    {
        String uri1 = "http://example/" ;
        String uri2 = "http://example/ns#" ;
        
        PrefixMapping pmap = new PrefixMappingSDB(sdb) ;
        pmap.setNsPrefix("ex1", uri1) ;
        pmap.setNsPrefix("ex2", uri2) ;
        assertEquals("ex2:foo", pmap.qnameFor("http://example.org/ns#foo")) ;
    }        
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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