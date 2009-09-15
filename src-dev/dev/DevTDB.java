/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevTDB
{
    // Compressor
    // SetupUtils - move into metafile?
    // B+Tree checking utility.
    
    // IndexFactory understanding index type name
    //    Registry<String->T>("bplustree", IndexBuilder)
    
    // Tuning for low memory / 32 bit - smaller fixed caches

    // Longer: packaging of TDB and Joseki for an unpack-and-go solution.
    // At least, specific documentation.
    
    // --- Setup
    // Tests - but keep old FactoryGraphTDB while it handles alternatives indexes.
    //    (Currently in ...sys...)

    // ?? NodeTableBuilder
    //  Non-DI: DatasetPrefixesTDB(IndexBuilder, Location)
    //  Non-DI: NodeTableFactory.create(IndexBuilder, Location)
    
    // == 0.8.3
    // Explain mode - See notes in class
    // Don't print optimizer type when set at info level.
    // Remove "warn" ?
    // Nodefile : NodeCodec
    //   NodecSSE: decode.
    // Better testing of (persistent) node table (without caching). 
    
    // ** NewSetup/DI
    // Use ARQ 2.8.1 and enable management info.
    //    (later: Phase 2 : Monitoring caches)
    
    // Investigate better record byte access.  Whole record?
    // Investigate adding a PSO index (for paths to get sorted order)
    // Investigate tracking sorted order in BGPs
    // Cache tables.
    
    // Assembler : TDB settings : document.
    
    // == Misc
    // Node cache on 64bit machines needs to be bigger or rebalence
    // FileMode, NodeCacheNode, ...
    // 32 bit => Direct, small
    // 64 bit => Mapped, large
    // Cache stats counters (prep for JMX but useful now)
    // Binary, not SSE.
    
    // == Monitoring
    // Assembler : TDB settings
    //  To a separate file - example in log4j.
    
    // ----
    // Publish => release and upload the zip to maven area.
    // IVY publish a zip
    
    // ** Document concurrency.
    
    // ------
    // Properties on the readers
    // NodeTable as ( Index<Node, NodeID>, Index<NodeId, Node> )
    //    Assumes variable length records
    //    Need better var index support first
    
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
