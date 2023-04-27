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

package org.apache.jena.fuseki.main.cmds;

import static arq.cmdline.ModAssembler.assemblerDescDecl;

import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModDatasetAssembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.ArgModuleGeneral;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiMainInfo;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.transaction.TransactionManager;
import org.slf4j.Logger;

public class FusekiMain extends CmdARQ {
    private static int defaultPort          = 3030;
    private static int defaultHttpsPort     = 3043;

    private static ArgDecl  argMem          = new ArgDecl(ArgDecl.NoValue,  "mem");
    private static ArgDecl  argUpdate       = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate");
    private static ArgDecl  argFile         = new ArgDecl(ArgDecl.HasValue, "file");

    private static ArgDecl  argTDB1mode     = new ArgDecl(ArgDecl.NoValue,  "tdb1");
    private static ArgDecl  argTDB2mode     = new ArgDecl(ArgDecl.NoValue,  "tdb2");
    private static ArgDecl  argMemTDB       = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB", "tdbmem");
    private static ArgDecl  argTDB          = new ArgDecl(ArgDecl.HasValue, "loc", "location", "tdb");

    // RDFS vocabulary applied to command line defined dataset.
    private static ArgDecl  argRDFS         = new ArgDecl(ArgDecl.HasValue, "rdfs");

    // No SPARQL dataset or services
    private static ArgDecl  argEmpty        = new ArgDecl(ArgDecl.NoValue,  "empty", "no-dataset");
    private static ArgDecl  argGeneralQuerySvc = new ArgDecl(ArgDecl.HasValue, "general");

    private static ArgDecl  argPort         = new ArgDecl(ArgDecl.HasValue, "port");
    private static ArgDecl  argLocalhost    = new ArgDecl(ArgDecl.NoValue,  "localhost", "local");
    private static ArgDecl  argTimeout      = new ArgDecl(ArgDecl.HasValue, "timeout");
    private static ArgDecl  argConfig       = new ArgDecl(ArgDecl.HasValue, "config", "conf");

    private static ArgDecl  argJettyConfig  = new ArgDecl(ArgDecl.HasValue, "jetty-config", "jetty");
    private static ArgDecl  argGZip         = new ArgDecl(ArgDecl.HasValue, "gzip");
    private static ArgDecl  argBase         = new ArgDecl(ArgDecl.HasValue, "base", "files");

    // This is now a no-op - CORS is included unless "--no-cors" is used.
    private static ArgDecl  argCORS         = new ArgDecl(ArgDecl.NoValue,  "withCORS", "cors", "CORS");
    private static ArgDecl  argNoCORS       = new ArgDecl(ArgDecl.NoValue,  "noCORS", "no-cors");
    private static ArgDecl  argWithPing     = new ArgDecl(ArgDecl.NoValue,  "withPing", "ping");
    private static ArgDecl  argWithStats    = new ArgDecl(ArgDecl.NoValue,  "withStats", "stats");
    private static ArgDecl  argWithMetrics  = new ArgDecl(ArgDecl.NoValue,  "withMetrics", "metrics");
    private static ArgDecl  argWithCompact  = new ArgDecl(ArgDecl.NoValue,  "withCompact", "compact");

    private static ArgDecl  argAuth         = new ArgDecl(ArgDecl.HasValue, "auth");

    private static ArgDecl  argHttps        = new ArgDecl(ArgDecl.HasValue, "https");
    private static ArgDecl  argHttpsPort    = new ArgDecl(ArgDecl.HasValue, "httpsPort", "httpsport", "sport");

    private static ArgDecl  argPasswdFile   = new ArgDecl(ArgDecl.HasValue, "passwd");
    private static ArgDecl  argRealm        = new ArgDecl(ArgDecl.HasValue, "realm");

    // Same as --empty --validators --general=/sparql, --files=ARG

    private static ArgDecl  argSparqler     = new ArgDecl(ArgDecl.HasValue, "sparqler");

    private static ArgDecl  argValidators   = new ArgDecl(ArgDecl.NoValue,  "validators");

    private static List<ArgModuleGeneral> additionalArgs = new ArrayList<>();
    public static void addArgModule(ArgModuleGeneral argModule) { additionalArgs.add(argModule); }

    // private static ModLocation modLocation = new ModLocation();
    private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

    private final ServerConfig serverConfig  = new ServerConfig();
    // Default
    private boolean useTDB2 = true;

    /** Build, but do not start, a server based on command line syntax. */
    public static FusekiServer build(String... argv) {
        FusekiMain inner = new FusekiMain(argv);
        inner.process();
        return inner.buildServer();
    }

    static void run(String... argv) {
        JenaSystem.init();
        new FusekiMain(argv).mainRun();
    }

    protected FusekiMain(String... argv) {
        super(argv);

        if ( false )
            // Consider ...
            TransactionManager.QueueBatchSize = TransactionManager.QueueBatchSize / 2;

        getUsage().startCategory("Fuseki Main");

        additionalArgs.forEach(aMod->super.addModule(aMod));

        // Control the order!
        add(argMem, "--mem",
            "Create an in-memory, non-persistent dataset for the server");
        add(argFile, "--file=FILE",
            "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file");
        add(argTDB2mode, "--tdb2",
            "Use TDB2 for command line persistent datasets");
        add(argTDB1mode, "--tdb1",
                "Use TDB1 for command line persistent datasets (default is TDB2)");
        add(argTDB, "--loc=DIR",
            "Use an existing TDB database (or create if does not exist)");
        add(argMemTDB, "--memTDB",
            "Create an in-memory, non-persistent dataset using TDB (testing only)");
//            add(argEmpty, "--empty",
//                "Run with no datasets and services");
        add(argRDFS, "--rdfs=FILE",
            "Apply RDFS on top of the dataset");
        add(argConfig, "--config=FILE",
            "Use a configuration file to determine the services");
        addModule(modDataset);
        add(argEmpty); // Hidden for now.
        add(argPort, "--port",
            "Listen on this port number");
        add(argLocalhost, "--localhost",
            "Listen only on the localhost interface");
        add(argTimeout, "--timeout=",
            "Global timeout applied to queries (value in ms) -- format is X[,Y] ");
        add(argUpdate, "--update",
            "Allow updates (via SPARQL Update and SPARQL HTTP Update)");
        add(argGZip, "--gzip=on|off",
            "Enable GZip compression (HTTP Accept-Encoding) if request header set");
        add(argBase, "--base=DIR",
            "Directory for static content");
        add(argSparqler, "--sparqler=DIR",
            "Run with SPARQLer services Directory for static content");
        add(argValidators, "--validators",
            "Install validators");
        add(argGeneralQuerySvc, "--general=PATH",
            "Add a general SPARQL endpoint (without a dataset) at /PATH");

        add(argAuth, "--auth=[basic|digest]",
            "Run the server using basic or digest authentication");
        add(argHttps, "--https=CONF",
            "https certificate access details. JSON file { \"cert\":FILE , \"passwd\"; SECRET } ");
        add(argHttpsPort, "--httpsPort=NUM",
            "https port (default port is 3043)");

        add(argPasswdFile, "--passwd=FILE",
            "Password file");
        add(argJettyConfig, "--jetty=FILE",
            "jetty.xml server configuration");
        add(argCORS); //, "--cors"); "Enable CORS");
        add(argNoCORS, "--no-cors", "Disable CORS");
        // put in the configuration file
//            add(argRealm, "--realm=REALM", "Realm name");

        add(argWithPing,    "--ping",       "Enable /$/ping");
        add(argWithStats,   "--stats",      "Enable /$/stats");
        add(argWithMetrics, "--metrics",    "Enable /$/metrics");
        add(argWithCompact, "--compact",    "Enable /$/compact/*");

        super.modVersion.addClass(Fuseki.class);
    }

    static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName";

    @Override
    protected String getSummary() {
        return getCommandName() + " " + argUsage;
    }

    @Override
    protected void processModulesAndArgs() {
        Logger log = Fuseki.serverLog;

        serverConfig.verboseLogging = super.isVerbose();

        boolean allowEmpty = contains(argEmpty) || contains(argSparqler);

        // ---- Checking consistency
        int numDefinitions = 0;

        if ( contains(argMem) )
            numDefinitions++;
        if ( contains(argFile) )
            numDefinitions++;
        if ( contains(assemblerDescDecl) )
            numDefinitions++;
        if ( contains(argTDB) )
            numDefinitions++;
        if ( contains(argMemTDB) )
            numDefinitions++;
        if ( contains(argConfig) )
            numDefinitions++;

        if ( numDefinitions == 0 && ! allowEmpty )
            throw new CmdException("No dataset specified on the command line.");

        if ( numDefinitions > 1 )
            throw new CmdException("Multiple ways providing a dataset. Only one of --mem, --file, --loc or --conf");

        if ( numDefinitions > 0 && allowEmpty )
            throw new CmdException("Dataset provided but 'no dataset' flag given");

        //---- check: Invalid: --conf + service name.
        if ( contains(argConfig) ) {
            if ( getPositional().size() != 0 )
                throw new CmdException("Can't have both a configutation file and a service name");
            if ( contains(argRDFS) )
                throw new CmdException("Need to define RDFS setup in the configuration file.");
        } else {
            if ( ! allowEmpty && getPositional().size() == 0 )
                throw new CmdException("Missing service name");
            if ( getPositional().size() > 1 )
                throw new CmdException("Multiple dataset path names given");
            if ( getPositional().size() != 0 )
                serverConfig.datasetPath = DataAccessPoint.canonical(getPositionalArg(0));
        }

        serverConfig.datasetDescription = "<unset>";

        // ---- check: Invalid: --update + --conf
        if ( contains(argUpdate) && contains(argConfig) )
            throw new CmdException("--update and a configuration file does not make sense (control using the configuration file only)");
        boolean allowUpdate = contains(argUpdate);
        serverConfig.allowUpdate = allowUpdate;

        boolean hasJettyConfigFile = contains(argJettyConfig);

        // ---- Port
        serverConfig.port = defaultPort;

        if ( contains(argPort) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify the port and also provide a Jetty configuration file");
            serverConfig.port = portNumber(argPort);
        }

        if ( contains(argLocalhost) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify 'localhost' and also provide a Jetty configuration file");
            serverConfig.loopback = true;
        }

        // ---- Dataset
        // Only one of these is choose from the checking above.

        // Which TDB to use to create a command line TDB database.
        if ( contains(argTDB1mode) )
            useTDB2 = false;
        if ( contains(argTDB2mode) )
            useTDB2 = true;

        if ( allowEmpty ) {
            serverConfig.empty = true;
            serverConfig.datasetDescription = "No dataset";
        }

        // Fuseki config file
        if ( contains(argConfig) ) {
            String file = getValue(argConfig);
            if ( file.startsWith("file:") )
                file = file.substring("file:".length());

            Path path = Path.of(file);
            if ( ! Files.exists(path) )
                throw new CmdException("File not found: "+file);
            if ( Files.isDirectory(path) )
                throw new CmdException("Is a directory: "+file);
            serverConfig.datasetDescription = "Configuration: "+path.toAbsolutePath();
            serverConfig.serverConfig = getValue(argConfig);
        }

        // Ways to setup a dataset.
        if ( contains(argMem) ) {
            serverConfig.datasetDescription = "in-memory";
            // Only one setup should be called by the test above but to be safe
            // and in case of future changes, clear the configuration.
            serverConfig.dsg = DatasetGraphFactory.createTxnMem();
            // Always allow, else you can't do very much!
            serverConfig.allowUpdate = true;
        }

        if ( contains(argFile) ) {
            List<String> filenames = getValues(argFile);
            serverConfig.datasetDescription = "in-memory, with files loaded";
            // Update is not enabled by default for --file
            serverConfig.allowUpdate = contains(argUpdate);
            serverConfig.dsg = DatasetGraphFactory.createTxnMem();

            for(String filename : filenames ) {
                String pathname = filename;
                if ( filename.startsWith("file:") )
                    pathname = filename.substring("file:".length());
                if ( !FileOps.exists(pathname) )
                    throw new CmdException("File not found: " + filename);

                // INITIAL DATA.
                Lang language = RDFLanguages.filenameToLang(filename);
                if ( language == null )
                    throw new CmdException("Can't guess language for file: " + filename);
                Txn.executeWrite(serverConfig.dsg,  ()-> {
                    try {
                        log.info("Dataset: in-memory: load file: " + filename);
                        RDFDataMgr.read(serverConfig.dsg, filename);
                    } catch (RiotException ex) {
                        throw new CmdException("Failed to load file: " + filename);
                    }
                });
            }
        }

        if ( contains(argMemTDB) ) {
            DSGSetup.setupMemTDB(useTDB2, serverConfig);
        }

        if ( contains(argTDB) ) {
            String directory = getValue(argTDB);
            DSGSetup.setupTDB(directory, useTDB2, serverConfig);
        }

        if ( contains(assemblerDescDecl) ) {
            serverConfig.datasetDescription = "Assembler: "+ getValue(assemblerDescDecl);
            // Need to add service details.
            Dataset ds = modDataset.createDataset();
            serverConfig.dsg = ds.asDatasetGraph();
        }

        if ( contains(argRDFS) ) {
            String rdfsVocab = getValue(argRDFS);
            if ( !FileOps.exists(rdfsVocab) )
                throw new CmdException("No such file for RDFS: "+rdfsVocab);
            serverConfig.rdfsGraph = RDFDataMgr.loadGraph(rdfsVocab);
            serverConfig.datasetDescription = serverConfig.datasetDescription+ " (with RDFS)";
            serverConfig.dsg = RDFSFactory.datasetRDFS(serverConfig.dsg, serverConfig.rdfsGraph);
        }

        // ---- Misc features.
        if ( contains(argTimeout) ) {
            String str = getValue(argTimeout);
            ARQ.getContext().set(ARQ.queryTimeout, str);
        }

        if ( contains(argSparqler) ) {
            String filebase = getValue(argSparqler);
            if ( ! FileOps.exists(filebase) )
                throw new CmdException("File area not found: "+filebase);
            serverConfig.contentDirectory = filebase;
            serverConfig.addGeneral = "/sparql";
            serverConfig.empty = true;
            serverConfig.validators = true;
        }

        if ( contains(argGeneralQuerySvc) ) {
            String z = getValue(argGeneralQuerySvc);
            if ( ! z.startsWith("/") )
                z = "/"+z;
            serverConfig.addGeneral = z;
        }

        if ( contains(argValidators) ) {
            serverConfig.validators = true;
        }

        // -- Server setup.

        if ( contains(argBase) ) {
            // Static files.
            String filebase = getValue(argBase);
            if ( ! FileOps.exists(filebase) ) {
                throw new CmdException("File area not found: "+filebase);
                //FmtLog.warn(Fuseki.configLog, "File area not found: "+filebase);
            }
            serverConfig.contentDirectory = filebase;
        }

        if ( contains(argPasswdFile) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify a password file and also provide a Jetty configuration file");
            serverConfig.passwdFile = getValue(argPasswdFile);
        }

        if ( contains(argRealm) )
            serverConfig.realm =  getValue(argRealm);

        if ( contains(argHttpsPort) && ! contains(argHttps) )
            throw new CmdException("https port given but not certificate details via --"+argHttps.getKeyName());

        if ( contains(argHttps) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify \"https\" and also provide a Jetty configuration file");
            serverConfig.httpsPort = defaultHttpsPort;
            if (  contains(argHttpsPort) )
                serverConfig.httpsPort = portNumber(argHttpsPort);
            String httpsSetup = getValue(argHttps);
            // The details go in a separate file that can be secured.
            serverConfig.httpsKeysDetails = httpsSetup;
        }

        if ( contains(argAuth) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify authentication and also provide a Jetty configuration file");
            String schemeStr = getValue(argAuth);
            serverConfig.authScheme = AuthScheme.scheme(schemeStr);
        }

        // Jetty server : this will be the server configuration regardless of other settings.
        if ( contains(argJettyConfig) ) {
            String jettyConfigFile = getValue(argJettyConfig);
            if ( ! FileOps.exists(jettyConfigFile) )
                throw new CmdException("Jetty config file not found: "+jettyConfigFile);
            serverConfig.jettyConfigFile = jettyConfigFile;
        }

        // 2020-10: Ignore argCORS - CORS is now on by default in Fuseki Main cmd
        serverConfig.withCORS = ! contains(argNoCORS);
        serverConfig.withPing = contains(argWithPing);
        serverConfig.withStats = contains(argWithStats);
        serverConfig.withMetrics = contains(argWithMetrics);
        serverConfig.withCompact = contains(argWithCompact);
    }

    private int portNumber(ArgDecl arg) {
        String portStr = getValue(arg);
        if ( portStr.isEmpty() )
            return -1;
        try {
            int port = Integer.parseInt(portStr);
            return port;
        } catch (NumberFormatException ex) {
            throw new CmdException(argPort.getKeyName() + " : bad port number: '" + portStr+"'");
        }
    }

    @Override
    protected void exec() {
        try {
            Logger log = Fuseki.serverLog;
            FusekiMainInfo.logServerCode(log);
            FusekiServer server = buildServer(serverConfig);
            infoCmd(server, log);
            try {
                server.start();
            } catch (FusekiException ex) {
                if ( ex.getCause() instanceof BindException ) {
                    if ( serverConfig.jettyConfigFile == null )
                        Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port="+serverConfig.port);
                    else
                        Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port in use");
                    System.exit(1);
                }
                throw ex;
            } catch (Exception ex) {
                throw new FusekiException("Failed to start server: " + ex.getMessage(), ex);
            }
            server.join();
            System.exit(0);
        }
        catch (AssemblerException | FusekiException  ex) {
            if ( ex.getCause() != null )
                System.err.println(ex.getCause().getMessage());
            else
                System.err.println(ex.getMessage());
            throw new TerminationException(1);
        }
    }

    private FusekiServer buildServer() {
        return buildServer(serverConfig);
    }

    // ServerConfig -> Setup the builder.
    private FusekiServer buildServer(ServerConfig serverConfig) {
        FusekiServer.Builder builder = builder();
        return buildServer(builder, serverConfig);
    }

    protected FusekiServer.Builder builder() {
        return FusekiServer.create();
    }

    private static FusekiServer buildServer(FusekiServer.Builder builder, ServerConfig serverConfig) {
        if ( serverConfig.jettyConfigFile != null )
            builder.jettyServerConfig(serverConfig.jettyConfigFile);
        builder.port(serverConfig.port);
        builder.loopback(serverConfig.loopback);
        builder.verbose(serverConfig.verboseLogging);

        if ( serverConfig.addGeneral != null )
            // Add SPARQL_QueryGeneral as a general servlet, not reached by the service router.
            builder.addServlet(serverConfig.addGeneral,  new SPARQL_QueryGeneral());

        if ( serverConfig.validators ) {
            // Validators.
            builder.addServlet("/$/validate/query",  new QueryValidator());
            builder.addServlet("/$/validate/update", new UpdateValidator());
            builder.addServlet("/$/validate/iri",    new IRIValidator());
            builder.addServlet("/$/validate/data",   new DataValidator());
        }

        if ( ! serverConfig.empty ) {
            if ( serverConfig.serverConfig != null )
                // Config file.
                builder.parseConfigFile(serverConfig.serverConfig);
            else
                // One dataset.
                builder.add(serverConfig.datasetPath, serverConfig.dsg, serverConfig.allowUpdate);
        }

        if ( serverConfig.contentDirectory != null )
            builder.staticFileBase(serverConfig.contentDirectory);

        if ( serverConfig.passwdFile != null )
            builder.passwordFile(serverConfig.passwdFile);

        if ( serverConfig.realm != null )
            builder.realm(serverConfig.realm);

        if ( serverConfig.httpsKeysDetails != null)
            builder.https(serverConfig.httpsPort, serverConfig.httpsKeysDetails);

        if ( serverConfig.authScheme != null )
            builder.auth(serverConfig.authScheme);

        if ( serverConfig.withCORS )
            builder.enableCors(true);

        if ( serverConfig.withPing )
            builder.enablePing(true);

        if ( serverConfig.withStats )
            builder.enableStats(true);

        if ( serverConfig.withMetrics )
            builder.enableMetrics(true);

        if ( serverConfig.withCompact )
            builder.enableCompact(true);

        return builder.build();
    }

    /** Information from the command line setup */
    private void infoCmd(FusekiServer server, Logger log) {
        if ( super.isQuiet() )
            return;

        if ( serverConfig.empty ) {
            FmtLog.info(log, "No SPARQL datasets services");
        } else {
            if ( serverConfig.datasetPath == null && serverConfig.serverConfig == null )
                log.error("No dataset path nor server configuration file");
        }

        DataAccessPointRegistry dapRegistry = DataAccessPointRegistry.get(server.getServletContext());
        if ( serverConfig.datasetPath != null ) {
            if ( dapRegistry.size() != 1 )
                log.error("Expected only one dataset in the DataAccessPointRegistry");
        }

        // Log details on startup.
        String datasetPath = serverConfig.datasetPath;
        String datasetDescription = serverConfig.datasetDescription;
        String serverConfigFile = serverConfig.serverConfig;
        String staticFiles = serverConfig.contentDirectory;
        boolean verbose = serverConfig.verboseLogging;

        if ( ! super.isQuiet() )
            FusekiCoreInfo.logServerCmdSetup(log, verbose, dapRegistry,
                                             datasetPath, datasetDescription, serverConfigFile, staticFiles);
    }

    @Override
    protected String getCommandName() {
        return "fuseki";
    }
}
