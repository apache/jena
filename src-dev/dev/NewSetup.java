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
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.store.QuadTable;
import com.hp.hpl.jena.tdb.store.TripleTable;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Makes things: datasets from locations, indexes */

public class NewSetup implements DatasetGraphMakerTDB
{
    private static final Logger log = LoggerFactory.getLogger(NewSetup.class) ;

    /* Logical information goes in the location metafile. This includes
     * dataset type, NodeTable type and indexes expected.  But it does
     * not include how the particular files are realised.
     * 
     * A NodeTable is a pair of id->Node and Node->id mappings. 
     * 
     * An index file has it's own .meta file saying that it is a B+tree and
     * the record size - everything needed to access it to build a RangeIndex.
     * The individual node table files are the same.  This means we can
     * open a single index or object file (e.g to dump) and it allows
     * for changes both in implementation technology and in overall design. 
     */
    
    // Maker at a place: X makeX(FileSet, MetaFile?, defaultBlockSize, defaultRecordFactory,
    
    // TODO BlockSize for indexes/rangeIndexes
    // TODO TDBFactoryGraph/ - this is a replacement.
    // XXX IndexBuilder that understand metadata?
    // TODO Tests.
    // TODO remove constructors (e.g. DatasetPrefixesTDB) that encapsulate the choices).  DI!
    // TODO Check everywhere else for non-DI constructors.
    
    // TDBFactory : machinary for the API (models, lots of different ways of making things)
    // --> factory.createDatasetGraph(Location) / factory.createDatasetGraph()

    // The main factory is ConcreteImplFactory
    // --> FactoryGraphTDB.createDatasetGraph(Location) /
    // FactoryGraphTDB.createDatasetGraphMem(

    // ---- Record factories
    public final static RecordFactory indexRecordTripleFactory  = new RecordFactory(LenIndexTripleRecord, 0) ;
    public final static RecordFactory indexRecordQuadFactory    = new RecordFactory(LenIndexQuadRecord, 0) ;
    public final static RecordFactory nodeRecordFactory         = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
    public final static RecordFactory prefixNodeFactory         = new RecordFactory(3*NodeId.SIZE, 0) ;
    
    // Sort out with IndexBuilder and ...tdb.index.factories.* when ready.
    // FactoryGraphTDB
    // TDBFactory

    // << Impl interface
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

    // >> Impl interface

    // And here we make datasets ... 
    public static DatasetGraphTDB buildDataset(Location location)
    {
        // XXX Check names with "Names" - separate out? 
        
        /* ---- this.meta - the logical structure of the dataset.
         * 
         * # Dataset design
         * tdb.create.version=0.9           # TDB version that created this dataset originally.  0.8 for pre-meta.
         * tdb.layout=v1                    # "version" for the design)
         * tdb.type=standalone              # --> nodes-and-triples/quads
         * tdb.created=                     # Informational timestamp of creation.
         * 
         * # Triple table
         * # Changing the indexes does not automatically change the indexing on the dataset.
         * tdb.indexes.triples.primary=SPO  # triple primary
         * tdb.indexes.triples=SPO,POS,OSP  # triple table indexes
         * 
         * # Quad table.
         * tdb.indexes.quads.primary=GSPO   # Quad table primary.
         * tdb.indexes.quads=GSPO,GPOS,GOSP,SPOG,POSG,OSPG  # Quad indexes
         *
         * # Node table.
         * tdb.nodetable.mappings=node2id,id2node
         *
         * and then for each file we have the concrete parameters for the file:
         * 
         * ---- An index
         * 
         * tdb.file.type=rangeindex        # Service provided.
         * tdb.file.impl=bplustree         # Implementation
         * tdb.file.impl.version=v1          
         * 
         * tdb.index.name=SPO
         * tdb.index.order=SPO
         *
         * tdb.bpt.record=24,0
         * (tdb.bpt.order=)
         * tdb.bpt.blksize=
         * 
         * ---- An object file
         *
         * tdb.file.type=object
         * tdb.file.impl=dat
         * tdb.file.impl.version=v1
         *
         * tdb.object.encoding=sse
         */ 
        
        // Check and set defaults.
        // On return, can just read the metadata key/value. 
        MetaFile metafile = locationMetadata(location) ;
        
        // Only support this so far.
        if ( ! propertyEquals(metafile, "tdb.layout", "v1") )
        {
            log.error("Not marked as a v1 layout") ;
            throw new TDBException("Wrong layout: "+metafile.getProperty("tdb.layout")) ;
        }
            
        if ( propertyEquals(metafile, "tdb.type", "standalone") )
        {
            log.error("Not marked as a standalone type") ;
            throw new TDBException("Wrong type: "+metafile.getProperty("tdb.type")) ;
            
        }
        
        
        // ---- Logical structure

        // -- Node Table.
        NodeTable nodeTable = makeNodeTable(location, Names.indexNode2Id, Names.indexId2Node) ;

        // -- Triple table
        IndexBuilder dftIndexBuilder = IndexBuilder.get() ;
        
        
        
        // -- Quad Table
        

        TupleIndex tripleIndexes[] = indexes(dftIndexBuilder, location, indexRecordTripleFactory,
                                             Names.primaryIndexTriples, Names.tripleIndexes) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, indexRecordTripleFactory, nodeTable, location) ;

        TupleIndex quadIndexes[] = indexes(dftIndexBuilder, location, indexRecordQuadFactory, Names.primaryIndexQuads,
                                           Names.quadIndexes) ;
        QuadTable quadTable = new QuadTable(quadIndexes, indexRecordQuadFactory, nodeTable, location) ; ;

        // ---- Prefixes
        DatasetPrefixStorage prefixes = makePrefixes(dftIndexBuilder, location) ;

        // ---- Create the DatasetGraph object
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, chooseOptimizer(location), location) ;

        // Finalize
        metafile.flush() ;
        return dsg ;
    }

    // XXX To do
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

    public static NodeTable makeNodeTable(Location location, String indexNode2Id, String indexId2Node)
    {
        if (location.isMem()) 
            return NodeTableFactory.createMem(IndexBuilder.mem()) ;

            //?? Check the object file style for this location.
//      checkMetadata(fsIdToNode.getMetaFile(), Names.kNodeTableType, NodeTable.type) ; 
//      checkMetadata(fsIdToNode.getMetaFile(), Names.kNodeTableLayout, NodeTable.layout) ;

        
        String nodeTableType = location.getMetaFile().getProperty(Names.kNodeTableType) ;

        // NodeTableBuilder abstraction?

        if (nodeTableType != null)
        {
            if ( ! nodeTableType.equals(NodeTable.type))
                log.info("Explicit node table type: " + nodeTableType + " (ignored)") ;
        }
        else
        {
            // No type given.  Fill it in.
            location.getMetaFile().setProperty(Names.kNodeTableType, NodeTable.type) ;
            location.getMetaFile().setProperty(Names.kNodeTableLayout, NodeTable.layout) ;
        }
        
        // -- make id to node mapping -- Names.indexId2Node
        FileSet fsIdToNode = new FileSet(location, indexId2Node) ;
        ObjectFile objectFile = makeObjectFile(fsIdToNode) ;

        
        // -- make node to id mapping -- Names.indexNode2Id
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
        String objectTableType = ObjectFile.type ;
        
        checkOrSetMetadata(fsIdToNode.getMetaFile(), Names.kObjectTableType, objectTableType) ;
        
//        checkMetadata(fsIdToNode.getMetaFile(), Names.kNodeTableType, NodeTable.type) ; 
//        checkMetadata(fsIdToNode.getMetaFile(), Names.kNodeTableLayout, NodeTable.layout) ;
        
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objFile = FileFactory.createObjectFileDisk(filename);
        return objFile ;
    }

    private static DatasetPrefixStorage makePrefixes(IndexBuilder indexBuilder, Location location)
    {
        TupleIndex prefixIndexes[] = indexes(indexBuilder, location, prefixNodeFactory, 
                                             Names.primaryIndexPrefix, Names.prefixIndexes) ;
        NodeTable nodeTable =  makeNodeTable(location, Names.prefixNode2Id, Names.prefixId2Node) ;
        return new DatasetPrefixesTDB(prefixIndexes, nodeTable) ;
    }
    
    /** Check and set default for the dataset design */
    public static MetaFile locationMetadata(Location location)
    {
        boolean newDataset = FileOps.existsAnyFiles(location.getDirectoryPath()) ; 

        MetaFile metafile = location.getMetaFile() ;
        boolean isPreMetadata = false ;
        
        if (!newDataset && metafile.existsMetaData())
        {
            // Existing metadata
            String verString = metafile.getProperty(Names.kVersion, "unknown") ;
            TDB.logInfo.debug("Location: " + location.toString()) ;
            TDB.logInfo.debug("Version:  " + verString) ;
        }
        else
        {
            // No metadata
            // Either it's brand new (so set the defaults)
            // or it's a pre-0.9 dataset (files exists)

            if ( ! newDataset )
            {
                // Well-know name of the primary triples index.
                boolean b = FileOps.exists(location.getPath("SPO.dat")) ;
                if ( !b )
                {
                    log.error("Existing files but no metadata and not old-style fixed layout") ;
                    throw new TDBException("Can't build dataset: "+location) ;
                }
                
                isPreMetadata = true ;
            }
        }
            
        // Ensure defaults.
        
        if ( newDataset )
        {
            ensurePropertySet(metafile, "tdb.create.version", TDB.VERSION) ;
            ensurePropertySet(metafile, "tdb.created", Utils.nowAsXSDDateTimeString()) ;
        }
        
        if ( isPreMetadata )
        {
            // Existing location (has some files in it) but no metadata.
            // Fake it as TDB 0.8.1 (which did not have metafiles)
            // If it's the wrong file format, things do badly wrong later.
            ensurePropertySet(metafile, "tdb.create.version", "0.8") ;
            metafile.setProperty(Names.kCreatedDate, Utils.nowAsXSDDateTimeString()) ;
        }
            
        ensurePropertySet(metafile, "tdb.layout", "v1") ;
        ensurePropertySet(metafile, "tdb.type", "standalone") ;
        
        String layout = metafile.getProperty("tdb.layout") ;
        
        if ( layout.equals("v1") )
        {
            ensurePropertySet(metafile, "tdb.indexes.triples.primary", "SPO") ;
            ensurePropertySet(metafile, "tdb.indexes.triples", "SPO,POS,OSP") ;
            ensurePropertySet(metafile, "tdb.indexes.quads.primary", "GSPO") ;
            ensurePropertySet(metafile, "tdb.indexes.quads", "GSPO,GPOS,GOSP,SPOG,POSG,OSPG") ;
            ensurePropertySet(metafile, "tdb.nodetable.mappings", "node2id,id2node") ;
        }
        
        metafile.flush() ;
        return metafile ; 
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

    private static boolean propertyEquals(MetaFile metafile, String key, String value)
    {
        return metafile.getProperty(key).equals(value) ;
    }

    private static void ensurePropertySet(MetaFile metafile, String key, String expected)
    {
        getOrSetDefault(metafile, key, expected) ;
    }

    // Get property - set the defaultvalue if not present.
    private static String getOrSetDefault(MetaFile metafile, String key, String expected)
    {
        String x = metafile.getProperty(key) ;
        if ( x == null )
        {
            metafile.setProperty(key, expected) ;
            x = expected ;
        }
        return x ;
    }
    
    // Check property is an expected value or set if missing
    private static void checkOrSetMetadata(MetaFile metafile, String key, String expected)
    {
        String x = metafile.getProperty(key) ;
        if ( x == null )
        {
            metafile.setProperty(key, expected) ;
            return ; 
        }
        if ( x.equals(expected) )
            return ;
        
        inconsistent(key, x, expected) ; 
    }
    
    private static void checkMetadata(MetaFile metafile, String key, String expected)
    {
        //log.info("checkMetaData["+key+","+expected+"]") ;
        
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