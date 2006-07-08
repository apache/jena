/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import arq.cmd.CmdUtils;

import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBase;
import com.hp.hpl.jena.sdb.junit.QueryTestSDBFactory;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleHSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.test.SDBTest;

@RunWith(AllTests.class)
public class DevTest extends TestSuite
{
//  Is there a better way to dynamically build a set of tests?
    static public TestSuite suite() {
        CmdUtils.setLog4j() ;
        return new DevTest();
    }

    private Store store ;
    
    private DevTest()
    {
        super("SDB - Dev test") ;
        if ( true )
        {
            SDBConnection.logSQLExceptions = true ;
            SDBConnection.logSQLStatements = false ;
            QueryCompilerBase.printSQL = false ;
        }
        init() ;
        loadTests() ;
    }
        
    private void loadTests()
    {
//        loadManifest(SDBTest.testDirSDB+"General/manifest.ttl") ;
//        loadManifest(SDBTest.testDirSDB+"BasicPatterns/manifest.ttl") ; 
        loadManifest(SDBTest.testDirSDB+"Optionals1/manifest.ttl") ; 
    }

    private void loadManifest(String s)
    {
        TestSuite ts = QueryTestSDBFactory.make(store, s) ; 
        ts.setName(ts.getName()+"/HSQL-mem") ;
        addTest(ts) ;
    }
    
    private void init()
    {
        JDBC.loadDriverHSQL() ;
        SDBConnection sdb = new SDBConnection("jdbc:hsqldb:mem:testdb1", "sa", "") ;
        store = new StoreSimpleHSQL(sdb) ;
        // Init.
        store.getTableFormatter().format() ;
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