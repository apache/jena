package tx;

public class DevTx
{
    // See all : // [TxTDB:PATCH-UP]
    // Memory mapped files.
    
    /*
     * 1 - everything a write block.
     *     This should then work for single app.
     * 2 - Convert indexes to differentiate and track read and write pages. 
     */
    
    /* add freeblock wrapper ? */
    
    
    
    /*
     * Layers:
     *   DatasetGraph
     *   Indexes
     *   Pages
     *   Blocks
     *   Storage = FileAccess 
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
