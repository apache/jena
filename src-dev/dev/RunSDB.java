/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static sdb.SDBCmd.sdbconfig;
import static sdb.SDBCmd.sdbdump;
import static sdb.SDBCmd.sdbload;
import static sdb.SDBCmd.sdbquery;
import static sdb.SDBCmd.setExitOnError;
import static sdb.SDBCmd.setSDBConfig;
import static sdb.SDBCmd.sparql;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.compiler.OpSQL;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTransformCopy;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTransformer;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.StoreConfig;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.util.FileManager;

public class RunSDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    public static void main(String[]argv)
    {
//        SDBConnection.logSQLExceptions = true ;
//        SDBConnection.logSQLStatements = true ;
        
        
        if ( true )
        {
            Query query = QueryFactory.create("SELECT * { ?s ?p ?o optional { ?s ?p ?v } }") ;
            Store store = StoreFactory.create(LayoutType.LayoutTripleNodesHash, DatabaseType.PostgreSQL) ;
            QueryEngineSDB qe = new QueryEngineSDB(store, query) ;
            Op op = qe.getOp() ; 
            SqlNode x = ((OpSQL)op).getSqlNode() ;
            System.out.println(x) ;
            System.out.println() ;
            SqlNode y = SqlTransformer.transform(x, new SqlTransformCopy()) ;
            System.out.println(y) ;
            System.exit(0) ;
        }
        
        
        
        
        setSDBConfig("../SDB-dev/sdb-LUBM-hash.ttl") ;
        sdbdump("--set=jdbcStream=true", "--set=jdbcFetchSize=10") ;
        System.exit(0) ;
        
//        
//        sdb.sdbprint.main("--sdb=sdb.ttl",  "--layout=layout2/index", "--query=Q.rq") ;
//        System.exit(0) ;
        
        //SDBConnection.logSQLQueries = true ;
        //SDBConnection.logSQLStatements = true ;
        
        //runPrint() ;
        //runScript() ;
        
        //runInMem("Q.rq", "D.ttl") ;
        run() ;
        System.err.println("Nothing ran!") ;
        System.exit(0) ;
    }

    private static void _runQuery(String queryFile, String dataFile, String sdbFile)
        {

        // SDBConnection.logSQLStatements = false ;
         // SDBConnection.logSQLExceptions = true ;
         
         setSDBConfig(sdbFile) ;
         
         if ( dataFile != null  )
         {
             setExitOnError(true) ;
             sdbconfig("--create") ; 
             sdbload(dataFile) ;
         }
         //sdbprint("--print=plan", "--file=Q.rq") ; 
         sdbquery("--file=Q.rq") ;
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
//        setSDBConfig("Store/sdb-hsqldb-mem.ttl") ;
//        sdbconfig("--create") ;
//        sdbload("D.ttl") ;

        //sparql("--data="+DIR+"data.ttl","--query="+DIR+"struct-10.rq") ;
        //ARQ.getContext().setFalse(StageBasic.altMatcher) ;
        Store store = SDBFactory.connectStore("Store/sdb-hsqldb-mem.ttl") ;
        store.getTableFormatter().create() ;
        Model model = SDBFactory.connectDefaultModel(store) ;
        FileManager.get().readModel(model, "D.ttl") ;
        Query query = QueryFactory.read("Q.rq") ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        QueryExecUtils.executeQuery(query, qexec) ;
        qexec.close() ;
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