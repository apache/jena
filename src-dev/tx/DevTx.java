package tx;


public class DevTx
{
    // Tidy up 
    // Is BlockType really necessary?
    // See HACK
    
    // ---- ---- ---- ----
    
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
    // BlockMgrTracker to dump inconsistences on finish*, not just log a warning.
    
    // PageMgr implements UnitMgr<Page> -- allocate(size) with no type. 
    // releasing the root after update and put --> warning
    // "Needs put()'ing"
    
    // B+Tree
    //   Avoid reparsing root blocks.  Maybe release only after change.
    
    // [TxTDB:PATCH-UP]
    // Memory mapped files.

    /*
     * Iterator tracking
     *   End transaction => close all open iterators.
     *   Need transaction - at least something to attach for tracking.
     *     ==> Add "transaction txn" to all RangeIndex operations.  Default null -> no transaction.
     *     OR
     *     ==> Add to B+Tree   .setTransaction(Transaction txn) 
     *   End transaction -> close block managers -> checking? 
     *   
     * Recycle DatasetGraphTx objects.  Setup - set PageView
     *   better setup.
     */

    /*
     * Layers:
     *   DatasetGraph
     *   Indexes
     *   Pages
     *   Blocks
     *   Storage = FileAccess (a sequence of blocks) 
     */
    
    /* 
     * Fast B+Tree creation: wrap an existsing BPTree with another that switches the block managers only.
     * BPtree.reattach (misses the test of if exists).
     *   Later.
     * Setup
     *   Transaction start: grab alloc id.
     */
}
