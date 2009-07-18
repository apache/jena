/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev ;

import static com.hp.hpl.jena.tdb.TDB.logExec;
import static com.hp.hpl.jena.tdb.TDB.logInfo;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexQuadRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexTripleRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import atlas.lib.ColumnMap;
import atlas.lib.FileOps;

import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.FileFactory;
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
import com.hp.hpl.jena.tdb.index.TupleIndexBuilder;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBPlusTree;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBTree;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryExtHash;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableBase;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB;
import com.hp.hpl.jena.tdb.store.QuadTable;
import com.hp.hpl.jena.tdb.store.TripleTable;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Makes things: datasets from locations, indexes */

public class ThingBuilder implements DatasetGraphMakerTDB
{
    // TODO BlockSize for indexes/rangeIndexes
    // TODO TDBFactoryGraph/ - this is a replacement.
    // XXX IndexBuilder.get() and name of range index type.
    // TODO Tests.
    // TODO TDBMaker.ConcreteImplFactory
    // TODO remove constructors (e.g. DatasetPrefixesTDB) that encapsulate the choices).  DI!

    
    // TDBFactory : machinary for the API (models, lots of different ways of
    // making things
    // --> factory.createDatasetGraph(Location) / factory.createDatasetGraph()

    // The main factory is ConcreteImplFactory
    // --> FactoryGraphTDB.createDatasetGraph(Location) /
    // FactoryGraphTDB.createDatasetGraphMem(

    // ---- Record factories
    public final static RecordFactory indexRecordTripleFactory = new RecordFactory(LenIndexTripleRecord, 0) ;
    public final static RecordFactory indexRecordQuadFactory   = new RecordFactory(LenIndexQuadRecord, 0) ;
    public final static RecordFactory nodeRecordFactory        = new RecordFactory(LenNodeHash, SizeOfNodeId) ;

    private static final Logger       log                      = LoggerFactory.getLogger(ThingBuilder.class) ;

    // Sort out with IndexBuilder and ...tdb.index.factories.* when ready.
    // FactoryGraphTDB
    // TDBFactory

    // ---- Impl interface
    public DatasetGraphTDB createDatasetGraph()
    {
        return buildDataset(Location.mem()) ;
    }

    public DatasetGraphTDB createDatasetGraph(Location location)
    {
        return buildDataset(location) ;
    }

    public void releaseDatasetGraph(DatasetGraphTDB dataset)
    {}

    // ---- Impl interface

    public static DatasetGraphTDB buildDataset(Location location)
    {
        MetaFile metafile = location.getMetaFile() ;
        if (metafile.existsMetaData())
        {
            String verString = metafile.getProperty(Names.kVersion, "unknown") ;
            TDB.logInfo.debug("Location: " + location.toString()) ;
            TDB.logInfo.debug("Version:  " + verString) ;
        }

        // ---- Any files at this location?

        if (!FileOps.existsAnyFiles(location.getDirectoryPath()))
        {
            // Fresh location.
            metafile.setProperty(Names.kVersion, TDB.VERSION) ;
            metafile.setProperty(Names.kCreatedDate, Utils.nowAsXSDDateTimeString()) ;
            locationMetadata(location) ;
        } else
        {
            // Existing location (has some files in it) but no metadata.
            // Fake it as TDB 0.8.1 (which did not have metafiles)
            // If it's the wrong file format, things do badly wrong later.
            if (!metafile.hasProperty(Names.kVersion)) 
                metafile.setProperty(Names.kVersion, "<=0.8.1") ;
        }

        // ---- Node Table.
        NodeTable nodeTable = makeNodeTable(location, Names.indexNode2Id, Names.indexId2Node) ;

        // ---- Triple table and quad table indexes.
        // IndexBuilder that groks
        IndexBuilder dftIndexBuilder = IndexBuilder.get() ;

        TupleIndex tripleIndexes[] = indexes(dftIndexBuilder, location, indexRecordTripleFactory,
                                             Names.primaryIndexTriples, Names.tripleIndexes) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, indexRecordTripleFactory, nodeTable, location) ;

        TupleIndex quadIndexes[] = indexes(dftIndexBuilder, location, indexRecordQuadFactory, Names.primaryIndexQuads,
                                           Names.quadIndexes) ;
        QuadTable quadTable = new QuadTable(quadIndexes, indexRecordQuadFactory, nodeTable, location) ; ;

        // ---- Prefixes
        DatasetPrefixStorage prefixes = makePrefixes(location) ;

        // ---- Create the DatasetGraph object
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, chooseOptimizer(location), location) ;

        // Finalize
        metafile.flush() ;
        return dsg ;
    }

    public static TupleIndex[] indexes(final IndexBuilder indexBuilder, final Location location,
                                       RecordFactory recordFactory, String primary, String[] descs)
    {
        TupleIndexBuilder builder = new TupleIndexBuilder() {
            // @Override
            public TupleIndex create(String primary, String desc, RecordFactory recordFactory)
            {
                return createTupleIndex(indexBuilder, recordFactory, location, primary, desc) ;
            }
        } ;

        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        int i = 0 ;
        for (String desc : descs)
        {
            indexes[i] = builder.create(primary, desc, recordFactory) ;
            i++ ;
        }
        return indexes ;
    }

    private static TupleIndex createTupleIndex(IndexBuilder indexBuilder, RecordFactory recordFactory,
                                               Location location, String primary, String desc)
    {
        // Map name of index to name of file.
        FileSet fileset = new FileSet(location, desc) ;
        RangeIndex rIdx1 = indexBuilder.newRangeIndex(fileset, recordFactory) ;
        TupleIndex tupleIndex = new TupleIndexRecord(desc.length(), new ColumnMap(primary, desc), recordFactory, rIdx1) ;
        return tupleIndex ;
    }

    // ---- Make things.
    // All the make* operations look for metadata and decide what to do.

    private static NodeTable makeNodeTable(Location location, String indexNode2Id, String indexId2Node)
    {
        if (location.isMem()) return NodeTableFactory.createMem(IndexBuilder.mem()) ;

        String nodeTableType = location.getMetaFile().getProperty(Names.kNodeTableType) ;

        // NodeTableBuilder??

        if (nodeTableType != null)
        {
            log.info("Explicit node table type: " + nodeTableType + " (ignored)") ;
            // Choose node table builder.
        }
        else
        {
            // No type given.  Fill it in.
            location.getMetaFile().setProperty(Names.kNodeTableType, NodeTable.type) ;
            location.getMetaFile().setProperty(Names.kNodeTableLayout, NodeTable.layout) ;
        }
        
        // -- make node to id mapping -- Names.indexNode2Id
      
        FileSet fsIdToNode = new FileSet(location, indexId2Node) ;
        ObjectFile objectFile = makeObjectFile(fsIdToNode) ;

        // -- make id to node mapping -- Names.indexId2Node
        // Make index of id to node (data table): Names.nodeTable

        FileSet fsNodeToId = new FileSet(location, indexNode2Id) ;
        Index nodeToId = createIndex(fsNodeToId, nodeRecordFactory) ;
        
        // Make the node table using the components established above.
        NodeTable nodeTable = new NodeTableBase(nodeToId, objectFile, 
                                                SystemTDB.Node2NodeIdCacheSize,
                                                SystemTDB.NodeId2NodeCacheSize) ;

        return nodeTable ;
    }

    private static ObjectFile makeObjectFile(FileSet fsIdToNode)
    {
        checkMetadata(fsIdToNode.getMetaFile(), Names.kNodeTableType, NodeTable.type) ; 
        checkMetadata(fsIdToNode.getMetaFile(), Names.kNodeTableLayout, NodeTable.layout) ;
        
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objFile = FileFactory.createObjectFileDisk(filename);
        return objFile ;
    }

    private static DatasetPrefixStorage makePrefixes(Location location)
    {
        // TODO Dependency Injection
        return DatasetPrefixesTDB.create(location) ;
//        
//        NodeTable nodeTable =  makeNodeTable(location, Names.indexNode2Id, Names.indexId2Node) ;
//        TupleIndex prefixTupleIndex = 
//        DatasetPrefixStorage prefixes = null ; //new DatasetPrefixesTDB(location, prefixTupleIndex, nodeTable) ; 
//        return prefixes ;

    }
    
    public static void locationMetadata(Location location)
    {
        MetaFile metafile = location.getMetaFile() ;
        if (metafile.existsMetaData())
        {
            String verString = metafile.getProperty(Names.kVersion, TDB.VERSION) ;
            TDB.logInfo.debug("Location: " + location.toString()) ;
            TDB.logInfo.debug("Version:  " + verString) ;
            metafile.flush() ;
        }
    }

    public static Index createIndex(FileSet fileset, RecordFactory recordFactory)
    {
        return chooseIndexBuilder(fileset).newIndex(fileset, recordFactory) ;
    }
    
    public static RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
    {
        // Block size control?
        return chooseIndexBuilder(fileset).newRangeIndex(fileset, recordFactory) ;
    }
        // MESSY.
        
//        MetaFile metafile = fileset.getMetaFile() ;
//        if (metafile == null) metafile = fileset.getLocation().getMetaFile() ;
//
//        String keyRoot = Names.makeName(Names.keyNS, fileset.getBasename(), Names.elIndex) ;
//        String keyType = Names.makeName(keyRoot, Names.elType) ;
//        String keyLayout = Names.makeName(keyRoot, Names.elLayout) ;
//        
//        
//        // Anything already there?
//        if (metafile.existsMetaData())
//        {
//            // Check version.
//            String indexTypeStr = metafile.getProperty(keyType) ;
//            String fileLayout = metafile.getProperty(keyLayout) ;
//
//            if (indexTypeStr != null)
//            {
//                IndexType indexType = IndexType.get(indexTypeStr) ;
//                if (indexType == null) 
//                    throw new TDBException("Unknown index type: '" + indexTypeStr + "'") ;
//                log.debug("Index type: explicit setting: "+indexTypeStr) ;
//                return chooseIndexBuilder(indexType).newRangeIndex(fileset, recordFactory) ;
//            }
//            // Metadata - no keyIndexType - default.
//        }
//
//        // Default to B+Tree
//        metafile.setProperty(keyType, IndexType.BPlusTree.getName()) ;
//        IndexBuilder idxBuidler = chooseIndexBuilder(fileset) ;
//        
//        return createRangeIndex(fileset, idxBuilder, blockSize, recordFactory) ;
//    }

    // From TDBFactoryGraph
    private static ReorderTransformation chooseOptimizer(Location location)
    {
        if ( location == null )
            return ReorderLib.identity() ;
        
        ReorderTransformation reorder = null ;
        if ( location.exists(Names.optStats) )
        {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats)) ;
                logInfo.info("Statistics-based BGP optimizer") ;  
            } catch (SSEParseException ex) { 
                log.warn("Error in stats file: "+ex.getMessage()) ;
                reorder = null ;
            }
        }
        
        if ( reorder == null && location.exists(Names.optDefault) )
        {
            // Not as good but better than nothing.
            reorder = ReorderLib.fixed() ;
            logInfo.info("Fixed pattern BGP optimizer") ;  
        }
        
        if ( location.exists(Names.optNone) )
        {
            reorder = ReorderLib.identity() ;
            logInfo.info("Optimizer explicitly turned off") ;
        }
    
        if ( reorder == null )
            reorder = SystemTDB.defaultOptimizer ;
        
        if ( reorder == null )
            logExec.warn("No BGP optimizer") ;
        
        return reorder ; 
    }

    // From IndexBuilder.
    private static IndexBuilder chooseIndexBuilder(FileSet fileset)
    {
        String key = Names.makeKey(Names.keyNS, fileset.getBasename(), Names.elIndex, Names.elType) ;
        String x = fileset.getMetaFile().getProperty(key) ;
        
        if ( x == null )
        {
            // TODO Remove duplication 
            fileset.getMetaFile().setProperty(key, IndexType.BPlusTree.getName()) ;
            return IndexBuilder.get() ;   // Default.
        }

        IndexType indexType = IndexType.get(x) ;

        switch (indexType)
        {
            case BTree : {
                IndexFactoryBTree idx = new IndexFactoryBTree() ;
                return new IndexBuilder(idx, idx) ;
            }
            case BPlusTree : {
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree() ;
                return new IndexBuilder(idx, idx) ;
            }
            case ExtHash : {
                IndexFactoryExtHash idxFactory = new IndexFactoryExtHash() ;
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree() ;
                return new IndexBuilder(idxFactory, idx) ;
            }
        }
        throw new TDBException("Unrecognized index type: " + x) ;
    }

    private static void checkMetadata(MetaFile metafile, String key, String expected)
    {
        String value = metafile.getProperty(key) ;
        if ( value == null && expected == null ) return ;
        if ( value == null && expected != null ) inconsistent(key, value, expected) ; 
        if ( value != null && expected == null ) inconsistent(key, value, expected) ; 
        if ( ! value.equals(expected) )          inconsistent(key, value, expected) ;
        
    }

    private static void inconsistent(String key, String actual, String expected) 
    {
        String msg = String.format("Inconsistent: key=%s value=%s expected=%s", 
                                   key, 
                                   (actual==null?"<null>":actual),
                                   (expected==null?"<null>":expected) ) ;
        throw new TDBException(msg) ; 
    }
    
    public static RangeIndex createBPTree(FileSet fileset, MetaFile metafile, int order, int blockSize,
                                          RecordFactory factory)
    {
        // ---- Checking
        if (blockSize < 0 && order < 0) throw new IllegalArgumentException("Neither blocksize nor order specificied") ;
        if (blockSize >= 0 && order < 0) order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        if (blockSize >= 0 && order >= 0)
        {
            int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if (order != order2) throw new IllegalArgumentException("Wrong order (" + order + "), calculated = "
                                                                    + order2) ;
        }

        // Iffy - does not allow for slop.
        if (blockSize < 0 && order >= 0)
        {
            // Only in-memory.
            blockSize = BPlusTreeParams.calcBlockSize(order, factory) ;
        }

        BPlusTreeParams params = null ;
        // Params from previous settings
        if (metafile != null & metafile.existsMetaData())
        {
            // Check version.
            // String fileType = metafile.getProperty(Names.kIndexType) ;
            // String fileVersion =
            // metafile.getProperty(Names.kIndexFileVersion) ;

            // Put block size in BPTParams?
            log.debug("Reading metadata ...") ;
            BPlusTreeParams params2 = BPlusTreeParams.readMeta(metafile) ;

            int blkSize2 = metafile.getPropertyAsInteger(BPlusTreeParams.ParamBlockSize) ;
            log.info(String.format("Block size -- %d, given %d", blkSize2, blockSize)) ;
            log.info("Read: " + params2.toString()) ;

            if (blkSize2 != blockSize) log.error(String.format("Metadata declares block size to be %d, not %d",
                                                               blkSize2, blockSize)) ;
            // params = ...;
            // Check.
            params = params2 ;
        } else
        {
            params = new BPlusTreeParams(order, factory) ;
            metafile.setProperty(BPlusTreeParams.ParamBlockSize, blockSize) ;
            params.addToMetaData(metafile) ;
            metafile.flush() ;
        }

        log.info("Params: " + params) ;
        return null ;
    }

    static private RangeIndex createBPTreeRangeIndex(FileSet fileset, MetaFile metafile, int blockSize,
                                                  RecordFactory factory)
    {
        int order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;

        metafile.setProperty(Names.kIndexType, Names.currentIndexType) ;
        metafile.setProperty(Names.kIndexFileLayout, Names.currentIndexFileVersion) ;
        metafile.setProperty(BPlusTreeParams.ParamBlockSize, blockSize) ;

        params.addToMetaData(metafile) ;
        metafile.flush() ;
        return IndexBuilder.get().newRangeIndex(fileset, factory) ;
    }

    static private NodeTable createNodeTable(Index nodeToId, ObjectFile objectFile, int nodeToIdCacheSize,
                                             int idToNodeCacheSize)
    {
        // Check metadata.

        return new NodeTableBase(nodeToId, objectFile, nodeToIdCacheSize, idToNodeCacheSize) ;
    }
}
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */