/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // == 0.8.0
    // Testing: SPARQL/Update integration
    //     TDBFactory caching
    
    // 1 -- FactoryGraphTDB._createGraph passes null for dataset
    //    Quadsless dataset?
    // 2 -- graph.close() --> does nto close the datset (sync?)
    // 3 -- caching and tests.
    // 4 -- Dataset -> return to cache.
    // 5 -- Update tests
    
    // Documentation?
    
    // Multiple routes to dataset - does it matter?
    // 
//    15:33:10 WARN  - exec                 :: No BGP optimizer
//    15:33:10 WARN  - exec                 :: No BGP optimizer
//    15:33:11 WARN  - exec                 :: No BGP optimizer
    // TDBFactory caching - need to remove from cache on close.

    
    
    // Metadata.
    //   Metafile for directory.
    //   Metafiles and opening indexes
    //   New and reattach
    // Relayering for index interface
    
    // ==== Build
    //   Multiple ivy modules
    //  Use <ivy:configure file="myconffile.xml" /> (not ivy:setting)
    
    // == tdbdump && tdbrestore
    // FileSetMetadata - const names in sys.Names (currently in BPlusTreeParams)
    // See IndexFactoryBPlusTree.createRangeIndex
    // See BPlusTreeParams.readMeta

    // To ARQ:
    //   Atlas? Iterator.
    //   Explain, and explain logger (from ARQ?)
    
    // NodeLib.encode/decode ==> swap to a Nodec

    // Version of BufferingWriter that works on OutputStreams.

    // ==== Execution
    // Quad pattern execution/optimization with filters (done for default graph).
    // File metafiles. FileSets
    //    Reopenable files.
    //      Alterntaive length hash codes.
    //      Record lengths
    //    Node file stats
    
    // ==== Tools
    // ** Command line optimizer "explain (bgp ....)"
    // ** LARQ builder for literals
    
    // ---- Quads:
    //   Filter placement : (filter (quads...)) : FILTER (between(x,y)) => range on index. => OpRange.
    //   Generalise quads to be quads, not (node, bgp) ??

    //   Quad loader; dataset merge.
    //   Generalised the pattern stuff in the optimizer?
    //   Dataset management??
    //   Tidy OpExecutorTDB.execute(quads)
    
    // ---- Optimizer
    //   Relationship of filter placement and OpExecutor and StageGenerators.
    //   The disjunction/UNION rewrite (ARQ)
    
    // ---- Documentation
    //  Concurrency policy
    //  Change assembler page to emphasise creating a dataset.
    
    // ---- BlockMgrs
    // Consistency - do not manage in block managers except where MRSW not safe.
    // TupleTable.size - at least an isEmpty 
    // ==> Reopenable
    // ==> .release(id)
    // ==> Accurate size (?? meaningful beyond isEmpty/notEmpty?)
    // ==> Metablocks.
    //   ==> Moveable roots.
    
    // ---- Interactions with ARQ
    // + Other indexes
    // + SPARQL/Update
    //   Dataset is cloned on a Joseki/SPARQL/update operation
    //   Need ARQ change + TDB to provide a GraphStore.
    // Optimizations
    // Quad-based filter placement
    
    // ---- Misc
    // Inlines => Inline56, Inline64

    // ---- tdbloader: 
    //   ** (script) to work on gzip files

    // ---- 32 bit mode.
    // ARC (Adaptive Replacement Cache) Cache Scheme
}
