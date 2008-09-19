/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // ** solver.reorder.StageGeneratorReorder
    // ** General StageGenerator one bindng at a time over a Stage.
    
    // ---- Optimizer
    //   Abbreviate planning when below a threshold.
    //   TDB.optimizerOn() / TDB.optimizerOff() -- leaving it in the StageGenerator chain.
    //   VarCounting : need to consider connectivity for boundedness propagation 
    //     when same possible choices (stats is the same only less so). 
    //     e.g. (?s P X) (?s1 P X) (?s P X)    vs   (?s1 P X) (?s P X) (?s P X) 
    
    // -- Work on Filter-BGP blocks.
    // OpCompilerTDB to intercept (later: in ARQ at "OpCompiler.compile")
    
    // -- Tests : VarCounter, stats matcher.
    
    // ---- B+Tree rewriter
    
    // ---- ExtHash : Trial
    
    // ---- New cache API alloc/return/invalidate (shrink/grow?) : stats
    
    // ---- Reopenable BlockMgrs (and the object file?) 
    
    // ---- tdbloader: 
    //   close indexes not in use in a given phase
    //   Especially efficient iterator() for B+Trees (not mmap).
    //   ** (script) to work on gzip files
    //   ** Write stats

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
