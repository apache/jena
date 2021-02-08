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

import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;
import arq.cmdline.ModDatasetAssembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiInfo;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.transaction.TransactionManager;
import org.apache.jena.tdb2.DatabaseMgr;
import org.slf4j.Logger;

public class FusekiMain extends CmdARQ {
    private static int defaultPort          = 3030;
    private static int defaultHttpsPort     = 3043;

    private static ArgDecl  argMem          = new ArgDecl(ArgDecl.NoValue,  "mem");
    private static ArgDecl  argUpdate       = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate");
    private static ArgDecl  argFile         = new ArgDecl(ArgDecl.HasValue, "file");

    private static ArgDecl  argTDB2mode     = new ArgDecl(ArgDecl.NoValue,  "tdb2");
    private static ArgDecl  argMemTDB       = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB", "tdbmem");
    private static ArgDecl  argTDB          = new ArgDecl(ArgDecl.HasValue, "loc", "location", "tdb");

    // No SPARQL dataset or services
    private static ArgDecl  argEmpty        = new ArgDecl(ArgDecl.NoValue,  "empty", "no-dataset");
    private static ArgDecl  argGeneralQuerySvc = new ArgDecl(ArgDecl.HasValue, "general");

    private static ArgDecl  argPort         = new ArgDecl(ArgDecl.HasValue, "port");
    private static ArgDecl  argLocalhost    = new ArgDecl(ArgDecl.NoValue,  "localhost", "local");
    private static ArgDecl  argTimeout      = new ArgDecl(ArgDecl.HasValue, "timeout");
    private static ArgDecl  argConfig       = new ArgDecl(ArgDecl.HasValue, "config", "conf");
    private static ArgDecl  argGZip         = new ArgDecl(ArgDecl.HasValue, "gzip");
    private static ArgDecl  argBase         = new ArgDecl(ArgDecl.HasValue, "base", "files");

    private static ArgDecl  argCORS         = new ArgDecl(ArgDecl.NoValue, "withCORS", "cors", "CORS");
    private static ArgDecl  argNoCORS         = new ArgDecl(ArgDecl.NoValue, "noCORS", "no-cors");
    private static ArgDecl  argWithPing     = new ArgDecl(ArgDecl.NoValue, "withPing", "ping");
    private static ArgDecl  argWithStats    = new ArgDecl(ArgDecl.NoValue, "withStats", "stats");
    private static ArgDecl  argWithMetrics  = new ArgDecl(ArgDecl.NoValue,  "withMetrics", "metrics");

    private static ArgDecl  argAuth         = new ArgDecl(ArgDecl.HasValue, "auth");

    private static ArgDecl  argHttps        = new ArgDecl(ArgDecl.HasValue, "https");
    private static ArgDecl  argHttpsPort    = new ArgDecl(ArgDecl.HasValue, "httpsPort", "httpsport", "sport");

    private static ArgDecl  argPasswdFile   = new ArgDecl(ArgDecl.HasValue, "passwd");
    private static ArgDecl  argRealm        = new ArgDecl(ArgDecl.HasValue, "realm");

    // Same as --empty --validators --general=/sparql, --files=ARG

    private static ArgDecl  argSparqler     = new ArgDecl(ArgDecl.HasValue, "sparqler");

    private static ArgDecl  argValidators   = new ArgDecl(ArgDecl.NoValue,  "validators");
    // private static ModLocation modLocation = new ModLocation();
    private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

    private final ServerConfig serverConfig  = new ServerConfig();
    private boolean useTDB2;

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
        // Control the order!
        add(argMem, "--mem",
            "Create an in-memory, non-persistent dataset for the server");
        add(argFile, "--file=FILE",
            "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file");
        add(argTDB2mode, "--tdb2",
            "Use TDB2 for command line persistent datasets (dfault is TDB1)");
        add(argTDB, "--loc=DIR",
            "Use an existing TDB database (or create if does not exist)");
        add(argMemTDB, "--memTDB",
            "Create an in-memory, non-persistent dataset using TDB (testing only)");
//            add(argEmpty, "--empty",
//                "Run with no datasets and services (validators only)");
        add(argConfig, "--config=",
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
        add(argValidators, "--validators",      "Install validators");
        add(argGeneralQuerySvc, "--general=PATH",    "Add a general SPARQL endpoint (without a dataset) at /PATH");

        add(argAuth, "--auth=[basic|Digest]", "Run the server using basic or digest authentication (dft: digest).");
        add(argHttps, "--https=CONF", "https certificate access details. JSON file { \"cert\":FILE , \"passwd\"; SECRET } ");
        add(argHttpsPort, "--httpsPort=NUM", "https port (default port is 3043)");

        add(argPasswdFile, "--passwd=FILE", "Password file");
        add(argCORS); //, "--cors"); "Enable CORS");
        add(argNoCORS, "--no-cors", "Disable CORS");
        // put in the configuration file
//            add(argRealm, "--realm=REALM", "Realm name");
        add(argWithPing,    "--ping",   "Enable /$/ping");
        add(argWithStats,   "--stats",  "Enable /$/stats");
        add(argWithMetrics, "--metrics",  "Enable /$/metrics");

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
        if ( contains(ModAssembler.assemblerDescDecl) )
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
        } else if ( ! allowEmpty ) {
            if ( getPositional().size() == 0 )
                throw new CmdException("Missing service name");
            if ( getPositional().size() > 1 )
                throw new CmdException("Multiple dataset path names given");
            serverConfig.datasetPath = DataAccessPoint.canonical(getPositionalArg(0));
        }

        serverConfig.datasetDescription = "<unset>";

        // ---- check: Invalid: --update + --conf
        if ( contains(argUpdate) && contains(argConfig) )
            throw new CmdException("--update and a configuration file does not make sense (control using the configuration file only)");
        boolean allowUpdate = contains(argUpdate);
        serverConfig.allowUpdate = allowUpdate;

        // ---- Port
        serverConfig.port = defaultPort;

        if ( contains(argPort) )
            serverConfig.port = portNumber(argPort);

        if ( contains(argLocalhost) )
            serverConfig.loopback = true;

        // ---- Dataset
        // Only one of these is choose from the checking above.

        // Which TDB to use to create a command line TDB database.
        useTDB2 = contains(argTDB2mode);
        String tag = useTDB2 ? "TDB2" : "TDB1";

        if ( allowEmpty ) {
            serverConfig.empty = true;
            serverConfig.datasetDescription = "No dataset";
        }

        // Fuseki config file
        if ( contains(argConfig) ) {
            String file = getValue(argConfig);
            if ( file.startsWith("file:") )
                file = file.substring("file:".length());

            Path path = Paths.get(file);
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
            serverConfig.datasetDescription = tag+" dataset in-memory";
            serverConfig.dsg =
                useTDB2
                ? DatabaseMgr.createDatasetGraph()
                    : TDBFactory.createDatasetGraph();
                serverConfig.allowUpdate = true;
        }

        if ( contains(argTDB) ) {
            String directory = getValue(argTDB);
            DSGSetup.setupTDB(directory, useTDB2, serverConfig);
        }

        if ( contains(ModAssembler.assemblerDescDecl) ) {
            serverConfig.datasetDescription = "Assembler: "+ getValue(ModAssembler.assemblerDescDecl);
            // Need to add service details.
            Dataset ds = modDataset.createDataset();
            serverConfig.dsg = ds.asDatasetGraph();
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

        if ( contains(argBase) ) {
            // Static files.
            String filebase = getValue(argBase);
            if ( ! FileOps.exists(filebase) ) {
                throw new CmdException("File area not found: "+filebase);
                //FmtLog.warn(Fuseki.configLog, "File area not found: "+filebase);
            }
            serverConfig.contentDirectory = filebase;
        }

        if ( contains(argPasswdFile) )
            serverConfig.passwdFile = getValue(argPasswdFile);

        if ( contains(argRealm) )
            serverConfig.realm =  getValue(argRealm);

        if ( contains(argHttpsPort) && ! contains(argHttps) )
            throw new CmdException("https port given but not certificate dtails via --"+argHttps.getKeyName());

        if ( contains(argHttps) ) {
            serverConfig.httpsPort = defaultHttpsPort;
            if (  contains(argHttpsPort) )
                serverConfig.httpsPort = portNumber(argHttpsPort);
            String httpsSetup = getValue(argHttps);
            // The details go in a separate file that can be secured.
            JsonObject httpsConf = JSON.read(httpsSetup);
            Path path = Paths.get(httpsSetup).toAbsolutePath();
            String keystore = httpsConf.get("keystore").getAsString().value();
            // Resolve relative to the https setup file.
            serverConfig.httpsKeystore = path.getParent().resolve(keystore).toString();

            serverConfig.httpsKeystorePasswd = httpsConf.get("passwd").getAsString().value();
        }

        if ( contains(argAuth) ) {
            String schemeStr = getValue(argAuth);
            serverConfig.authScheme = AuthScheme.scheme(schemeStr);
        }

        // 2020-10: Ignore argCORS - CORS is now on by default in Fuseki Main cmd  
        serverConfig.withCORS = ! contains(argNoCORS);
        serverConfig.withPing = contains(argWithPing);
        serverConfig.withStats = contains(argWithStats);
        serverConfig.withMetrics = contains(argWithMetrics);

//            if ( contains(argGZip) ) {
//                if ( !hasValueOfTrue(argGZip) && !hasValueOfFalse(argGZip) )
//                    throw new CmdException(argGZip.getNames().get(0) + ": Not understood: " + getValue(argGZip));
//                jettyServerConfig.enableCompression = super.hasValueOfTrue(argGZip);
//            }
    }

    private int portNumber(ArgDecl arg) {
        String portStr = getValue(arg);
        try {
            int port = Integer.parseInt(portStr);
            return port;
        } catch (NumberFormatException ex) {
            throw new CmdException(argPort.getKeyName() + " : bad port number: " + portStr);
        }

    }

    @Override
    protected void exec() {
        try {
            FusekiServer server = buildServer(serverConfig);
            info(server);
            try {
                server.start();
            } catch (FusekiException ex) {
                if ( ex.getCause() instanceof BindException ) {
                    Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port="+serverConfig.port);
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
        builder.port(serverConfig.port);
        builder.loopback(serverConfig.loopback);
        builder.verbose(serverConfig.verboseLogging);

        if ( serverConfig.addGeneral != null )
            // Add SPARQL_QueryGeneral as a general servlet, not reached by the service router.
            builder.addServlet(serverConfig.addGeneral,  new SPARQL_QueryGeneral());

        if ( serverConfig.validators ) {
            // Validators.
            builder.addServlet("/validate/query",  new QueryValidator());
            builder.addServlet("/validate/update", new UpdateValidator());
            builder.addServlet("/validate/iri",    new IRIValidator());
            builder.addServlet("/validate/data",   new DataValidator());
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

        if ( serverConfig.httpsKeystore != null )
            builder.https(serverConfig.httpsPort, serverConfig.httpsKeystore, serverConfig.httpsKeystorePasswd);

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

        return builder.build();
    }

    private void info(FusekiServer server) {
        if ( super.isQuiet() )
            return;

        Logger log = Fuseki.serverLog;

        FusekiInfo.server(log);

        DataAccessPointRegistry dapRegistry = DataAccessPointRegistry.get(server.getServletContext());
        if ( serverConfig.empty ) {
            FmtLog.info(log, "No SPARQL datasets services");
        } else {
            if ( serverConfig.datasetPath == null && serverConfig.serverConfig == null )
                log.error("No dataset path nor server configuration file");
        }

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
            FusekiInfo.logServerSetup(log, verbose, dapRegistry,
                                      datasetPath, datasetDescription, serverConfigFile, staticFiles);
    }

    @Override
    protected String getCommandName() {
        return "fuseki";
    }
}
