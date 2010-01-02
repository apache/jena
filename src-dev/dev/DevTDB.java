/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;


public class DevTDB
{
    // == 0.8.5
    // + Dynamic datasets
    // + Dataset management
    
    // Dataset.close() always calls TDBMaker.releaseDataset - shouldn't there be a reference count?
    
    // NodeFmtLib - expose safeBNodeLabel flag.
    // And a decode operation.
    
    // NodeId:  
    // Bit 0: 0 - 63 bits of id (hash!) or block allocation for cluster.
    // Bit 0: 1 - inline
    // Schema compatibility needs to handle this carefully.
    
    // Case canonicalized lang tags? Affects hashing.
    
    // Node hash is quite unsubtle (but schema change) 
    
    // Separate out an interface for UpdateTracking
    // start/stop update + start/stop read
    
    // NodeTableFactory and SetupTDB.makeNodeTable have common code. 
    //   Remove NodeTableFactory and have one per-technology setup/maker

    // -> BDB-JE and compressed blocks.
    
    // ** Grand roll out
    //    Atlas to atlas
    //    riot to ARQ, PrrefixMapping=>PrefixMap, Prologue change.
    //    FmtUtils and NodeFmtLib 
    
    // Sort out NodecSSE and NodecLib
    
    // **** Redo IndexBuilder and NodeTableBuilder (with caching
    //   ?? SetupTDB(IndexBuilder, NodeTableBuilder, PrefixTableBuilder) ;
    
    // Cache clear to call drop handlers on all cache implementations.
    
    // === Projects
    // -> Stopping long running queries - 
    //    hook in BGP/Quad patterns
    // -> Dynamic datasets
    // -> BDB-JE & transactions
    // -> BDB-JE and compressed blocks.
    
    // Where is IndexBuilder used now?
    // Interface is IndexFactory and includes the record factory.
    //  See SetupTDB.makeRangeIndex(Location location, String indexName, 
    // RangeIndexMaker.makeRangeIndex(location, name, dft k and v, cache sizes)
    // Meaning of cache sizes is per-type  
    
    // Need control over caching sizes etc.
    
    // Setup
    // Const for names.
    //    Remove SystemTDB.intValue and friends.
    // **** Parameterize by:
    //   NodeTableFactory
    //   BlockMgrFactory
    //   IndexFactory [dft to over BlockMgrFactory]
    //   RangeIndexFactory [dft to over BlockMgrFactory]

    // Document:
    //   Settings
    //   Better tables.

    // Sort out DatasetGraphMakerTDB -> One type, not thing+mem.
    //   Remove FactoryGraphTDB
    //   IndexMakers?
    // ?? DatasetGraphSetupMem == TDBMakerFactoryGraphMem

    // See metadata stuff in BPlusTreeParams
    // Test.
    
    // Rethink.check cache synchronization (ByteBuffer)
    //   RecordBufferPageMgr / BPTreeNodeMgr / BTreeNodeMgr / HashBucketMgr -> fromByteBuffer
    //   Why/what does the sync in fromByteBuffer do?
    // Why not BlockConverter.get on block mgr?  Hhow many blockMgrs are there?
    
    // TestCacheSet
    // Check node table use of caches
    // Don't print optimizer type when set at info level.

    // Special case
    // <s> p1 ?o1 ; p2 ?o2 ; p3 ?o3 ... and do ((<s> 0 0)->(<s>+1 0 0)]
    
    // == Atlas
    // Separate out atlas properly.
    
    // ----
    // B+Tree checking utility.
    // Dataset checking utility.

    // IndexFactory understanding index type name
    //    Registry<String->T>("bplustree", IndexBuilder)
    
    // Longer: packaging of TDB and Joseki for an unpack-and-go solution.
    // At least, specific documentation.
    
    // --- Setup
    // Check supressed deprecations and switch to Setup.*

    // == Misc
    // Node cache on 64bit machines needs to be bigger or rebalence
    // Cache stats counters (prep for JMX but useful now)
    
    // ==== RIOT
    // ** Connect the errorhandler set via JenaReaderbase to the one used by RIOT-Turtle/RIOT-NTriples.
    // Properties on the readers
    // Error handler, not exceptions, in Turtle.
    //   Check for continuation.
    // TriG
    
    // == tdbdump && tdbrestore
    // ---- Optimizer
    //   The disjunction/UNION rewrite (ARQ)
    
    // ---- Documentation
    
    // ---- BlockMgrs
    // Consistency - do not manage in block managers except where MRSW not safe.
    // TupleTable.size - at least an isEmpty 
    // ==> Reopenable
    // ==> .release(id)
    // ==> Accurate size (?? meaningful beyond isEmpty/notEmpty?)
    // ==> Metablocks.
    //   ==> Moveable roots.
    
    // ---- Misc
    // Inlines => Inline56, Inline64, ??

    // ---- tdbloader: 
    //   ** (script) to work on gzip files
}
