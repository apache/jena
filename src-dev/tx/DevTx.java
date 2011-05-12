package tx;


public class DevTx
{
    // Run tests with BlockMgrFactory.tracked on.
    // FileAccessDirect (and delete BlockMgrDirect) [DONE]
    // FileAccessMapped (and delete BlockMgrmapped)
    // tests for BlockMgr tracking?
    
    // Tidy up 
    //   See HACK (BPTreeNode)
    //   See [TxTDB:PATCH-UP]
    //   See FREE
    
    // Caching pages across actions sequences. e.g BPT root block.
    // Iterators
    // Cache in transaction so forgettable?
    // Session.migrate ; DatsetGraph isa session. ; session nesting and overlap - session manager?
    
    // ---- ---- ---- ----
    
    // [TxTDB:PATCH-UP]

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
     * Cache root block.
     * Setup
     *   Transaction start: grab alloc id.
     */
    
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
}
