/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Inline NodeIds 
    // Code in NodeTableBase
    
    // Interface Sync everywhere?
    // CountingSync.
    
    // Bulk loader : index control.
    // Text index builder
    // Only do NodeId->Node for needed nodes (special counts?).
    
    // QueryHandler to access subjectsFor etc. 
    
    // Analysis tools: NT=>predicate distribution.
    // Namespace extractor.

    // 2/ Packing and BTrees (leaves)
    //    B+Tree (lesser)
    // 3/ P ExtHash
    
    // Const from a properties file.
    
    // com.hp.hpl.jena.util.FileUtils - use faster "buffered" reader (extend BufferedReader)
    // Filter evaluation - get only nodes used

    // Fix BDB form

    // Move public ops from BTreeNode to BTree, and make BTreeNode statics for all operations?
}
