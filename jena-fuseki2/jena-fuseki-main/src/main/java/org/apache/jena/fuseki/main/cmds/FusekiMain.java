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
import java.util.Objects;

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
import org.apache.jena.fuseki.main.sys.FusekiAutoModules;
import org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser;
import org.apache.jena.fuseki.main.sys.FusekiModules;
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
import org.apache.jena.riot.*;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb1.transaction.TransactionManager;
import org.slf4j.Logger;

public class FusekiMain extends CmdARQ {
    /** Default HTTP port when running from the command line. */
    public static int defaultPort          = 3030;
    /** Default HTTPS port when running from the command line. */
    public static int defaultHttpsPort     = 3043;

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
    // Set the servlet context path (the initial path for URLs.) for any datasets.
    // A context of "/path" and a dataset name of "/ds", service "sparql" is accessed as "/path/ds/sparql"
    private static ArgDecl  argContextPath  = new ArgDecl(ArgDecl.HasValue, "pathBase", "contextPath", "pathbase", "contextpath");
    // Static files. URLs are affected by argPathBase
    private static ArgDecl  argBase         = new ArgDecl(ArgDecl.HasValue, "base", "files");

    private static ArgDecl  argCORS         = new ArgDecl(ArgDecl.HasValue, "withCORS", "cors", "CORS", "cors-config");
    private static ArgDecl  argNoCORS       = new ArgDecl(ArgDecl.NoValue,  "noCORS", "no-cors");
    private static ArgDecl  argWithPing     = new ArgDecl(ArgDecl.NoValue,  "withPing", "ping");
    private static ArgDecl  argWithStats    = new ArgDecl(ArgDecl.NoValue,  "withStats", "stats");
    private static ArgDecl  argWithMetrics  = new ArgDecl(ArgDecl.NoValue,  "withMetrics", "metrics");
    private static ArgDecl  argWithCompact  = new ArgDecl(ArgDecl.NoValue,  "withCompact", "compact");

    // Default is "true" and use modules found by the ServiceLoader.
    private static ArgDecl  argEnableModules  = new ArgDecl(ArgDecl.HasValue,  "modules", "fuseki-modules");

    private static ArgDecl  argAuth         = new ArgDecl(ArgDecl.HasValue, "auth");

    private static ArgDecl  argHttps        = new ArgDecl(ArgDecl.HasValue, "https");
    private static ArgDecl  argHttpsPort    = new ArgDecl(ArgDecl.HasValue, "httpsPort", "httpsport", "sport");

    private static ArgDecl  argPasswdFile   = new ArgDecl(ArgDecl.HasValue, "passwd");
    private static ArgDecl  argRealm        = new ArgDecl(ArgDecl.HasValue, "realm");

    // Same as --empty --validators --general=/sparql, --files=ARG

    private static ArgDecl  argSparqler     = new ArgDecl(ArgDecl.HasValue, "sparqler");

    private static ArgDecl  argValidators   = new ArgDecl(ArgDecl.NoValue,  "validators");

    private static List<ArgModuleGeneral> additionalArgs = new ArrayList<>();

    // private static ModLocation modLocation = new ModLocation();
    private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

    private final ServerArgs serverArgs  = new ServerArgs();
    // Default
    private boolean useTDB2 = true;

    // -- Programmatic ways to create a server using command line syntax.

    /**
     * Create a {@link org.apache.jena.fuseki.main.FusekiServer.Builder} which has
     * been setup according to the command line arguments.
     * The builder can be further modified.
     */
    public static FusekiServer.Builder builder(String... args) {
        // Parses command line, sets arguments.
        FusekiMain fusekiMain = new FusekiMain(args);
        // Process command line args according to the argument specified.
        fusekiMain.process();
        // Apply command line/serverArgs to a builder.
        FusekiServer.Builder builder = fusekiMain.builder();
        applyServerArgs(builder, fusekiMain.serverArgs);
        return builder;
    }

    /**
     * Build, but do not start, a server based on command line syntax.
     */
    public static FusekiServer build(String... args) {
        FusekiServer.Builder builder = builder(args);
        return builder.build();
    }

    /**
     * Create a server and run, within the same JVM.
     * This is the command line entry point.
     */
    static void run(String... argv) {
        JenaSystem.init();
        new FusekiMain(argv).mainRun();
    }

    /**
     * Registers a custom arguments module
     * <p>
     * This approach is useful when your custom arguments affect global server/runtime setup and don't need to directly
     * impact Fuseki Server building.  If you need to impact server building then use
     * {@link #addCustomiser(FusekiServerArgsCustomiser)} instead
     * </p>
     * @param argModule Arguments module
     * @deprecated Register a {@link org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser} via
     * {@link #addCustomiser(FusekiServerArgsCustomiser)} instead.
     */
    @Deprecated(forRemoval =  true)
    public static void addArgModule(ArgModuleGeneral argModule) { additionalArgs.add(argModule); }

    private static final List<FusekiServerArgsCustomiser> customiseServerArgs = new ArrayList<>();
    /**
     * Registers a CLI customiser
     * <p>
     * A CLI customiser can add one/more custom arguments into the Fuseki Server CLI arguments and then can apply those
     * to the Fuseki server being built during the processing of {@link #processModulesAndArgs()}.  This allows for
     * custom arguments that directly affect how the Fuseki server is built to be created.
     * </p>
     * @param customiser CLI customiser
     */
    public static void addCustomiser(FusekiServerArgsCustomiser customiser) {
        Objects.requireNonNull(customiser);
        customiseServerArgs.add(customiser);
    }

    /**
     * Resets any previously registered CLI customisers
     */
    public static void resetCustomisers() {
        customiseServerArgs.clear();
    }

    // --

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
        add(argContextPath, "--contextPath=PATH",
            "Context path for the server");
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
        add(argCORS, "--cors=FILE", "Configure CORS settings from file");
        add(argNoCORS, "--no-cors", "Disable CORS");
        // put in the configuration file
//            add(argRealm, "--realm=REALM", "Realm name");

        add(argWithPing,    "--ping",       "Enable /$/ping");
        add(argWithStats,   "--stats",      "Enable /$/stats");
        add(argWithMetrics, "--metrics",    "Enable /$/metrics");
        add(argWithCompact, "--compact",    "Enable /$/compact/*");

        add(argEnableModules, "--modules=true|false", "Enable Fuseki modules");

        super.modVersion.addClass("Fuseki", Fuseki.class);

        for (FusekiServerArgsCustomiser customiser : customiseServerArgs) {
            customiser.serverArgsModify(this);
        }
    }

    static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName";

    @Override
    protected String getSummary() {
        return getCommandName() + " " + argUsage;
    }

    @Override
    protected void processModulesAndArgs() {
        Logger log = Fuseki.serverLog;

        serverArgs.verboseLogging = super.isVerbose();

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
                throw new CmdException("Can't have both a configuration file and a service name");
            if ( contains(argRDFS) )
                throw new CmdException("Need to define RDFS setup in the configuration file.");
        } else {
            if ( ! allowEmpty && getPositional().size() == 0 )
                throw new CmdException("Missing service name");
            if ( getPositional().size() > 1 )
                throw new CmdException("Multiple dataset path names given");
            if ( getPositional().size() != 0 )
                serverArgs.datasetPath = DataAccessPoint.canonical(getPositionalArg(0));
        }

        serverArgs.datasetDescription = "<unset>";

        // ---- check: Invalid: --update + --conf
        if ( contains(argUpdate) && contains(argConfig) )
            throw new CmdException("--update and a configuration file does not make sense (control using the configuration file only)");
        boolean allowUpdate = contains(argUpdate);
        serverArgs.allowUpdate = allowUpdate;

        boolean hasJettyConfigFile = contains(argJettyConfig);

        // ---- Port
        serverArgs.port = defaultPort;

        if ( contains(argPort) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Cannot specify the port and also provide a Jetty configuration file");
            serverArgs.port = portNumber(argPort);
        }

        if ( contains(argLocalhost) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Cannot specify 'localhost' and also provide a Jetty configuration file");
            serverArgs.loopback = true;
        }

        // ---- Dataset
        // Only one of these is chosen from the checking above.

        // Which TDB to use to create a command line TDB database.
        if ( contains(argTDB1mode) )
            useTDB2 = false;
        if ( contains(argTDB2mode) )
            useTDB2 = true;

        if ( allowEmpty ) {
            serverArgs.empty = true;
            serverArgs.datasetDescription = "No dataset";
        }

        // Fuseki config file
        if ( contains(argConfig) )
            serverArgs.serverConfigFile = getValue(argConfig);

        // Ways to set up a dataset.
        if ( contains(argMem) ) {
            serverArgs.datasetDescription = "in-memory";
            // Only one setup should be called by the test above but to be safe
            // and in case of future changes, clear the configuration.
            serverArgs.dsg = DatasetGraphFactory.createTxnMem();
            // Always allow, else you can't do very much!
            serverArgs.allowUpdate = true;
        }

        if ( contains(argFile) ) {
            List<String> filenames = getValues(argFile);
            serverArgs.datasetDescription = "in-memory, with files loaded";
            // Update is not enabled by default for --file
            serverArgs.allowUpdate = contains(argUpdate);
            serverArgs.dsg = DatasetGraphFactory.createTxnMem();

            for(String filename : filenames ) {
                String pathname = filename;
                if ( filename.startsWith("file:") )
                    pathname = filename.substring("file:".length());
                if ( !FileOps.exists(pathname) )
                    throw new CmdException("File not found: " + filename);

                // INITIAL DATA.
                Lang language = RDFLanguages.filenameToLang(filename);
                if ( language == null )
                    throw new CmdException("Cannot guess language for file: " + filename);
                Txn.executeWrite(serverArgs.dsg,  ()-> {
                    try {
                        log.info("Dataset: in-memory: load file: " + filename);
                        RDFDataMgr.read(serverArgs.dsg, filename);
                    } catch (RiotException ex) {
                        throw new CmdException("Failed to load file: " + filename);
                    }
                });
            }
        }

        if ( contains(argMemTDB) ) {
            DSGSetup.setupMemTDB(useTDB2, serverArgs);
        }

        if ( contains(argTDB) ) {
            String directory = getValue(argTDB);
            DSGSetup.setupTDB(directory, useTDB2, serverArgs);
        }

        if ( contains(assemblerDescDecl) ) {
            serverArgs.datasetDescription = "Assembler: "+ getValue(assemblerDescDecl);
            // Need to add service details.
            Dataset ds = modDataset.createDataset();
            serverArgs.dsg = ds.asDatasetGraph();
        }

        if ( contains(argRDFS) ) {
            String rdfsVocab = getValue(argRDFS);
            if ( !FileOps.exists(rdfsVocab) )
                throw new CmdException("No such file for RDFS: "+rdfsVocab);
            serverArgs.rdfsGraph = RDFDataMgr.loadGraph(rdfsVocab);
            serverArgs.datasetDescription = serverArgs.datasetDescription+ " (with RDFS)";
            serverArgs.dsg = RDFSFactory.datasetRDFS(serverArgs.dsg, serverArgs.rdfsGraph);
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
            serverArgs.contentDirectory = filebase;
            serverArgs.addGeneral = "/sparql";
            serverArgs.empty = true;
            serverArgs.validators = true;
        }

        if ( contains(argGeneralQuerySvc) ) {
            String z = getValue(argGeneralQuerySvc);
            if ( ! z.startsWith("/") )
                z = "/"+z;
            serverArgs.addGeneral = z;
        }

        if ( contains(argValidators) ) {
            serverArgs.validators = true;
        }

        // -- Server setup.

        if ( contains(argContextPath) ) {
            String contextPath = getValue(argContextPath);
            contextPath = sanitizeContextPath(contextPath);
            if ( contextPath != null )
                serverArgs.servletContextPath = contextPath;
        }

        if ( contains(argBase) ) {
            // Static files.
            String filebase = getValue(argBase);
            if ( ! FileOps.exists(filebase) ) {
                throw new CmdException("File area not found: "+filebase);
                //FmtLog.warn(Fuseki.configLog, "File area not found: "+filebase);
            }
            serverArgs.contentDirectory = filebase;
        }

        if ( contains(argPasswdFile) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify a password file and also provide a Jetty configuration file");
            serverArgs.passwdFile = getValue(argPasswdFile);
        }

        if ( contains(argRealm) )
            serverArgs.realm =  getValue(argRealm);

        if ( contains(argHttpsPort) && ! contains(argHttps) )
            throw new CmdException("https port given but not certificate details via --"+argHttps.getKeyName());

        if ( contains(argHttps) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify \"https\" and also provide a Jetty configuration file");
            serverArgs.httpsPort = defaultHttpsPort;
            if (  contains(argHttpsPort) )
                serverArgs.httpsPort = portNumber(argHttpsPort);
            String httpsSetup = getValue(argHttps);
            // The details go in a separate file that can be secured.
            serverArgs.httpsKeysDetails = httpsSetup;
        }

        if ( contains(argAuth) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify authentication and also provide a Jetty configuration file");
            String schemeStr = getValue(argAuth);
            serverArgs.authScheme = AuthScheme.scheme(schemeStr);
        }

        // Jetty server : this will be the server configuration regardless of other settings.
        if ( contains(argJettyConfig) ) {
            String jettyConfigFile = getValue(argJettyConfig);
            if ( ! FileOps.exists(jettyConfigFile) )
                throw new CmdException("Jetty config file not found: "+jettyConfigFile);
            serverArgs.jettyConfigFile = jettyConfigFile;
        }

        boolean withModules = hasValueOfTrue(argEnableModules);
        if ( withModules ) {
            // Use the discovered ones.
            FusekiAutoModules.enable(true);
            // Allows for external setting of serverArgs.fusekiModules
            if ( serverArgs.fusekiModules == null ) {
                FusekiAutoModules.setup();
                serverArgs.fusekiModules = FusekiAutoModules.load();
            }
        } else {
            // Disabled module discovery.
            FusekiAutoModules.enable(false);
            // Allows for external setting of serverArgs.fusekiModules
            if ( serverArgs.fusekiModules == null ) {
                serverArgs.fusekiModules = FusekiModules.empty();
            }
        }

        if ( contains(argCORS) ) {
            String corsConfigFile = getValue(argCORS);
            if ( ! FileOps.exists(corsConfigFile) )
                throw new CmdException("CORS config file not found: "+corsConfigFile);
            serverArgs.corsConfigFile = corsConfigFile;
        } else if (contains(argNoCORS)) {
            serverArgs.withCORS = ! contains(argNoCORS);
        }

        serverArgs.withPing = contains(argWithPing);
        serverArgs.withStats = contains(argWithStats);
        serverArgs.withMetrics = contains(argWithMetrics);
        serverArgs.withCompact = contains(argWithCompact);

        // Deal with any customisers
        for (FusekiServerArgsCustomiser customiser : customiseServerArgs) {
            customiser.serverArgsPrepare(this, serverArgs);
        }
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

    private static String sanitizeContextPath(String contextPath) {
        if ( contextPath.isEmpty() )
            return null;
        if ( contextPath.equals("/") )
            return null;
        if ( contextPath.endsWith("/") ) {
            throw new CmdException("Path base must not end with \"/\": '"+contextPath+"'");
            //contextPath = StringUtils.chop(contextPath);
        }
        if ( ! contextPath.startsWith("/") )
            contextPath = "/"+contextPath;
        return contextPath;
    }

    @Override
    protected void exec() {
        try {
            Logger log = Fuseki.serverLog;
            FusekiMainInfo.logServerCode(log);
            FusekiServer server = makeServer(serverArgs);
            infoCmd(server, log);
            try {
                server.start();
            } catch (FusekiException ex) {
                if ( ex.getCause() instanceof BindException ) {
                    if ( serverArgs.jettyConfigFile == null )
                        Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port="+serverArgs.port);
                    else
                        Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port in use");
                    System.exit(1);
                }
                throw ex;
            } catch (Exception ex) {
                throw new FusekiException("Failed to start server: " + ex.getMessage(), ex);
            }
            // This does not normally return.
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

    /**
     * Take a {@link ServerArgs} and make a {@Link FusekiServer}.
     * The server has not been started.
     */
    private FusekiServer makeServer(ServerArgs serverArgs) {
        FusekiServer.Builder builder = builder();
        return buildServer(builder, serverArgs);
    }

    protected FusekiServer.Builder builder() {
        return FusekiServer.create();
    }

    /**
     * Process {@link ServerArgs} and build a server.
     * The server has not been started.
     */
    private static FusekiServer buildServer(FusekiServer.Builder builder, ServerArgs serverArgs) {
        applyServerArgs(builder, serverArgs);
        return builder.build();
    }

    /** Apply {@link ServerArgs} to a {@link FusekiServer.Builder}. */
    private static void applyServerArgs(FusekiServer.Builder builder, ServerArgs serverArgs) {
        if ( serverArgs.jettyConfigFile != null )
            builder.jettyServerConfig(serverArgs.jettyConfigFile);
        builder.port(serverArgs.port);
        builder.loopback(serverArgs.loopback);
        builder.verbose(serverArgs.verboseLogging);

        if ( serverArgs.addGeneral != null )
            // Add SPARQL_QueryGeneral as a general servlet, not reached by the service router.
            builder.addServlet(serverArgs.addGeneral,  new SPARQL_QueryGeneral());

        if ( serverArgs.validators ) {
            // Validators.
            builder.addServlet("/$/validate/query",  new QueryValidator());
            builder.addServlet("/$/validate/update", new UpdateValidator());
            builder.addServlet("/$/validate/iri",    new IRIValidator());
            builder.addServlet("/$/validate/data",   new DataValidator());
        }

//        if ( ! serverArgs.empty ) {
//            if ( serverArgs.serverConfig != null )
//                // Config file.
//                builder.parseConfigFile(serverArgs.serverConfig);
//            else
//                // One dataset.
//                builder.add(serverArgs.datasetPath, serverArgs.dsg, serverArgs.allowUpdate);
//        }

        if ( ! serverArgs.empty ) {
            // A CLI customiser may have already set the model.
            if (serverArgs.serverConfigModel != null ) {
                builder.parseConfig(serverArgs.serverConfigModel);
                serverArgs.datasetDescription = "Configuration: provided";
            } else if ( serverArgs.serverConfigFile != null ) {
                // if there is a configuration file, read it.
                // Read the model.
                String file = serverArgs.serverConfigFile;
                if ( file.startsWith("file:") )
                    file = file.substring("file:".length());
                Path path = Path.of(file);
                if ( ! Files.exists(path) )
                    throw new CmdException("File not found: "+file);
                if ( Files.isDirectory(path) )
                    throw new CmdException("Is a directory: "+file);
                serverArgs.datasetDescription = "Configuration: "+path.toAbsolutePath();
                serverArgs.serverConfigModel = RDFParser.source(path).toModel();
                builder.parseConfig(serverArgs.serverConfigModel);
            } else {
                // One dataset up by command line arguments.
                if ( serverArgs.dsg == null || serverArgs.datasetPath == null ) {
                    // Internal error: should have happened during checking earlier.
                    throw new CmdException("Failed to set the dataset service");
                }
                builder.add(serverArgs.datasetPath, serverArgs.dsg, serverArgs.allowUpdate);
            }
        }

        if ( serverArgs.fusekiModules != null )
            builder.fusekiModules(serverArgs.fusekiModules);

        if ( serverArgs.servletContextPath != null )
            builder.contextPath(serverArgs.servletContextPath);

        if ( serverArgs.contentDirectory != null )
            builder.staticFileBase(serverArgs.contentDirectory);

        if ( serverArgs.passwdFile != null )
            builder.passwordFile(serverArgs.passwdFile);

        if ( serverArgs.realm != null )
            builder.realm(serverArgs.realm);

        if ( serverArgs.httpsKeysDetails != null)
            builder.https(serverArgs.httpsPort, serverArgs.httpsKeysDetails);

        if ( serverArgs.authScheme != null )
            builder.auth(serverArgs.authScheme);

        if ( serverArgs.withCORS )
            builder.enableCors(true, serverArgs.corsConfigFile);

        if ( serverArgs.withPing )
            builder.enablePing(true);

        if ( serverArgs.withStats )
            builder.enableStats(true);

        if ( serverArgs.withMetrics )
            builder.enableMetrics(true);

        if ( serverArgs.withCompact )
            builder.enableCompact(true);

        // Allow customisers to process their own arguments.
        for (FusekiServerArgsCustomiser customiser : customiseServerArgs) {
            customiser.serverArgsBuilder(builder, builder.configModel());
        }
    }

    /** Information from the command line setup */
    private void infoCmd(FusekiServer server, Logger log) {
        if ( super.isQuiet() )
            return;

        if ( serverArgs.empty ) {
            FmtLog.info(log, "No SPARQL datasets services");
        } else {
            if ( serverArgs.datasetPath == null && serverArgs.serverConfigModel == null )
                log.error("No dataset path nor server configuration file");
        }

        DataAccessPointRegistry dapRegistry = DataAccessPointRegistry.get(server.getServletContext());
        if ( serverArgs.datasetPath != null ) {
            if ( dapRegistry.size() != 1 )
                log.error("Expected only one dataset in the DataAccessPointRegistry");
        }

        // Log details on startup.
        String datasetPath = serverArgs.datasetPath;
        String datasetDescription = serverArgs.datasetDescription;
        String serverConfigFile = serverArgs.serverConfigFile;
        String staticFiles = serverArgs.contentDirectory;
        boolean verbose = serverArgs.verboseLogging;

        if ( ! super.isQuiet() )
            FusekiCoreInfo.logServerCmdSetup(log, verbose, dapRegistry,
                                             datasetPath, datasetDescription, serverConfigFile, staticFiles);
    }

    @Override
    protected String getCommandName() {
        return "fuseki";
    }
}
