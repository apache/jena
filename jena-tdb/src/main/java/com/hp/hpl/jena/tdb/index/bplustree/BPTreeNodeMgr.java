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

import static com.hp.hpl.jena.tdb.base.block.BlockType.BPTREE_BRANCH ;
import static com.hp.hpl.jena.tdb.base.block.BlockType.BPTREE_LEAF ;
import static com.hp.hpl.jena.tdb.base.block.BlockType.RECORD_BLOCK ;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockType ;
import com.hp.hpl.jena.tdb.base.buffer.PtrBuffer ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;

/** BPlusTreePageMgr = BPlusTreeNode manager */
public final class BPTreeNodeMgr extends BPTreePageMgr<BPTreeNode>
{
    // Only "public" for external very low level tools in development to access this class.
    // Assume package access.

    public BPTreeNodeMgr(BPlusTree bpTree, BlockMgr blockMgr)
    {
        super(bpTree, new Block2BPTreeNode(bpTree), blockMgr) ;
    }
   
    /** Allocate root node space. The root is a node with a Records block.*/ 
    public int createEmptyBPT()
    { 
        // Must be inside already : startUpdate() ;
        // Create an empty records block.
        
        BPTreePage recordsPage = bpTree.getRecordsMgr().create() ;
        if ( recordsPage.getId() != BPlusTreeParams.RootId )
            // [TxTDB:PATCH-UP]
            throw new TDBException("Root blocks must be at position zero (got "+recordsPage.getId()+")") ;
        // Empty data block.
        // [TxTDB:PATCH-UP]
        recordsPage.write();
        recordsPage.release() ;
        
        BPTreeNode n = createNode(BPlusTreeParams.RootParent) ;
        // n.ptrs is currently invalid.  count was 0 so thinks it has a pointer.
        // Force to right layout.
        n.ptrs.setSize(0) ;         // No pointers
        n.ptrs.add(recordsPage.getId()) ;  // Add the page below
        
        //n.ptrs.set(0, page.getId()) ; // This is the same as the size is one.
        
        n.isLeaf = true ;
        n.setCount(0) ;     // Count is count of records.
        int rootId = n.getId()  ;
        n.write();
        n.release() ;
        // Must be inside already : finishUpdate() ;
        return rootId ;
    }
    
    /** Allocate space for a fresh node. */ 
    public BPTreeNode createNode(int parent)
    { 
        BPTreeNode n = create(BPTREE_BRANCH) ;
        n.isLeaf = false ;
        n.parent = parent ;
        return n ;
    }

    /** Fetch a block for the root. */
    public BPTreeNode getRoot(int id)
    {
        return getRead(id, BPlusTreeParams.RootParent) ;
    }
    
    // Maybe we should not inherit but wrap.
    @Override
    public BPTreeNode getWrite(int id)
    { throw new UnsupportedOperationException("call getWrite(int, int)") ; }
    @Override
    public BPTreeNode getRead(int id)
    { throw new UnsupportedOperationException("call getRead(int, int)") ; }
    
    /** Fetch a block - fill in the parent id, which is not in the on-disk bytes */
    public BPTreeNode getRead(int id, int parent)
    {
        // [TxTDB:PATCH-UP]
        BPTreeNode n = super.getRead(id) ;
        n.parent = parent ;
        return n ;
    }
    
    /** Fetch a block - fill in the parent id, which is not in the on-disk bytes */
    public BPTreeNode getWrite(int id, int parent)
    {
        // [TxTDB:PATCH-UP]
        BPTreeNode n = super.getWrite(id) ;
        n.parent = parent ;
        return n ;
    }

    private static class Block2BPTreeNode implements BlockConverter<BPTreeNode>
    {
        private final BPlusTree bpTree ;

        Block2BPTreeNode(BPlusTree bpTree) { this.bpTree = bpTree ; }
        
        @Override
        public BPTreeNode createFromBlock(Block block, BlockType bType)
        { 
            return overlay(bpTree, block, bType==RECORD_BLOCK, 0) ;
        }

        @Override
        public BPTreeNode fromBlock(Block block)
        {
            // [TxTDB:PATCH-UP]
            // synchronized - needed for multiple reader? 
            synchronized (block)
            {
                int x = block.getByteBuffer().getInt(0) ;
                BlockType type = getType(x) ;
                
                if ( type != BPTREE_BRANCH && type != BPTREE_LEAF )
                    throw new BPTreeException("Wrong block type: "+type) ; 
                int count = decodeCount(x) ;
                return overlay(bpTree, block, (type==BPTREE_LEAF), count) ;
            }
        }

        @Override
        public Block toBlock(BPTreeNode node)
        {
            // It's manipulated in-place so no conversion needed, 
            // Just the count needs to be fixed up. 
//            ByteBuffer bb = node.getBackingByteBuffer() ;
//            BlockType bType = (node.isLeaf ? BPTREE_LEAF : BPTREE_BRANCH ) ;

            Block block = node.getBackingBlock() ;
            BlockType bType = (node.isLeaf ? BPTREE_LEAF : BPTREE_BRANCH ) ;

            int c = encodeCount(bType, node.getCount()) ;
            block.getByteBuffer().putInt(0, c) ;
            return block ;
        }
    }
    
//    // Leaves have a count of -(count+1)
//    // (same as the binary search encoding of "not found")
//    private static final int encCount(int i)     { return -(i+1) ; } 
//    private static final int decCount(int i)     { return -i-1 ; }

    // ----
    private static final BlockType getType(int x)
    {
        return BlockType.extract( x>>>24 ) ;
    }
    
    private static final int encodeCount(BlockType type, int i)
    {
        return (type.id()<<24) | (i&0x00FFFFFF) ;
    }
    
    private static final int decodeCount(int i)
    { 
        return i & 0x00FFFFFF ;
    }
    
    /** byte[] layout.
     * 
     * New:
     *  0: Block type
     *  1-3: Count 
     *  Internal nodes:
     *    4-X:        Records: b+tree.MaxRec*record length
     *    X- :        Pointers: b+tree.MaxPtr*ptr length 
     */
    private static BPTreeNode overlay(BPlusTree bpTree, Block block, boolean asLeaf, int count)
    {
//        if ( byteBuffer.order() != Const.NetworkOrder )
//            throw new BTreeException("ByteBuffer in wrong order") ;

        // Fix up the id later.
        BPTreeNode n = new BPTreeNode(bpTree, block) ;
        // The count is zero at the root only.
        // When the root is zero, it's a leaf.
        formatBPTreeNode(n, bpTree, block, asLeaf, -2, count) ; 
        return n ;
    }
        
    static void formatBPTreeNode(BPTreeNode n, BPlusTree bpTree, Block block, boolean leaf, int parent, int count)
    {
        BPlusTreeParams params = bpTree.getParams() ;

        int ptrBuffLen = params.MaxPtr * params.getPtrLength() ;
        // Only store the key part of records in a B+Tree block
        // OLD - Node table has real value part - what's going on? 
        
        // [Issue:FREC]
        // Allocate space for record, key and value, despite slight over allocation.
        int recBuffLen = params.MaxRec * params.getRecordLength() ;
        
        // [Issue:FREC] Should be: key space only.
        // int recBuffLen = params.MaxRec * params.getKeyLength() ;

        n.parent = parent ;
        n.setCount(count) ;
        n.isLeaf = leaf ; 

        int header = BPlusTreeParams.BlockHeaderSize ;
        int rStart = header ;
        int pStart = header+recBuffLen ;

        // Find the number of pointers.
        int numPtrs = -1 ;
        
        // The root can have count zero - which means one pointer always.
        // Junk when creating a new new node.
        if ( n.getCount() < 0 )
        {
            numPtrs = 0 ;
            n.setCount(decodeCount(n.getCount())) ; 
        }
        else
            numPtrs = n.getCount()+1 ;

        ByteBuffer byteBuffer = block.getByteBuffer() ; 
        
        // -- Records area
        byteBuffer.position(rStart) ;
        byteBuffer.limit(rStart+recBuffLen) ;
        ByteBuffer bbr = byteBuffer.slice() ;
        //bbr.limit(recBuffLen) ;
        n.setRecordBuffer(new RecordBuffer(bbr, n.getParams().keyFactory, n.getCount())) ;

        // -- Pointers area
        byteBuffer.position(pStart) ;
        byteBuffer.limit(pStart+ptrBuffLen) ;
        
        ByteBuffer bbi = byteBuffer.slice() ;
        //bbi.limit(ptrBuffLen) ;
        n.ptrs = new PtrBuffer(bbi, numPtrs) ;

        // Reset
        byteBuffer.rewind() ;
    }
    
    static final void formatForRoot(BPTreeNode n, boolean asLeaf)
    {
        BPTreeNodeMgr.formatBPTreeNode(n, n.getBPlusTree(), n.getBackingBlock(), asLeaf, BPlusTreeParams.RootParent, 0) ;
        // Tweak for the root-specials.  The node is not consistent yet.
        // Has one dangling pointer.
    }
    
}
