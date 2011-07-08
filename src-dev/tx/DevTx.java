package tx;


public class DevTx
{
    // Tests
    
    // NodeTable journalling and recovery
    // 3/ general setup
    // Replay ==> JournalCtl.
    // warmReplay, coldReplay 
    // Dataset API
    // DatasetGraphAPI - everything some kinds of transaction?
    // Iterator tracking.
    //   DatasetGraphTDB.find*, prefixes, OpExecutorTDB.OpExecutorPlainTDB=>SolverLib.execute 
    //   OpExecutorTDB.OpExecutorPlainTDB
    //     =>SolverLib.execute 
    //     =>StageMatchTuple
    //     =>NodeTupleTable.find [NodeTupleTableConcrete]
    //         ====> "ConcurrencyPolicy" => "system state"
    //            Iterator<Tuple<NodeId>> find(Tuple<NodeId> tuple) ==> checkIterator: 
    //     =>RangeIndex eventually.
    //     **** Catch in NodeTupleTable.find
    // Larger grain "start write" -- one on each NodeTupleTableConcrete.addRow but need a parser or triple "add" 
    
    // ?? Read-only by concurrency policy.
    
    
    // Evenetually:
    // Iterator<Tuple<NodeId>> find(Tuple<NodeId> tuple)
    
    // DSG.add(Quad(tripleInQuad, triple)) does not affect default graph.
    
    // Every sync hits the NodeTupleTable sync the node table repeatedly - keep dirty flag? 
    
    // Config
    //   One config file?
    //   Cache sizes
    //   Index names
    //   Length of NodeId?
    //   Setting of content properties.
    
    // TestObjectFileTrans -- more tests.
    // TestObjectFileBuffering --> make abstract, it stress tests the BufferChannel.
    
    // Channel+Adler32
    
    // Tidy up 
    //   See HACK (BPTreeNode)
    //   See [TxTDB:PATCH-UP]
    //   See [TxTDB:TODO]
    //   See FREE
    //   See [ITER]
    
    // Optimizations:
    //   ByteBuffer.allocateDirect + pooling
    //     http://mail-archives.apache.org/mod_mbox/mina-dev/200804.mbox/%3C47F90DF0.6050101@gmail.com%3E
    //     http://mail-archives.apache.org/mod_mbox/mina-dev/200804.mbox/%3Cloom.20080407T064019-708@post.gmane.org%3E

    // Other:
    //   Sort out IndexBulder/IndexFactory/(IndexMaker in test)
    
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
     * Fast B+Tree creation: wrap an existsing BPTree with another that switches the block managers only.
     *    BPTree.attach with warpping BlockMgrs.
     *    Delay creation of some things?
     * Cache root block.
     * Setup
     *   Transaction start: grab alloc id.
     */
    
    // TDB 0.8.10 is rev 8718; TxTDB forked at 8731
    // Diff of SF ref 8718 to Apache cross over applied. (src/ only)
    // Now Apache: rev 1124661 
}
