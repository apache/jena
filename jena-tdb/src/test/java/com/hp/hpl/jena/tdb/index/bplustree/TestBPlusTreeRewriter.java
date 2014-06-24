/*
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

package com.hp.hpl.jena.tdb.index.bplustree;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Bytes ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class TestBPlusTreeRewriter extends BaseTest
{
    // See also CmdTestBlusTreeRewriter for randomized testing. 
    
    static int KeySize     = 4 ;
    static int ValueSize   = 8 ;
    static RecordFactory recordFactory = new RecordFactory(KeySize,ValueSize) ;
    
    // The BPlusTreeRewriter works directly on storage.
    static boolean b ; 
    @BeforeClass public static void beforeClass()   { b = BlockMgrFactory.AddTracker ; BlockMgrFactory.AddTracker = false ; }
    @AfterClass  public static void afterClass()    { BlockMgrFactory.AddTracker = b  ;}
    
    @Test public void bpt_rewrite_01()  { runTest(2, 0) ; }
    @Test public void bpt_rewrite_02()  { runTest(3, 0) ; }

    @Test public void bpt_rewrite_03()  { runTest(2, 1) ; }
    @Test public void bpt_rewrite_04()  { runTest(3, 1) ; }

    @Test public void bpt_rewrite_05()  { runTest(2, 2) ; }
    @Test public void bpt_rewrite_06()  { runTest(3, 2) ; }
    
    @Test public void bpt_rewrite_07()  { runTest(2, 100) ; }
    @Test public void bpt_rewrite_08()  { runTest(3, 100) ; }
    
    @Test public void bpt_rewrite_99()  { runTest(5, 1000) ; }
    
    static void runTest(int order, int N)
    { runOneTest(order, N , recordFactory, false) ; }
    
    static void runOneTest(int order, int N, RecordFactory recordFactory, boolean debug)
    {
        BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory) ;
        BPlusTreeRewriter.debug = debug ;

        // ---- Test data
        List<Record> originaldata = TestBPlusTreeRewriter.createData(N, recordFactory) ;

        FileSet destination = FileSet.mem() ;
        // ---- Rewrite
        // Write leaves to ...
        BlockMgr blkMgr1 = BlockMgrFactory.create(destination, Names.bptExtTree, bptParams.getCalcBlockSize(), 10, 10) ;
        // Write nodes to ...
        BlockMgr blkMgr2 = BlockMgrFactory.create(destination, Names.bptExtTree, bptParams.getCalcBlockSize(), 10, 10) ;
        
        BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(originaldata.iterator(), bptParams, 
                                                             recordFactory, blkMgr1, blkMgr2) ;

        if ( debug )
        {
            BPlusTreeRewriterUtils.divider() ;
            bpt2.dump();
        }
        
        // ---- Checking
        bpt2.check() ;
        
        scanComparision(originaldata, bpt2) ;
        findComparison(originaldata, bpt2) ;
        sizeComparison(originaldata, bpt2) ;
    }
    
    public static void scanComparision(List<Record> originaldata, BPlusTree bpt2)
    {
        // ** Scan comparison
        Iterator<Record> iter1 = originaldata.iterator() ;
        Iterator<Record> iter2 = bpt2.iterator() ;
        long count = 0 ;
        for ( ; iter1.hasNext() ; )
        {
            count++ ;
            Record r1 = iter1.next();
            if ( ! iter2.hasNext() )
                error("Deviation: new B+Tree is smaller") ; 
            Record r2 = iter2.next();
    
            if ( ! Record.equals(r1, r2) )
                error("Deviation in iteration record %d: %s : %s", count, r1, r2) ;
        }
        if ( iter2.hasNext() )
            error("New B+Tree larger than original") ; 
    }

    public static void findComparison(List<Record> originaldata, BPlusTree bpt2)
    {
        Iterator<Record> iter1 = originaldata.iterator() ;
    
        long count = 0 ;
        for ( ; iter1.hasNext() ; )
        {
            count++ ;
            Record r1 = iter1.next();
            
            Record r3 = bpt2.find(r1) ;
            if ( r3 == null )
            {
                r3 = bpt2.find(r1) ;
                error("Deviation in find at record %d: %s : null", count, r1) ;
            }
            if ( ! Record.equals(r1, r3) )
                error("Deviation in find at record %d: %s : %s", count, r1, r3) ;
        }
    
    }

    public static void sizeComparison(List<Record> originaldata, BPlusTree bpt2)
    {
      long count1 = originaldata.size() ;
      long count2 = bpt2.size() ;
      //System.out.printf("Sizes = %d / %d\n", count1, count2) ;
      if ( count1 != count2 )
          // Not error - this test does not identify why there was a problem so continue.
          System.err.println("**** DIFFERENT") ;
    }

    static List<Record> createData(int N, RecordFactory recordFactory)
    {
        List<Record> originaldata = new ArrayList<>(N) ;
        for ( int i = 0; i < N ; i++ )
        {
            Record record = recordFactory.create() ;
            Bytes.setInt(i+1, record.getKey()) ;
            if ( recordFactory.hasValue() )
                Bytes.setInt(10*i+1, record.getValue()) ;
            originaldata.add(record) ;
        }
        return originaldata ;
    }
    
    static List<Record> createData2(int ORDER, int N, RecordFactory recordFactory)
    {
        // Use a B+Tree - so original data can be unsorted.
        BPlusTree bpt = SetupTDB.createBPTree(FileSet.mem(), ORDER, -1, -1, -1, recordFactory) ;

        //BPlusTreeParams.checkAll() ;
        // 200 -> runt leaf problem.

        // Problem is that a node in a stripe is less than half full 
        // -> illegal BPT.
        // -> specially rebalance if 2 or more blocks.
        // -> spot one bloc (= root)
        //    PeekIterator.

        for ( int i = 0; i < N ; i++ )
        {
            Record record = recordFactory.create() ;
            Bytes.setInt(i+1, record.getKey()) ;
            bpt.add(record) ;
        }

        return Iter.toList(bpt.iterator()) ;
    }


    private static void error(String string, Object ...args)
    {
        String msg = String.format(string, args) ;
        System.err.println(msg) ;
        throw new BPTreeException(msg) ;
    }
}
