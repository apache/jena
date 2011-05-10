/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import static com.hp.hpl.jena.tdb.index.IndexTestLib.testInsert ;

import java.util.Iterator ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import tx.transaction.TransactionManager ;

import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrLogger ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrTracker ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.base.record.RecordLib ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPTreeNode ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

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
    
    static public void tree_ins_2_01() 
    {
        if ( true )
        {
            Log.enable(BPTreeNode.class.getName(), "ALL") ;
            SystemTDB.Checking = true ;
            BPlusTreeParams.CheckingNode = true ;
            BPlusTreeParams.CheckingTree = true ;
            BPlusTreeParams.Logging = true ;
        }
        int[] keys = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        RangeIndex rIndex = makeRangeIndex(2, 2) ;
        testInsert(rIndex, keys) ;
    }
    
    static protected RangeIndex makeRangeIndex(int order, int minRecords)
    {
        BPlusTree bpt = BPlusTree.makeMem(order, minRecords, RecordLib.TestRecordLength, 0) ;
        BlockMgr mgr1 = bpt.getNodeManager().getBlockMgr() ;
        BlockMgr mgr2 = bpt.getRecordsMgr().getBlockMgr() ;
        
        mgr1 = new BlockMgrTracker("BPT/Nodes", mgr1) ;
        mgr2 = new BlockMgrTracker("BPT/Records", mgr2) ;
        
        return BPlusTree.attach(bpt.getParams(), mgr1, mgr2) ;
    }
    
    public static void main(String... args)
    {
        //tree_ins_2_01() ; exit(0) ;
        
        test.BPlusTreeRun.main("test", "--bptree:track", "3", "10", "1") ; exit(0) ;
        
        bpTreeTracking() ; exit(0) ;
        
        Location location ;
        if ( false )
        {
            String dirname = "DBX" ;
            if ( false && FileOps.exists(dirname) )
                FileOps.clearDirectory(dirname) ;
            location = new Location(dirname) ;
        } else
            location = Location.mem() ;

        TransactionManager txnMgr = new TransactionManager() ;
        DatasetGraphTDB dsg = txnMgr.build(location) ;
        //dsg.add(SSE.parseQuad("(_ <s> <p> 'o')")) ;
        
        DatasetGraphTxView dsgX1 = txnMgr.begin(dsg) ;
        dsgX1.add(SSE.parseQuad("(_ <sx> <px> 'ox1')")) ;
        
//        System.out.println("Base:") ;
//        //System.out.println(dsg) ;
//        query("SELECT count(*) { ?s ?p ?o }", dsg) ;
        
        System.out.println("Transaction:") ;
        //System.out.println(dsgX) ;
        query("SELECT count(*) { ?s ?p ?o }", dsgX1) ;
        update("CLEAR DEFAULT", dsgX1) ;
        query("SELECT count(*) { ?s ?p ?o }", dsgX1) ;
        
        System.out.println("Base:") ;
        //System.out.println(dsg) ;
        query("SELECT count(*) { ?s ?p ?o }", dsg) ;
        
        DatasetGraphTxView dsgX2 = txnMgr.begin(dsg) ;
        dsgX2.add(SSE.parseQuad("(_ <sx> <px> 'ox2')")) ;

        
        System.out.println("Transaction:") ;
        //System.out.println(dsgX) ;
        query("SELECT count(*) { ?s ?p ?o }", dsgX1) ;
        dsgX1.abort() ;
        
        System.out.println("Done") ;
        System.exit(0) ;
        
    }
    
    public static void query(String queryStr, DatasetGraph dsg)
    {
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg)) ;
        QueryExecUtils.executeQuery(query, qExec) ;
    }
    
    public static void update(String updateStr, DatasetGraph dsg)
    {
        UpdateRequest req = UpdateFactory.create(updateStr) ;
        UpdateAction.execute(req, dsg) ;
    }
    
    private static RangeIndex createBPT(int order, RecordFactory rf, boolean logging)
    {
        String label = "B+Tree" ;
        BPlusTreeParams params = new BPlusTreeParams(order, rf) ;
        System.out.println(label+": "+params) ;
        
        int blockSize  = BPlusTreeParams.calcBlockSize(order, rf) ;
        System.out.println("Block size = "+blockSize) ;
        
        BlockMgr mgr1 = BlockMgrFactory.createMem("B1", blockSize) ;
        mgr1 = new BlockMgrTracker("BlkMgr/Nodes", mgr1) ;
        if ( logging )
            mgr1 = new BlockMgrLogger("BlkMgr/Nodes", mgr1, true) ;
        
        BlockMgr mgr2 = BlockMgrFactory.createMem("B2", blockSize) ;

        mgr2 = new BlockMgrTracker("BlkMgr/Records", mgr2) ;
        if ( logging )
            mgr2 = new BlockMgrLogger("BlkMgr/Records", mgr2, true) ;
        
        BPlusTree bpt = BPlusTree.create(params, mgr1, mgr2) ;
        return bpt ;
    }
    
    public static void bpTreeTracking(String... args)
    {
        final Logger log = LoggerFactory.getLogger("BPlusTree") ;

        if ( false )
        {
//            SystemTDB.Checking = true ;
//            BPlusTreeParams.CheckingNode = true ;
//            BPlusTreeParams.CheckingTree = true ;
            BPlusTreeParams.Logging = true ;
            Log.enable(BPTreeNode.class.getName(), "ALL") ;
        }
        RecordFactory rf = new RecordFactory(8,8) ;
        RangeIndex rIndex = createBPT(3, rf, false) ;

        log.info("Created") ;
        log.info("") ;
        boolean b = rIndex.isEmpty() ;
        log.info("isEmpty done") ;
        
        // Add logging.
        //rIndex = new RangeIndexLogger(rIndex, log) ;

        System.out.println() ;
        
        log.info("ADD") ;
        for ( int i = 0 ; i < 50 ; i++ ) 
        {
            log.info("i = "+i) ;
            Record r = record(rf, i+0x100000L, i+0x90000000L) ;
            rIndex.add(r) ;
        }
        //exit(0) ;
        log.info("DELETE") ;
        for ( int i = 0 ; i < 50 ; i++ ) 
        {
            log.info("i = "+i) ;
            Record r = record(rf, i+0x100000L, i+0x90000000L) ;
            rIndex.delete(r) ;
        }

        exit(0) ;
        
        System.out.println() ;
        
        Record r = record(rf, 3+0x100000L, 0) ;
        r = rIndex.find(r) ;
        
        System.out.println() ;
        
        Iterator<Record> iter = rIndex.iterator() ;
        for ( ; iter.hasNext() ; )
            System.out.println(iter.next()) ;
        System.out.println() ;

//        bpt.dump() ;
    }

    static Record record(RecordFactory rf, long key, long val)
    {
        Record r = rf.create() ;
        Bytes.setLong(key, r.getKey()) ;
        Bytes.setLong(val, r.getValue()) ;
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