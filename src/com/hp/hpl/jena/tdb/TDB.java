/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP}
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import java.util.Iterator ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;
import com.hp.hpl.jena.riot.JenaReaderNTriples2 ;
import com.hp.hpl.jena.riot.JenaReaderTurtle2 ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder ;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt;
import com.hp.hpl.jena.sparql.mgt.SystemInfo;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.assembler.VocabTDB ;
import com.hp.hpl.jena.tdb.lib.Sync ;
import com.hp.hpl.jena.tdb.modify.UpdateProcessorTDB ;
import com.hp.hpl.jena.tdb.solver.Explain ;
import com.hp.hpl.jena.tdb.solver.OpExecutorTDB ;
import com.hp.hpl.jena.tdb.solver.QueryEngineTDB ;
import com.hp.hpl.jena.tdb.solver.StageGeneratorDirectTDB ;
import com.hp.hpl.jena.tdb.solver.Explain.InfoLevel ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;

public class TDB
{
    /** IRI for TDB */  
    public static final String tdbIRI = "http://jena.hpl.hp.com/#tdb" ;
    
    // Internal logging
    private static final Logger log = LoggerFactory.getLogger(TDB.class) ;
    
    /** Logger for general information */ 
    public static final Logger logInfo = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.info") ;
    
    /** Logger for execution information */
    public static final Logger logExec = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.exec") ;
    
    public final static String namespace = "http://jena.hpl.hp.com/2008/tdb#" ;

    /** Symbol to use the union of named graphs as the default graph of a query */ 
    public static final Symbol symUnionDefaultGraph          = SystemTDB.allocSymbol("unionDefaultGraph") ;
    
    /** Symbol to enable logging of execution.  Must also set log4j, or other logging system,
     * for logger "com.hp.hpl.jena.tdb.exec"
     * e.g. log4j.properties -- log4j.logger.com.hp.hpl.jena.tdb.exec=INFO
     */
    public static final Symbol symLogExec           = SystemTDB.allocSymbol("logExec") ;

    /** Set or unset execution logging - logging is to logger "com.hp.hpl.jena.tdb.exec" at level INFO.
     * An appropriate logging configuration is also required.
     * @deprecated Use setExecutionLogging(Explain.InfoLevel)}
     */
    @Deprecated
    public static void setExecutionLogging(boolean state)
    {
        if ( ! state )
        {
            TDB.getContext().unset(TDB.symLogExec) ;
            return ;
        }
        
        TDB.getContext().set(TDB.symLogExec, state) ;
        if ( ! logExec.isInfoEnabled() )
            log.warn("Attempt to enable execution logging but the logger is not logging at level info") ;
    }
    
    /** Set execution logging - logging is to logger "com.hp.hpl.jena.tdb.exec" at level INFO.
     * An appropriate logging configuration is also required.
     */
    public static void setExecutionLogging(Explain.InfoLevel infoLevel)
    {
        if ( InfoLevel.NONE.equals(infoLevel) )
        {
            TDB.getContext().unset(TDB.symLogExec) ;
            return ;
        }
        
        TDB.getContext().set(TDB.symLogExec, infoLevel) ;
        if ( ! logExec.isInfoEnabled() )
            log.warn("Attempt to enable execution logging but the logger is not logging at level info") ;
    }
    

    public static Context getContext()     { return ARQ.getContext() ; }  
    
    // Called on assembler loading.
    /** TDB System initialization - normally, this is not explicitly called because
     * all routes to use TDB will cause initialization to occur.  However, calling it
     * repeatedly is safe and low cost.
     */
    public static void init() { }
    
    /** Release any and all system resources held by TDB.
     *  This does NOT close or release datasets or graphs held by client code. 
     */
    public static void closedown()
    {
        TDBMaker.clearDatasetCache() ;
    }
    
    /** Sync a TDB synchronizable object (model, graph dataset). Do nothing otherwise */
    public static void sync(Model model)
    {
        sync(model.getGraph()) ;
    }
    
    /** Sync a TDB synchronizable object (model, graph dataset). Do nothing otherwise */
    public static void sync(Graph graph)
    {
        sync(graph, true) ;
    }

    /** Sync a TDB synchronizable object (model, graph dataset). Do nothing otherwise */
    public static void sync(Dataset dataset)
    { 
        DatasetGraph ds = dataset.asDatasetGraph() ;
        sync(ds) ;
    }
    
    /** Sync a TDB synchronizable object (model, graph daatset). Do nothing otherwise */
    public static void sync(DatasetGraph dataset)
    { 
        if ( dataset instanceof DatasetGraphTDB )
            sync(dataset, true) ;
        else
        {
            // May be a general purpose datsset with TDB objects in it.
            Iterator<Node> iter = dataset.listGraphNodes() ;
            for ( ; iter.hasNext() ; )
            {
                Node n = iter.next();
                Graph g = dataset.getGraph(n) ;
                sync(g) ;
            }
        }
    }

    
    /** Sync a TDB synchronizable object (model, graph daatset). 
     *  If force is true, synchronize as much as possible (e.g. file metadata)
     *  else make a reasonable attenpt at synchronization but does not gauarantee disk state. 
     * Do nothing otherwise */
    private static void sync(Object object, boolean force)
    {
        if ( object instanceof Sync )
            ((Sync)object).sync(force) ;
    }
    
    static { initWorker() ; }
    
    private static boolean initialized = false ;
    private static synchronized void initWorker()
    {
        if ( initialized )
            return ;
        initialized = true ;
        
        SystemTDB.init() ;
        ARQ.init() ;
        
        // Set management information.
        // Needs ARQ > 2.8.0
        String NS = TDB.PATH ;
        ARQMgt.register(NS+".system:type=SystemInfo", new SystemInfo(TDB.tdbIRI, TDB.VERSION, TDB.BUILD_DATE)) ;

        AssemblerUtils.init() ;
        VocabTDB.init();
        QueryEngineTDB.register() ;
        UpdateProcessorTDB.register() ;

        wireIntoExecution() ;
        
        // Override N-TRIPLES and Turtle with faster implementations.
        String readerNT = JenaReaderNTriples2.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", readerNT) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE", readerNT) ;
        
        String readerTTL = JenaReaderTurtle2.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("N3", readerTTL) ;
        RDFReaderFImpl.setBaseReaderClassName("TURTLE", readerTTL) ;
        RDFReaderFImpl.setBaseReaderClassName("Turtle", readerTTL) ;
        RDFReaderFImpl.setBaseReaderClassName("TTL", readerTTL) ;
        
        if ( log.isDebugEnabled() )
            log.debug("\n"+ARQ.getContext()) ;
    }

    private static void wireIntoExecution()
    {
        // TDB does it itself.
        TDB.getContext().set(ARQ.filterPlacement, false) ;
        // Globally change the stage generator to intercept BGP on TDB
        StageGenerator orig = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
        
        // Wire in the TDB stage generator which will make TDB work whether
        // or not the TDB executor is used. This means that datasets of mixed graph
        // types inside a general purpose dataset work.
        StageGenerator stageGenerator = new StageGeneratorDirectTDB(orig) ;
        StageBuilder.setGenerator(ARQ.getContext(), stageGenerator) ;

        // Wire in the new OpExecutor.  This is normal way to execute with a dataset.
        QC.setFactory(ARQ.getContext(), OpExecutorTDB.OpExecFactoryTDB) ;
    }
    
    // ---- Static constants read by modVersion
    // ---- Must be after initialization.
    
    static private String metadataLocation = "com/hp/hpl/jena/tdb/tdb-properties.xml" ;
    static private Metadata metadata = new Metadata(metadataLocation) ;
    
    /** The root package name for TDB */   
    public static final String PATH = "com.hp.hpl.jena.tdb";

    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"
    
    public static final String NAME = "TDB" ;
    
    /** The full name of the current TDB version */   
    public static final String VERSION = metadata.get(PATH+".version", "DEV") ;

    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = metadata.get(PATH+".build.datetime", "unset") ;
    
    // Final initialization 
    static {
        initlization2() ;
    }

    private static void initlization2()
    {
        //TDB.logInfo.info("TDB: "+TDB.VERSION) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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