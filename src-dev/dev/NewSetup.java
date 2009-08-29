/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev ;

import static com.hp.hpl.jena.tdb.TDB.logExec ;
import static com.hp.hpl.jena.tdb.TDB.logInfo ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import atlas.lib.ColumnMap ;
import atlas.lib.FileOps ;
import atlas.lib.StrUtils ;

import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.file.MetaFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.graph.DatasetPrefixStorage ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.IndexType ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBPlusTree ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBTree ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryExtHash ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableBase ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.QuadTable ;
import com.hp.hpl.jena.tdb.store.TripleTable ;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

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
    
    // Flushing of metefiles.
    
    // Maker at a place: X makeX(FileSet, MetaFile?, defaultBlockSize, defaultRecordFactory,
    // TODO Tests.
    // TODO remove constructors (e.g. DatasetPrefixesTDB) that encapsulate the choices).  DI!
    // TODO Check everywhere else for non-DI constructors.
    

    // ---- Record factories
//    public final static RecordFactory indexRecordTripleFactory  = new RecordFactory(LenIndexTripleRecord, 0) ;
//    public final static RecordFactory indexRecordQuadFactory    = new RecordFactory(LenIndexQuadRecord, 0) ;
    public final static RecordFactory nodeRecordFactory         = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
//    public final static RecordFactory prefixNodeFactory         = new RecordFactory(3*NodeId.SIZE, 0) ;
    
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
         * tdb.nodetable.mapping.node2id=node2id
         * tdb.nodetable.mapping.id2node=id2node
         *
         * # Prefixes.
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
            error("Excepted 'v1': Wrong layout: "+metafile.getProperty("tdb.layout")) ;
            
        if ( ! propertyEquals(metafile, "tdb.type", "standalone") )
            error("Not marked as a standalone type: "+metafile.getProperty("tdb.type")) ;
        
        // ---------------------
        
        // ---- Logical structure

        // -- Node Table.
        
        String indexNode2Id = get(metafile, "tdb.nodetable.mapping.node2id") ;
        String indexId2Node = get(metafile, "tdb.nodetable.mapping.id2node") ;
        log.info("Object table: "+indexNode2Id+" - "+indexId2Node) ;
        
        NodeTable nodeTable = makeNodeTable(location, indexNode2Id, indexId2Node) ;

        TripleTable tripleTable = makeTripleTable(location, nodeTable, Names.primaryIndexTriples, Names.tripleIndexes) ;
        QuadTable quadTable = makeQuadTable(location, nodeTable, Names.primaryIndexQuads, Names.quadIndexes) ;

        
//        // SWEEP
//        IndexBuilder dftIndexBuilder = IndexBuilder.get() ;
//
//        TupleIndex tripleIndexes[] = _indexes(dftIndexBuilder, location, indexRecordTripleFactory,
//                                             Names.primaryIndexTriples, Names.tripleIndexes) ;
//        TripleTable tripleTable = new TripleTable(tripleIndexes, indexRecordTripleFactory, nodeTable, location) ;
//
//        TupleIndex quadIndexes[] = _indexes(dftIndexBuilder, location, indexRecordQuadFactory, Names.primaryIndexQuads,
//                                           Names.quadIndexes) ;
//        QuadTable quadTable = new QuadTable(quadIndexes, indexRecordQuadFactory, nodeTable, location) ; ;

        // ---- Prefixes
        DatasetPrefixStorage prefixes = null ; //_makePrefixes(dftIndexBuilder, location) ;

        // ---- Create the DatasetGraph object
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, chooseOptimizer(location), location) ;

        // Finalize
        metafile.flush() ;
        return dsg ;
    }

    public static TripleTable makeTripleTable(Location location, NodeTable nodeTable, String dftPrimary, String[] dftIndexes)
    {
        MetaFile metafile = location.getMetaFile() ;
        String primary = getOrSetDefault(metafile, "tdb.indexes.triples.primary", dftPrimary) ;
        String x = getOrSetDefault(metafile, "tdb.indexes.triples", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;
        
        if ( indexes.length != 3 )
            error("Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.info("Triple table: "+primary+" :: "+StrUtils.join(",", indexes)) ;
        
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        if ( tripleIndexes.length != indexes.length )
            error("Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, location) ;
        metafile.flush() ;
        return tripleTable ;
    }
    
    public static QuadTable makeQuadTable(Location location, NodeTable nodeTable, String dftPrimary, String[] dftIndexes)
    {
        MetaFile metafile = location.getMetaFile() ; 
        String primary = getOrSetDefault(metafile, "tdb.indexes.quads.primary", dftPrimary) ;
        String x = getOrSetDefault(metafile, "tdb.indexes.quads", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;

        if ( indexes.length != 6 )
            error("Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.info("Quad table: "+primary+" :: "+StrUtils.join(",", indexes)) ;
        
        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        if ( quadIndexes.length != indexes.length )
            error("Wrong number of triple table tuples indexes: "+quadIndexes.length) ;
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, location) ;
        metafile.flush() ;
        return quadTable ;
    }



    public static TupleIndex[] makeTupleIndexes(Location location, String primary, String[] descs)
    {
        if ( primary.length() != 3 && primary.length() != 4 )
            error("Bad primary key length: "+primary.length()) ;

        int indexRecordLen = primary.length()*NodeId.SIZE ;
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        for (int i = 0 ; i < indexes.length ; i++)
            indexes[i] = makeTupleIndex(location, primary, descs[i], indexRecordLen) ;
        return indexes ;
    }
    
    private static TupleIndex makeTupleIndex(Location location, String primary, String indexName, int keyLength)
    {
        if ( primary.length() != indexName.length() )
            error("Bad index name length: primary="+primary+", index="+indexName) ;
        
        /*
        * tdb.file.type=rangeindex        # Service provided.
        * tdb.file.impl=bplustree         # Implementation
        * tdb.file.impl.version=v1          
        */

        FileSet fs = new FileSet(location, indexName) ;
        // Physical
        MetaFile metafile = fs.getMetaFile() ;
        checkOrSetMetadata(metafile, "tdb.file.type", "rangeindex") ;
        String indexType = getOrSetDefault(metafile, "tdb.file.impl", "bplustree") ;
        //checkOrSetMetadata(metafile, "tdb.file.impl.version", "v1") ;
        if ( ! indexType.equals("bplustree") )
        {
            log.error("Unknown index type: "+indexType) ;
            throw new TDBException("Unknown index type: "+indexType) ;
        }
        
        RangeIndex rIndex = makeRangeIndex(location, indexName, keyLength) ;
        TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexName), rIndex.getRecordFactory(), rIndex) ;
        return tupleIndex ;
    }
    
    private static RangeIndex makeRangeIndex(Location location, String indexName, int dftKeyLength)
    {
        /*
         * tdb.file.type=rangeindex        # Service provided.
         * tdb.file.impl=bplustree         # Implementation
         * tdb.file.impl.version=v1          
         */
         FileSet fs = new FileSet(location, indexName) ;
         // Physical
         MetaFile metafile = fs.getMetaFile() ;
         checkOrSetMetadata(metafile, "tdb.file.type", "rangeindex") ;
         String indexType = getOrSetDefault(metafile, "tdb.file.impl", "bplustree") ;
         //checkOrSetMetadata(metafile, "tdb.file.impl.version", "v1") ;
         if ( ! indexType.equals("bplustree") )
         {
             log.error("Unknown index type: "+indexType) ;
             throw new TDBException("Unknown index type: "+indexType) ;
         }
         RangeIndex rIndex =  makeBPlusTree(fs, dftKeyLength, 0) ;
         
         if ( rIndex.getRecordFactory().valueLength() != 0 )
             error("Value length not zero: "+rIndex.getRecordFactory().valueLength()) ;
         
         metafile.flush();
         return rIndex ;
    }
    
    private static RangeIndex makeBPlusTree(FileSet fs, int dftKeyLength, int dftValueLength)
    {
        // ---- BPlusTree
        // Get parameters.
        /*
         * tdb.bpt.record=24,0
         * tdb.bpt.blksize=
         * tdb.bpt.order=
         */
        
        MetaFile metafile = fs.getMetaFile() ;
        RecordFactory recordFactory = makeRecordFactory(metafile, "tdb.bpt.record", dftKeyLength, dftValueLength) ;
        
        String blkSizeStr = getOrSetDefault(metafile, "tdb.bpt.blksize", Integer.toString(SystemTDB.BlockSize)) ; 
        int blkSize = parseInt(blkSizeStr, "Bad block size") ;
        
        int calcOrder = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
        String orderStr = getOrSetDefault(metafile, "tdb.bpt.order", Integer.toString(calcOrder)) ;
        int order = parseInt(orderStr, "Bad order for B+Tree") ;
        if ( order != calcOrder )
            error("Wrong order (" + order + "), calculated = "+calcOrder) ;

        RangeIndex rIndex =  _createBPTree(fs, order, blkSize, recordFactory) ;
        metafile.flush() ;
        return rIndex ;
    }

    private static RecordFactory makeRecordFactory(MetaFile metafile, String keyName, int keyLenDft, int valLenDft)
    {
        String recSizeStr = null ;
        
        if ( keyLenDft >= 0 && valLenDft >= 0 )
        {
            String dftRecordStr = keyLenDft+","+valLenDft ;
            recSizeStr = getOrSetDefault(metafile, keyName, dftRecordStr) ;
        }
        else
            recSizeStr = get(metafile, keyName) ;
        
        if ( recSizeStr == null )
            error("Failed to get a record factory description from "+keyName) ;
        
        
        String[] recordLengths = recSizeStr.split(",") ;
        if ( recordLengths.length != 2 )
            error("Bad record length: "+recSizeStr) ;

        int keyLen = parseInt(recordLengths[0], "Bad key length ("+recSizeStr+")") ;
        int valLen = parseInt(recordLengths[1], "Bad value length ("+recSizeStr+")") ;
        
        return new RecordFactory(keyLen, valLen) ;
    }
    
    private static int parseInt(String str, String messageBase)
    {
        try { return Integer.parseInt(str) ; }
        catch (NumberFormatException ex) { error(messageBase+": "+str) ; return -1 ; }
    }
    
    // ---- Make things.
    // All the make* operations look for metadata and decide what to do.

    public static NodeTable makeNodeTable(Location location, String indexNode2Id, String indexId2Node)
    {
        if (location.isMem()) 
            return NodeTableFactory.createMem(IndexBuilder.mem()) ;

        /* Logical:
         * # Node table.
         * tdb.nodetable.mapping.node2id=node2id
         * tdb.nodetable.mapping.id2node=id2node
         * 
         * Physical:
         * 1- Index file for node2id
         * 2- Cached direct lookup object file for id2node
         *    Encoding. 
         */   
        
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
        //checkMetadata(fsIdToNode.getMetaFile(), /*Names.kNodeTableType,*/ NodeTable.type) ; 
        
        ObjectFile objectFile = makeObjectFile(fsIdToNode) ;
        
        // -- make node to id mapping -- Names.indexNode2Id
        // Make index of id to node (data table): Names.nodeTable

        FileSet fsNodeToId = new FileSet(location, indexNode2Id) ;
        Index nodeToId = makeBPlusTree(fsNodeToId, nodeRecordFactory.keyLength(), nodeRecordFactory.valueLength()) ;
        
        // Make the node table using the components established above.
        NodeTable nodeTable = new NodeTableBase(nodeToId, objectFile, 
                                                SystemTDB.Node2NodeIdCacheSize,
                                                SystemTDB.NodeId2NodeCacheSize) ;

        return nodeTable ;
    }

    public static ObjectFile makeObjectFile(FileSet fsIdToNode)
    {
        /* Physical
         * ---- An object file
         * tdb.file.type=object
         * tdb.file.impl=dat
         * tdb.file.impl.version=v1
         *
         * tdb.object.encoding=sse 
         */
        
        MetaFile metafile = fsIdToNode.getMetaFile() ;
        checkOrSetMetadata(metafile, "tdb.file.type", ObjectFile.type) ;
        checkOrSetMetadata(metafile, "tdb.file.impl", "dat") ;
        checkOrSetMetadata(metafile, "tdb.file.impl.version", "v1") ;
        checkOrSetMetadata(metafile, "tdb.object.encoding", "sse") ;
        
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objFile = FileFactory.createObjectFileDisk(filename);
        metafile.flush();
        return objFile ;
    }

    private static DatasetPrefixStorage _makePrefixes(IndexBuilder indexBuilder, Location location)
    {
//        TupleIndex prefixIndexes[] = _indexes(indexBuilder, location, prefixNodeFactory, 
//                                             Names.primaryIndexPrefix, Names.prefixIndexes) ;
//        NodeTable nodeTable =  makeNodeTable(location, Names.prefixNode2Id, Names.prefixId2Node) ;
//        return new DatasetPrefixesTDB(prefixIndexes, nodeTable) ;
        return null ;
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
            ensurePropertySet(metafile, "tdb.indexes.triples.primary", Names.primaryIndexTriples) ;
            ensurePropertySet(metafile, "tdb.indexes.triples", StrUtils.join(",", Names.tripleIndexes)) ;

            ensurePropertySet(metafile, "tdb.indexes.quads.primary", Names.primaryIndexQuads) ;
            ensurePropertySet(metafile, "tdb.indexes.quads", StrUtils.join(",", Names.quadIndexes)) ;
            
            ensurePropertySet(metafile, "tdb.nodetable.mapping.node2id", Names.indexNode2Id) ;
            ensurePropertySet(metafile, "tdb.nodetable.mapping.id2node", Names.indexId2Node) ;
        }
        
        metafile.flush() ;
        return metafile ; 
    }

//    public static Index createIndex(FileSet fileset, RecordFactory recordFactory)
//    {
//        return chooseIndexBuilder(fileset).newIndex(fileset, recordFactory) ;
//    }
//    
//    public static RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
//    {
//        // Block size control?
//        return chooseIndexBuilder(fileset).newRangeIndex(fileset, recordFactory) ;
//    }
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

    private static String get(MetaFile metafile, String key)
    {
        return metafile.getProperty(key) ;
    }
    
    private static String[] getSplit(MetaFile metafile, String key)
    {
        return metafile.getProperty(key).split(",") ;
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
    
    private static void error(String msg)
    {
        log.error(msg) ;
        throw new TDBException(msg) ;
    }
    
    public static RangeIndex _createBPTree(FileSet fileset, int order, int blockSize,
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

        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        String fnNodes = fileset.filename(Names.bptExt1) ;
        BlockMgr blkMgrNodes = BlockMgrFactory.createFile(fnNodes, blockSize) ;
        
        String fnRecords = fileset.filename(Names.bptExt2) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.createFile(fnRecords, blockSize) ;
        return BPlusTree.attach(params, blkMgrNodes, blkMgrRecords) ;
    }

//    static private RangeIndex createBPTreeRangeIndex(FileSet fileset, MetaFile metafile, int blockSize,
//                                                  RecordFactory factory)
//    {
//        int order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
//        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
//
//        metafile.setProperty(Names.kIndexType, Names.currentIndexType) ;
//        metafile.setProperty(Names.kIndexFileLayout, Names.currentIndexFileVersion) ;
//        metafile.setProperty(BPlusTreeParams.ParamBlockSize, blockSize) ;
//
//        params.addToMetaData(metafile) ;
//        metafile.flush() ;
//        return IndexBuilder.get().newRangeIndex(fileset, factory) ;
//    }

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