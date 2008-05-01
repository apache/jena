/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // See Store/gbt.ttl
    
    // Interface Sync everywhere?
    // CountingSync.
    //   bound variable tracking
    //   LARQ++
    // Sort out library of datasets : Store/...
    
    // Assembler
    //    :location <.> ;
    //  [] a :GraphTDB
    // See TripleIndexAssembler, NodeTableAssembler
    
    
    //
    // Plan for a mega-hash id version (96 bits, hash based)
    // Version of NodeTable that does Logical => Physical id translation
    // And a PageMgr.

    // 32 bit: Try without each of the read cache and write cahes to see value of each
    //   And which index cache is having the most effect?
    //   And/Or, better, combine into one cache?
    
    // Special to test speed of node loading only.
    // Inlines => Inline56, Inline64
    
    // ARQ: Var scope handing - add to OpBase?
    
    // QueryHandler to access subjectsFor etc. 
    
    // Analysis tools: 
    //    NT=>predicate distribution.
    //    Namespace extractor.
    // BGP Optimizer II

    // ExtHash
    // com.hp.hpl.jena.util.FileUtils - use faster "buffered" reader (extend BufferedReader)

    // Fix BDB form
}
