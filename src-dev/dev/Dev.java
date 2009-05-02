/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // == 0.8.0
    //   Metafile for directory.
    //   Metafiles and opening indexes
    //   New and reattach
    //   TDBFactory - cache graphs - graph.close is return to cache (and sync) 

    
    // tdbrestore
    // FileSetMetadata - const names in sys.Names (currently in BPlusTreeParams)
    // See IndexFactoryBPlusTree.createRangeIndex
    // See BPlusTreeParams.readMeta
    
    // Graph by name graph from TDB database.
    // Tricky because of non-shared TDB engine => shared/cached engines.
    
    // To ARQ:
    //   Atlas? Iterator.
    //   Explain, and explain logger (from ARQ?)
    
    // Location+String => Filesets
    //   createBlockMgr - tie to metadata?
     
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
    //   Generalise quads to be quads, not (node, bgp).

    //   Quad loader; dataset merge.
    //   Generalised the pattern stuff in the optimizer?
    //   Dataset management??
    //   Tidy OpExecutorTDB.execute(quads)
    
    // ---- Optimizer
    //   Relationship of filter placement and OpExecutor and StageGenerators.
    //   The disjunction/UNION rewrite (ARQ)
    
    // ---- Graph
    // listSubjects via QueryHandlerTDB (make a range query)
    
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
    
    // ---- Misc
    // Inlines => Inline56, Inline64

    // ---- Materialized views
    // Key->Action paradigm
    //   Simple pattern keys (property, 2P). 
    
    // ---- tdbloader: 
    //   ** (script) to work on gzip files
    // ---- 32 bit mode.
    // ARC (Adaptive Replacement Cache) Cache Scheme
}
