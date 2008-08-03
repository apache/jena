/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Build - to sftp server
    // Need jsch.
    // Make pom an artifact => publish it.
    // Make the file layout correct for Maven (see that email message)
    
    // B+Tree rewriter
    
    // New cache API alloc/return/invalidate (shrink/grow?)
    //   Stats.
    
    // Documentation on the wiki
    
    // New organsiation for files:
    //   TripleIndex -> Index/RangeIndex (records) -> BlockFile (id-Block)
    //   NodeTable   -> ObjectFile (NodId->Node) -> VarFile (id-bytes)
    
    // tdbloader: 
    //   close indexes not in use in a given phase
    //   More aggressively cache B+Tree indexes, less on leaves (enough for 32 bit?)
    //   Especially efficient iterator() for B+Trees (not mmap).

    // Node table caching?
    
    // Use of java properties for key values.

    // FileFactory2
    //   IndexBuilder to migrate to be policy for data files as well.
    //   Combine with FileFactory, BlockMgrFactory
    
    // Reopenable BlockMgrs (and the object file?) 
    //   Memory versions are a "file" (fit into framework for testing).
    //     Maybe not worth the effort to rework but needs to be reopenable.
    //   Root is an NIO FileChannel
    
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.
    
    // TDBFactory ==> "create" ==> connect(... , boolean canCreate) ;
    // Location-keyed cache of TDB graphs 
    
    // BulkLoader
    //    - shared formatting with GraphLoadMonitor
    
    // Misc :
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
