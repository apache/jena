package tx;


public class DevTx
{
    // Block or page - dirty flag? tie to promote? setModified()
    // BPTree.delete - avoids double put but gets into more trouble.
    //   .write, and auto write on releaseWrite?
    
    // check BPTreeNode for:
    //  Use of bpTree.getNodeManager() -- should be a  page.op() call.
    
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
    // BlockMgrTracker to dump inconsistences on finish*, not just log a warning.
    
    // PageMgr implements UnitMgr<Page>
    //   Add "write" - put = write - releaseWrite.
    // PageBlockMgr.free, not via bypassing PageBlockMgr.
    
    // releasing the root after update and put --> warning
    // "Needs put()'ing"
    
    // B+Tree
    //   Avoid reparsing root blocks.  Maybe release only after change.
    
    // Next: 
    //   Write test harness for lock read/write usage in BPT operations.
    
    // Block to have a clean/dirty flag.
    //   Promote pages or promote blocks?  Blocks - page pass down promotion.
    // End of transaction forces end of iterators, release of blocks etc.
    //   Blocks - assumes that system does magic to promote 
    //   Demote? No == put().
    
    // See all : // [TxTDB:PATCH-UP]
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
