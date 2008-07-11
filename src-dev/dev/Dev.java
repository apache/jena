/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Sort out names - Names.xyz
    
    // Different names for nodes.dat file under the two schemes.

    // Build needs to create tmp/testing
    
    // CI - tidy up
    // CC dashborad assumes the use of ant default target

    // Huge store : 96bits hash ids?
    // IndexBuilder to migrate to be policy for data files as well.
    
    // PExt!
    
    // removeAll implementation: depends on iterator.remove
    // but can do faster as a specific operation.
    
    // Documentation on the wiki
    //   Assembler
    //   TDBFactory
    //   Commands
    
    // TDBFactory ==> "create" ==> connect(... , boolean canCeate) ;
    // TDB connections?
    // TDBFactory, same Location ==> same model. 
    // ModelSource?
 
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
    
    // ARQ: Var scope handling - add to OpBase?
    
    // QueryHandler to access subjectsFor etc. 
    
    // Analysis tools: 
    //    NT=>predicate distribution.
    //    Namespace extractor.
    // BGP Optimizer II

    // com.hp.hpl.jena.util.FileUtils - use faster "buffered" reader (extend BufferedReader)

    // Consts from a properties file.
    // Fix BDB form
}
