/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class Dev
{
    // Different names for nodes.dat file under the two schemes.
    // B+Trees 
    
    // CI
    // BPlusTree and non-existent entries
    //   Make sure BPlusTree tests geting run.
    // TestGraphBPlusTreeMem and 2
    // PGraphFactory
    
    // PtrBuffer has ref to BTreeParams.
    // Role of PGraphFactory?
    //    Move cerate stuff here
    // GraphBplusTree and GraphBTree to go.
    // IndexBuilder to include ppolicy for data files
    
    // TestGraphBPlusTreeMem and Mem2.
    // Duplicate above for BTree and B+Tree
    // IndexBuilder.mem() - fixed.
    
    // Global IndexFactory.  create(Location, Name) => (Range)Index
    // IndexBuilder for the global, any location (IndexFactoryFactory)
    // Use IndexBuilder in PGraphFactory, TDBFactory, TripleIndexAssembler
    
    // PExt!
    
    // Nodetable to use BPlusTrees 
    // Complete refactoring for 96 bit ids.
    
    // BPT testing
    // Use with assembler and explicit indexes
    
    // removeAll implementation: depends on iterator.remove
    // but can do fatser as a specific operation.
    
    // Query union models << architecture
    
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
