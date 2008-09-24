/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // ---- Filter placement
    // ---- Replan 1 in 100 requests
    
    // ---- Matcher
    // Back-tracking?
    
    // ---- Tests : VarCounter, stats matcher.

    // ---- Optimizer
    //   Abbreviate planning when below a threshold (likely selectivity?), or number of triples planned.
    //   Consider doing all plans from first choice.
    //     VarCounting : need to consider connectivity for boundedness propagation
    //     or consider all plans at step one.
    // Documentation
    
    // ---- Work on Filter-BGP blocks.
    //     OpCompilerTDB to intercept
    
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
