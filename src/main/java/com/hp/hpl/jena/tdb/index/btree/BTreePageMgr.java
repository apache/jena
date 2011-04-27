/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.btree;

import static com.hp.hpl.jena.tdb.base.block.BlockType.BTREE_NODE;
import static com.hp.hpl.jena.tdb.base.block.BlockType.RECORD_BLOCK;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.block.BlockConverter;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockType;
import com.hp.hpl.jena.tdb.base.buffer.PtrBuffer;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.sys.Session ;

final class BTreePageMgr implements Session
{
    private BTree btree ;
    private BlockMgr blockMgr ;
    private Block2BTreeNode converter ;

    BTreePageMgr(BTree btree, BlockMgr blockMgr)
    {
        this.btree = btree ;
        this.blockMgr = blockMgr ;
        this.converter = new Block2BTreeNode() ;
    }
   
    public BlockMgr getBlockMgr() { return blockMgr ; } 
    
//    /** Allocate an uninitialized slot.  Fill with a .put later */ 
//    public int allocateId()           { return blockMgr.allocateId() ; }
    
    /** Allocate root node space. */ 
    public BTreeNode createRoot()
    { 
        return create(BTreeParams.RootParent, true) ;
    }
    
    /** Allocate space. */ 
    public BTreeNode create(int parent, boolean makeLeaf)
    { 
        int id = blockMgr.allocateId() ;
        ByteBuffer bb = blockMgr.allocateBuffer(id) ;

        BlockType bType = (makeLeaf ? RECORD_BLOCK : BTREE_NODE ) ;
        BTreeNode n = converter.createFromByteBuffer(bb, bType) ;
        n.id = id ;
        n.parent = parent ;
        return n ;
    }

    /** Fetch a block for the root. s*/
    public BTreeNode getRoot(int id)
    {
        return get(id, BTreeParams.RootParent) ;
    }
    
    /** Fetch a block */
    public BTreeNode get(int id, int parent)
    {
        ByteBuffer bb = blockMgr.get(id) ;
        BTreeNode n = converter.fromByteBuffer(bb) ;
        n.id = id ;
        n.parent = parent ;
        return n ;
    }

    public void put(BTreeNode node)
    {
        // ByteBuffer bb = node.getByteBuffer() ;
        ByteBuffer bb = converter.toBlock(node) ;
        blockMgr.put(node.getId(), bb) ;
    }

    public void release(int id)     { blockMgr.freeBlock(id) ; }
    
    public boolean valid(int id)    { return blockMgr.valid(id) ; }
    
    public void dump()
    { 
        for ( int idx = 0 ; valid(idx) ; idx++ )
        {
            BTreeNode n = get(idx, BTreeParams.NoParent) ;
            System.out.println(n) ;
        }
    }
    
    /** Signal the start of an update operation */
    @Override
    public void startUpdate()       { blockMgr.startUpdate() ; }
    
    /** Signal the completion of an update operation */
    @Override
    public void finishUpdate()      { blockMgr.finishUpdate() ; }

    /** Signal the start of an update operation */
    @Override
    public void startRead()         { blockMgr.startRead() ; }
    
    /** Signal the completeion of an update operation */
    @Override
    public void finishRead()        { blockMgr.finishRead() ; }
    
    // ---- On-disk support
    
    // Using a BlockConverter interally.
    
    private class Block2BTreeNode implements BlockConverter.Converter<BTreeNode>
    {
        @Override
        public BTreeNode createFromByteBuffer(ByteBuffer bb, BlockType bType)
        { 
            return overlay(btree, bb, bType==RECORD_BLOCK, 0) ;
        }

        @Override
        public BTreeNode fromByteBuffer(ByteBuffer byteBuffer)
        {
            synchronized (byteBuffer)
            {
                // Must call from a context that is single threaded for reading.
                int x = byteBuffer.getInt(0) ;
                BlockType type = getType(x) ;

                if ( type != BlockType.BTREE_NODE && type != BlockType.RECORD_BLOCK )
                    throw new BTreeException("Wrong block type: "+type) ; 
                int count = decCount(x) ;
                return overlay(btree, byteBuffer, (type==BlockType.RECORD_BLOCK), count) ;
            }
        }

        @Override
        public ByteBuffer toBlock(BTreeNode node)
        {
            // It's manipulated in-place so no conversion needed, 
            // Just the count needs to be fixed up. 
            ByteBuffer bb = node.getByteBuffer() ;
            BlockType bType = (node.isLeaf ? RECORD_BLOCK : BTREE_NODE ) ;
            int c = encCount(bType, node.count) ;
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
    
    private static final int encCount(BlockType type, int i)
    {
        return (type.id()<<24) | (i&0x00FFFFFF) ;
    }
    
    private static final int decCount(int i)
    { 
        return i & 0x00FFFFFF ;
    }
    
    /** byte[] layout.
     * 
     * New:
     *  0: Block type
     *  1-3: Count 
     *      For an internal node, it is the number of pointers
     *      For a leaf node, it is the number of records.
     *  Leaves:
     *     4- :  Records (count of them)
     *  Internal nodes:
     *    4-X:        Records: btree.MaxRec*record length
     *    X- :        Pointers: btree*MaxPtr*ptr length 

     * OLD    
     *    0-3:        Header: Number in use.
     *      Negative (as -(i+1)implies a leaf.
     *** Change: 8 bytes (=>64bit aligned?).  Include a "block type"
     *** Or pack 8/24.
     * Leaf:
     *    4-       Records: btree.NumRec* 
     * Non-leaf:
     *    4-X:        Records: btree.MaxRec*record length
     *    X- :        Pointers: btree*MaxPtr*ptr length 
     */
    // Produce a BTreeNode from a ByteBuffer
    private static BTreeNode overlay(BTree bTree, ByteBuffer byteBuffer, boolean asLeaf, int count)
    {
//        if ( byteBuffer.order() != Const.NetworkOrder )
//            throw new BTreeException("ByteBuffer in wrong order") ;

        // Fix up the id later.
        BTreeNode n = new BTreeNode(bTree, -1, byteBuffer) ;
        // The count is zero at the root only.
        // When the root is zero, it's a leaf.
        formatBTreeNode(n, bTree, byteBuffer, asLeaf, count) ; 
        return n ;
    }
        
    static BTreeNode formatBTreeNode(BTreeNode n, BTree bTree, ByteBuffer byteBuffer, boolean leaf, int count)
    {
        BTreeParams params = bTree.getParams() ;

        int ptrBuffLen ;
        int recBuffLen ;
        
        
        if ( leaf )
        {
            // XXX Leaf/Non-leaf
            // Will be zero.
            ptrBuffLen = params.MaxPtr * BTreeParams.getPtrLength() ;
            recBuffLen = params.MaxRecLeaf * params.getRecordLength() ;
        }
        else
        {
            ptrBuffLen = params.MaxPtr * BTreeParams.getPtrLength() ;
            recBuffLen = params.MaxRecNonLeaf * params.getRecordLength() ;
        }

//      if ( (ptrBuffLen+recBuffLen+BTreeParams.BlockHeaderSize) > n.byteBuffer.capacity() )
//      {
//      int x = (ptrBuffLen+recBuffLen+4) ;
//      throw new BTreeException(format("Short byte block: expected=%d, actual=%d", x, n.byteBuffer.capacity())) ;
//      }

        n.id = -1 ;
        n.parent = -2 ;
        n.count = count ;
        n.isLeaf = leaf ; 

        int header = BTreeParams.BlockHeaderSize ;
        int rStart = header ;
        int pStart =  header+recBuffLen ;

        // Find the number of pointers.
        int numPtrs = -1 ;
            
        if ( n.count < 0 )
        {
            numPtrs = 0 ;
            n.count = decCount(n.count) ; 
        }
        else if ( n.count == 0 )    // The root.
        {
            numPtrs = 0 ;
        }
        else    // Count > 0
            numPtrs = n.count+1 ;

        n.byteBuffer.position(rStart) ;
        n.byteBuffer.limit(rStart+recBuffLen) ;
        ByteBuffer bbr = n.byteBuffer.slice() ;
        //bbr.limit(recBuffLen) ;
        n.records = new RecordBuffer(bbr, n.bTreeParams.recordFactory, n.count) ;

//        if ( n.isLeaf )
//        {
//            n.ptrs = null ;
//        }
//        else
        {
            n.byteBuffer.position(pStart) ;
            n.byteBuffer.limit(pStart+ptrBuffLen) ;
            
            ByteBuffer bbi = n.byteBuffer.slice() ;
            //bbi.limit(ptrBuffLen) ;
            n.ptrs = new PtrBuffer(bbi, numPtrs) ;
        }
        
        n.byteBuffer.rewind() ;
        return n ;
    }
    
    static final void formatForRoot(BTreeNode n, boolean asLeaf)
    {
        BTreePageMgr.formatBTreeNode(n, n.bTree, n.getByteBuffer(), asLeaf, 0) ;
        // Tweak for the root-specials.  The node is not consistent yet.
        n.id = 0 ;
        n.parent = BTreeParams.RootParent ;
    }
    
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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