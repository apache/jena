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

package org.apache.jena.dboe.trans.bplustree;

import static org.apache.jena.dboe.base.block.BlockType.BPTREE_BRANCH;
import static org.apache.jena.dboe.base.block.BlockType.BPTREE_LEAF;
import static org.apache.jena.dboe.base.block.BlockType.RECORD_BLOCK;

import java.nio.ByteBuffer ;

import org.apache.jena.dboe.base.block.Block;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.block.BlockType;
import org.apache.jena.dboe.base.buffer.PtrBuffer;
import org.apache.jena.dboe.base.buffer.RecordBuffer;
import org.apache.jena.dboe.base.page.BlockConverter;
import org.apache.jena.dboe.base.page.PageBlockMgr;

/** BPlusTreePageMgr = BPlusTreeNode manager */
public final class BPTreeNodeMgr extends PageBlockMgr<BPTreeNode>
{
    // Only "public" for external very low level tools in development to access this class.
    // Assume package access.

    public BPTreeNodeMgr(BPlusTree bpTree, BlockMgr blockMgr) {
        super(new Block2BPTreeNode(bpTree), blockMgr) ;
    }
   
    /** Allocate space for a fresh node. */
    public BPTreeNode createNode(int parent) {
        BPTreeNode n = create(BPTREE_BRANCH) ;
        n.setIsLeaf(false) ;
        n.setParent(parent) ;
        return n ;
    }

    @Override
    public BPTreeNode getWrite(int id) {
        return super.getWrite(id, BPlusTreeParams.UnsetParent) ;
    }

    @Override
    public BPTreeNode getRead(int id) {
        return super.getRead(id, BPlusTreeParams.UnsetParent) ;
    }

    /** Fetch a block - fill in the parent id, which is not in the on-disk bytes */
    @Override
    public BPTreeNode getRead(int id, int parent) {
        BPTreeNode n = super.getRead$(id) ;
        n.setParent(parent);
        return n ;
    }

    /** Fetch a block - fill in the parent id, which is not in the on-disk bytes */
    @Override
    public BPTreeNode getWrite(int id, int parent) {
        BPTreeNode n = super.getWrite$(id) ;
        n.setParent(parent);
        return n ;
    }

    boolean isWritable(int id) {
        //System.err.println("BPTreeNodeMgr.isWritable") ;
        return false ;
//      return bpTree.state.modifiableNodeBlock(id) ;
    }

    private static class Block2BPTreeNode implements BlockConverter<BPTreeNode>
    {
        private final BPlusTree bpTree ;

        Block2BPTreeNode(BPlusTree bpTree) { this.bpTree = bpTree ; }
        
        @Override
        public BPTreeNode createFromBlock(Block block, BlockType bType) {
            return overlay(bpTree, block, bType == RECORD_BLOCK, 0) ;
        }

        @Override
        public BPTreeNode fromBlock(Block block) {
            // synchronized - needed for multiple reader?
            synchronized (block) {
                int x = block.getByteBuffer().getInt(0) ;
                BlockType type = getType(x) ;

                if ( type != BPTREE_BRANCH && type != BPTREE_LEAF )
                    throw new BPTreeException("Wrong block type: " + type) ;
                int count = decodeCount(x) ;
                return overlay(bpTree, block, (type == BPTREE_LEAF), count) ;
            }
        }

        @Override
        public Block toBlock(BPTreeNode node) {
            // It's manipulated in-place so no conversion needed, 
            // Just the count needs to be fixed up. 
//            ByteBuffer bb = node.getBackingByteBuffer() ;
//            BlockType bType = (node.isLeaf ? BPTREE_LEAF : BPTREE_BRANCH ) ;

            Block block = node.getBackingBlock() ;
            BlockType bType = (node.isLeaf() ? BPTREE_LEAF : BPTREE_BRANCH ) ;

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
    private static final BlockType getType(int x) {
        return BlockType.extract(x >>> 24) ;
    }

    private static final int encodeCount(BlockType type, int i) {
        return (type.id() << 24) | (i & 0x00FFFFFF) ;
    }

    private static final int decodeCount(int i) {
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
    private static BPTreeNode overlay(BPlusTree bpTree, Block block, boolean asLeaf, int count) {
        // if ( byteBuffer.order() != Const.NetworkOrder )
        // throw new BTreeException("ByteBuffer in wrong order") ;

        // Fix up the id later.
        BPTreeNode n = new BPTreeNode(bpTree) ;
        // The count is zero at the root only.
        // When the root is zero, it's a leaf.
        formatBPTreeNode(n, bpTree, block, asLeaf, BPlusTreeParams.NoParent, count) ;
        return n ;
    }

    static void formatBPTreeNode(BPTreeNode n, BPlusTree bpTree, Block block, boolean leaf, int parent, int count) {
        BPlusTreeParams params = bpTree.getParams() ;

        int ptrBuffLen = params.MaxPtr * params.getPtrLength() ;
        // Only store the key part of records in a B+Tree block
        // OLD - Node table has real value part - what's going on?

        // [Issue:FREC]
        // Allocate space for record, key and value, despite slight over
        // allocation.
        int recBuffLen = params.MaxRec * params.getRecordLength() ;

        // [Issue:FREC] Should be: key space only.
        // int recBuffLen = params.MaxRec * params.getKeyLength() ;

        n.id = block.getId().intValue() ;
        n.setParent(parent) ;
        n.setCount(count) ;
        n.setIsLeaf(leaf) ;

        int header = BPlusTreeParams.BlockHeaderSize ;
        int rStart = header ;
        int pStart = header + recBuffLen ;

        // Find the number of pointers.
        int numPtrs = -1 ;

        // The root can have count zero - which means one pointer always.
        // Junk when creating a new new node.
        if ( n.getCount() < 0 ) {
            numPtrs = 0 ;
            n.setCount(decodeCount(n.getCount())) ;
        } else
            numPtrs = n.getCount() + 1 ;

        // Block dependent
        
        n.block = block ;
        ByteBuffer byteBuffer = block.getByteBuffer() ;

        // -- Records area
        byteBuffer.position(rStart) ;
        byteBuffer.limit(rStart + recBuffLen) ;
        ByteBuffer bbr = byteBuffer.slice() ;
        // bbr.limit(recBuffLen) ;
        n.setRecordBuffer(new RecordBuffer(bbr, n.params.keyFactory, n.getCount())) ;

        // -- Pointers area
        byteBuffer.position(pStart) ;
        byteBuffer.limit(pStart + ptrBuffLen) ;

        ByteBuffer bbi = byteBuffer.slice() ;
        // bbi.limit(ptrBuffLen) ;
        n.setPtrBuffer(new PtrBuffer(bbi, numPtrs)) ;

        // Reset
        byteBuffer.rewind() ;
    }
    
    static final void formatForRoot(BPTreeNode n, boolean asLeaf) {
        BPTreeNodeMgr.formatBPTreeNode(n, n.bpTree, n.getBackingBlock(), asLeaf, BPlusTreeParams.RootParent, 0) ;
        // Tweak for the root-specials. The node is not consistent yet.
        // Has one dangling pointer.
    }
}
