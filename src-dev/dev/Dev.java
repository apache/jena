/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // New cache API alloc/return/invalidate (shrink/grow?)
    //   Stats.
    // Check caches.
    //   More aggressively cache B+Tree indexes
    
    // BlockMgr - get() ==> pin() and unpin() (with checking!)
    
    // Node table caching?
    // Use of java properties fro key values.
    // Different names for nodes.dat file under the two schemes.

    // CI: 
    CC dashboard assumes the use of ant default target

    // Huge store : 96bits hash ids?
    // IndexBuilder to migrate to be policy for data files as well.
    
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.
    
    // Documentation on the wiki
    //   Assembler
    //   TDBFactory
    //   Commands
    
    // TDBFactory ==> "create" ==> connect(... , boolean canCeate) ;
 
    // Location-keyed cache of TDB graphs 
    
    // BulkLoader
    //    - shared formatting with GraphLoadMonitor
    
    // Misc :
    // Interface Sync everywhere?
    // CountingSync.
    //   bound variable tracking
    //   LARQ++
    
    // Plan for a mega-hash id version (96 bits, hash based)
    //    Parameter of hash size.
    // Version of NodeTable that does Logical => Physical id translation
    //    And a PageMgr wrapper for same.

    // Inlines => Inline56, Inline64

    // QueryHandler to access subjectsFor etc. 

    // Fix BDB form
}
