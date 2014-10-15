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

package org.apache.jena.fuseki;

import static org.apache.jena.fuseki.Fuseki.serverLog ;

import java.io.File ;
import java.io.InputStream ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.mgt.ManagementServer ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.server.ServerConfig ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.SysRIOT ;
import org.eclipse.jetty.server.Server ;
import org.slf4j.Logger ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModDatasetAssembler ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class FusekiCmd extends CmdARQ
{
    private static String log4Jsetup = StrUtils.strjoinNL(
          "## Plain output to stdout"
          , "log4j.appender.jena.plain=org.apache.log4j.ConsoleAppender"
          , "log4j.appender.jena.plain.target=System.out"
          , "log4j.appender.jena.plain.layout=org.apache.log4j.PatternLayout"
          , "log4j.appender.jena.plain.layout.ConversionPattern=%d{HH:mm:ss} %-5p %m%n"
          
          , "## Plain output with level, to stderr"
          , "log4j.appender.jena.plainlevel=org.apache.log4j.ConsoleAppender"
          , "log4j.appender.jena.plainlevel.target=System.err"
          , "log4j.appender.jena.plainlevel.layout=org.apache.log4j.PatternLayout"
          , "log4j.appender.jena.plainlevel.layout.ConversionPattern=%d{HH:mm:ss} %-5p %m%n"
          
          , "## Everything"
          , "log4j.rootLogger=INFO, jena.plain"
          , "log4j.logger.com.hp.hpl.jena=WARN"
          , "log4j.logger.org.openjena=WARN"
          , "log4j.logger.org.apache.jena=WARN"
          
          , "# Server log."
          , "log4j.logger.org.apache.jena.fuseki.Server=INFO"
          , "# Request log."
          , "log4j.logger.org.apache.jena.fuseki.Fuseki=INFO"
          , "log4j.logger.org.apache.jena.tdb.loader=INFO"
          , "log4j.logger.org.eclipse.jetty=ERROR"
          
          , "## Parser output"
          , "log4j.additivity."+SysRIOT.riotLoggerName+"=false"
          , "log4j.logger."+SysRIOT.riotLoggerName+"=INFO, jena.plainlevel "
        ) ;

    
    // Set logging.
    // 1/ Use log4j.configuration is defined.
    // 2/ Use file:log4j.properties 
    // 3/ Use Built in.
    
    static void setLogging() {
        // No loggers have been created but configuration may have been set up. 
        String x = System.getProperty("log4j.configuration", null) ;
        
        if ( x != null && ! x.equals("set") ) {
            // "set" indicates that CmdMain set logging.
            // Use standard log4j initialization.
            return ;
        }
        
        String fn = "log4j.properties" ;
        File f = new File(fn) ;
        if ( f.exists() ) {
            System.out.println("File") ;
            // Use file log4j.properties
            System.setProperty("log4j.configuration", "file:"+fn) ;
            return ;
        }
        // Use built-in for Fuseki.
        LogCtl.resetLogging(log4Jsetup) ;     
    }
    
    static { setLogging() ; }

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
    
    private static ArgDecl argMgtPort       = new ArgDecl(ArgDecl.HasValue, "mgtPort", "mgtport") ;
    private static ArgDecl argMem           = new ArgDecl(ArgDecl.NoValue,  "mem") ;
    private static ArgDecl argAllowUpdate   = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate") ;
    private static ArgDecl argFile          = new ArgDecl(ArgDecl.HasValue, "file") ;
    private static ArgDecl argMemTDB        = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB") ;
    private static ArgDecl argTDB           = new ArgDecl(ArgDecl.HasValue, "loc", "location") ;
    private static ArgDecl argPort          = new ArgDecl(ArgDecl.HasValue, "port") ;
    private static ArgDecl argLocalhost     = new ArgDecl(ArgDecl.NoValue, "localhost", "local") ;
    private static ArgDecl argTimeout       = new ArgDecl(ArgDecl.HasValue, "timeout") ;
    private static ArgDecl argFusekiConfig  = new ArgDecl(ArgDecl.HasValue, "config", "conf") ;
    private static ArgDecl argJettyConfig   = new ArgDecl(ArgDecl.HasValue, "jetty-config") ;
    private static ArgDecl argGZip          = new ArgDecl(ArgDecl.HasValue, "gzip") ;
    private static ArgDecl argUber          = new ArgDecl(ArgDecl.NoValue,  "uber", "über") ;   // Use the überservlet (experimental)
    private static ArgDecl argBasicAuth     = new ArgDecl(ArgDecl.HasValue, "basic-auth") ;
    
    private static ArgDecl argGSP           = new ArgDecl(ArgDecl.NoValue,  "gsp") ;    // GSP compliance mode
    
    private static ArgDecl argHome          = new ArgDecl(ArgDecl.HasValue, "home") ;
    private static ArgDecl argPages         = new ArgDecl(ArgDecl.HasValue, "pages") ;
    
    //private static ModLocation          modLocation =  new ModLocation() ;
    private static ModDatasetAssembler  modDataset = new ModDatasetAssembler() ;
    
    // fuseki [--mem|--desc assembler.ttl] [--port PORT] **** /datasetURI

    static public void main(String...argv)
    {
        // Just to make sure ...
        ARQ.init() ;
        TDB.init() ;
        Fuseki.init() ;
        new FusekiCmd(argv).mainRun() ;
    }
    
    private int port                    = 3030 ;
    private int mgtPort                 = -1 ;
    private boolean listenLocal         = false ;

    private DatasetGraph dsg            = null ; 
    private String datasetPath          = null ;
    private boolean allowUpdate         = false ;
    
    private String fusekiConfigFile     = null ;
    private boolean enableCompression   = true ;
    private String jettyConfigFile      = null ;
    private String authConfigFile       = null ;
    private String homeDir              = null ;
    private String pagesDir             = null ;
    
    public FusekiCmd(String...argv)
    {
        super(argv) ;
        
        if ( false )
            // Consider ...
            TransactionManager.QueueBatchSize =  TransactionManager.QueueBatchSize / 2 ;
        
        getUsage().startCategory("Fuseki") ;
        addModule(modDataset) ;
        add(argMem,     "--mem",                "Create an in-memory, non-persistent dataset for the server") ;
        add(argFile,    "--file=FILE",          "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file") ;
        add(argTDB,     "--loc=DIR",            "Use an existing TDB database (or create if does not exist)") ;
        add(argMemTDB,  "--memTDB",             "Create an in-memory, non-persistent dataset using TDB (testing only)") ;
        add(argPort,    "--port",               "Listen on this port number") ;
        add(argPages,   "--pages=DIR",          "Set of pages to serve as static content") ; 
        // Set via jetty config file.
        add(argLocalhost,   "--localhost",      "Listen only on the localhost interface") ;
        add(argTimeout, "--timeout=",           "Global timeout applied to queries (value in ms) -- format is X[,Y] ") ;
        add(argAllowUpdate, "--update",         "Allow updates (via SPARQL Update and SPARQL HTTP Update)") ;
        add(argFusekiConfig, "--config=",       "Use a configuration file to determine the services") ;
        add(argJettyConfig, "--jetty-config=FILE",  "Set up the server (not services) with a Jetty XML file") ;
        add(argBasicAuth, "--basic-auth=FILE",  "Configure basic auth using provided Jetty realm file, ignored if --jetty-config is used") ;
        add(argMgtPort, "--mgtPort=port",       "Enable the management commands on the given port") ; 
        add(argHome, "--home=DIR",              "Root of Fuseki installation (overrides environment variable FUSEKI_HOME)") ; 
        add(argGZip, "--gzip=on|off",           "Enable GZip compression (HTTP Accept-Encoding) if request header set") ;
        
        add(argUber) ;
        //add(argGSP) ;
        
        super.modVersion.addClass(TDB.class) ;
        super.modVersion.addClass(Fuseki.class) ;
    }

    static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName" ; 
    
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
        if ( contains(argMemTDB) ) x++ ;

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

            Lang language = RDFLanguages.filenameToLang(filename) ;
            if ( language == null )
                throw new CmdException("Can't guess language for file: "+filename) ;
            InputStream input = IO.openFile(filename) ; 
            
            if ( RDFLanguages.isQuads(language) )
                RDFDataMgr.read(dsg, filename) ;
            else
                RDFDataMgr.read(dsg.getDefaultGraph(), filename) ;
        }
        
        if ( contains(argMemTDB) )
        {
            log.info("TDB dataset: in-memory") ;
            dsg = TDBFactory.createDatasetGraph() ;
        }
        
        if ( contains(argTDB) )
        {
            String dir = getValue(argTDB) ;
            
            if ( Lib.equal(dir, Names.memName) ) {
                log.info("TDB dataset: in-memory") ;
            } else {
                if ( ! FileOps.exists(dir) )
                    throw new CmdException("Directory not found: "+dir) ;
                log.info("TDB dataset: directory="+dir) ;
            }
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
        
        if ( contains(argMgtPort) )
        {
            String mgtPortStr = getValue(argMgtPort) ;
            try {
                mgtPort = Integer.parseInt(mgtPortStr) ;
            } catch (NumberFormatException ex)
            {
                throw new CmdException(argMgtPort.getKeyName()+" : bad port number: "+mgtPortStr) ;
            }
        }

        if ( contains(argLocalhost) )
            listenLocal = true ;
            
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
                throw new CmdException("No such file: "+jettyConfigFile) ;
        }
        
        if ( contains(argBasicAuth) )
        {
            authConfigFile = getValue(argBasicAuth) ;
            if ( !FileOps.exists(authConfigFile) )
                throw new CmdException("No such file: " + authConfigFile) ;
        }
        
        if ( contains(argHome) )
        {
           List<String> args = super.getValues(argHome) ;
           homeDir = args.get(args.size()-1) ;
        }
        
        if ( contains(argPages) )
        {
           List<String> args = super.getValues(argPages) ;
           pagesDir = args.get(args.size()-1) ;
        }

        if ( contains(argGZip) )
        {
            if ( ! hasValueOfTrue(argGZip) && ! hasValueOfFalse(argGZip) )
                throw new CmdException(argGZip.getNames().get(0)+": Not understood: "+getValue(argGZip)) ;
            enableCompression = super.hasValueOfTrue(argGZip) ;
        }
        
        if ( contains(argUber) )
            SPARQLServer.überServlet = true ;
        
        if ( contains(argGSP) )
        {
            SPARQLServer.überServlet = true ;
            Fuseki.graphStoreProtocolPostCreate = true ;
        }

    }

    private static String sort_out_dir(String path)
    {
        path.replace('\\', '/') ;
        if ( ! path.endsWith("/"))
            path = path +"/" ;
        return path ;
    }
    
    @Override
    protected void exec()
    {
        if ( homeDir == null )
        {
            if ( System.getenv(Fuseki.FusekiHomeEnv) != null )
                 homeDir = System.getenv(Fuseki.FusekiHomeEnv) ;
            else
                 homeDir = "." ;
        }
        
        homeDir = sort_out_dir(homeDir) ;
        Fuseki.configLog.info("Home Directory: " + FileOps.fullDirectoryPath(homeDir));
        if ( ! FileOps.exists(homeDir) )
            Fuseki.configLog.warn("No such directory for Fuseki home: "+homeDir) ;
        
        String staticContentDir = pagesDir ;
        if ( staticContentDir == null )
            staticContentDir = homeDir+Fuseki.PagesStatic ;
        
        Fuseki.configLog.debug("Static Content Directory: "+ FileOps.fullDirectoryPath(staticContentDir)) ;

        if ( ! FileOps.exists(staticContentDir) ) {
            Fuseki.configLog.warn("No such directory for static content: " + FileOps.fullDirectoryPath(staticContentDir)) ;
            Fuseki.configLog.warn("You may need to set the --pages or --home option to configure static content correctly");
        }
        
        if ( jettyConfigFile != null )
            Fuseki.configLog.info("Jetty configuration: "+jettyConfigFile) ;
        
        ServerConfig serverConfig ;
        
        if ( fusekiConfigFile != null )
        {
            Fuseki.configLog.info("Configuration file: "+fusekiConfigFile) ;
            serverConfig = FusekiConfig.configure(fusekiConfigFile) ;
        }
        else 
        {
            serverConfig = FusekiConfig.defaultConfiguration(datasetPath, dsg, allowUpdate, listenLocal) ;
            if ( ! allowUpdate )
                Fuseki.serverLog.info("Running in read-only mode.");
        }
        
        // TODO Get from parsing config file.
        serverConfig.port = port ;
        serverConfig.pages = staticContentDir ;
        serverConfig.mgtPort = mgtPort ;
        serverConfig.pagesPort = port ;
        serverConfig.loopback = listenLocal ;
        serverConfig.enableCompression = enableCompression ;
        serverConfig.jettyConfigFile = jettyConfigFile ;
        serverConfig.authConfigFile = authConfigFile ;
        serverConfig.verboseLogging = ( super.isVerbose() || super.isDebug() ) ;
        
        SPARQLServer server = new SPARQLServer(serverConfig) ;
        
        // Temporary
        Fuseki.setServer(server) ;
        
        Server mgtServer = null ;
        
        if ( mgtPort > 0 )
        {
            Fuseki.configLog.info("Management services on port "+mgtPort) ;
            mgtServer = ManagementServer.createManagementServer(mgtPort) ;
            try { mgtServer.start() ; }
            catch (java.net.BindException ex)
            { serverLog.error("SPARQLServer: Failed to start management server: " + ex.getMessage()) ; System.exit(1) ; }
            catch (Exception ex)
            { serverLog.error("SPARQLServer: Failed to start management server: " + ex.getMessage(), ex) ; System.exit(1) ; }
        }

        server.start() ;
        try { server.getServer().join() ; } catch (Exception ex) {}

        if ( mgtServer != null )
        {
            try { mgtServer.stop() ; } 
            catch (Exception e) { serverLog.warn("Failed to cleanly stop the management server", e) ; }
        }
        System.exit(0) ;
    }
    

    @Override
    protected String getCommandName()
    {
        return "fuseki" ;
    }
}
