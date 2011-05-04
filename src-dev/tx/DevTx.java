package tx;

public class DevTx
{
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
    
    // See all : // [TxTDB:PATCH-UP]
    // Memory mapped files.
    

    // BlockMgr: remove these: 
    // public int blockSize() ;
    
    // public boolean valid(int id) ; -- used for checking iteration.
    // public boolean isClosed() ; 
    
    /* add freeblock wrapper ? */

    /*
     * Layers:
     *   DatasetGraph
     *   Indexes
     *   Pages
     *   Blocks
     *   Storage = FileAccess (a sequence of blocks) 
     */
    
    /* NEXT
     * BPTreeNode - recursive operations to be static functions.
     *   Makes release conordination easier.
     * 2 phase:
     *   Fixup B+Tree/B-Tree by always asking to write blocks.  Marked [TxTDB:PATCH-UP]
      * BlockType arg to Block() makes no sense - may not know in advance.
     *   Separately rewrite to be better.
     *   
     * 
     * Setup
     * BlockMgr: releaseRead/releaseWrite or just release?
     *   Alloc variable size.
     * Transaction start: grab alloc id.
     * 
     *   allocateId - allocateBuffer combined
     *    If we allocate space for a block, we must honour the id->disk location.
     *   abort needs to release 
     *    
     *   
     *   Page: BlockId, BlockType, BlockStatus, ByteBuffer (data only) 
     *   
     *   freeBlock inc contents. freeBlock(id, byteBuffer)
     *   Do we need a Block class?  Page class?
     * BlockMgr.getRead, BlockMgr.getWrite
     */
}
