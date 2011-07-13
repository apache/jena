/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.util.FileManager ;

public class TxMain
{
    static { Log.setLog4j() ; }
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    static void exit(int rc)
    {
        System.out.println("EXIT") ;
        System.exit(rc) ;
    }
    
    static public String DBdir = "DB" ;
    
    public static void main(String... args)
    {
        initFS() ;
        StoreConnection sConn = StoreConnection.make(DBdir) ;
        
        // ---- Simple
        if ( false )
        {
            DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
            //load("D.ttl", dsg) ;    // Loads 3 triples
            dsg.getDefaultGraph().add(SSE.parseTriple("(<s> <p> <o>)")) ;
            dsg.add(SSE.parseQuad("(<g> <s> <p> 123)")) ;
            dsg.add(SSE.parseQuad("(_ <s> <p> 123)")) ;
            
            dsg.commit() ;
            dsg.close() ;
            DatasetGraphTxn dsgRead = sConn.begin(ReadWrite.READ) ;
            dump(dsgRead) ;
            query("SELECT ?g (count(*) AS ?C) { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } } GROUP BY ?g", dsgRead) ;
            dsgRead.close() ;
            exit(0) ;
        }
        
        if ( false )
        {
            // Blocking transaction
            DatasetGraphTxn dsgRead = sConn.begin(ReadWrite.READ) ;
            
            
            DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ; // Optional label.
            //load("D.ttl", dsg) ;    // Loads 3 triples
            dsg.getDefaultGraph().add(SSE.parseTriple("(<s> <p> <o>)")) ;
            dsg.add(SSE.parseQuad("(<g> <s> <p> 123)")) ;
            dsg.add(SSE.parseQuad("(_ <s> <p> 123)")) ;
            dsg.commit() ;
            dsg.close() ;
            
            dump(dsgRead) ;
            query("SELECT ?g (count(*) AS ?C) { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } } GROUP BY ?g", dsgRead) ;
            dsgRead.close() ;
            DatasetGraphTxn dsgRead2  = sConn.begin(ReadWrite.READ) ;
            query("SELECT ?g (count(*) AS ?C) { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } } GROUP BY ?g", dsgRead2) ;
            dsgRead2.close() ;
            exit(0) ;
        }
        
        // BUG somewhere.
        //   Check DevTx list of things to do.
        // Take a blocking read connection.
        DatasetGraphTxn dsgRead = sConn.begin(ReadWrite.READ) ; //dsgRead.close() ;

        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        load("D.ttl", dsg) ;    // Loads 3 triples
        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead) ;
        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ;
        dsg.commit() ;
        dsg.close() ;
        dsg = null ;
        
        // Reader after update.
        // First reader still reading.
        
        //DatasetGraphTxn dsgRead2 = sConn.begin(ReadWrite.READ) ;
        //query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead2) ;
        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead) ;

        // A writer.
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.WRITE) ;
        load("D1.ttl", dsg2) ; // Loads 1 triples
        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg2) ;
        //query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead2) ;
        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead) ;
        dsg2.commit() ;
        dsg2.close() ;
        dsg2 = null ;

        //dsgRead2.close() ;

//        DatasetGraphTxn dsgRead2 = sConn.begin(ReadWrite.READ) ;
//        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead2) ;
//        dsgRead2.close() ;

        dsgRead.close() ;   // Transaction can now write changes to the real DB.
        
        // ILLEGAL!!!!
        // query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead) ;
        
        DatasetGraphTxn dsgRead3 = sConn.begin(ReadWrite.READ) ;
        query("SELECT (count(*) AS ?C) { ?s ?p ?o }", dsgRead3) ;
        
        exit(0) ;
    }
    
    private static void write(Graph graph, String lang)
    {
        Model model = ModelFactory.createModelForGraph(graph) ;
        model.write(System.out, lang) ; 
    }

    private static void initFS()
    {
        FileOps.ensureDir(DBdir) ;
        FileOps.clearDirectory(DBdir) ;
    }
    
    private static DatasetGraphTDB build()
    {
        //return DatasetBuilderStd.build() ;
        //DatasetGraphTDB dsg = TDBFactory.createDatasetGraph(DBdir) ;
        DatasetGraphTDB dsg = DatasetBuilderStd.build(DBdir) ;
        return dsg ;
    }

    public static void dump(DatasetGraphTxn dsg)
    {
        RiotWriter.writeNQuads(System.out, dsg) ;
    }
    
    public static void query(String queryStr, DatasetGraphTxn dsg)
    {
        String x = "Query ("+dsg.getTransaction().getLabel()+")" ;
        query(x, queryStr, dsg) ;
    }
    
    public static void query(String label, String queryStr, DatasetGraphTxn dsg)
    {
        System.out.print("**** ") ;
        System.out.println(label) ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg)) ;
        QueryExecUtils.executeQuery(query, qExec) ;
    }
    
    private static void load(String file, DatasetGraphTxn dsg)
    {
        String x = "Load ("+dsg.getTransaction().getLabel()+")" ;
        load(x, file, dsg) ;
    }
    
    private static void load(String label, String file, DatasetGraph dsg)
    {
        System.out.print("**** ") ;
        System.out.println(label) ;
        System.out.println("Load: "+file) ;
        Model m = ModelFactory.createModelForGraph(dsg.getDefaultGraph()) ;
        FileManager.get().readModel(m, file) ;
        
    }

    public static void update(String updateStr, DatasetGraph dsg)
    {
        UpdateRequest req = UpdateFactory.create(updateStr) ;
        UpdateAction.execute(req, dsg) ;
    }
    
    static Record record(RecordFactory rf, int key, int val)
    {
        Record r = rf.create() ;
        Bytes.setInt(key, r.getKey()) ;
        if ( rf.hasValue() )
            Bytes.setInt(val, r.getValue()) ;
        return r ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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