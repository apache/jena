/*
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

package com.hp.hpl.jena.tdb.store.bulkloader2;

import static com.hp.hpl.jena.sparql.util.Utils.nowAsString ;

import java.io.FileNotFoundException ;
import java.io.FileOutputStream ;
import java.io.OutputStream ;
import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.atlas.logging.ProgressLogger ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotReader ;
import org.slf4j.Logger ;
import tdb.cmdline.CmdTDB ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollectorNodeId ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkLoader ;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkStreamRDF ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.sys.Names ;

/** Build node table - write triples/quads as text file */
public class CmdNodeTableBuilder extends CmdGeneral
{
    static { LogCtl.setLog4j() ; }
    private static Logger cmdLog =TDB.logLoader ;

    private static ArgDecl argLocation = new ArgDecl(ArgDecl.HasValue, "loc", "location") ;
    private static ArgDecl argTriplesOut = new ArgDecl(ArgDecl.HasValue, "triples") ;
    private static ArgDecl argQuadsOut = new ArgDecl(ArgDecl.HasValue, "quads") ;
    private String locationString ;
    private String dataFileTriples ;
    private String dataFileQuads ;
    private List<String> datafiles ;
    private Location location ;
    
    public static void main(String...argv)
    {
        CmdTDB.init() ;
        DatasetBuilderStd.setOptimizerWarningFlag(false) ;
        new CmdNodeTableBuilder(argv).mainRun() ;
    }
    
    public CmdNodeTableBuilder(String...argv)
    {
        super(argv) ;
        super.add(argLocation,      "--loc",        "Location") ;
        super.add(argTriplesOut,    "--triples",    "Output file for triples") ;
        super.add(argQuadsOut,      "--quads",      "Output file for quads") ;
    }
        
    @Override
    protected void processModulesAndArgs()
    {
        if ( !super.contains(argLocation) ) throw new CmdException("Required: --loc DIR") ;
//        if ( !super.contains(argTriplesOut) ) throw new CmdException("Required: --triples FILE") ;
//        if ( !super.contains(argQuadsOut) ) throw new CmdException("Required: --quads FILE") ;
        
        locationString   = super.getValue(argLocation) ;
        location = new Location(locationString) ;

        dataFileTriples  = super.getValue(argTriplesOut) ;
        if ( dataFileTriples == null )
            dataFileTriples = location.getPath("triples", "tmp") ;
        
        dataFileQuads    = super.getValue(argQuadsOut) ;
        if ( dataFileQuads == null )
            dataFileQuads = location.getPath("quads", "tmp") ;
        
        if ( Lib.equal(dataFileTriples, dataFileQuads) )
            cmdError("Triples and Quads work files are the same") ;
        
        //datafiles  = getPositionalOrStdin() ;
        datafiles  = getPositional() ;
        if ( datafiles.isEmpty() )
            datafiles = Arrays.asList("-") ;
        
        // ---- Checking.
//        if ( false ) 
//            SetupTDB.makeNodeTable(location, locationString, 0, outputFile, 0) ;

        for( String filename : datafiles)
        {
            Lang lang = RDFLanguages.filenameToLang(filename, RDFLanguages.NQUADS) ;
            if ( lang == null )
                // Does not happen due to default above.
                cmdError("File suffix not recognized: " +filename) ;
            if ( ! filename.equals("-") && ! FileOps.exists(filename) )
                cmdError("File does not exist: "+filename) ;
        }
    }
    
    @Override
    protected void exec()
    {
        // This formats the location correctly.
        // But we're not really interested in it all.
        DatasetGraphTDB dsg = DatasetBuilderStd.create(location) ;
        
        // so close indexes and the prefix table.
        dsg.getTripleTable().getNodeTupleTable().getTupleTable().close();
        dsg.getQuadTable().getNodeTupleTable().getTupleTable().close();
        // Later - attach prefix table to parser.
        dsg.getPrefixes().close() ;
        
        ProgressLogger monitor = new ProgressLogger(cmdLog, "Data", BulkLoader.DataTickPoint,BulkLoader.superTick) ;
        OutputStream outputTriples = null ;
        OutputStream outputQuads = null ;
        
        try { 
            outputTriples = new FileOutputStream(dataFileTriples) ; 
            outputQuads = new FileOutputStream(dataFileQuads) ;
        }
        catch (FileNotFoundException e) { throw new AtlasException(e) ; }
        
        NodeTableBuilder sink = new NodeTableBuilder(dsg, monitor, outputTriples, outputQuads) ; 
        monitor.start() ;
        sink.startBulk() ;
        for( String filename : datafiles)
        {
            if ( datafiles.size() > 0 )
                cmdLog.info("Load: "+filename+" -- "+Utils.nowAsString()) ;
            RiotReader.parse(filename, sink) ;
        }
        sink.finishBulk() ;
        IO.close(outputTriples) ;
        IO.close(outputQuads) ;
        
        // ---- Stats
        
        // See Stats class.
        if ( ! location.isMem() )
            Stats.write(dsg.getLocation().getPath(Names.optStats), sink.getCollector().results()) ;
        
        // ---- Monitor
        long time = monitor.finish() ;

        long total = monitor.getTicks() ;
        float elapsedSecs = time/1000F ;
        float rate = (elapsedSecs!=0) ? total/elapsedSecs : 0 ;
        String str =  String.format("Total: %,d tuples : %,.2f seconds : %,.2f tuples/sec [%s]", total, elapsedSecs, rate, nowAsString()) ;
        cmdLog.info(str) ;
    }

    static class NodeTableBuilder implements BulkStreamRDF
    {
        private DatasetGraphTDB dsg ;
        private NodeTable nodeTable ;
        private WriteRows writerTriples ;
        private WriteRows writerQuads ;
        private ProgressLogger monitor ;
        private StatsCollectorNodeId stats ;

        NodeTableBuilder(DatasetGraphTDB dsg, ProgressLogger monitor, OutputStream outputTriples, OutputStream outputQuads)
        {
            this.dsg = dsg ;
            this.monitor = monitor ;
            NodeTupleTable ntt = dsg.getTripleTable().getNodeTupleTable() ; 
            this.nodeTable = ntt.getNodeTable() ;
            this.writerTriples = new WriteRows(outputTriples, 3, 20000) ; 
            this.writerQuads = new WriteRows(outputQuads, 4, 20000) ; 
            this.stats = new StatsCollectorNodeId(nodeTable) ;
        }
        
        @Override
        public void startBulk()
        {}

        @Override
        public void start()
        {}

        @Override
        public void finish()
        {}

        @Override
        public void finishBulk()
        {
            writerTriples.flush() ;
            writerQuads.flush() ;
            nodeTable.sync() ;
        }
            
        @Override
        public void triple(Triple triple)
        {
            Node s = triple.getSubject() ;
            Node p = triple.getPredicate() ;
            Node o = triple.getObject() ;
            process(Quad.tripleInQuad,s,p,o);
        }

        @Override
        public void quad(Quad quad)
        {
            Node s = quad.getSubject() ;
            Node p = quad.getPredicate() ;
            Node o = quad.getObject() ;
            Node g = null ;
            // Union graph?!
            if ( ! quad.isTriple() && ! quad.isDefaultGraph() )
                g = quad.getGraph() ;
            process(g,s,p,o);
        }

       
        private void process(Node g, Node s, Node p, Node o)
        {
            NodeId sId = nodeTable.getAllocateNodeId(s) ; 
            NodeId pId = nodeTable.getAllocateNodeId(p) ;
            NodeId oId = nodeTable.getAllocateNodeId(o) ;
            
            if ( g != null )
            {
                NodeId gId = nodeTable.getAllocateNodeId(g) ;
                writerQuads.write(gId.getId()) ;
                writerQuads.write(sId.getId()) ;
                writerQuads.write(pId.getId()) ;
                writerQuads.write(oId.getId()) ;
                writerQuads.endOfRow() ;
                stats.record(gId, sId, pId, oId) ;
            }
            else
            {
                writerTriples.write(sId.getId()) ;
                writerTriples.write(pId.getId()) ;
                writerTriples.write(oId.getId()) ;
                writerTriples.endOfRow() ;
                stats.record(null, sId, pId, oId) ;
            }
            monitor.tick() ;
        }

        public StatsCollectorNodeId getCollector() { return stats ; }

        @Override
        public void base(String base)
        {}

        @Override
        public void prefix(String prefix, String iri)
        {}
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" --loc=DIR [--triples=tmpFile1] [--quads=tmpFile2] FILE ..." ;
    }

    @Override
    protected String getCommandName()
    {
        return this.getClass().getName() ;
    }
}
