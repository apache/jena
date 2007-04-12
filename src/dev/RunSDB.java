/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static sdb.SDBCmd.* ;
import junit.framework.TestSuite;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.sdb.test.SDBTestSuite1;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.util.FileManager;


public class RunSDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    public static void main(String[]argv)
    {
        SDBConnection.logSQLExceptions = true ;
        
        //QBuilder.main(null) ; System.exit(0) ;
        
        
        //runQuery() ;
        //SDBConnection.logSQLStatements = true ;
        
        //runInMem("Q.rq", "D.ttl") ;
        //runQuery("Q.rq", "D.ttl") ;
        //runQuery("Q.rq") ;
        
        //runQuad() ;
        //runQuery() ;
        //runPrint() ;
        //runScript() ;
        
        run() ;
        //runTest() ;
        System.err.println("Nothing ran!") ;
        System.exit(0) ;
    }

    public static void runQuery()
    {
        String queryFile = "Q.rq" ;
        String dataFile = "D.ttl" ;
        
//        queryFile = "testing/Algebra/filter-nested-2.rq" ;
//        dataFile  = "testing/Algebra/data.ttl" ;
        
        System.out.println(QueryFactory.read(queryFile)) ;
        
        System.out.println("*** Reference") ;
        runInMem(queryFile, dataFile) ;
        System.out.println("*** SDB") ;
        runQuery(queryFile, dataFile) ;
    }
        
     public static void runQuery(String queryFile, String dataFile)
     {
        //String a[] = {"-v", "--time","--sdb=Store/sdb-hsqldb-file.ttl", "--query=Q.rq" } ;
//        String a[] = {"--format", "--load="+dataFile,"--sdb=sdb.ttl", "--query="+queryFile } ;
        
//        SDBConnection.logSQLStatements = false ;
//        SDBConnection.logSQLExceptions = true ;
         setSDBConfig("sdb.ttl") ;
         //setExitOnError(true) ;
         sdbconfig() ; 
         sdbload(dataFile) ;
         sdbprint("--print=plan", "--file=Q.rq") ; 
         sdbquery("--file=Q.rq") ;
         System.exit(0) ;
     }

     public static void runQuery(String queryFile)
     {
//        SDBConnection.logSQLStatements = false ;
//        SDBConnection.logSQLExceptions = true ;
        sdbquery("--layout=layout2/index", "--sdb=sdb.ttl", "--query="+queryFile ) ;
        System.exit(0) ;
     }
     
     public static void runInMem(String queryFile, String dataFile)
     {
         if ( true )
             sparql("--data="+dataFile, "--query="+queryFile) ;
         else
         {
             Query query = QueryFactory.read(queryFile) ;
             Model model = FileManager.get().loadModel(dataFile) ;
             QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
             QueryExecUtils.executeQuery(query, qExec, ResultsFormat.FMT_TEXT) ;
         }
    }
    
    public static void runPrint()
    {
        runPrint("Q.rq") ;
        System.exit(0) ;
    }
    
    public static void runPrint(String filename)
    {
        //QueryCompilerBasicPattern.printAbstractSQL = true ;
        sdb.sdbprint.main("--print=sql", "--print=op", "--sdb=sdb.ttl", "--query="+filename) ;
        System.exit(0) ;
    }
    
   
    public static void runTest()
    {
        if ( false )
        {
            SDB.init() ;
            SDBTestSuite1.includeMySQL = true ;
            SDBTestSuite1.includeHSQL = false ;
            TestSuite ts = SDBTestSuite1.suite() ;
            SimpleTestRunner.runAndReport(ts) ;
            System.exit(0) ;
        }
        String[] a = { "--sdb=sdb.ttl", "--dbName=DB/test2", 
            "testing/Algebra/manifest.ttl",
            "testing/Structure/manifest.ttl"
            } ;

        sdb.sdbtest.main(a) ;
        System.exit(0) ;
    }

    static void runScript()
    {
        String[] a = { } ;
        sdb.sdbscript.main(a) ;
    }
    
    public static void runConf()
    {
        JDBC.loadDriverHSQL() ;
        CmdUtils.setLog4j() ;
        
        String hsql = "jdbc:hsqldb:mem:aname" ;
        //String hsql = "jdbc:hsqldb:file:tmp/db" ;

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
    
    public static void run()
    {
        
//        String args = "--sdb=sdb.ttl --layout=layout2/index --dbName=DB2-index --query=Q.rq" ;
//        String [] a = args.split(" ") ;
//        sdb.sdbquery.main(a) ;

//        String[] a = new String[]{
//            "--sdb=Store/sdb-mysql-innodb.ttl",
//            "--layout=layout2/index",
//            "--create",
//            "--dbName=test2-index"} ;
        
        
        
        String args = "--sdb=sdb.ttl --dbName=DB2-index --file S" ;
        String [] a = args.split(" ") ;
        sdb.sdbsql.main(a) ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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