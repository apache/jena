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

import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriterUtils.divider ;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriterUtils.printIndexBlocks ;
import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriterUtils.summarizeDataBlocks ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorWithBuffer ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.buffer.PtrBuffer ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr ;

public class BPlusTreeRewriter
{
    static private Logger log = LoggerFactory.getLogger(BPlusTreeRewriter.class) ; 

    static boolean rebalance = true ;
    static boolean debug = false ;
    static boolean materialize = debug ;
    
    // Process:
    // 1/ Take a stream of records and create leaves.
    //    Emit the RecordBufferPage (B+Tree leaves).
    // 2/ Take a stream of RecordBufferPage and create the first level of branches.
    // 3/ Take each branch level and create upper braches until root hit.
    // 4/ Copy root block to root real location.
    
    // --------------------------------
    
    /** Given a stream of records and details of the B+Tree to build, go and build it. 
     *  @return A newly built BPlusTree
     */ 
    public static BPlusTree packIntoBPlusTree(Iterator<Record> iterRecords, 
                                              BPlusTreeParams bptParams, 
                                              RecordFactory recordFactory, 
                                              BlockMgr blkMgrNodes,
                                              BlockMgr blkMgrRecords)
    {
        // **** Attach to storage.
        // Small caches as we mostly work on a block then move on.
        // Only read behind and look ahead actually do any work on an existing block.
        // (check this by rerunning with different cache sizes).
        
        if ( ! iterRecords.hasNext() )
            // No records. Just return a B+Tree.
            return BPlusTree.create(bptParams, blkMgrNodes, blkMgrRecords) ;
    
        // Dummy B+tree needed to carry parameters around.
        BPlusTree bpt2 = BPlusTree.attach(bptParams, blkMgrNodes, blkMgrRecords) ;
    
        // Allocate and format a root index block.
        // We will use this slot later and write in the correct root.
        /// The root has to be block zero currently.

        BPTreeNode root = bpt2.getNodeManager().createNode(BPlusTreeParams.RootParent) ;
        int rootId = root.getId() ;
        if ( rootId != 0 )
        {
            log.error("**** Not the root: "+rootId) ;
            throw new BPTreeException() ;
        }
    
        // ******** Pack data blocks.
        Iterator<Pair<Integer, Record>> iter = writePackedDataBlocks(iterRecords, bpt2) ;
    
        // ******** Index layer
        // Loop until one block only.
        // Never zero blocks.
        // Output is a single pair pointing to the root - but the root is in the wrong place.
    
        boolean leafLayer= true ;
        while(true)
        {
            iter = genTreeLevel(iter, bpt2, leafLayer) ;
            // Advances iter.
            IteratorWithBuffer<Pair<Integer, Record>> iter2 = new IteratorWithBuffer<>(iter, 2) ;
            boolean singleBlock = ( iter2.peek(1) == null ) ;
            // Having peeked ahead, use the real stream.
            iter = iter2 ;
            if ( singleBlock )
                break  ;
            leafLayer = false ;
        }

        // ******** Put root in right place.
        Pair<Integer, Record> pair = iter.next() ;
        if ( iter.hasNext() )
        {
            log.error("**** Building index layers didn't result in a single block") ;
            return null ;
        }
        fixupRoot(root, pair, bpt2) ;

        // ****** Finish the tree.
        blkMgrNodes.sync() ;
        blkMgrRecords.sync() ;
        // Force root reset.
        bpt2 = BPlusTree.create(bptParams, blkMgrNodes, blkMgrRecords) ;
        return bpt2 ;
    }

    // **** data block phase

    /** Pack record blocks into linked RecordBufferPages */
    private static Iterator<Pair<Integer, Record>> writePackedDataBlocks(Iterator<Record> records, final BPlusTree bpt)
    {
        if ( debug )
        {
            divider() ;
            System.out.println("---- Data level") ;
        }

        final RecordBufferPageMgr mgr = bpt.getRecordsMgr().getRecordBufferPageMgr() ;
        Iterator<RecordBufferPage> iter = new RecordBufferPageLinker(new RecordBufferPagePacker(records, mgr)) ;

        Transform<RecordBufferPage, Pair<Integer, Record>> transform = new Transform<RecordBufferPage, Pair<Integer, Record>>()
        {
            @Override
            public Pair<Integer, Record> convert(RecordBufferPage rbp)
            {
                mgr.put(rbp) ;
                Record r = rbp.getRecordBuffer().getHigh() ;
                r = bpt.getRecordFactory().createKeyOnly(r) ;
                return new Pair<>(rbp.getId(), r) ;
            }
        } ;
        // Write and convert to split pairs.
        Iterator<Pair<Integer, Record>> iter2 = Iter.map(iter, transform) ;

        
        if ( debug )
        {
            if ( rebalance ) System.out.println("Before rebalance (data)") ;
            iter2 = summarizeDataBlocks(iter2, bpt.getRecordsMgr().getRecordBufferPageMgr()) ;
            //iter2 = printDataBlocks(iter2, bpt.getRecordsMgr().getRecordBufferPageMgr()) ;
        }
        
        if ( rebalance )
            iter2 = new RebalenceDataEnd(iter2, bpt) ;

        // Testing - materialize - debug wil have done this
        if ( materialize && ! debug )
            iter2 = Iter.toList(iter2).iterator() ;

        if ( debug && rebalance )
        {
            System.out.println("After rebalance (data)") ;
            iter2 = summarizeDataBlocks(iter2, bpt.getRecordsMgr().getRecordBufferPageMgr()) ;
            //iter2 = printDataBlocks(iter2, bpt.getRecordsMgr().getRecordBufferPageMgr()) ;
        }
        return iter2 ;
    }
    
    private static class RebalenceDataEnd extends RebalenceBase
    {
        private RecordBufferPageMgr mgr ;
        private BPlusTree bpt ;

        public RebalenceDataEnd(Iterator<Pair<Integer, Record>> iter, BPlusTree bpt)
        {
            super(iter) ;
            this.bpt = bpt ;
        }
        
        @Override
        protected Record rebalance(int id1, Record r1, int id2, Record r2)
        {
            RecordBufferPageMgr mgr = bpt.getRecordsMgr().getRecordBufferPageMgr() ;
            RecordBufferPage page1 = mgr.getWrite(id1) ;
            RecordBufferPage page2 = mgr.getWrite(id2) ;
            
            // Wrong calculatation.
            for ( int i = page2.getCount() ; i <  page1.getMaxSize()/2 ; i++ )
            {
                //shiftOneup(node1, node2) ;
                Record r = page1.getRecordBuffer().getHigh() ;
                page1.getRecordBuffer().removeTop() ;

                page2.getRecordBuffer().add(0, r) ;
            }

            mgr.put(page1) ;
            mgr.put(page2) ;
            
            Record splitPoint = page1.getRecordBuffer().getHigh() ;
            splitPoint = bpt.getRecordFactory().createKeyOnly(splitPoint) ;
            //Record splitPoint = node1.maxRecord() ;
            return splitPoint ;
        }
    }
    
    // ---------------------------------------------------------------------------------------------
    
    // **** tree block phase

    /* Status: need to address the parent issue - how to set the parent as we work up the BPT.
     *   One way is to work in memory and assume that there is enough room
     *   for the B+Tree nodes (not the record nodes).
     *   
     *   Another way is to rely on the caching and two-pass each new layer to fix up parents.
     *   
     *   3rd way - parents aren't actually used in B+Trees.
     *     Set the BPT code to ignore parent of -2.
     *     Always set the new nodes to -2 for a rebuild. 
     */

    // Idea a layer processor for BPT nodes has a sink that is the parent.
    // Need to rebal the last two blocks of a level.
    // Same for data blocks.
    
    
    // ---- Block stream to BTreeNodeStream.
    
    private static Iterator<Pair<Integer, Record>> genTreeLevel(Iterator<Pair<Integer, Record>> iter,
                                                               BPlusTree bpt,
                                                               boolean leafLayer)
    {
        if ( debug )
        {
            divider() ;
            System.out.println("---- Index level") ;
        }
        
        Iterator<Pair<Integer, Record>> iter2 = new BPTreeNodeBuilder(iter, bpt.getNodeManager(), leafLayer, bpt.getRecordFactory()) ;
        
        if ( debug )
        {
            if ( rebalance ) System.out.println("Before rebalance (index)") ;
            //iter2 = summarizeIndexBlocks(iter2, bpt.getNodeManager()) ;
            iter2 = printIndexBlocks(iter2, bpt.getNodeManager()) ;
        }
        
        if ( rebalance )
            iter2 = new RebalenceIndexEnd(iter2, bpt, leafLayer) ;
        
        if ( materialize && !debug )
            iter2 = Iter.toList(iter2).iterator() ;
        
        if ( debug && rebalance )
        {
            System.out.println("After rebalance (index)") ;
            //iter2 = summarizeIndexBlocks(iter2, bpt.getNodeManager()) ;
            iter2 = printIndexBlocks(iter2, bpt.getNodeManager()) ;
        }
        return iter2 ;
    }
    
    private abstract static class RebalenceBase extends IteratorWithBuffer<Pair<Integer, Record>>
    {
        protected RebalenceBase(Iterator<Pair<Integer, Record>> iter)
        {
            super(iter, 2) ;
        }
        
        @Override
        protected final void endReachedInner()
        {
            Pair<Integer, Record> pair1 = peek(0) ;
            Pair<Integer, Record> pair2 = peek(1) ;
            if ( pair1 == null || pair2 == null )
                // Insufficient blocks to repack.
                return ;
            
            if ( debug ) System.out.printf("Rebalance: %s %s\n", pair1, pair2) ; 
            Record newSplitPoint = rebalance(pair1.car(), pair1.cdr(), pair2.car(), pair2.cdr()) ; 
            // Needed??
            if ( newSplitPoint != null )
            {
                if ( debug ) System.out.println("Reset split point: "+pair1.cdr()+" => "+newSplitPoint) ;
                pair1 = new Pair<>(pair1.car(), newSplitPoint) ;
                if ( debug ) System.out.printf("   %s %s\n", pair1, pair2) ;
                set(0, pair1) ;
            }
        }
        
        protected abstract Record rebalance(int id1, Record r1, int id2, Record r2) ;
    }
    
    private static class RebalenceIndexEnd extends RebalenceBase
    {
        private BPlusTree bpt ;

        public RebalenceIndexEnd(Iterator<Pair<Integer, Record>> iter, BPlusTree bpt, boolean leafLayer)
        {
            super(iter) ;
            this.bpt = bpt ;
        }
        
        @Override
        protected Record rebalance(int id1, Record r1, int id2, Record r2)
        {
            BPTreeNodeMgr mgr = bpt.getNodeManager() ; 
            BPTreeNode node1 = mgr.getWrite(id1, BPlusTreeParams.NoParent) ;
            BPTreeNode node2 = mgr.getWrite(id2, BPlusTreeParams.NoParent) ;
            
            // rebalence
            // ** Need rebalance of data leaf layer. 
            int x = node2.getCount() ;
            if ( node2.getCount() >= bpt.getParams().getMinRec() )
                return null ;

            Record splitPoint = r1 ;
            
            // Shift up all in one go and use .set.
            // Convert to block move ; should be code in BPTreeNode to do this (insert).
            for ( int i = node2.getCount() ; i <  bpt.getParams().getMinRec() ; i++ )
            {

                Record r = splitPoint ;

                //shiftOneup(node1, node2) ;
                int ptr = node1.getPtrBuffer().getHigh() ;
                splitPoint = node1.getRecordBuffer().getHigh() ; 

                node1.getPtrBuffer().removeTop() ;
                node1.getRecordBuffer().removeTop() ;
                node1.setCount(node1.getCount()-1) ;

                node2.getPtrBuffer().add(0, ptr) ;
                node2.getRecordBuffer().add(0, r) ;
                node2.setCount(node2.getCount()+1) ;

                // Need high of moved substree.

                if ( debug ) System.out.printf("-- Shift up: %d %s\n", ptr, r) ;
            }
            mgr.put(node1) ;
            mgr.put(node2) ;
            
            return splitPoint ;
        }
    }

    private static void fixupRoot(BPTreeNode root, Pair<Integer, Record> pair, BPlusTree bpt2)
    {
        root.getPtrBuffer().clear() ;
        root.getRecordBuffer().clear() ;
        
        if ( BPlusTreeRewriter.debug )
        {
            divider() ;
            System.out.printf("** Process root: %s\n", pair) ;
        }
        
        //BPTreeNode => BPTree copy.
        BPTreeNode node = bpt2.getNodeManager().getRead(pair.car(), BPlusTreeParams.RootParent) ;
        copyBPTreeNode(node, root, bpt2) ;
        
        bpt2.getNodeManager().release(node) ;
    }
    
    private static void copyBPTreeNode(BPTreeNode nodeSrc, BPTreeNode nodeDst, BPlusTree bpt2)
    {
        PtrBuffer pBuff = nodeSrc.getPtrBuffer() ;
        pBuff.copy(0, nodeDst.getPtrBuffer(), 0, pBuff.getSize()) ;
        RecordBuffer rBuff = nodeSrc.getRecordBuffer() ;
        rBuff.copy(0, nodeDst.getRecordBuffer(), 0, rBuff.getSize()) ;
        nodeDst.setCount(nodeSrc.getCount()) ;
        nodeDst.setIsLeaf(nodeSrc.isLeaf()) ;
        bpt2.getNodeManager().put(nodeDst) ;
    }
}
