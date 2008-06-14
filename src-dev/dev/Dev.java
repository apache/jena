/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Build.  Ant and Ivy. 
    
    // Commands:
    // --desc defaults to tdb.ttl?
    // May need to swap the (ARQ) ModDataset > ModDatsetGeneral > ModAssembler
    // and then ModAssembler > ModTDBDataset 
    
    // Initialization - make suu initialization happens on assembler route
    
    // Documenation on the wiki
    //   Assembler
    //   TDBFactory
    
    // TDBFactory ==> "create" ==> connect(... , boolean canCeate) ;
    // TDBFactory, same Location ==> same model. 
    // ModelSource?
 
    // Assembler to cache graphs made (by location) so reuse is same graph engine 
    // Careful about holding too much memory. Soft refs
    //   Cache in BlockMgrFactory.createStdFile (direct files)
    
    // BulkLoader
    //    - move processing out of tdb.tdbloader
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

    // 32 bit: Try without each of the read cache and write cahes to see value of each
    //   And which index cache is having the most effect?
    //   And/Or, better, combine into one cache?
    
    // Special to test speed of node loading only.
    // Inlines => Inline56, Inline64
    
    // ARQ: Var scope handling - add to OpBase?
    
    // QueryHandler to access subjectsFor etc. 
    
    // Analysis tools: 
    //    NT=>predicate distribution.
    //    Namespace extractor.
    // BGP Optimizer II

    // Some cleaning up around counting and sync.

    // com.hp.hpl.jena.util.FileUtils - use faster "buffered" reader (extend BufferedReader)

    // Apps: Namespace extractor in tdbexamine

    // Consts from a properties file.
    
    // Fix BDB form
}
