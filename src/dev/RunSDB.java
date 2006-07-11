/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.IOException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;
import sdb.*;
import sdb.cmd.CmdDesc;
import sdb.cmd.ScriptDesc;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.junit.SimpleTestRunner;
import com.hp.hpl.jena.query.junit.TestItem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.junit.QueryTestSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreBase;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

public class RunSDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    public static void main(String[]argv)
    {
        SDBConnection.logSQLExceptions = true ;
        //SDBConnection.logSQLStatements = true ;
        //runQuery() ;
        runPrint() ;
        //runLoad() ;
        
        //runDump() ;
        //runScript() ;
        
        //runTest() ;
        //runCommand() ;
        //runCode() ;
        //runConf() ;
        //runTestManifest() ;

        System.err.println("Nothing ran!") ;
        System.exit(0) ;
    }

    public static void runQuery()
    {
        String DB="sdb2" ;
//        String a[] = {"--dbtype=mySQL",
//            "--dbHost=localhost", "--dbName=SDB2",
//            "--schema=schema2",
//            "-v",
//        } ;
        
        String a[] = {"-v", "--sdb=sdb.ttl", "@Q.rq" } ;
      
        SDBConnection.logSQLStatements = false ;
        SDBConnection.logSQLExceptions = true ;
        sdbquery.main(a) ;
        System.exit(0) ;
    }
    
    public static void runPrint()
    {
        //String[] a = {"-v", "--layout=layout2", "@Q.rq"} ;
        String[] a = {"-v", "--layout=layout2", "@Q.rq"} ;
        sdb.sdbprint.main(a) ;
        System.exit(0) ;
    }
    
    public static void runCommand()
    {
        String args[] = { "-v", "--sdb=sdb-hsqldb.ttl", "tmp/SDB2"} ; 
        sdbformat.main(args) ;
        System.exit(0) ;
        
        //String DB= "brainz" ;
        String DB="sdb2" ;
//        String a[] = {"--dbtype=mySQL",
//            "--dbHost=localhost", "--dbName=SDB2",
//            "--schema=schema2",
//            "-v",
//        } ;
        
        // The database is protected in other ways than user/password
        String a[] = {"--debug", "--sdb=sdb.ttl"} ;
      
        SDBConnection.logSQLStatements = false ;
        SDBConnection.logSQLExceptions = true ;
        sdbinfo.main(a) ;
        System.exit(0) ;
    }
    
    public static void runLoad()
    {
        String a[]= { "--sdb=sdb-hsqldb.ttl", "Data/data.ttl" } ;
        sdbload.main(a) ;
        System.exit(0) ;
    }
    
    public static void runDump()
    {
        String a[]= { "--sdb=sdb-hsqldb.ttl"} ;
        sdbdump.main(a) ;
        System.exit(0) ;
    }
    
    public static void runTestManifest()
    {
        String manifest = "testing/BasicPatterns/manifest.ttl" ;
        String a[]= { "--sdb=sdb.ttl", "--schema=schema2", manifest } ;
        sdbtest.main(a) ;
        System.exit(0) ;
    }
        
    public static void runTest()
    {
        SDBConnection conn = new SDBConnection("jdbc:mysql://localhost/SDB2", Access.getUser(), Access.getPassword()) ;
        TestItem testItem = new TestItem("SDB",
                                         "testing/Integration/integrate-1.rq",
                                         "testing/Integration/data.ttl",
                                         null) ;
        Store store = new StoreBase(null, null, null, null, null ) ; 
        Test test = new QueryTestSDB(store, "SDB", null, testItem) ;
        TestSuite ts = new TestSuite() ;
        ts.addTest(test) ;
        SimpleTestRunner.runAndReport(ts) ;
        store.close() ;
        System.exit(0) ;
    }

    public static void runCode()
    {
        System.exit(0) ;
    }

    static void runScript()
    {
        String filename = "script.ttl" ;
        ScriptDesc sd = ScriptDesc.read(filename) ;
        for ( CmdDesc cd : sd.getSteps() )
            runOneCmd(cd) ;
        
//        String filename = "cmd.ttl" ;
//        CmdDesc desc = CmdDesc.read(filename) ;
        System.exit(0) ;
    }

    static void runOneCmd(CmdDesc desc)
    {
        System.out.println(desc) ;
        try {
            String cmd = desc.getCmd() ;
            Class c = Class.forName(cmd) ;
            Method m = c.getMethod("main", new Class[]{String[].class}) ;
            m.invoke(null, new Object[]{desc.asStringArray()}) ;
        } catch (Exception ex) { ex.printStackTrace(System.err) ; }

        System.out.println(desc) ;
    }
    
    static String getString(String filename)
    {
        try { return FileUtils.readWholeFileAsUTF8("Q.rq") ; }
        catch (IOException ex)
        { 
            System.err.println("Failed to read "+filename) ;
            System.err.println(ex.getMessage()) ;
            return null ;
        }
    }

    public static void runConf()
    {
        JDBC.loadDriverHSQL() ;
        CmdUtils.setLog4j() ;
        
        String hsql = "jdbc:hsqldb:mem:aname" ;
        //String hsql = "jdbc:hsqldb:file:tmp/db" ;

        SDB.init() ;
        SDBConnection sdb = SDBFactory.createConnection(hsql, "sa", "");
        StoreConfig conf = new StoreConfig(sdb) ;
        
        Model m = FileManager.get().loadModel("Data/data2.ttl") ;
        conf.setModel(m) ;
        
        // Unicode: 03 B1"α"
        Model m2 = conf.getModel() ;
        
        if ( ! m.isIsomorphicWith(m2) )
            System.out.println("**** Different") ;
        else
            System.out.println("**** Same") ;
        
        conf.setModel(m) ;
        m2 = conf.getModel() ;

        
        m2.write(System.out, "N-TRIPLES") ;
        m2.write(System.out, "N3") ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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