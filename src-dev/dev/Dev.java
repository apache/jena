/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
   
    
    // ---- Release
    //   ?? ARQ Optimizer and controlling optimizations per graph (esp FilterPlacement).
    // Optimizer documentation
    // Final testing

    // ---- Reopenable BlockMgrs (and the object file?) 
    
    // ---- ExtHash trials
    
    // ---- Optimization
    //   Abbreviate planning when below a threshold (likely selectivity?), or number of triples planned.
    //   Consider doing all plans from first choice.
    // ** Documentation
    
    // ---- Assembler 
    //    Setting options (globally, per model)
    //    Variable indexes
    
    // ---- Materialized views
    
    // ---- Range queries (filter E (bgp T)) ==> (range T E....) where E has one var and the one var is in T
    //    Given NodeId structure, should be doable for ints and dates.
    
    // ---- Reifier
    
    // ---- Quads
    //   Common abstractions
    //       quad table (share with SDB)
    //       dataset as graph + quads
    //       dataset management
    
    // ---- Loader - traverse of SPO via a direct block mgr?
    //   ** Close indexes not in use in a given phase
    //  GraphTDB.primaryTraverse() -> Iterator<Tuple<NodeId>>??
    //    Do properly : reopenable indexes (under the triple index)
    //      Reopenable block mgr's .passivate/.activate
    //    Open second index on same
    //    - shared formatting with GraphLoadMonitor
    // ---- tdbloader: 
    //   ** (script) to work on gzip files
    //   ** Write stats (where to hook in to get unique?)

    
    // Design/static (long term)
    //   Scoping and rewrite with later execution
    //   Generalise the approach:
    //     OpJoin => sequence // OpAugment/Op1 or IndexedLeftJoin/Op2 : an indexed LeftJoin
    //     OpAugment is "match or {}" rather than "match or nil" OpDefault? OpConditional?
    //     OpConditional(left, right) -- streaming - 
    //        try to extend with left and for each row can't get an extension, do right instead. 
    //        Over-engineered? OpConditional(expr) only in a sequence.
    //        Or OpN is a sequence of left then augment right1, right2, ...
    
    //   TransformCopy 
    //      - maybe Transform should be "Op transform(OpABC)"
    //      - and TransformCopy(list) is TransformRewrite. (there are no non-TransformCopy's)
    
    // ---- TestStats, VarCounter.
    // ---- A way to force display of optimizer even for the deep parts (assumes no inputs?)
    
    // ------------------------------------------
    
    // ---- B+Tree rewriter
    
    // ---- New cache API alloc/return/invalidate (shrink/grow?) : stats
    // Weak references and more space.
    
    // ---- Node Table
    //     Compression
    //     short string inline?
    
    // ---- Graph
    // QueryHandler to access subjectsFor etc. 
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.

    // Link Assembler (custom indexes) to TDBFactory 
    // TDBFactory ==> "create" ==> connect(... , boolean canCreate) ;
    // Location-keyed cache of TDB graphs 
    
    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same?

    // Inlines => Inline56, Inline64

    // Update BDB form
}
