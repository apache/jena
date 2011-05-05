package tx;


public class DevTx
{
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
    
    // Next: 
    //   Write test harness for nlock read/write usage in BPT operations.
    //   Release()
    
    // Block to have a clean/dirty flag.
    // Promote pages or promote blocks?
    //   Blocks - assumes that system does magic to promote 
    //   Demote? No == put().
    //    Page extends Block?
    
    // See all : // [TxTDB:PATCH-UP]
    // Memory mapped files.
    // Avoid reparsing root blocks.  Maybe release only after change.
    
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
