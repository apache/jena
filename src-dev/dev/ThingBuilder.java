/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexQuadRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexTripleRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import atlas.lib.FileOps;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.file.MetaFile;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.graph.DatasetPrefixStorage;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.IndexType;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBPlusTree;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBTree;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryExtHash;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableBase;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.QuadTable;
import com.hp.hpl.jena.tdb.store.TripleTable;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Makes things: datasets from locations, indexes */

public class ThingBuilder
{
    // TDBFactory : machinary for the API (models, lots of different ways of making things
    //  --> factory.createDatasetGraph(Location) / factory.createDatasetGraph()
    
    // The main factory is ConcreteImplFactory
    // --> FactoryGraphTDB.createDatasetGraph(Location) / FactoryGraphTDB.createDatasetGraphMem(
    
    // ---- Record factories
    public final static RecordFactory indexRecordTripleFactory = new RecordFactory(LenIndexTripleRecord, 0) ; 
    public final static RecordFactory indexRecordQuadFactory = new RecordFactory(LenIndexQuadRecord, 0) ; 
    public final static RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
    
    private static final Logger log = LoggerFactory.getLogger(ThingBuilder.class) ;

    // Sort out with IndexBuilder and ...tdb.index.factories.* when ready.
    // FactoryGraphTDB
    // TDBFactory
    
    // Assembler vs metafiles.
    public static DatasetGraphTDB buildDataset(Location location)
    {
        // Process: 
        //   Location Metadata
        //   Build node file
        //   Build indexes and tables
        //   Build prefixes
        //   Make the dataset
        
        MetaFile metafile = location.getMetaFile() ;
        if ( metafile.existsMetaData() )
        {
            String verString = metafile.getProperty(Names.keyVersion, "unknown") ;
            TDB.logInfo.debug("Location: "+location.toString()) ;
            TDB.logInfo.debug("Version:  "+verString) ;
        }
        
        // Any files at this location?
        
        if ( ! FileOps.existsAnyFiles(location.getDirectoryPath()) )
        {
            // Fresh location.
            metafile.setProperty(Names.keyVersion, TDB.VERSION) ;
            //metafile.setProperty(Names.keyVersion, Utils.nowAsXSDDateTimeString()) ;
            return createNew(location) ;
        }

        // Some files exists.
        
        // Existing location (has some files in it).
        // Existing files, no metadata.
        // Fake it as TDB 0.8.1 (which did not have metafiles
        // If it's the wrong file format, things do badly wrong later.
        
        if ( ! metafile.hasProperty(Names.keyVersion) )
        {
            metafile.setProperty(Names.keyVersion, "<=0.8.1") ;
            metafile.flush() ;
        }

        // ---- Node Table.
        NodeTable nodeTable = null ;
        
        // ---- Triple table and quad table indexes.
        IndexBuilder dftIndexBuilder = null ;
        
        TupleIndex tripleIndexes[] = indexes(dftIndexBuilder, location, indexRecordTripleFactory, 
                                             Names.primaryIndexTriples, Names.tripleIndexes) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, indexRecordTripleFactory, nodeTable, location) ;
        
        TupleIndex quadIndexes[] = indexes(dftIndexBuilder, location,  indexRecordQuadFactory, 
                                           Names.primaryIndexQuads, Names.quadIndexes) ;
        QuadTable quadTable = new QuadTable(quadIndexes, indexRecordQuadFactory, nodeTable, location) ; ;
        
        // Names.primaryIndexTriples / Names.tripleIndexes
        // Names.primaryIndexQuads / Names.quadIndexes 
        // RecordFactory.indexRecordTripleFactory <-- SystemTDB.LenIndexTripleRecord
        // RecordFactory.indexRecordQuadFactory <-- SystemTDB.LenIndexQuadRecord
        
        // ---- Node Table.
        
        // ---- Prefixes
        
        // RecordFactory nodeRecordFactory <-- new RecordFactory(LenNodeHash, SizeOfNodeId) ;
        
        DatasetPrefixStorage prefixes = null ;
        
        
        // ---- Create the DatasetGraph object
        return new DatasetGraphTDB(tripleTable, quadTable, prefixes, chooseOptimizer(location), location) ;
    }
    
    private static ReorderTransformation chooseOptimizer(Location location) { return null ; }
    
    public static TupleIndex[] indexes(final IndexBuilder indexBuilder, final Location location, RecordFactory recordFactory, String primary, String[] descs)
    {
        return null ;
        
    }
//        TupleIndexBuilder builder = new TupleIndexBuilder()
//        {
//            //@Override
//            public TupleIndex create(String primary, String desc, RecordFactory recordFactory)
//            {
//                return createTupleIndex(indexBuilder, recordFactory, location, primary, desc) ;
//            }
//        } ;
//        
//        TupleIndex indexes[] = new TupleIndex[descs.length] ;
//        int i = 0 ;
//        for ( String desc : descs )
//        {
//            indexes[i] = builder.create(primary, desc, recordFactory) ;
//            i++ ;
//        }
//        return indexes ;
//    }
//    
//    private static TupleIndex createTupleIndex(IndexBuilder indexBuilder, RecordFactory recordFactory, Location location, String primary, String desc)
//    {
//        // Map name of index to name of file.
//        FileSet fileset = new FileSet(location, desc) ;
//        RangeIndex rIdx1 = indexBuilder.newRangeIndex(fileset, recordFactory) ;
//        TupleIndex tupleIndex = new TupleIndexRecord(desc.length(), new ColumnMap(primary, desc), recordFactory, rIdx1) ; 
//        return tupleIndex ;
//    }
    
    // Decide on an index builder.
    private static IndexBuilder chooseIndexBuilder(Location location, String primary, String desc)
    {
        return null ;
    }
    
    public static DatasetGraphTDB createNew(Location location)
    {
        MetaFile metafile = location.getMetaFile() ;
        if ( metafile.existsMetaData() )
        {
            String verString = metafile.getProperty(Names.keyVersion, "unknown") ;
            TDB.logInfo.debug("Location: "+location.toString()) ;
            TDB.logInfo.debug("Version:  "+verString) ;
        }
        
        // New createDatasetGraph(Location) 
        
        //return FactoryGraphTDB.createDatasetGraph(location) ;
        return null ;
    }
    
    // Properties.

    // General:
    //    tdb.version=
    //    tdb.indexType=
    //    tdb.indexFileVersion=
    
    // B+Tree:
    //    tdb.bptree.order=
    //    tdb.bptree.keyLength=
    //    tdb.bptree.valueLength=
    //    tdb.bptree.blockSize=
    
    // ExtHashTable
    //   tdb.exthash...
    
    // Cluster
    //   tdb.cluster....
    
    // IndexBuilder.createIndexBuilder(IndexType) is broken - fixed pairing. 
    
    // See SystemTDB.getIndexType()
    //   IndexBuilder.chooseIndexBuilder.
    //   Return IndexRangeFactory
    
    public static RangeIndex createRangeIndex(FileSet fileset, RecordFactory factory)
    {
        return createRangeIndex(fileset, SystemTDB.BlockSize, factory) ;
    }
    
    public static RangeIndex createRangeIndex(FileSet fileset, int blockSize, RecordFactory factory)
    {
        // This is the main worker function
        // 1 - Decide if there is an index already at that location (metafile)
        // 
        MetaFile metafile = fileset.getMetaFile() ;
        if ( metafile == null )
            metafile = fileset.getLocation().getMetaFile() ;
        
        // Anything already there?
        if ( metafile.existsMetaData() )
        {
            // Check version.
            String indexTypeStr = metafile.getProperty(Names.keyIndexType) ; 
            String fileVersion = metafile.getProperty(Names.keyIndexFileVersion) ;
            
            if ( indexTypeStr != null )
            {
                IndexType indexType = IndexType.get(indexTypeStr) ;
                if ( indexType == null )
                    throw new TDBException("Unknown uindex type: '"+indexTypeStr+"'") ;
                return chooseIndexBuilder(indexType).newRangeIndex(fileset, factory) ;
            }
            //Metadata - no keyIndexType - default.
        }
        
        // No metadata or unrelated metadata.  Create as if new (may attach to existing files).
        
        //return chooseIndexBuilder(IndexType.BPlusTree).newRangeIndex(fileset, factory) ;
        
        // No - call default.
        return createNewRangeIndex(fileset, metafile, blockSize, factory) ;
    }
    
    // From IndexBuilder.
    // TODO Check the IndexFcatories for metadata files
    private static IndexBuilder chooseIndexBuilder(IndexType indexType)
    {
        switch (indexType)
        {
            case BTree:
            {
                IndexFactoryBTree idx = new IndexFactoryBTree() ;
                return new IndexBuilder(idx, idx) ;
            }
            case BPlusTree:
            {
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree() ;
                return new IndexBuilder(idx, idx) ;
            }
            case ExtHash:
            {
                IndexFactoryExtHash idxFactory = new IndexFactoryExtHash() ;
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree() ;
                return new IndexBuilder(idxFactory, idx) ;
            }
        }
        throw new TDBException("Unrecognized index type: " + indexType) ;
    }
    
    public static RangeIndex createBPTree(FileSet fileset, MetaFile metafile, int order, int blockSize, RecordFactory factory)
    {
        // ----  Checking
        if ( blockSize < 0 && order < 0 )
            throw new IllegalArgumentException("Neither blocksize nor order specificied") ;
        if ( blockSize >= 0 && order < 0 )
            order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        if ( blockSize >= 0 && order >= 0 )
        {
            int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if ( order != order2 )
                throw new IllegalArgumentException("Wrong order ("+order+"), calculated = "+order2) ;
        }
        
        // Iffy - does not allow for slop. 
        if ( blockSize < 0 && order >= 0 )
        {
            // Only in-memory.
            blockSize = BPlusTreeParams.calcBlockSize(order, factory) ;
        }
        
        BPlusTreeParams params = null ;
        // Params from previous settings
        if ( metafile != null & metafile.existsMetaData() )
        {
            // Check version.
            //String fileType = metafile.getProperty(Names.keyIndexType) ; 
            //String fileVersion = metafile.getProperty(Names.keyIndexFileVersion) ;
            
            
            // Put block size in BPTParams?
            log.debug("Reading metadata ...") ;
            BPlusTreeParams params2 = BPlusTreeParams.readMeta(metafile) ;

            int blkSize2 = metafile.getPropertyAsInteger(BPlusTreeParams.ParamBlockSize) ;
            log.info(String.format("Block size -- %d, given %d", blkSize2, blockSize)) ;
            log.info("Read: "+params2.toString()) ;

            if ( blkSize2 != blockSize )
                log.error(String.format("Metadata declares block size to be %d, not %d", blkSize2, blockSize)) ;  
            // params = ...;
            // Check.
            params = params2 ;
        }
        else
        {
            params = new BPlusTreeParams(order, factory) ;
            metafile.setProperty(BPlusTreeParams.ParamBlockSize, blockSize) ;
            params.addToMetaData(metafile) ;
            metafile.flush();
        }

        log.info("Params: "+params) ;
        return null ;
    }
    
    static private  RangeIndex createNewRangeIndex(FileSet fileset, MetaFile metafile, int blockSize, RecordFactory factory)
    {
        int order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        
        metafile.setProperty(Names.keyIndexType, Names.currentIndexType) ; 
        metafile.setProperty(Names.keyIndexFileVersion, Names.currentIndexFileVersion) ;
        metafile.setProperty(BPlusTreeParams.ParamBlockSize, blockSize) ;
        
        params.addToMetaData(metafile) ;
        metafile.flush();
        // TODO integrate
        // TODO Test files existence
        return IndexBuilder.get().newRangeIndex(fileset, factory) ;
    }
    
    static private NodeTable createNodeTable(Index nodeToId, ObjectFile objectFile, int nodeToIdCacheSize, int idToNodeCacheSize)
    {
        // Check metadata.
        
        
        return new NodeTableBase(nodeToId, objectFile, nodeToIdCacheSize, idToNodeCacheSize) ;
    }
}
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */