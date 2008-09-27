/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // ---- Sort out filter placement and reordering
    // Dynamic:
    //    Use QueryIterPeek
    //    Wire in and mothball StageGeneratorTDB 
    // Design/Dynamic
    //   QueryIterPeek to look forward at one Binding.
    //   OpExt(Main?) to mark a BGP for no further work.  Or OpLabel - need to compleet OpLabel
    //   Simple case - ignore bindings - correct at top level.
    // Long term: static
    //   Static ==> more modular, better if algebra extsnds to all op (e.g. IndexedJoin)  
    
    // Design/static
    //   Scoping and rewrite with later execution
    //   generalise the approach:
    //     OpJoin => sequnece // OpAugment/Op1 or IndexedLeftJoin/Op2 : an indexed LeftJoin
    //     OpAugment is very like "match or {}" rather than "match or nil"
    
    //   TransformCopy 
    //      - maybe Transform should be "Op transform(OpABC)"
    //      - and TransformCopy is TransformRewrite. (there are no non-TransformCopy's)
    
    // ---- TestStats, VarCounter.
    // ---- A way to force display of optimizer even for the deep parts (assumes no inputs?)
    
    // ---- Matcher
    // Back-tracking?
    
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
