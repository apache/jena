/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Out-of-range DateTimes.
    // Scriped test cases for inline data.
    
    // Interface Sync everywhere?
    // CountingSync.
    //   bound variable tracking
    //   LARQ++
    // Inline xsd:dateTimes/xsd:dates
    // Sort out library of datasets : Store/...
    
    // Assembler
    //    :location <.> ;
    //    Better, less specific type names
    //    Classes -- classes vs properties
    //           
    //      tdb:GraphTDB: mode=standard,large
    //
    // Plan for a mega-hash id version
    // Version of NodeTable that does Logical => Physical id translation
    // And a PageMgr.

    // Triggers: triples only
    
    // 32 bit: Try without each of the read cache and write cahes to see value of each
    //   And which index cache is having the most effect?
    //   And/Or, better, combine into one cache?
    
    // Special to test speed of node loading.
    //  Subclass PGraphBase, which assumes at least an SPO.
    //     GraphNodesOnly
    // Inlines => Inline56, Inline64
    
    // ARQ: Var scope handing - add to OpBase?
    
    // BGP Optimizer.
    
    // QueryHandler to access subjectsFor etc. 
    
    // Analysis tools: NT=>predicate distribution.
    // Namespace extractor.

    // 2/ Packing and BTrees (leaves)
    //    B+Tree (lesser)
    // 3/ P ExtHash
    // Move public ops from BTreeNode to BTree, and make BTreeNode statics for all operations?
    
    // com.hp.hpl.jena.util.FileUtils - use faster "buffered" reader (extend BufferedReader)

    // Fix BDB form

}
