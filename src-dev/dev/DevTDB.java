/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevTDB
{
    // ** metadata files and BPT creation.
    
    // == 0.8.2
    
    // ** Document concurrency.
    // ** Check assembler page for emphasise on creating a dataset.
    // ** Check for tests of assembler and GraphTDB
    // Enable metadata? [later]
    
    // ------
    // Properties on the readers
    // NodeTable as ( Index<Node, NodeID>, Index<NodeId, Node> )
    //    Assumes variable length records
    //    Need better var index support first
    
    // BDB-JE? BlockMgr as index?
    
    // Document concurrency 
    // Graph.getLock in Jena?  Share with dataset.
    
    // ==== RIOT
    // ** Connect the errorhandler set via JenaReaderbase to the one used by RIOT-Turtle/RIOT-NTriples.
    // Error handler, not exceptions, in Turtle.
    //   Check for continuation.
    
    // == tdbdump && tdbrestore
    // FileSetMetadata - const names in sys.Names (currently in BPlusTreeParams)
    // See IndexFactoryBPlusTree.createRangeIndex
    // See BPlusTreeParams.readMeta

    // NodeLib.encode/decode ==> swap to a Nodec

    // Version of BufferingWriter that works on OutputStreams.

    // Quad loader; dataset merge.
    // Dataset management??
    
    // ---- Optimizer
    //   The disjunction/UNION rewrite (ARQ)
    
    // ---- Documentation
    
    // ---- BlockMgrs
    // Consistency - do not manage in block managers except where MRSW not safe.
    // TupleTable.size - at least an isEmpty 
    // ==> Reopenable
    // ==> .release(id)
    // ==> Accurate size (?? meaningful beyond isEmpty/notEmpty?)
    // ==> Metablocks.
    //   ==> Moveable roots.
    
    // ---- Misc
    // Inlines => Inline56, Inline64, ??

    // ---- tdbloader: 
    //   ** (script) to work on gzip files
}
