/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.IOException;

import arq.cmd.CmdUtils;
import arq.cmd.QueryCmdUtils;
import arq.cmd.ResultsFormat;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

import dev.alq.QueryEngineQuad;

public class RunSDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    public static void main(String[]argv)
    {
        SDBConnection.logSQLExceptions = true ;
        //SDBConnection.logSQLStatements = true ;
        
        runQuad() ;
        //runQuery() ;
        //runPrint() ;
        //runScript() ;
        //run() ;
        System.err.println("Nothing ran!") ;
        System.exit(0) ;
    }

    public static void runQuad()
    {
        Store store = SDBFactory.connectStore("Store/sdb-hsqldb-inMemory.ttl") ;
        store.getTableFormatter().format() ;
        Query query = QueryFactory.read("Q.rq") ;
        query.serialize(System.out) ;
        System.out.println("----------------") ;
        QueryEngineQuad engine = new QueryEngineQuad(store, query) ;
        SqlNode sqlNode = engine.toSqlNode() ;
//        System.out.println(sqlNode.toString()) ;
//        System.out.println("----------------") ;
        String sqlString = GenerateSQL.toSQL(sqlNode) ;
        System.out.println(sqlString) ;
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
        
        //String a[] = {"-v", "--time","--sdb=Store/sdb-hsqldb-file.ttl", "--query=Q.rq" } ;
        String a[] = {"--format", "--load=D.ttl","--sdb=sdb.ttl", "--query=Q.rq" } ;
        QC.printBlock = true ;
        QC.printAbstractSQL = true ;
        QC.printSQL = true ;
        
//        SDBConnection.logSQLStatements = false ;
//        SDBConnection.logSQLExceptions = true ;
        sdb.sdbquery.main(a) ;
        System.exit(0) ;
    }
    
    public static void runPrint()
    {
        //QueryCompilerBasicPattern.printAbstractSQL = true ;
        String[] a = {"-v", "--sql", "--sdb=sdb.ttl", "--query=Q.rq"} ;
        //String[] a = {"--sdb=sdb.ttl","--sql" , "--query=PerfTests/UniProt/ex4.rq"} ;
        sdb.sdbprint.main(a) ;
        System.exit(0) ;
    }
   
    public static void run()
    {
        SDB.init() ;
        // Create store - assumed  to be 
        Store store = SDBFactory.connectStore("Store/sdb-hsqldb-inMemory.ttl") ;
        store.getTableFormatter().format() ;

        Model model = SDBFactory.connectModel(store) ;
        model.read("file:D.ttl", "N3") ;
        //model.write(System.out, "N3")  ;
        
        String qs = "SELECT ?s ?p ?S { ?s ?p ?S }" ;
        Query query = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        QueryCmdUtils.executeQuery(query, qExec, ResultsFormat.FMT_TEXT) ;
        qExec.close() ;
        System.exit(0) ;
    }

    static void runScript()
    {
        String[] a = { } ;
        sdb.sdbscript.main(a) ;
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