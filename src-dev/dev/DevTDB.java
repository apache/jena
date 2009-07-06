/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevTDB
{
    // ** metadata files and BPT creation.
    
    
    // == 0.8.2
    // Need (more) tests for symUnionDefaultGraph
    // ** Document concurrency.
    // ** Move DatasetGraphBase to ARQ
    // ** Check for tests of assembler and GraphTDB
    // Enable metadata?
    // ** New ARQ build (arq-extra?)
    //    Commands need moving.
    //      EarlReport
    //      Update ivy
    
    // ------
    // NodeTable as ( Index<Node, NodeID>, Index<NodeId, Node> )
    //    Assumes variable length records
    //    Need better var index support first
    
    // BDB-JE? BlockMgr as index?
    
    // Document concurrency 
    // Graph.getLock in Jena?  Share with dataset.
    // Remove all relics of creating graphs without a dataset (FactoryGraphTDB._createGraph())
    
    // ----
    
    // Clean up BPlusTree creation.  Currently need different blockmgrs for each B+T nodes, leaves. 
    // Ability to overwrite.
    
    // ** Use PrefixMappingPersistent with DatasetPrefixes. 

    // ==== Build
    // Multiple artifacts : macro for tasks.
    // Compile tests separately from main codebase.
    // Main code to a jar; test remain in classes-test
    //   Path for src->artifact and src->NotArtifact
    
    // Copy with no sources, no javadoc for auxillary jars
    // Confs - simplify "main"
    // Rewrite build-lib to use a macro for publishing - dev and main are then calls with a resolver argument.
    // Rename resolvers consistently in ivysettings.xml
    
    // == tdbdump && tdbrestore
    // FileSetMetadata - const names in sys.Names (currently in BPlusTreeParams)
    // See IndexFactoryBPlusTree.createRangeIndex
    // See BPlusTreeParams.readMeta

    // To ARQ:
    //   DatasetGraphBase
    //   Atlas? Iterator.
    
    // NodeLib.encode/decode ==> swap to a Nodec

    // Version of BufferingWriter that works on OutputStreams.

    //  Reopenable files.
    //    Alterntaive length hash codes.
    //    Record lengths
    
    //   Quad loader; dataset merge.
    //   Dataset management??
    
    // ---- Optimizer
    //   The disjunction/UNION rewrite (ARQ)
    
    // ---- Documentation
    //  Concurrency policy
    //  Change assembler page to emphasise creating a dataset.
    
    // ---- BlockMgrs
    // Consistency - do not manage in block managers except where MRSW not safe.
    // TupleTable.size - at least an isEmpty 
    // ==> Reopenable
    // ==> .release(id)
    // ==> Accurate size (?? meaningful beyond isEmpty/notEmpty?)
    // ==> Metablocks.
    //   ==> Moveable roots.
    
    // ---- Misc
    // Inlines => Inline56, Inline64

    // ---- tdbloader: 
    //   ** (script) to work on gzip files

    // ---- 32 bit mode.
    // Different caching schemes.
}
