/*
 * 
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader2;

import static com.hp.hpl.jena.sparql.util.Utils.nowAsString ;

import java.io.FileNotFoundException ;
import java.io.FileOutputStream ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.List ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.system.SinkExtendTriplesToQuads ;
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
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollectorNodeId ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkLoader ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

/** Build node table - write triples/quads as text file */
public class CmdNodeTableBuilder extends CmdGeneral
{
    static { Log.setLog4j() ; }
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
        CmdTDB.setLogging() ;
        SetupTDB.setOptimizerWarningFlag(false) ;
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
        
        datafiles  = super.getPositional() ;
        
        // ---- Checking.
//        if ( false ) 
//            SetupTDB.makeNodeTable(location, locationString, 0, outputFile, 0) ;

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
    
    @Override
    protected void exec()
    {
        // This formats the location correctly.
        // But we're not really interested in it all.
        DatasetGraphTDB dsg = SetupTDB.buildDataset(location) ;
        
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
        Sink<Triple> sink2 = new SinkExtendTriplesToQuads(sink) ;
        
        monitor.start() ;
        for( String filename : datafiles)
        {
            if ( datafiles.size() > 0 )
                cmdLog.info("Load: "+filename+" -- "+Utils.nowAsString()) ;
            
            InputStream in = IO.openFile(filename) ;
            Lang lang = Lang.guess(filename, Lang.NQUADS) ;
            if ( lang.isTriples() )
                RiotLoader.readTriples(in, lang, null, sink2) ;
            else
                RiotLoader.readQuads(in, lang, null, sink) ;
        }
        sink.close() ;
        IO.close(outputTriples) ;
        IO.close(outputQuads) ;
        
        // ---- Stats
        
        // See Stats class.
        if ( ! location.isMem() )
            Stats.write(dsg, sink.getCollector()) ;
        
        // ---- Monitor
        long time = monitor.finish() ;

        long total = monitor.getTicks() ;
        float elapsedSecs = time/1000F ;
        float rate = (elapsedSecs!=0) ? total/elapsedSecs : 0 ;
        String str =  String.format("Total: %,d tuples : %,.2f seconds : %,.2f tuples/sec [%s]", total, elapsedSecs, rate, nowAsString()) ;
        cmdLog.info(str) ;
    }

    static class NodeTableBuilder implements Sink<Quad>
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
            this.stats = new StatsCollectorNodeId() ;
        }
        
        //@Override
        public void send(Quad quad)
        {
            Node s = quad.getSubject() ;
            Node p = quad.getPredicate() ;
            Node o = quad.getObject() ;
            Node g = null ;
            // Union graph?!
            if ( ! quad.isTriple() && ! quad.isDefaultGraph() )
                g = quad.getGraph() ;
            
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

        //@Override
        public void flush()
        {
            writerTriples.flush() ;
            writerQuads.flush() ;
            nodeTable.sync() ;
        }

        //@Override
        public void close()
        { flush() ; }
        
        public StatsCollectorNodeId getCollector() { return stats ; }
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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