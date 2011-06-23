/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxnTDB ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;
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
        if ( false ) SystemTDB.setFileMode(FileMode.direct) ;
        
        initFS() ;
        DatasetGraphTDB dsg0 = build() ;
        
        deconstruct(dsg0) ;
        
        load("D.ttl", dsg0) ;
        query("SELECT (Count(*) AS ?c) { ?s ?p ?o }", dsg0) ;
        

        //query("SELECT * { ?s ?p ?o }", dsg0) ;
        //exit(0) ;
        System.out.println("Txn") ;
        DatasetGraphTxnTDB dsg = buildTx(dsg0) ;
        load("D1.ttl", dsg) ;
        Triple t = SSE.parseTriple("( <http://example/z1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example/foo>)") ;
        dsg.delete(new Quad(Quad.defaultGraphNodeGenerated, t)) ;
        
        
        //dsg.commit() ;
        System.out.println("Query 1") ;
        query("SELECT * { ?s ?p ?o }", dsg) ;
        //query("SELECT (Count(*) AS ?c) { ?s ?p ?o }", dsg) ;
        System.out.println("Query 2") ;
        query("SELECT * { ?s ?p ?o }", dsg0) ;
        exit(0) ;
        
        //query("SELECT * { ?s ?p ?o }", dsg0) ;

        load("D1.ttl", dsg0) ;
        System.out.println("Query 2") ;
        query("SELECT * { ?s ?p ?o }", dsg) ;
        
        exit(0) ;
        System.out.println("Commit") ;
        dsg.commit() ;
        query("SELECT * { ?s ?p ?o }", dsg) ;

        exit(0) ;
    }
    
    private static void deconstruct(DatasetGraphTDB dsg)
    {
        /* Better:
        dsg.getBlockMgrs(), getNodeTable() etc 
          */  
        
        // Tuples
        NodeTupleTable ntt = dsg.getTripleTable().getNodeTupleTable() ;
        NodeTable nt = ntt.getNodeTable() ;
        // Find indexes.  Find object table.
        
        for ( TupleIndex index : ntt.getTupleTable().getIndexes() )
        {
            // Find RangeIndex, find BPT, find BlockMgrs.
        }
        
        DatasetPrefixStorage prefixes = dsg.getPrefixes() ;
        // Cast to TDB.
        DatasetPrefixesTDB prefixesTDB = null ;
        
        
        
        
    }

    private static void initFS()
    {
        FileOps.ensureDir(DBdir) ;
        FileOps.clearDirectory(DBdir) ;
    }
    
    private static DatasetGraphTDB build()
    {
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph(DBdir) ;
        return dsg ;
    }

    private static DatasetGraphTxnTDB buildTx(DatasetGraph dsg)
    {
        DatasetGraphTxnTDB dsg2 = new TransactionManager().begin(dsg) ;
        return dsg2 ;
    }

    public static void query(String queryStr, DatasetGraph dsg)
    {
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg)) ;
        QueryExecUtils.executeQuery(query, qExec) ;
    }
    
    private static void load(String file, DatasetGraph dsg)
    {
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