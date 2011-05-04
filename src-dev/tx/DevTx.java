package tx;

public class DevTx
{
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
    
    // See all : // [TxTDB:PATCH-UP]
    // Memory mapped files.

    // Block/BlockMgr - remove block type entirely?
    
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
     * Setup
     *   Transaction start: grab alloc id.
     */
}
