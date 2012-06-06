/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tdb;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfLong ;

import java.io.InputStream ;
import java.io.StringWriter ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.tdb.store.bulkloader3.* ;
import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.data.* ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.* ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.lang.LabelToNode ;
import org.openjena.riot.lang.LangNQuads ;
import org.openjena.riot.lang.LangNTriples ;
import org.openjena.riot.out.NodeToLabel ;
import org.openjena.riot.out.OutputLangUtils ;
import org.openjena.riot.system.* ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import tdb.cmdline.CmdTDB ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriter ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkLoader ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class tdbloader3 extends CmdGeneral
{
    static { Log.setCmdLogging() ; }
    private static Logger cmdLog = LoggerFactory.getLogger(tdbloader3.class) ;

    private static String runId = String.valueOf(System.currentTimeMillis()) ; // a unique identifier for this run, it's used for blank node labels

    private static ArgDecl argLocation      = new ArgDecl(ArgDecl.HasValue, "loc", "location") ;
    private static ArgDecl argCompression   = new ArgDecl(ArgDecl.NoValue,  "comp", "compression") ;
    private static ArgDecl argBufferSize    = new ArgDecl(ArgDecl.HasValue, "buf", "buffer-size") ;
    private static ArgDecl argGzipOutside   = new ArgDecl(ArgDecl.NoValue,  "gzip-outside") ;
    private static ArgDecl argSpillSize     = new ArgDecl(ArgDecl.HasValue, "spill", "spill-size") ;
    private static ArgDecl argSpillSizeAuto = new ArgDecl(ArgDecl.NoValue,  "spill-auto", "spill-size-auto") ;
    private static ArgDecl argNoStats       = new ArgDecl(ArgDecl.NoValue,  "no-stats") ;
    private static ArgDecl argNoBuffer      = new ArgDecl(ArgDecl.NoValue,  "no-buffer") ;
    private static ArgDecl argMaxMergeFiles = new ArgDecl(ArgDecl.HasValue, "max-merge-files") ;

    private Location location ;
    private String locationString ;
    private List<String> datafiles ;
    public static int spill_size = 1000000 ;
    public static boolean spill_size_auto = false ;
    public static boolean no_stats = false ;
    
    private Comparator<Tuple<Long>> comparator = new TupleComparator();
    private TripleSerializationFactory tripleSerializationFactory = new TripleSerializationFactory() ;
    private QuadSerializationFactory quadSerializationFactory = new QuadSerializationFactory() ;
    
    public static void main(String...argv)
    {
        CmdTDB.init() ;
        TDB.setOptimizerWarningFlag(false) ;
        new tdbloader3(argv).mainRun() ;
    }
    
    public tdbloader3(String...argv)
    {
        super(argv) ;
        super.add(argLocation,      "--loc",               "Location") ;
        super.add(argCompression,   "--compression",       "Use compression for intermediate files") ;
        super.add(argBufferSize,    "--buffer-size",       "The size of buffers for IO in bytes") ;
        super.add(argGzipOutside,   "--gzip-outside",      "GZIP...(Buffered...())") ;
        super.add(argSpillSize,     "--spill-size",        "The size of spillable segments in tuples|records") ;
        super.add(argSpillSizeAuto, "--spill-size-auto",   "Automatically set the size of spillable segments") ;
        super.add(argNoStats,       "--no-stats",          "Do not generate the stats file") ;
        super.add(argNoBuffer,      "--no-buffer",         "Do not use Buffered{Input|Output}Stream") ;
        super.add(argMaxMergeFiles, "--max-merge-files",   "Specify the maximum number of files to merge at the same time (default: 100)") ;
    }
        
    @Override
    protected void processModulesAndArgs()
    {
        if ( !super.contains(argLocation) ) throw new CmdException("Required: --loc DIR") ;
        
        locationString   = super.getValue(argLocation) ;
        location = new Location(locationString) ;

        if ( super.hasArg(argSpillSize) ) 
            spill_size = Integer.valueOf(super.getValue(argSpillSize)) ;
        no_stats = super.hasArg(argNoStats) ;

        // this is to try different ways to create Input/Output streams
        DataStreamFactory.setUseCompression( super.hasArg(argCompression) ) ;
        DataStreamFactory.setGZIPOutside( super.hasArg(argGzipOutside) ) ;
        if ( super.hasArg(argBufferSize) ) 
            DataStreamFactory.setBufferSize( Integer.valueOf(super.getValue(argBufferSize)) ) ;
        DataStreamFactory.setBuffered( ! super.hasArg(argNoBuffer) ) ;
        if ( super.hasArg(argMaxMergeFiles) )
            MultiThreadedSortedDataBag.MAX_SPILL_FILES = Integer.valueOf(super.getValue(argMaxMergeFiles)) ;
        if ( super.hasArg(argSpillSizeAuto) ) 
            spill_size_auto = true ;
        
        datafiles  = super.getPositional() ;

        for( String filename : datafiles)
        {
            Lang lang = Lang.guess(filename, Lang.NQUADS) ;
            if ( lang == null )
                // Does not happen due to default above.
                cmdError("File suffix not recognized: " +filename) ;
            if ( ! FileOps.exists(filename) )
                cmdError("File does not exist: "+filename) ;
        }

    }
    
    private ThresholdPolicy<Tuple<Long>> getThresholdPolicy(SerializationFactory<Tuple<Long>> serializationFactory) {
        if ( spill_size_auto == true ) {
            long memory = Math.round( Runtime.getRuntime().maxMemory() * 0.065 ) ; // in bytes
            cmdLog.info("Threshold spill is: " + memory) ;
            return new ThresholdPolicyMemory<Tuple<Long>>(memory, serializationFactory);
        } else {
            return new ThresholdPolicyCount<Tuple<Long>>(spill_size);            
        }
    }
    
    @Override
    protected void exec()
    {
        // This formats the location correctly.
        DatasetGraphTDB dsg = SetupTDB.buildDataset(location) ;

        // so close indexes and the prefix table.
        dsg.getTripleTable().getNodeTupleTable().getTupleTable().close();
        dsg.getQuadTable().getNodeTupleTable().getTupleTable().close();
        // Later - attach prefix table to parser.
        dsg.getPrefixes().close() ;

        ProgressLogger monitorTotal = new ProgressLogger(cmdLog, "tuples", BulkLoader.DataTickPoint,BulkLoader.superTick) ;
        monitorTotal.start() ;

        DataBag<Tuple<Long>> outputTriples = new MultiThreadedSortedDataBag<Tuple<Long>>(getThresholdPolicy(tripleSerializationFactory), new TripleSerializationFactory(), comparator);
        DataBag<Tuple<Long>> outputQuads = new MultiThreadedSortedDataBag<Tuple<Long>>(getThresholdPolicy(quadSerializationFactory), new QuadSerializationFactory(), comparator);

        // Node table and input data using node ids (rather than RDF node values)
        Sink<Quad> sink = new NodeTableBuilder2(dsg, monitorTotal, outputTriples, outputQuads) ; 
        Sink<Triple> sink2 = new SinkExtendTriplesToQuads(sink) ;

        // Build primary indexes: SPO and GSPO
        BPlusTree bptSPO = null ;
        BPlusTree bptGSPO = null ;
        try {
            for( String filename : datafiles)
            {
                if ( datafiles.size() > 0 )
                    cmdLog.info("Load: "+filename+" -- "+Utils.nowAsString()) ;
                
                InputStream in = IO.openFile(filename) ;
                Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in) ;
                ParserProfile profile = createParserProfile(runId, filename);
                Lang lang = Lang.guess(filename, Lang.NQUADS) ;
                if ( lang.isTriples() ) {
                    LangNTriples parser = new LangNTriples(tokenizer, profile, sink2) ;
                    parser.parse() ;
                } else {
                    LangNQuads parser = new LangNQuads(tokenizer, profile, sink) ;
                    parser.parse() ;
                }
                IO.close(in) ; // TODO: final {}
            }
            sink.close() ;

            // spill(outputTriples) ;
            bptSPO = createBPlusTreeIndex(Names.primaryIndexTriples, outputTriples) ;

            // spill(outputQuads) ;
            bptGSPO = createBPlusTreeIndex(Names.primaryIndexQuads, outputQuads) ;
        } finally {
            outputTriples.close() ;
            outputQuads.close() ;
        }

        // Secondary POS and OSP indexes
        for ( String indexName : Names.tripleIndexes ) {
        	if ( !indexName.equals(Names.primaryIndexTriples) ) {
        		createBPlusTreeIndex(indexName, new ColumnMap(Names.primaryIndexTriples, indexName), bptSPO) ;
        	}
        }

        // Secondary GPOS, GOSP, POSG and OSPG indexes
        for ( String indexName : Names.quadIndexes ) {
        	if ( !indexName.equals(Names.primaryIndexQuads) ) {
        		createBPlusTreeIndex(indexName, new ColumnMap(Names.primaryIndexQuads, indexName), bptGSPO) ;
        	}
        }

        if ( ! no_stats ) {
            if ( ! location.isMem() ) {
                dsg = SetupTDB.buildDataset(location) ;
                Stats.write(dsg, ((NodeTableBuilder2)sink).getCollector()) ;
            }            
        }

        ProgressLogger.print ( cmdLog, monitorTotal ) ;
    }
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" --loc=DIR FILE ..." ;
    }

    @Override
    protected String getCommandName()
    {
        return this.getClass().getName() ;
    }
    
    public static void spill ( DataBag<?> bag ) {
        if ( bag instanceof MultiThreadedSortedDataBag<?> ) {
            ((MultiThreadedSortedDataBag<?>)bag).spill() ;
        }
    }
    
    private BPlusTree createBPlusTreeIndex(String indexName, DataBag<Tuple<Long>> tuples) {
        deleteExistingBPlusTreeIndex(indexName) ;
        
    	final int size = indexName.length() ;

    	if ( ( size != 3 ) && ( size != 4 ) ) throw new AtlasException("Unsupported size.") ;
    	
    	final RecordFactory recordFactory ;
    	if ( size == 3 ) {
    		recordFactory = new RecordFactory(SystemTDB.LenIndexTripleRecord, 0) ;
    	} else {
    		recordFactory = new RecordFactory(SystemTDB.LenIndexQuadRecord, 0) ;
    	}
    	
        int order = BPlusTreeParams.calcOrder(SystemTDB.BlockSize, recordFactory) ;
        BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory) ;
        
        int readCacheSize = 10 ;
        int writeCacheSize = 100 ;
        
        FileSet destination = new FileSet(location, indexName) ;
        BlockMgr blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExtTree, SystemTDB.BlockSize, readCacheSize, writeCacheSize) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExtRecords, SystemTDB.BlockSize, readCacheSize, writeCacheSize) ;
        
        cmdLog.info("Index: creating " + indexName + " index...") ;
        final ProgressLogger monitor = new ProgressLogger(cmdLog, "records to " + indexName, BulkLoader.DataTickPoint,BulkLoader.superTick) ;
        monitor.start() ;
        
        Transform<Tuple<Long>, Record> transformTuple2Record = new Transform<Tuple<Long>, Record>() {
			@Override public Record convert(Tuple<Long> tuple) {
				Record record = recordFactory.create() ;
				for ( int i = 0; i < size; i++ ) {
					Bytes.setLong(tuple.get(i), record.getKey(), i * SystemTDB.SizeOfLong) ;					
				}
				monitor.tick() ;
				return record ;
			}
        };
        
        BPlusTree bpt2 ;
        Iterator<Tuple<Long>> it = tuples.iterator() ;
        Iterator<Record> iter = null ;
        try {
            iter = Iter.iter(it).map(transformTuple2Record) ;
            bpt2 = BPlusTreeRewriter.packIntoBPlusTree(iter, bptParams, recordFactory, blkMgrNodes, blkMgrRecords) ;
            bpt2.sync() ;
        } finally {
            Iter.close(it) ;
            Iter.close(iter) ;
        }

        ProgressLogger.print ( cmdLog, monitor ) ;

        return bpt2 ;
    }

    private void createBPlusTreeIndex(String indexName, final ColumnMap colMap, BPlusTree bpt) {
    	final int size = indexName.length() ;

    	if ( ( size != 3 ) && ( size != 4 ) ) throw new AtlasException("Unsupported size.") ;
    	
    	DataBag<Tuple<Long>> outTuples ;
    	if ( size == 3 ) {
    		outTuples = new MultiThreadedSortedDataBag<Tuple<Long>>(getThresholdPolicy(tripleSerializationFactory), tripleSerializationFactory, comparator);
    	} else {
    		outTuples = new MultiThreadedSortedDataBag<Tuple<Long>>(getThresholdPolicy(quadSerializationFactory), quadSerializationFactory, comparator);
    	}
    	
        cmdLog.info("Index: sorting data for " + indexName + " index...") ;
        final ProgressLogger monitor = new ProgressLogger(cmdLog, "records to " + indexName, BulkLoader.DataTickPoint,BulkLoader.superTick) ;
        monitor.start() ;
        
        Transform<Record, Tuple<Long>> transformTuple2Tuple = new Transform<Record, Tuple<Long>>() {
            @Override public Tuple<Long> convert(Record record) {
                Long[] ids = new Long[size] ;
                for ( int i = 0 ; i < size ; i++ ) {
                    ids[colMap.fetchSlotIdx(i)] = Bytes.getLong(record.getKey(), i*SizeOfLong) ;
                }
                monitor.tick() ;
                return Tuple.create(ids) ;
            }
        };

        // Reads BPlusTree index and sort it for a different index according to ColumnMap
        try {
            Iterator<Record> bptIter = bpt.iterator() ;
            try {
                outTuples.addAll(Iter.iter(bptIter).map(transformTuple2Tuple).iterator()) ;
            } finally {
                Iter.close(bptIter) ;
            }

            ProgressLogger.print ( cmdLog, monitor ) ;
            
            createBPlusTreeIndex(indexName, outTuples) ;
    	} finally {
            outTuples.close() ;
            outTuples = null ;
    	}
    }
    
    private void deleteExistingBPlusTreeIndex(String indexName) {
        FileOps.delete(location.absolute(indexName, Names.bptExtTree)) ;
        FileOps.delete(location.absolute(indexName, Names.bptExtRecords)) ;
    }

    
    // Utility methods for RDF parsing...

    public static final NodeToLabel nodeToLabel = NodeToLabel.createBNodeByLabelAsGiven();
    private static final Prologue prologue = new Prologue(null, IRIResolver.createNoResolve()); 

    public static String serialize(Node node) {
        StringWriter out = new StringWriter();
        OutputLangUtils.output(out, node, prologue, nodeToLabel);
        return out.toString();
    }
    
    private static ParserProfile createParserProfile(String runId, String filename) {
        LabelToNode labelMapping = new CustomLabelToNode(runId, filename);
        return new ParserProfileBase(prologue, ErrorHandlerFactory.errorHandlerStd, labelMapping);
    }

    private static ParserProfile createParserProfile() {
        LabelToNode labelMapping = LabelToNode.createUseLabelAsGiven() ;
        return new ParserProfileBase(prologue, ErrorHandlerFactory.errorHandlerStd, labelMapping);
    }

    private static ParserProfile profile = createParserProfile();

    public static Node parse(String string) {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        if ( ! tokenizer.hasNext() )
            return null ;
        Token t = tokenizer.next();
        Node n = profile.create(null, t) ;
        if ( tokenizer.hasNext() )
            Log.warn(RiotLib.class, "String has more than one token in it: "+string) ;
        return n ;
    }

    
}

