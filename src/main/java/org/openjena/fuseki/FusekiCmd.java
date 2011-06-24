/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import java.io.InputStream ;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.server.SPARQLServer ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.lang.SinkQuadsToDataset ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.slf4j.Logger ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModDatasetAssembler ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class FusekiCmd extends CmdARQ
{
    // Arguments:
    // --update
    
    // Specific switches:
    
    // --admin=on/off
    
    // --http-update
    // --http-get
    
    // --sparql-query
    // --sparql-update
    
    // pages/validators/
    // pages/control/
    // pages/query/ or /pages/sparql/
    
    private static ArgDecl argMem           = new ArgDecl(ArgDecl.NoValue,  "mem") ;
    private static ArgDecl argAllowUpdate   = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate") ;
    private static ArgDecl argFile          = new ArgDecl(ArgDecl.HasValue, "file") ;
    private static ArgDecl argMemTDB        = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB") ;
    private static ArgDecl argTDB           = new ArgDecl(ArgDecl.HasValue, "loc", "location") ;
    private static ArgDecl argPort          = new ArgDecl(ArgDecl.HasValue, "port") ;
    private static ArgDecl argHost          = new ArgDecl(ArgDecl.HasValue, "host") ;
    private static ArgDecl argTimeout       = new ArgDecl(ArgDecl.HasValue, "timeout") ;
    
    //private static ModLocation          modLocation =  new ModLocation() ;
    private static ModDatasetAssembler  modDataset = new ModDatasetAssembler() ;
    
    // fuseki [--mem|--desc assembler.ttl] [--port PORT] **** /datasetURI

    static public void main(String...argv)
    {
        // Just to make sure ...
        ARQ.init();
        Fuseki.init() ;
        new FusekiCmd(argv).mainRun() ;
    }
    
    private int port = 3030 ;
    private String clientHost = null;
    private DatasetGraph dsg ;
    private String datasetPath ;
    private boolean allowUpdate = false ;
    
    public FusekiCmd(String...argv)
    {
        super(argv) ;
        getUsage().startCategory("Fuseki") ;
        addModule(modDataset) ;
        add(argMem,     "--mem",                "Create an in-memory, non-persistent dataset for the server") ;
        add(argFile,    "--file=FILE",          "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file") ;
        add(argTDB,     "--loc=DIR",            "Use an existing TDB database (or create if does not exist)") ;
        add(argMemTDB,  "--memTDB",             "Create an in-memory, non-persistent dataset using TDB (testing only)") ;
        add(argPort,    "--port",               "Listen on this port number") ;
        add(argHost,    "--host=name or IP",    "Listen on a particualr interface (e.g. localhost)") ;
        add(argTimeout, "--timeout",            "Global timeout applied to queries (value in ms) -- format is X[,Y] ") ;
        add(argAllowUpdate, "--update",         "Allow updates (via SPARQL Update and SPARQL HTTP Update)") ;
        super.modVersion.addClass(TDB.class) ;
        super.modVersion.addClass(Fuseki.class) ;
    }

    static String argUsage = "[--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] [--host HOST] /DatasetPathName" ; 
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" "+argUsage ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        int x = 0 ;
        
        Logger log = Fuseki.serverLog ;
        
        ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
        if ( contains(argMem) ) x++ ; 
        if ( contains(argFile) ) x++ ;
        if ( contains(assemblerDescDecl) ) x++ ;
        if ( contains(argTDB) ) x++ ;
        
        TDB.setOptimizerWarningFlag(false) ;
        
        if ( x > 1 )
            throw new CmdException("Only one of --mem, --file, --loc or --desc") ;
        
        if ( x == 0 )
            throw new CmdException("Required: one of --mem, --file, --loc or --desc") ;

        if ( contains(argMem) )
        {
            log.info("Dataset: in-memory") ;
            dsg = DatasetGraphFactory.createMem() ;
        }
        if ( contains(argFile) )
        {
            dsg = DatasetGraphFactory.createMem() ;
            // replace by RiotLoader after ARQ refresh.
            String filename = getValue(argFile) ;
            log.info("Dataset: in-memory: load file: "+filename) ;
            if ( ! FileOps.exists(filename) )
                throw new CmdException("File not found: "+filename) ;

            Lang language = Lang.guess(filename) ;
            if ( language == null )
                throw new CmdException("Can't guess language for file: "+filename) ;
            InputStream input = IO.openFile(filename) ; 
            
            if ( language.isQuads() )
            {
                Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
                RiotLoader.readQuads(input, language, filename, sink) ;
            }
            else
            {
                Sink<Triple> sink = new SinkTriplesToGraph(dsg.getDefaultGraph()) ;
                RiotLoader.readTriples(input, language, filename, sink) ;
            }
        }
        
        if ( contains(argMemTDB) )
        {
            log.info("TDB dataset: in-memory") ;
            dsg = TDBFactory.createDatasetGraph() ;
        }
        
        if ( contains(argTDB) )
        {
            String dir = getValue(argTDB) ;
            log.info("TDB dataset: directory="+dir) ;
            if ( ! FileOps.exists(dir) )
                throw new CmdException("Directory not found: "+dir) ;
            dsg = TDBFactory.createDatasetGraph(dir) ;
        }
        
        // Otherwise
        if ( contains(assemblerDescDecl) )
        {
            log.info("Dataset from assembler") ;
            Dataset ds = modDataset.createDataset() ;
            if ( ds != null )
                dsg = ds.asDatasetGraph() ;
        }
        
        if ( contains(argPort) )
        {
            String portStr = getValue(argPort) ;
            try {
                port = Integer.parseInt(portStr) ;
            } catch (NumberFormatException ex)
            {
                throw new CmdException(argPort.getKeyName()+" : bad port number: "+portStr) ;
            }
        }
        
        if ( contains(argHost) )
        {
        	clientHost = getValue(argHost);
        	try {
        		InetAddress.getByName(clientHost);
        	} catch (UnknownHostException e) {
        		throw new CmdException("unknown host name");
        	}
        }
            
        if ( dsg == null )
            throw new CmdException("No dataset defined: "+argUsage) ;
        
        if ( getPositional().size() == 0 )
            throw new CmdException("No dataset path name given") ;
        if ( getPositional().size() > 1  )
            throw new CmdException("Multiple dataset path names given") ;
        datasetPath = getPositionalArg(0) ;
        if ( datasetPath.length() > 0 && ! datasetPath.startsWith("/") )
            throw new CmdException("Dataset path name must begin with a /: "+datasetPath) ;
        
        allowUpdate = contains(argAllowUpdate) ;
        
        if ( contains(argTimeout) )
        {
            String str = getValue(argTimeout) ;
            ARQ.getContext().set(ARQ.queryTimeout, str) ;
        }
        
    }

    @Override
    protected void exec()
    {
        SPARQLServer server = new SPARQLServer(dsg, datasetPath, clientHost, port, allowUpdate, super.isVerbose()) ;
        server.start() ;
        try { server.getServer().join() ; } catch (Exception ex) {}
    }

    @Override
    protected String getCommandName()
    {
        return "fuseki" ;
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