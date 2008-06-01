/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.btree;

import static com.hp.hpl.jena.tdb.base.ConfigTest.TestRecordLength;

import java.io.File;

import test.BaseTest;

import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;

/** Support for testing BTrees */

public class BTreeTestBase extends BaseTest
{
    public static class BTreeMaker implements RangeIndexMaker
    {
        private int order ;
    
        public BTreeMaker(int order) { this.order = order ; }
        
        @Override
        public RangeIndex make()
        {
            BTreeParams p = new BTreeParams(order, TestRecordLength, 0) ;
            BlockMgr mgr = null ;
            
            if ( true )
                mgr = BlockMgrFactory.createMem(p.getBlockSize()) ;
            else
            {
                File f = new File(filename) ;
                f.delete() ;
                mgr = BlockMgrFactory.createFile(filename, p.getBlockSize()) ;
            }
            BTree bTree = new BTree(order, TestRecordLength, mgr) ;
            return bTree ;
        }
        
        @Override
        public String getLabel() { return "Btree order = "+order ; } 
    }

    static String filename = "tmp/test.btree" ;
//    public static BTree createBTree(int order)
//    {
//        BTreeParams p = new BTreeParams(order, TestRecordLength, 0) ;
//        BlockMgr mgr = null ;
//        
//        if ( true )
//            mgr = BlockMgrFactory.createMem(p.getBlockSize()) ;
//        else
//        {
//            File f = new File(filename) ;
//            f.delete() ;
//            mgr = BlockMgrFactory.createFile(filename, p.getBlockSize()) ;
//        }
//        BTree bTree = new BTree(order, TestRecordLength, mgr) ;
//        return bTree ;
//    }

    public static RangeIndex buildRangeIndex(RangeIndexMaker maker, int[] keys)
    {
        RangeIndex rIndex = maker.make() ;
        RangeIndexTestLib.add(rIndex, keys) ;
        return rIndex ;
    }

    public static BTree testInsert(int order, int[] keys)
    {
        BTree bt = buildBTree(order) ;
        RangeIndexTestLib.testInsert(bt, keys) ;
        return bt ;
    }

    private static BTree buildBTree(int order)
    {
        {
            BTreeParams p = new BTreeParams(order, TestRecordLength, 0) ;
            BlockMgr mgr = null ;
            
            if ( true )
                mgr = BlockMgrFactory.createMem(p.getBlockSize()) ;
            else
            {
                File f = new File(filename) ;
                f.delete() ;
                mgr = BlockMgrFactory.createFile(filename, p.getBlockSize()) ;
            }
            BTree bTree = new BTree(order, TestRecordLength, mgr) ;
            return bTree ;
        }
    }

    public static BTree testDelete(int order, int[] keys1, int[] keys2)
    {
        BTree bt = buildBTree(order) ;
        RangeIndexTestLib.testInsertDelete(bt, keys1, keys2) ;
        return bt ; 
    }

    public static void randTest(int order, int maxValue, int numKeys)
    {
        RangeIndexTestLib.randTest(new BTreeMaker(order), maxValue, numKeys) ;
    }

    static BTree buildBTree(int order, int[] keys)
    {
        BTree bt = buildBTree(order) ;
        RangeIndexTestLib.add(bt, keys) ;
        return bt ;
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