/*
 * ong time (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static lib.FileOps.clearDirectory;

import java.util.Iterator;

import lib.FileOps;
import org.apache.log4j.Level;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.graph.Graph;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.bplustree.BPlusTree;
import com.hp.hpl.jena.tdb.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.btree.BTreeParams;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;

public class Run
{
    static String divider = "" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = "" ;
    }
    
    public static void main(String ... args)
    {
        org.apache.log4j.Logger.getLogger("com.hp.hpl.jena.tdb").setLevel(Level.ALL) ;
        // Don't seem to tbe writing to disk.
        // Run once data created. 
        bpt_test() ; System.exit(0) ;
        
        // Also check absent triples.
        
        // Do NOW!
        TDB.getContext().set(TDB.symIndexType, "bplustree") ;
        
        Location loc = new Location("tmp") ;
        FileOps.clearDirectory(loc.getDirectoryPath()) ;
        
        Graph graph = TDBFactory.createGraph(loc) ;
        
//        Node s1 = SSE.parseNode("<s1>") ;
//        Node p1 = SSE.parseNode("<p1>") ;
//        Node o1 = SSE.parseNode("<o1>") ;
//        Node s2 = SSE.parseNode("<s2>") ;
//        Node p2 = SSE.parseNode("<p2>") ;
//        Node o2 = SSE.parseNode("<o2>") ;
//        Triple t1 = new Triple(s1,p1,o1) ; 
//        Triple t2 = new Triple(s2,p2,o2) ;
//        
//        graph.add(t1) ;
//        graph.add(t2) ;
//        graph.delete(t1) ;
        
        
        Model model = ModelFactory.createModelForGraph(graph) ;
        
        //query("SELECT * { <no> <no> <no>}", model) ;
        //System.out.flush() ;
        
        if ( model.isEmpty() )
        {  
            System.out.println("**** Load data") ;
            FileManager.get().readModel(model, "D.ttl") ;
            ((PGraphBase)graph).sync(true) ;
            System.out.println("Size = "+model.size()) ;
        }
        query("SELECT * { ?s ?p ?o}", model) ;
        model.close() ;
        
        System.exit(0) ;
        
        //tdbquery("dataset.ttl", "SELECT * { ?s ?p ?o}") ;

//        ARQ.getContext().set(TDB.symFileMode, "mapped") ;
//        Model model = TDBFactory.createModel("tmp") ;
//        query("SELECT * { ?s ?p ?o}", model) ;
//        System.exit(0) ;
    }
    
    public static void bpt_test()
    {
        TDB.getContext().set(TDB.symFileMode, "mapped" ) ;
        Location location = new Location("tmp") ;
        FileOps.clearDirectory(location.getDirectoryPath()) ;
        
        int order = BPlusTreeParams.calcOrder(Const.BlockSize, PGraphBase.indexRecordFactory) ;
        BPlusTreeParams params = new BPlusTreeParams(order, PGraphBase.indexRecordFactory) ;
        
        String fnNodes = location.getPath("X", "idn") ;
        
        BlockMgr blkMgrNodes = BlockMgrFactory.createFile(fnNodes, Const.BlockSize) ;
        
        String fnRecords = location.getPath("X", "dat") ;
        BlockMgr blkMgrRecords = BlockMgrFactory.createFile(fnRecords, Const.BlockSize) ;
        
        BPlusTree bt = BPlusTree.attach(params, blkMgrRecords, blkMgrNodes) ;
        
        NodeId n = NodeId.create(0x41424344) ;
        Record rn = NodeLib.record(PGraphBase.indexRecordFactory, n, n, n) ;
        bt.add(rn) ;
        bt.sync(true) ;
        bt = null ;
        
        // Reattach
        blkMgrNodes = BlockMgrFactory.createFile(fnNodes, Const.BlockSize) ;
        blkMgrRecords = BlockMgrFactory.createFile(fnRecords, Const.BlockSize) ;
        bt = BPlusTree.attach(params, blkMgrRecords, blkMgrNodes) ;
        
        System.out.println("Loop") ;
        Iterator<Record> iter = bt.iterator() ;
        for ( ; iter.hasNext() ; )
        {
            Record r = iter.next() ;
            System.out.println(r) ;
        }
        
        
        System.exit(0) ;

    }
    
    
    static int BlockSize               = 8*1024 ;
    static int SegmentSize = 8 * 1024 * 1024 ; 
    static int blocksPerSegment = SegmentSize/BlockSize ;
    
    private static int segment(int id) { return id/blocksPerSegment ; }
    private static int byteOffset(int id) { return (id%blocksPerSegment)*BlockSize ; }

    
    public static void seg()
    {
//        Id: 1179
//        Seg=1
//        Segoff=1,269,760

        System.out.printf("Blocksize = %d , Segment size = %d\n", BlockSize, SegmentSize) ;
        System.out.printf("blocksPerSegment = %d\n", blocksPerSegment) ;
        
        
        for ( int id : new int[]{1,2,3,4,5,1428, 1179})
        {
            int seg = segment(id) ;                     // Segment.
            int segOff = byteOffset(id) ; 
            System.out.printf("%d => [%d, %,d]\n", id, seg, segOff) ;
            System.out.printf("%,d\n", id*BlockSize) ;
        }
        System.exit(0) ;
//        String[] a = { "--set", "tdb:logBGP=true", "--desc="+assembler, query } ;
//        tdb.tdbquery.main(a) ;
//        System.exit(0) ;
        
 
        
        // ----
        btreePacking(3, 32, 8*1024) ; System.exit(0) ;
        btreePacking(3, 64, 8*1024) ;
        btreePacking(4, 128, 8*1024) ;
        System.exit(0) ;
                
        // ----
        System.exit(0) ;
        
        // ----
        String dir = "tmp" ;
        clearDirectory(dir) ;
        System.exit(0) ;
    }
     
    private static void report()
    {
        ARQ.getContext().set(TDB.symFileMode, "mapped") ;
        
        Model model = TDBFactory.createModel("foo");
        Resource r = model.createResource("http://com.xxx/test");

        Property op = model.createProperty("http://property/bar");
        Statement s = r.getProperty(op);
        if (s == null) {
            Resource list = model.createList();
            r.addProperty(op, list);
            s = r.getProperty(op);
        }

        model.write(System.err);
        
        //model.close() ;
        
        System.err.println("-------------");
        model = TDBFactory.createModel("foo");
        model.write(System.err);


        System.exit(0) ;

    }


    public static void btreePacking(int slots, int slotSize, int blkSize)
    {
        divider() ;
        RecordFactory f  = new RecordFactory(slots*slotSize/8,0) ;
        System.out.printf("Input: %d slots, size %d bytes, %d blocksize\n", slots,slotSize/8, blkSize ) ;
        System.out.println("Btree: "+BTreeParams.calcOrder(blkSize, f.recordLength())) ;      
        System.out.println("Packed leaf : "+blkSize/f.recordLength()) ;
        BTreeParams p = new BTreeParams(BTreeParams.calcOrder(blkSize, f.recordLength()), f) ;
        System.out.println(p) ;
    }             
    
    private static void query(String str, Model model)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    private static void tdbquery(String assembler, String query)
    {
        String[] a = { "--set", "tdb:logBGP=true", "--desc="+assembler, query } ;
        tdb.tdbquery.main(a) ;
        System.exit(0) ;
    }
    
    private static void tdbloader(String assembler, String file)
    {
        tdb.tdbloader.main("--desc="+assembler, file) ; 
        System.exit(0) ;
    }
    
    private static void tdbconfig(String assembler, String file)
    {
        tdb.tdbconfig.main("stats", "--desc="+assembler) ;
        System.exit(0) ;
    }

}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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