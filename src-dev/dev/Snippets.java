/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static lib.FileOps.clearDirectory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import lib.FileOps;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.bplustree.BPlusTree;
import com.hp.hpl.jena.tdb.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.btree.BTreeParams;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;
import com.sleepycat.je.*;

public class Snippets
{
    public static void bpt_test()
    {
        //TDB.getContext().set(TDB.symFileMode, "mapped" ) ;
        Location location = new Location("tmp") ;
        String fnNodes = location.getPath("X", "idn") ;
        String fnRecords = location.getPath("X", "dat") ;
        
        boolean disk = true ;
        
        BPlusTree bt = null ;
        BPlusTreeParams params = null ;
        
        BlockMgr mgr1 = null ;
        BlockMgr mgr2 = null ;
        
        if ( disk )
        {
            FileOps.clearDirectory(location.getDirectoryPath()) ;
            int order = BPlusTreeParams.calcOrder(Const.BlockSize, PGraphBase.indexRecordFactory) ;
            params = new BPlusTreeParams(order, PGraphBase.indexRecordFactory) ;
            mgr1 = BlockMgrFactory.createFile(fnNodes, Const.BlockSize) ;
            mgr2 = BlockMgrFactory.createFile(fnRecords, Const.BlockSize) ;
        }
        else
        {
            params = new BPlusTreeParams(5, PGraphBase.indexRecordFactory.keyLength(), PGraphBase.indexRecordFactory.valueLength()) ;
            
            int maxRecords = 2*5 ;
            int rSize = RecordBufferPage.HEADER+(maxRecords*params.getRecordLength()) ;
            
            mgr1 = BlockMgrFactory.createMem("Nodes", params.getBlockSize()) ;
            mgr2 = BlockMgrFactory.createMem("Records", rSize) ;
        }
        
        bt = BPlusTree.attach(params, mgr1, mgr2) ;
        NodeId n = NodeId.create(0x41424344) ;
        Record rn = NodeLib.record(PGraphBase.indexRecordFactory, n, n, n) ;
        bt.add(rn) ;
        bt.sync(true) ;
        
        bt.dump() ;
        
        System.out.println() ;
        
        bt = null ;
        
        // Reattach
        if ( disk )
        {
            mgr1 = BlockMgrFactory.createFile(fnNodes, Const.BlockSize) ;
            mgr2 = BlockMgrFactory.createFile(fnRecords, Const.BlockSize) ;
        }
        
        bt = BPlusTree.attach(params, mgr1, mgr2) ;
        
        System.out.println("Loop") ;
        Iterator<Record> iter = bt.iterator() ;
        for ( ; iter.hasNext() ; )
        {
            Record r = iter.next() ;
            System.out.println(r) ;
        }
        
        
        System.exit(0) ;
    
    }
    public static void BDB()
        {
            Environment myDbEnvironment = null;
            Database myDatabase = null ;
    
            try {
                try {
                    EnvironmentConfig envConfig = new EnvironmentConfig();
                    envConfig.setAllowCreate(true);
                    //envConfig.setTransactional(true) ;
                    myDbEnvironment = new Environment(new File("tmp/dbEnv"), envConfig);
    
                    // Open the database. Create it if it does not already exist.
                    DatabaseConfig dbConfig = new DatabaseConfig();
                    //dbConfig.setTransactional(true) ;
    
                    dbConfig.setAllowCreate(true);
                    myDatabase = myDbEnvironment.openDatabase(null, 
                                                              "GRAPH", 
                                                              dbConfig); 
                    String aKey = "key" ;
                    DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));
                    DatabaseEntry theData = new DatabaseEntry("DATA".getBytes("UTF-8"));
    
    //                if ( myDatabase.put(null, theKey, theData) !=
    //                    OperationStatus.SUCCESS )
    //                {
    //                    System.out.println("Bad put") ;
    //                    return ;
    //                }
    
                    // Perform the get.
                    if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) ==
                        OperationStatus.SUCCESS) {
                        System.out.println(theData) ;
                        String s = new String(theData.getData(), "UTF-8") ;
                        System.out.println("Get: "+s) ;
                    }
    
    
                } catch (DatabaseException dbe) {
                    dbe.printStackTrace(); 
                    // Exception handling goes here
                } catch (UnsupportedEncodingException ex)
                {
                    ex.printStackTrace();
                } 
                try {
                    if (myDatabase != null) {
                        myDatabase.close();
                    }
                    if (myDbEnvironment != null) {
                        myDbEnvironment.cleanLog();
                        myDbEnvironment.close();
                    } 
                } catch (DatabaseException dbe) {
                    dbe.printStackTrace();
                }
            } finally { 
                System.out.println("Finished/BDB") ;
                System.exit(0) ;
            }
        }

    static void typedNode()
        {
    //      typedNode("'2008-04-27T16:52:17+01:00'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17-05:00'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17Z'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17+00:00'^^xsd:dateTime") ;
          typedNodeOne("'2008-04-27T16:52:17'^^xsd:dateTime") ;
          typedNodeOne("'2008-04-27'^^xsd:date") ;
          System.exit(0) ;
        }

    static void typedNodeOne(String x)
    {
        System.out.println("Input = "+x) ;
        Node n = SSE.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        if ( nodeId == null )
        {
            System.out.println("null nodeid") ;
            return ;
        }
        
        System.out.printf("NodeId : %s\n", nodeId) ;
        Node n2 = NodeId.extract(nodeId) ;
        if ( n2 == null )
        {
            System.out.println("null node") ;
            return ;
        }
        String y = FmtUtils.stringForNode(n2) ;
        System.out.println("Output = "+y) ;
        if ( ! n.equals(n2) )
        {
            System.out.println("Different:") ;
            System.out.println("  "+n) ;
            System.out.println("  "+n2) ;
        }
    }

    static void abbrev()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        abbrev.add("z", "http://") ;
        abbrevOne(abbrev, "http://example") ;
        abbrevOne(abbrev, "foo") ;
        abbrevOne(abbrev, ":foo") ;
        abbrevOne(abbrev, "::foo") ;
        abbrevOne(abbrev, ":::foo") ;
    }

    static void abbrevOne(StringAbbrev abbrev, String string)
    {
        String a = abbrev.abbreviate(string) ;
        String a2 = abbrev.expand(a) ;
        System.out.println(string) ;
        System.out.println(a) ;
        System.out.println(a2) ;
        System.out.println() ;
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
     
    public static void btreePacking(int slots, int slotSize, int blkSize)
    {
        //divider() ;
        RecordFactory f  = new RecordFactory(slots*slotSize/8,0) ;
        System.out.printf("Input: %d slots, size %d bytes, %d blocksize\n", slots,slotSize/8, blkSize ) ;
        System.out.println("Btree: "+BTreeParams.calcOrder(blkSize, f.recordLength())) ;      
        System.out.println("Packed leaf : "+blkSize/f.recordLength()) ;
        BTreeParams p = new BTreeParams(BTreeParams.calcOrder(blkSize, f.recordLength()), f) ;
        System.out.println(p) ;
    }             
    

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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