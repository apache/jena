/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.trans.bplustree;

import static org.seaborne.dboe.trans.bplustree.BPlusTreeParams.CheckingNode ;
import org.seaborne.dboe.DBOpEnvException ;
import org.seaborne.dboe.base.block.BlockMgr ;
import org.seaborne.dboe.base.block.BlockMgrFactory ;
import org.seaborne.dboe.base.block.BlockMgrTracker ;
import org.seaborne.dboe.base.recordbuffer.RecordBufferPage ;
import org.seaborne.dboe.base.recordbuffer.RecordBufferPageMgr ;

/** Make BPlusTrees - this code works in close association with the BPlusTree constructor */
public class BPlusTreeFactory {

    /** Create the in-memory structures to correspond to
     * the supplied block managers for the persistent storage.
     * Initialize the persistent storage to the empty B+Tree if it does not exist.
     * This is the normal way to create a B+Tree.
     */
    public static BPlusTree create(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrLeaves) {
        BPlusTree bpt = attach(params, blkMgrNodes, blkMgrLeaves) ;
        return bpt ;
    }

    /**
     * Create the in-memory structures to correspond to the supplied block
     * managers for the persistent storage. Does not inityalize the B+Tree - it
     * assumes the block managers correspond to an existing B+Tree.
     */
    private static BPlusTree attach(BPlusTreeParams params, BlockMgr blkMgrNodes, BlockMgr blkMgrRecords) {
        // Creating and initializing the BPlusTree object is a two stage process.

        // * Create the Java object so it can be in other structures
        //   but it is not fully initialized yet.
        //     Create datastructures being careful not to use the object
        //   Ensure formatted
        // * Initialize.
        BPlusTree bpt = new BPlusTree(params) ; 
        BPTreeNodeMgr nodeManager = new BPTreeNodeMgr(bpt, blkMgrNodes) ;
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(params.getRecordFactory(), blkMgrRecords) ;
        BPTreeRecordsMgr recordsMgr = new BPTreeRecordsMgr(params.getRecordFactory(), recordPageMgr) ;
        int rootId = createIfAbsent(nodeManager, recordsMgr) ;
        bpt.init(rootId, nodeManager, recordsMgr) ;
        return bpt ;
    }

    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(int order, int keyLength, int valueLength)
    { return makeMem(null, order, keyLength, valueLength) ; }

    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(String name, int order, int keyLength, int valueLength)
    { return makeMem(name, order, -1, keyLength, valueLength) ; }

    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(int order, int minDataRecords, int keyLength, int valueLength)
    { return makeMem(null, order, minDataRecords, keyLength, valueLength) ; }

    /** (Testing mainly) Make an in-memory B+Tree, with copy-in, copy-out block managers */
    public static BPlusTree makeMem(String name, int order, int minDataRecords, int keyLength, int valueLength) {
        if ( name == null )
            name = "Mem" ;
        BPlusTreeParams params = new BPlusTreeParams(order, keyLength, valueLength) ;
    
        int blkSize ;
        if ( minDataRecords > 0 ) {
            int maxDataRecords = 2 * minDataRecords ;
            // int rSize = RecordBufferPage.HEADER+(maxRecords*params.getRecordLength()) ;
            blkSize = RecordBufferPage.calcBlockSize(params.getRecordFactory(), maxDataRecords) ;
        } else
            blkSize = params.getCalcBlockSize() ;
    
        BlockMgr mgr1 = BlockMgrFactory.createMem(name + "(nodes)", params.getCalcBlockSize()) ;
        BlockMgr mgr2 = BlockMgrFactory.createMem(name + "(records)", blkSize) ;
        BPlusTree bpTree = BPlusTreeFactory.create(params, mgr1, mgr2) ;
        return bpTree ;
    }

    /** Debugging */
    public static BPlusTree addTracking(BPlusTree bpTree) {
        BlockMgr mgr1 = bpTree.getNodeManager().getBlockMgr() ;
        BlockMgr mgr2 = bpTree.getRecordsMgr().getBlockMgr() ;
        mgr1 = BlockMgrTracker.track(mgr1) ;
        mgr2 = BlockMgrTracker.track(mgr2) ;
    
        return BPlusTreeFactory.attach(bpTree.getParams(), mgr1, mgr2) ;
    }
    
    /** Create if does not exist */ 
    private static int createIfAbsent(BPTreeNodeMgr nodeManager, BPTreeRecordsMgr recordsMgr) {
        // This fixes the root to being block 0
        if ( nodeManager.getBlockMgr().valid(BPlusTreeParams.RootId) )
            return BPlusTreeParams.RootId ;
        
        // Create/format

        // Fresh BPlusTree root node.
        int rootIdx = createEmptyBPT(nodeManager, recordsMgr) ;
        if ( rootIdx != 0 )
            throw new InternalError() ;

        if ( CheckingNode ) {
            BPTreeNode root = nodeManager.getRead(rootIdx, BPlusTreeParams.RootParent) ;
            root.checkNodeDeep() ;
            nodeManager.release(root) ;
        }

        // Sync created blocks to disk - any caches are now clean.
        nodeManager.getBlockMgr().sync() ;
        recordsMgr.getBlockMgr().sync() ;
        return rootIdx ;
    }

    /** Allocate root node space. The root is a Node with a Records block.*/ 
    private static int createEmptyBPT(BPTreeNodeMgr nodeManager, BPTreeRecordsMgr recordsMgr) { 

        // Create an empty records block.
        BPTreeRecords recordsPage = recordsMgr.create() ;
        if ( recordsPage.getId() != BPlusTreeParams.RootId )
            throw new DBOpEnvException("Root blocks must be at position zero (got "+recordsPage.getId()+")") ;
        // Empty data block.
        recordsMgr.write(recordsPage) ;
        recordsMgr.release(recordsPage) ;

        // Not this - we haven't full initialized and the BPTreeRecords has null BPTreeRecordsMgr
        //recordsPage.write();
        //recordsPage.release() ;

        BPTreeNode n = nodeManager.createNode(BPlusTreeParams.RootParent) ;
        // n.ptrs is currently invalid.  count was 0 so thinks it has a pointer.
        // Force to right layout.
        n.ptrs.setSize(0) ;         // No pointers
        n.ptrs.add(recordsPage.getId()) ;  // Add the page below

        //n.ptrs.set(0, page.getId()) ; // This is the same as the size is one.

        n.isLeaf = true ;
        n.setCount(0) ;     // Count is count of records.
        int rootId = n.getId()  ;
        nodeManager.write(n) ;
        nodeManager.release(n) ;
        // Again, not this.
        //n.write();
        //n.release() ;
        // Must be inside already : finishUpdate() ;
        return rootId ;
    }
}

