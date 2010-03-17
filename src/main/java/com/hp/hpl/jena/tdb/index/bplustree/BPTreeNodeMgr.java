/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import static com.hp.hpl.jena.tdb.base.block.BlockType.BPTREE_BRANCH ;
import static com.hp.hpl.jena.tdb.base.block.BlockType.BPTREE_LEAF ;
import static com.hp.hpl.jena.tdb.base.block.BlockType.RECORD_BLOCK ;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockType ;
import com.hp.hpl.jena.tdb.base.buffer.PtrBuffer ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;

/** BPlusTreePageMgr = BPlusTreeNode manager */
public final class BPTreeNodeMgr extends BPTreePageMgr
{
    // Only "public" for external very low level tools in development to access this class.
    // Assume package access.

    private BlockMgr blockMgr ;
    private Block2BPTreeNode converter ;

    public BPTreeNodeMgr(BPlusTree bpTree, BlockMgr blockMgr)
    {
        super(bpTree) ;
        this.blockMgr = blockMgr ;
        this.converter = new Block2BPTreeNode() ;
    }
   
    public BlockMgr getBlockMgr() { return blockMgr ; } 
    
    /** Allocate an uninitialized slot.  Fill with a .put later */ 
    public int allocateId()           { return blockMgr.allocateId() ; }
    
    /** Allocate root node space. The root is a node with a Records block.*/ 
    public BPTreeNode createRoot()
    { 
        // Create an empty records block.
        int recId = bpTree.getRecordsMgr().allocateId() ;
        BPTreePage page = bpTree.getRecordsMgr().create(recId) ;
        page.put();
        
        BPTreeNode n = createNode(BPlusTreeParams.RootParent) ;
        // n.ptrs is currently invalid.  count was 0 so thinks it has a pointer.
        // Force to right layout.
        n.ptrs.setSize(0) ;         // No pointers
        n.ptrs.add(page.getId()) ;  // Add the page below
        
        //n.ptrs.set(0, page.getId()) ; // This is the same as the size is one.
        
        n.isLeaf = true ;
        n.setCount(0) ;     // Count is count of records. 
        n.put();
        return n ;
    }
    
//    /** Allocate space for a leaf node. */
//    public BPTreeLeaf createLeaf(int parent)
//    {
//        int id = btree.getRecordPageMgr().allocateId() ;
//        RecordBufferPage page = btree.getRecordPageMgr().create(id) ;
//        BPTreeLeaf leaf = new BPTreeLeaf(btree, page) ;
//        return leaf ;
//    }
    
    /** Allocate space for a fresh node. */ 
    public BPTreeNode createNode(int parent)
    { 
        int id = blockMgr.allocateId() ;
        ByteBuffer bb = blockMgr.allocateBuffer(id) ;
        //bb.clear();
        BPTreeNode n = converter.createFromByteBuffer(bb, BPTREE_BRANCH) ;
        n.setId(id) ;
        n.isLeaf = false ;
        n.parent = parent ;
        return n ;
    }

    /** Fetch a block for the root. s*/
    public BPTreeNode getRoot(int id)
    {
        return get(id, BPlusTreeParams.RootParent) ;
    }
    
    /** Fetch a block */
    public BPTreeNode get(int id, int parent)
    {
        ByteBuffer bb = blockMgr.get(id) ;
        BPTreeNode n = converter.fromByteBuffer(bb) ;
        n.setId(id) ;
        n.parent = parent ;
        return n ;
    }
    

    public void put(BPTreeNode node)
    {
        ByteBuffer bb = converter.toByteBuffer(node) ;
        blockMgr.put(node.getId(), bb) ;
    }

    public void release(int id)     { blockMgr.freeBlock(id) ; }
    
    public boolean valid(int id)    { return blockMgr.valid(id) ; }
    
    public void dump()
    { 
        for ( int idx = 0 ; valid(idx) ; idx++ )
        {
            BPTreeNode n = get(idx, BPlusTreeParams.NoParent) ;
            System.out.println(n) ;
        }
    }
    
    /** Signal the start of an update operation */
    public void startRead()         { blockMgr.startRead() ; }

    /** Signal the completeion of an update operation */
    public void finishRead()        { blockMgr.finishRead() ; }

    /** Signal the start of an update operation */
    public void startUpdate()       { blockMgr.startUpdate() ; }
    
    /** Signal the completion of an update operation */
    public void finishUpdate()      { blockMgr.finishUpdate() ; }

    private class Block2BPTreeNode implements BlockConverter.Converter<BPTreeNode>
    {
        //@Override
        public BPTreeNode createFromByteBuffer(ByteBuffer bb, BlockType bType)
        { 
            return overlay(bpTree, bb, bType==RECORD_BLOCK, 0) ;
        }

        //@Override
        public BPTreeNode fromByteBuffer(ByteBuffer byteBuffer)
        {
            synchronized (byteBuffer)
            {
                int x = byteBuffer.getInt(0) ;
                BlockType type = getType(x) ;
                
                if ( type != BPTREE_BRANCH && type != BPTREE_LEAF )
                    throw new BPTreeException("Wrong block type: "+type) ; 
                int count = decodeCount(x) ;
                return overlay(bpTree, byteBuffer, (type==BPTREE_LEAF), count) ;
            }
        }

        //@Override
        public ByteBuffer toByteBuffer(BPTreeNode node)
        {
            // It's manipulated in-place so no conversion needed, 
            // Just the count needs to be fixed up. 
            ByteBuffer bb = node.getBackingByteBuffer() ;
            BlockType bType = (node.isLeaf ? BPTREE_LEAF : BPTREE_BRANCH ) ;
            int c = encodeCount(bType, node.getCount()) ;
            bb.putInt(0, c) ;
            return bb ;
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
    private static BPTreeNode overlay(BPlusTree bpTree, ByteBuffer byteBuffer, boolean asLeaf, int count)
    {
//        if ( byteBuffer.order() != Const.NetworkOrder )
//            throw new BTreeException("ByteBuffer in wrong order") ;

        // Fix up the id later.
        BPTreeNode n = new BPTreeNode(bpTree, -1, byteBuffer) ;
        // The count is zero at the root only.
        // When the root is zero, it's a leaf.
        formatBPTreeNode(n, bpTree, byteBuffer, asLeaf, count) ; 
        return n ;
    }
        
    static BPTreeNode formatBPTreeNode(BPTreeNode n, BPlusTree bTree, ByteBuffer byteBuffer, boolean leaf, int count)
    {
        BPlusTreeParams params = bTree.getParams() ;

        int ptrBuffLen = params.MaxPtr * params.getPtrLength() ;
        int recBuffLen = params.MaxRec * params.getRecordLength() ;

        n.setId(-1) ;
        n.parent = -2 ;
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

        // -- Records area
        n.getBackingByteBuffer().position(rStart) ;
        n.getBackingByteBuffer().limit(rStart+recBuffLen) ;
        ByteBuffer bbr = n.getBackingByteBuffer().slice() ;
        //bbr.limit(recBuffLen) ;
        n.records = new RecordBuffer(bbr, n.params.keyFactory, n.getCount()) ;

        // -- Pointers area
        n.getBackingByteBuffer().position(pStart) ;
        n.getBackingByteBuffer().limit(pStart+ptrBuffLen) ;
        
        ByteBuffer bbi = n.getBackingByteBuffer().slice() ;
        //bbi.limit(ptrBuffLen) ;
        n.ptrs = new PtrBuffer(bbi, numPtrs) ;

        // Reset
        n.getBackingByteBuffer().rewind() ;
        return n ;
    }
    
    static final void formatForRoot(BPTreeNode n, boolean asLeaf)
    {
        BPTreeNodeMgr.formatBPTreeNode(n, n.bpTree, n.getBackingByteBuffer(), asLeaf, 0) ;
        // Tweak for the root-specials.  The node is not consistent yet.
        // Has one dangling pointer.
        n.setId(0) ;
        n.parent = BPlusTreeParams.RootParent ;
    }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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