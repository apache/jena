/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // (tidying) : ARQ : treat memory graphs as a storage (StageGenBasicPattern in ARQ).
    // TDB.optimizerOn() / TDB.optimizerOff() -- leaving it in the StageGenerator chain.
    // reorder -> return a list on indexes (a reorder function), not the pattern itself.
    // The fast lokup in stats isn't perfect - if TERM is stronger than <p>, it misses it.
    // By perfect: from <p> try TERM and ANY; from ?p try VAR and ANY  
    
    // Stats: What about a rules
    // VarCounting : need to consider connectivity for boundedness propagation 
    // when same possible choices (stats is the same only less so). 
    // e.g. (?s P X) (?s1 P X) (?s P X)    vs   (?s1 P X) (?s P X) (?s P X) 
    
    // -- Work on Filter-BGP blocks.  
    // -- Tests : VarCounter, stats matcher.
    // -- tdbloader: write/update stats
    // -- ARQ: (A) FilterBGP units and (B) optimizer policy hook (C) per source optimization.
    
    // Link Assembler (custom indexes) to TDBFactory 
    
    // ---- B+Tree rewriter
    
    // ---- New cache API alloc/return/invalidate (shrink/grow?)
    //   Stats.
    
    // ---- Reopenable BlockMgrs (and the object file?) 
    
    // ---- tdbloader: 
    //   close indexes not in use in a given phase
    //   Especially efficient iterator() for B+Trees (not mmap).
    //   (script) to work on gzip files
    //   Write stats

    // ---- Use of java properties for key values.
    // Or config file.

    // ---- Graph
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.
    
    // TDBFactory ==> "create" ==> connect(... , boolean canCreate) ;
    // Location-keyed cache of TDB graphs 
    
    // BulkLoader
    //    - shared formatting with GraphLoadMonitor
    
    // ---- Misc :
    // Interface Sync everywhere?
    // CountingSync.
    //   bound variable tracking
    //   LARQ++
    
    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same?

    // Inlines => Inline56, Inline64

    // QueryHandler to access subjectsFor etc. 

    // Fix BDB form
}
