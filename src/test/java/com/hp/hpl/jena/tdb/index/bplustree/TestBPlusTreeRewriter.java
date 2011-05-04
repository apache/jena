/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Bytes ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.bplustree.BPTreeException ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriter ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriterUtils ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class TestBPlusTreeRewriter extends BaseTest
{
    // See also CmdTestBlusTreeRewriter for randomized testing. 
    
    static int KeySize     = 4 ;
    static int ValueSize   = 8 ;
    static RecordFactory recordFactory = new RecordFactory(KeySize,ValueSize) ;
    
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
        BlockMgr blkMgr1 = BlockMgrFactory.create(destination, Names.bptExt1, bptParams.getCalcBlockSize(), 10, 10) ;
        // Write nodes to ...
        BlockMgr blkMgr2 = BlockMgrFactory.create(destination, Names.bptExt1, bptParams.getCalcBlockSize(), 10, 10) ;
        
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
        List<Record> originaldata = new ArrayList<Record>(N) ;
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
        BPlusTree bpt = (BPlusTree)SetupTDB.createBPTree(FileSet.mem(), ORDER, -1, -1, -1, recordFactory) ;
        //BPlusTree bpt = BPlusTree.makeMem(ORDER, bptParams.getMinRec() , 4, 0) ;

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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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