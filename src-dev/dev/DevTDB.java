package dev ;




public class DevTDB

{
    // 1M flush on secondaries (primary as well?)
    //  LoaderNodeTupleTable.load() [primary]
    //  BuilderSecondaryIndexesSequential.createSecondaryIndexes[secondary]
    //   ==> LoaderNodeTupleTable.copyIndex
    
    // Query timeout
    
    // Bug:??
    // DSG.contains => DSG.getGraph 
    // DSG.getGraph can create the node so is not a read-only operation.
    //   Change to delay creating the NodeId until add/delete change
    //   and otherwise is null/unknown.
    
    // Integrate TestDatasetConfig (any other union tests?)
    // To ARQ for union rewrite tests as a base test.
    
    // Document: new events
    // http://openjena.org/wiki/TDB/JavaAPI#Concurrency
    // http://openjena.org/wiki/TDB/Concurrency
    // and ref to ARQ.
    // http://openjena.org/wiki/TDB/Assembler to document unionDefaultGraph
    
    // --explain.
    // http://openjena.org/wiki/TDB/Configuration
    // http://openjena.org/wiki/TDB/Optimizer
    // Update: http://openjena.org/wiki/TDB/Optimizer#Investigating_what_is_going_on
    
    //** Union graph in assembler.
    // Document ; not for default model; test [done?]
    // DateTimeNode.unpack : not fixed 3 digit fractional seconds.
    
    // == Misc
    //    Tidy up wiki (esp "Use from java" - use datasets, show loader use)
    //    TDBLoader from a dataset (or is this RIOT? or a general "Loader"?)
    //    TDBLoader javadoc - class info as well.
    
    // Inference pipeline
    // TDB+Inference
    //   TDB/BulkLoader.loadTriples$ (loadQuads$)
    //     to add a inference sink wrapper.
 
    // TDBFactory and Dataset.close().
    //   Can we check .close has done everything, flushes and all.
    //   Reference counting? 
    //   Document better
    
    // BPT internal consistency checks.
    //   Level count added as block read in.
    //   Leaf thread to check increasing keys.
    
    // Missing tests for DatasetGraphTDB?
    
    // Check writing to union graph is handled appropriately.
    
    // BPT rewriters
    
    // kill -9 ism's
    
    // Loader stats numbers of quads+triples
    
    // Configuring for union graph in assembler/location
    
    // retire: GraphStoreUtils.action
    
    // Expose a tream interface to the bulk loader.
    
    // =====
    // Enable FILTER assignment for strings (and numbers?) via dataset context setting?
    // Tuples
    // Interface, TupleFactory, TupleImpl
    // TupleMask (or TupleImpl itself has a length field?? TupleMask(T[] or Tuple<T>)) 
    // Sort : with colmap?
    // TupleSlice
    
    // Build: copy/filter a file, don't inline properties file.
    
    // Free block recycling.
    
    // 2 store dataset
    // Compact B+Trees
    // ---- Core system

    // ---- Pipeline
    // Weak inferencing (in query rewriting):
    // owl:equivalentClass, owl:equivalentProperty,
    // owl:inverseOf, owl:SymmetricProperty, owl:TransitiveProperty
    // rdfs:subClassOf (aux table).
    // rdfs:subPropertyOf (aux table).
    
    // Stats and datasets
    //   tdbstats
    
    // Early truncation of patterns
    //  ?s <p> <foo> . ?s <q> ?v . ?s <r> ?x
    // Favour connected next triple pattern (but grounding makes this less relevant) 
    // If <r> has no solutions fall back to triple pattern 1
    // Just need to keep var -> first def mapping but all mentions may be useful. 
    // (Idea from Alisdair)
    
    // In-JVM caches - make a function of heap size. (stil not perfect)
    // Settable in this.info.
    //   NodeCache
    //   Block cache for 32 bit
    //   Run with 32 bit block cache on 64 bit large machine to measure difference.

    // ** Advanced block work - free chain management.
    // Where does the head of the free chain go?  In .info? c.f. moving root. In root link field?!
    //   Separate control file (allows swicth bewteen two very different block files)
    //   Header block (a bit big!)
    //   Virtual to physical block system. **
    // Block* is currently physical blocks.
    // VBlocks.
    
    // Negative count? No 
    // Block is currently (B+Tree block)
    //  final public static int COUNT      = 0 ;
    //  final public static int LINK       = 4 ;
    // HashBucket
    //   final public static int TRIE        = COUNT+4 ;
    //   final public static int BITLEN      = TRIE+4 ;
    // On-disk: first 4 bytes is type << 24 | count
    //   ?? Allocate a type for a free block.
    //   ?? Maintain a free block disk
    //   ==> recordfile level.
    
    // NodeId:  
    // Bit 0: 0 - 63 bits of id (hash!) or block allocation for cluster.
    // Bit 0: 1 - inline
    // Schema compatibility needs to handle this carefully.
    
    // Case canonicalized lang tags? Affects hashing.
    
    // NodeTableFactory and SetupTDB.makeNodeTable have common code. 
    //   Remove NodeTableFactory and have one per-technology setup/maker
    
    // ** Grand roll out
    //    atlas to atlas
    //    riot to riot
    //        ARQ, PrefixMapping=>PrefixMap, Prologue change.
    //    FmtUtils and NodeFmtLib 
    
    // Sort out NodecSSE and NodecLib
    
    // === Projects
    // -> Stopping long running queries - 
    //    hook in BGP/Quad patterns
    // -> BDB-JE & transactions
    // -> BDB-JE and compressed blocks.
    
    // Sort out DatasetGraphMakerTDB -> One type, not thing+mem.
    //   Remove FactoryGraphTDB
    //   IndexMakers?
    // ?? DatasetGraphSetupMem == TDBMakerFactoryGraphMem

    // ----
    // B+Tree checking utility.
    // Dataset checking utility.

    // IndexFactory understanding index type name
    //    Registry<String->T>("bplustree", IndexBuilder)
    
    // == Misc
    // Node cache on 64bit machines needs to be bigger or rebalence
    // Cache stats counters (prep for JMX but useful now)
    
    // ---- Optimizer
    
    // ---- Documentation
    
    // ---- Misc
    // Inlines => Inline56, Inline64, ??
}
