/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // ==== For a graph-release:
    //   Simple BGP optimizer.
    // ====
    
    // Interface Sync everywhere?
    // CountingSync.
    
    // Bulk loader : index control.
    // Text index builder
    // Only do NodeId->Node for needed nodes (special counts?).
    
    // Block typing.
    
    // QueryHandler to access subjectsFor etc. 
    
    // Analysis tools: NT=>predicate distribution.
    // Namespace extractor.

    // 2/ Packing and BTrees (leaves)
    //    B+Tree (lesser)
    /*    Started: see BT
    *     Affected:
    *       Insert:  split, splitRoot, 
    *       Delete:  merge, shiftLeft, shiftRight, shuffleUp shuffleUpAll, shuffleDown   
    */
    // 3/ P ExtHash
    // 4/ Per node table encoding of Nodes
    
    // Const from a properties file.
    
    // com.hp.hpl.jena.util.FileUtils - use faster "buffered" reader (extend BufferedReader)
    // First : BulkLoader, with delayed index building.
    
    // Filter evaluation - get only nodes used

    // Fix BDB form

    // Move public ops from BTreeNode to BTree, and make BTreeNode statics for all operations?
}
