/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    
    // Enable stats
    
    // --> Reorder in the wrong place.  Substitute on a per input basis.
    // --> Need to add something for connectivity
    
    // 1/ StageGeneratorPGraphBGP : reorder : needs to pass in list of defined vars
    //    Work on Filter-BGP blocks.  
    // 2/ Test (and JUnit for the stats matcher)
    // 3/ tdbloader: write/update stats
    // ARQ: (A) FilterBGP units and (B) optimizer policy hook (C) per source optimization.
    
    // Extract/generalise the pattern matcher and apply to BGPs.
    // Link Assembler (custom indexes) to TDBFactory 
    // Modular build.
    
    // ---- B+Tree rewriter
    
    // ---- New cache API alloc/return/invalidate (shrink/grow?)
    //   Stats.
    
    // ---- New organsiation for files:
    //   TripleIndex -> Index/RangeIndex (records) -> BlockFile (id-Block)
    //   NodeTable   -> ObjectFile (NodId->Node) -> VarFile (id-bytes)
    //   IndexBuilder to migrate to be policy for data files as well.
    //   ?? Combine with FileFactory, BlockMgrFactory
    // ---- Reopenable BlockMgrs (and the object file?) 
    //   Memory versions are a "file" (fit into framework for testing).
    //     Maybe not worth the effort to rework but needs to be reopenable.
    //   Root is an NIO FileChannel (Or Chanel with a mem impl?)
    
    // ---- tdbloader: 
    //   close indexes not in use in a given phase
    //   Especially efficient iterator() for B+Trees (not mmap).
    //   (script) to work on gzip files 

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
