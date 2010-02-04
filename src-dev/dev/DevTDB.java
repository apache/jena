/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;


public class DevTDB
{
    // RIOT
    // Tests for N-Quads and Trig
    
    // **** Per dataset context.  Merge into execution.
    //   Set in assembler
    //   Global context for this as well TDB.getDatasetDefault() ;
    // 
    // In-JVM caches - make a function of heap size.
    // Settable in this.info.
    //   NodeCache
    //   Block cache for 32 bit
    //   Run with 32 bit block cache on 64 bit large machine to measure difference.
    
    // Special cases
    //   <s> p1 ?o1 ; p2 ?o2 ; p3 ?o3 ... and do ((<s> 0 0)->(<s>+1 0 0)]
    //   Materialized answers.

    // == 0.8.5
    // + Dynamic datasets  [DONE] 
    //   Documentation  [DONE] ?Check
    // ++ RDFS inference??
    
    // Terminating queries with abort top to bottom.
    
    // Dataset.close() always calls TDBMaker.releaseDataset - shouldn't there be a reference count?
    
    // NodeFmtLib - expose safeBNodeLabel flag.
    // And a decode operation.
    
    // NodeId:  
    // Bit 0: 0 - 63 bits of id (hash!) or block allocation for cluster.
    // Bit 0: 1 - inline
    // Schema compatibility needs to handle this carefully.
    
    // Case canonicalized lang tags? Affects hashing.
    
    // NodeTableFactory and SetupTDB.makeNodeTable have common code. 
    //   Remove NodeTableFactory and have one per-technology setup/maker
    
    // ** Grand roll out
    //    atlas to atlas
    //    riot to ARQ, PrrefixMapping=>PrefixMap, Prologue change.
    //    FmtUtils and NodeFmtLib 
    
    // Sort out NodecSSE and NodecLib
    
    // **** Redo IndexBuilder and NodeTableBuilder (with caching
    //   ?? SetupTDB(IndexBuilder, NodeTableBuilder, PrefixTableBuilder) ;
    
    // === Projects
    // -> Stopping long running queries - 
    //    hook in BGP/Quad patterns
    // -> BDB-JE & transactions
    // -> BDB-JE and compressed blocks.
    
    // Sort out DatasetGraphMakerTDB -> One type, not thing+mem.
    //   Remove FactoryGraphTDB
    //   IndexMakers?
    // ?? DatasetGraphSetupMem == TDBMakerFactoryGraphMem

    // Rethink/check cache synchronization (ByteBuffer)  [No outstanding reported problems]
    //   RecordBufferPageMgr / BPTreeNodeMgr / BTreeNodeMgr / HashBucketMgr -> fromByteBuffer
    //   Why/what does the sync in fromByteBuffer do?
    // Why not BlockConverter.get on block mgr?  Hhow many blockMgrs are there?
    
    // == Atlas
    // Separate out atlas properly.
    
    // ----
    // B+Tree checking utility.
    // Dataset checking utility.

    // IndexFactory understanding index type name
    //    Registry<String->T>("bplustree", IndexBuilder)
    
    // Longer: packaging of TDB and Joseki for an unpack-and-go solution.
    // At least, specific documentation.
    
    // == Misc
    // Node cache on 64bit machines needs to be bigger or rebalence
    // Cache stats counters (prep for JMX but useful now)
    
    // ==== RIOT
    // Properties on the readers
    // TriG
    
    // == tdbdump && tdbrestore
    // ---- Optimizer
    
    // ---- Documentation
    
    // ---- Misc
    // Inlines => Inline56, Inline64, ??

    // ---- tdbloader: 
    //   ** (script) to work on gzip files
    // See IO.openFile.
}
