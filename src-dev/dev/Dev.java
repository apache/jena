/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // To ARQ:
    //   NodeConst
    //   Atlas? Iterator.
    //   Explain, and explain logger.
    
    // Use ARQ features
    //   com.hp.hpl.jena.sparql.engine.optimizer
    //   Explain.
    
    // Location+String => Filesets
    //   createBlockMgr - tie to metadata?
     
    // NodeLib.encode/decode ==> swap to a Nodec

    // ==== Next
    // Quad pattern execution/optimization with filters (done for default graph).
    // File metafiles. FileSets
    //    Reopenable files.
    //      Alterntaive length hash codes.
    //      Record lengths
    //      
    //    Node file stats
    
    // ==== Tools
    // ** Command line optimizer "explain (bgp ....)"
    // ** LARQ builder for literals
    
    // ---- Quads:
    //   Filter placement : (filter (quads...)) : FILTER (between(x,y)) => range on index. => OpRange.
    //   Generalise quads to be quads, not (node, bgp).

    //   Quad loader; dataset merge.
    //   Generalised the pattern stuff in teh optimizer?
    //   Dataset management??
    //   Tidy OpExecutorTDB.execute(quads)
    
    // ---- Optimizer
    //   Relationship of filter placement and OpExecutor and StageGenerators.
    //   The disjunction/UNION rewrite
    
    // ---- Graph
    // listSubjects via QueryHandlerTDB (make a range query)
    
    // ---- Documentation
    //  Concurrency policy
    
    // ---- Indexing and block managers
    // Ideally, B+Tree should be able to have different orders in leaves and branches.

    // Generic TupleIndex<T extends Something> for NodeId or Node
    //   e.g. Need mapper from T to TupleIndexRecord to abstract. 
    
    // Consistency - do not manage in block managers except where MRSW not safe.
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
    
    // TDBFactory - cache graphs - graph.close is return to cache (and sync) 
    
    // ---- Misc
    // OpIndex(?) - generative stream of possibilities.
    // { ?s :p ?x . ?s :q ?w } specials?  "OpAllProperties"
    // Inlines => Inline56, Inline64

    // ---- Tuple reader tests
    // [incremental]
    // Checking, N-ary and non-RDF. BRT.
    // Prefix mapping?  Migrate to a dump format.
    // Byte version.
    
    // ---- BlockMgrs
    // Reopenable BlockMgrs (and the object file?) 
    //   Needed?  SPO close followed by linear scans only.
    //   Reopen the whole graph (??)
    // TupleTable.size - at least an isEmpty 
    //   Not just an empty block manager.

    // ---- Materialized views
    // Key->Action paradigm
    //   Simple pattern keys (property, 2P). 
    
    // ---- tdbloader: 
    //   ** (script) to work on gzip files
    // ---- 32 bit mode.
    // ARC (Adaptive Replacement Cache) Cache Scheme

    //   TransformCopy 
    //      - maybe Transform should be "Op transform(OpABC)"
    //      - and TransformCopy(list) is TransformRewrite. (there are no non-TransformCopy's)

}
