/**
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

package dev;

import static sdb.SDBCmd.sdbconfig ;
import static sdb.SDBCmd.sdbload ;
import static sdb.SDBCmd.sdbquery ;
import static sdb.SDBCmd.setExitOnError ;
import static sdb.SDBCmd.setSDBConfig ;
import static sdb.SDBCmd.sparql ;

import java.sql.Connection ;
import java.sql.SQLException ;
import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.logging.Log ;
import sdb.SDBCmd ;
import arq.cmd.CmdUtils ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sdb.SDBFactory ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.sql.JDBC ;
import com.hp.hpl.jena.sdb.sql.SDBConnection ;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph ;
import com.hp.hpl.jena.sdb.store.StoreConfig ;
import com.hp.hpl.jena.sdb.test.junit.SDBTestUtils ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.util.FileManager ;

public class RunSDB
{
    static { Log.setLog4j() ; CmdUtils.setN3Params() ; }
    
    private static Store create(Model assem)
    {
        // Create a store and format
        Dataset ds = DatasetFactory.assemble(assem) ;
        Store store = ((DatasetStoreGraph)ds.asDatasetGraph()).getStore() ;
        store.getTableFormatter().create() ;
        return store ;
    }
    
    public static void main(String ... argv) throws SQLException
    {
        sdb.sdbprint.main("--sdb=Store/sdb-pgsql.ttl", 
                         //"--sdb=Store/sdb-mssql-express.ttl",
                         "--print=op",
                         "--print=sql",
                         "--file=Q.rq") ;
        System.exit(0) ;
        sdb.sdbprint.main("--sdb=sdb.ttl", "--print=op", "@Q.arq") ;
        System.exit(0) ;
        
        {
            String dir = "testing/Assembler/" ; 
            Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
            Resource xDft = assem.getResource("http://example/test#graphDft") ;
            Resource xNamed = assem.getResource("http://example/test#graphNamed") ;

            Store store = create(assem) ;

            Model model1 = (Model)Assembler.general.open(xDft) ;
            Model model2 = (Model)Assembler.general.open(xNamed) ;
            
            Resource s = model1.createResource() ;
            Property p = model1.createProperty("http://example/p") ;
            Literal o = model1.createLiteral("foo") ;
            
            model1.add(s,p,o) ;
            System.out.println(model1.contains(s, p, o)) ;
            
            System.out.println("----") ;
            model1.write(System.out, "TTL") ;
            System.out.println("----") ;
            model2.write(System.out, "TTL") ;
            System.out.println("----") ;
            
            model1.size() ;
            
            System.out.println("Size 1 : "+model1.size()) ;
            System.out.println("Size 2 : "+model2.size()) ;
            
//            System.out.println("model1:") ;
//            StmtIterator sIter1 = model1.listStatements() ;
//            for ( ; sIter1.hasNext() ; )
//                System.out.println(sIter1.next()) ;
//            
//            System.out.println("model2:") ;
//            StmtIterator sIter2 = model1.listStatements() ;
//            for ( ; sIter2.hasNext() ; )
//                System.out.println(sIter2.next()) ;
            
            System.out.println((model1.isIsomorphicWith(model2))) ;
            System.exit(0) ;
            
        }
        {
            
            
            //sdb.sdbconfig.main("--sdb=sdb.ttl", "--create") ;
            //sdb.sdbprint.main("--sdb=sdb.ttl", "select *  where { GRAPH<G> { ?s ?p ?o}} limit 5") ;
            sdb.sdbprint.main("--sdb=sdb.ttl", "--file=Q.rq") ;
            System.exit(0) ;
        }        
        {
            // Make sure the database is created but empty first.
            String modelName = "http://example/g1" ;
            Store store = SDBFactory.connectStore("sdb.ttl") ;
            // Update.

            Dataset dataset = SDBFactory.connectDataset(store);
            Model model = SDBFactory.connectNamedModel(store, modelName);
            FileManager.get().readModel(model, "D.ttl") ;
            Iterator<String> modelNameIterator = dataset.listNames();
            for ( ; modelNameIterator.hasNext() ; )
            {
                System.out.println("Model: "+modelNameIterator.next()) ;
            }
            System.out.println("1 -----") ;
            model.write(System.out, "N-TRIPLES") ;
            System.out.println("-----") ;
            model.removeAll() ;
            System.out.println("2 -----") ;
            model.write(System.out, "N-TRIPLES") ;
            System.out.println("-----") ;
            store.close() ;
            

            store = SDBFactory.connectStore("sdb.ttl") ;
            dataset = SDBFactory.connectDataset(store);
            modelNameIterator = dataset.listNames();
            for ( ; modelNameIterator.hasNext() ; )
            {
                System.out.println("Model: "+modelNameIterator.next()) ;
            }
            model = SDBFactory.connectNamedModel(store, modelName);
            System.out.println("3 -----") ;
            model.write(System.out, "N-TRIPLES") ;
            System.out.println("-----") ;
            System.exit(0) ;
        }
//        sdb.sdbtest.main("--sdb=sdb.ttl", "testing/manifest-sdb.ttl") ;
//        System.exit(0) ;
        
        //TestStores2Connections1.main(argv) ;
        
        runPrint("Q.rq") ; System.exit(0) ;

        {
            // SPARQL/Update
            Store store = SDBTestUtils.createInMemoryStore() ;
            
            GraphStore gs = SDBFactory.connectGraphStore(store) ;
            UpdateAction.readExecute("update.ru", gs) ;
            
            Iter<Node> iter = Iter.iter(gs.listGraphNodes()) ;
            System.out.println(">>>");
            for ( Node n : iter)
                System.out.println(n);
            System.out.println("<<<");
            
            // Does not see new graph.
            SSE.write(gs.toDataset()) ;
//            
//            
//            SSE.write(gs.getDefaultGraph()) ;
//            IndentedWriter.stdout.println();
            System.out.println("-- Get named graph");
            SSE.write(gs.getGraph(Node.createURI("http://example/foo"))) ;
            IndentedWriter.stdout.println() ;
//            
            System.out.println("----");
            Dataset ds = SDBFactory.connectDataset(store) ;
            SSE.write(ds) ;
          System.out.println("----");
            System.exit(0) ;
        }
        runPrint() ;
    }
    

    public static void cmds()
    {
        SDBCmd.setSDBConfig("sdb.ttl") ;
        sdbconfig("--create") ;
        sdbload("--graph=file:data1", "D1.ttl") ;
        sdbload("--graph=file:data2", "D2.ttl") ;
        sdbquery("--query=Q.rq") ;
        System.exit(0) ;
    }
    
    public static void devEnsureProject()
    {
        // Hit ensureProject?
        // Constructors to read file and get a model.
        Store store = SDBFactory.connectStore("sdb.ttl") ;
        Model model = SDBFactory.connectDefaultModel(store) ;
        System.out.println(model.contains(null, null, (RDFNode)null)) ;
        System.exit(0) ;
    }
    
    private static void runQuery(String queryFile, String dataFile, String sdbFile)
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

    private static void runQueryDefaultModel(String queryFile, String sdbFile)
    {
        Model model = SDBFactory.connectDefaultModel(sdbFile) ;
        Query query = QueryFactory.read(queryFile) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        ResultSetFormatter.out(qexec.execSelect()) ;
        qexec.close() ;
    }
    
    
    private static void runQuery(String queryFile)
    {
        Store store = SDBFactory.connectStore("sdb.ttl") ;
        Dataset ds = SDBFactory.connectDataset(store) ;
        DatasetGraph dsg = ds.asDatasetGraph() ;
        Iterator<?> iter = dsg.listGraphNodes() ;
        for ( ; iter.hasNext() ; )
        {
            Object x = iter.next();
            System.out.println(x) ;
        }
        System.out.println("Query") ;
        Query query = QueryFactory.read(queryFile) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        
        QueryExecUtils.executeQuery(query, qExec, null) ;
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
        sdb.sdbprint.main("--print=sql", "--print=query", "--print=op", "--sdb=sdb.ttl", "--query="+filename) ;
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
        Log.setLog4j() ;
        
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
        
        
        
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver") ;
            String url = "jdbc:db2://sweb-sdb-4:50000/TEST" ;
                
            Connection conn = JDBC.createConnection(url, "user", "password") ;
            System.out.println("Happy") ;
        
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
        }
        
        System.exit(0) ;
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
