/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // ---- Sync after update (32 bit, Tomcat shutdown)
    //   Thread to poke?
    //   Update .start/.finish?
    
    // ---- Next: materialized views
    
    // ---- Optimization
    //   Instrumentation
    
    // Design/static (long term?)
    //   Scoping and rewrite with later execution
    //   generalise the approach:
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
    
    // ---- Optimizer
    //   Abbreviate planning when below a threshold (likely selectivity?), or number of triples planned.
    //   Consider doing all plans from first choice.
    //     VarCounting : need to consider connectivity for boundedness propagation
    //     or consider all plans at step one.
    // Documentation

    // ------------------------------------------
    
    // ---- B+Tree rewriter
    
    // ---- ExtHash : Trial
    
    // ---- New cache API alloc/return/invalidate (shrink/grow?) : stats
    // Weak references and more space.
    
    // ---- Reopenable BlockMgrs (and the object file?) 
    
    // ---- Node Table
    //     Compression
    
    // ---- tdbloader: 
    //   ** Close indexes not in use in a given phase
    //   Especially efficient iterator() for B+Trees (not mmap).
    //   ** (script) to work on gzip files
    //   ** Write stats (where to hook in to get unique?)
    //   32 bit ideas: 

    // ---- Use of java properties for key values.
    // Or config file.

    // ---- Graph
    // QueryHandler to access subjectsFor etc. 
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.

    // Link Assembler (custom indexes) to TDBFactory 
    // TDBFactory ==> "create" ==> connect(... , boolean canCreate) ;
    // Location-keyed cache of TDB graphs 
    
    // ---- BulkLoader
    //    - shared formatting with GraphLoadMonitor
    
    // ---- Misc :
    // Interface Sync everywhere?
    // CountingSync.
    //   bound variable tracking
    //   LARQ++
    
    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same?

    // Inlines => Inline56, Inline64

    // Fix BDB form
}
