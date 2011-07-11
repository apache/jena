/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexMap ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableInline ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxnTDB ;
import com.hp.hpl.jena.tdb.transaction.NodeTableTrans ;
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
        
        // Tests
        
        // StoreConfig is the static part
        // DatasetControl is the active part.
        //  DSG_TDB does not know the control - set by the builder.
        
        // Read-only dataset for read-only transactions.
        // Not in a transaction.
        
        // ConcurrencyPolicy -> DatasetControl
        //   .resetControl, remove .setReadMode
        
        StoreConnection sConn = StoreConnection.make(DBdir) ;
        DatasetGraphTxnTDB dsg = sConn.begin(ReadWrite.WRITE) ;
        load("D.ttl", dsg) ;
        query("DSG1", "SELECT (count(*) AS ?C) { ?s ?p ?o }", dsg) ;
        
        dsg.abort();
        dsg.close() ;
        
        
    }
    
    private static void write(Graph graph, String lang)
    {
        Model model = ModelFactory.createModelForGraph(graph) ;
        model.write(System.out, lang) ; 
    }

    private static void execNT()
    {
        String dir = "DB" ;
        FileOps.clearDirectory(dir) ;
        
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph(dir) ;
        
//        BPlusTree index = BPlusTree.makeMem(20, 20, SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
//        Index idx = index ;
        Index idx = new IndexMap(recordFactory) ;
            
        //ObjectFile objectFile = FileFactory.createObjectFileMem() ;
        ObjectFile objectFile = FileFactory.createObjectFileDisk("DB/N.jrnl") ;
        NodeTable nt0 = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;

        // Add to the base table.
        Node node1 = NodeFactory.parseNode("<x>") ; 
        NodeId id_1 = nt0.getAllocateNodeId(node1) ;

        // Set up the trans table.
        NodeTableTrans ntt = new NodeTableTrans(nt0, idx, objectFile) ;
        NodeTable nt = NodeTableInline.create(ntt) ;
        
        ntt.begin(null) ;
        
        Node node2 = NodeFactory.parseNode("<y>") ;
        NodeId id_2 = nt.getAllocateNodeId(node2) ;
        System.out.println("==> "+id_2) ;

        Node node3 = NodeFactory.parseNode("123") ;
//      n = nt.getNodeForNodeId(id_2) ;
//      System.out.println("2: ==> "+n) ;
        NodeId id_3 = nt.getAllocateNodeId(node3) ;
        System.out.println("==> "+id_3) ;
        
        Node n = nt.getNodeForNodeId(id_1) ;
        System.out.println("1: ==> "+n) ;
        n = nt.getNodeForNodeId(id_2) ;
        System.out.println("2: ==> "+n) ;
        n = nt.getNodeForNodeId(id_3) ;
        System.out.println("3: ==> "+n) ;

        NodeId x = nt.getNodeIdForNode(node2) ;
        System.out.println("==> "+x) ;
        
        System.out.println("---- Txn") ;
        System.out.println("Base:  "+nt0.getNodeIdForNode(node1)) ;
        System.out.println("Trans: "+nt.getNodeIdForNode(node1)) ;
        System.out.println("Base:  "+nt0.getNodeIdForNode(node2)) ;
        System.out.println("Trans: "+nt.getNodeIdForNode(node2)) ;
        System.out.println("Base:  "+nt0.getNodeIdForNode(node3)) ;
        System.out.println("Trans: "+nt.getNodeIdForNode(node3)) ;
        
        ntt.commit(null) ;
        
        System.out.println("---- Commit") ;
        System.out.println("Base:  "+nt0.getNodeIdForNode(node1)) ;
        System.out.println("Trans: "+nt.getNodeIdForNode(node1)) ;
        System.out.println("Base:  "+nt0.getNodeIdForNode(node2)) ;
        System.out.println("Trans: "+nt.getNodeIdForNode(node2)) ;
        System.out.println("Base:  "+nt0.getNodeIdForNode(node3)) ;
        System.out.println("Trans: "+nt.getNodeIdForNode(node3)) ;
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

    public static void query(String label, String queryStr, DatasetGraph dsg)
    {
        System.out.print("**** ") ;
        System.out.println(label) ;
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