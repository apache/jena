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

package org.openjena.fuseki;

import java.io.InputStream ;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays ;
import java.util.List ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.config.FusekiConfig ;
import org.openjena.fuseki.config.FusekiConfig.ServiceDesc ;
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
    private static ArgDecl argFusekiConfig  = new ArgDecl(ArgDecl.HasValue, "config", "conf") ;
    private static ArgDecl argJettyConfig   = new ArgDecl(ArgDecl.HasValue, "jetty-config") ;
    
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
    
    private String fusekiConfigFile = null ;
    private String jettyConfigFile = null ;
    
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
        // Set via jetty config file.
        //add(argHost,    "--host=name or IP",    "Listen on a particular interface (e.g. localhost)") ;
        add(argTimeout, "--timeout",            "Global timeout applied to queries (value in ms) -- format is X[,Y] ") ;
        add(argAllowUpdate, "--update",         "Allow updates (via SPARQL Update and SPARQL HTTP Update)") ;
        add(argFusekiConfig, "--config=",       "Use a configuration file to determine the services") ;
        add(argJettyConfig, "--jetty-config=",  "Set up the server (not services) with a Jetty XML file") ;
        super.modVersion.addClass(TDB.class) ;
        super.modVersion.addClass(Fuseki.class) ;
    }

    static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] [--host HOST] /DatasetPathName" ; 
    
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
        
        if ( contains(argFusekiConfig) )
            fusekiConfigFile = getValue(argFusekiConfig) ;
        
        ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
        if ( contains(argMem) ) x++ ; 
        if ( contains(argFile) ) x++ ;
        if ( contains(assemblerDescDecl) ) x++ ;
        if ( contains(argTDB) ) x++ ;

        if ( fusekiConfigFile != null )
        {
            if ( x > 1 )
                throw new CmdException("Dataset specificed on the command line and also a configuration file specificed.") ;
        }
        else
        {
            if ( x == 0 )
                throw new CmdException("Required: either --config=FILE or one of --mem, --file, --loc or --desc") ;
        }

        
        TDB.setOptimizerWarningFlag(false) ;
        
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
        
        if ( contains(argFusekiConfig) )
        {
            if ( dsg != null )
                throw new CmdException("Dataset specificed on the command line and also a configuration file specificed.") ;
            fusekiConfigFile = getValue(argFusekiConfig) ;
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
            
        if ( fusekiConfigFile == null && dsg == null )
            throw new CmdException("No dataset defined and no configuration file: "+argUsage) ;
        
        if ( dsg != null )
        {
            if ( getPositional().size() == 0 )
                throw new CmdException("No dataset path name given") ;
            if ( getPositional().size() > 1  )
                throw new CmdException("Multiple dataset path names given") ;
            datasetPath = getPositionalArg(0) ;
            if ( datasetPath.length() > 0 && ! datasetPath.startsWith("/") )
                throw new CmdException("Dataset path name must begin with a /: "+datasetPath) ;
            
            allowUpdate = contains(argAllowUpdate) ;
        }
        
        if ( contains(argTimeout) )
        {
            String str = getValue(argTimeout) ;
            ARQ.getContext().set(ARQ.queryTimeout, str) ;
        }
        
        if ( contains(argJettyConfig) )
        {
            jettyConfigFile = getValue(argJettyConfig) ;
            if ( !FileOps.exists(jettyConfigFile) )
                throw new CmdException("No such file: : "+jettyConfigFile) ;
        }
    }

    @Override
    protected void exec()
    {
        SPARQLServer server ;
        if ( fusekiConfigFile != null )
        {
            List<ServiceDesc> services = FusekiConfig.configure(fusekiConfigFile) ;
            server =  new SPARQLServer(jettyConfigFile, port, services) ;
        }
        else
        {
            ServiceDesc sDesc = FusekiConfig.defaultConfiguration(datasetPath, dsg, allowUpdate) ;
            server = new SPARQLServer(jettyConfigFile, port, Arrays.asList(sDesc) ) ;
        }
        server.start() ;
        try { server.getServer().join() ; } catch (Exception ex) {}
    }
    

    @Override
    protected String getCommandName()
    {
        return "fuseki" ;
    }
}
