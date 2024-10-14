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

import java.io.File;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModDatasetAssembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.io.IO;
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
import org.apache.jena.fuseki.main.sys.FusekiCliCustomiser;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.FusekiAutoModules;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.system.spot.TDBOps;
import org.apache.jena.fuseki.validation.DataValidator;
import org.apache.jena.fuseki.validation.IRIValidator;
import org.apache.jena.fuseki.validation.QueryValidator;
import org.apache.jena.fuseki.validation.UpdateValidator;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.transaction.TransactionManager;
import org.apache.jena.tdb2.DatabaseMgr;
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

    /**
     * Registers a custom arguments module
     * <p>
     * This approach is useful when your custom arguments affect global server/runtime setup and don't need to directly
     * impact Fuseki Server building.  If you need to impact server building then use
     * {@link #addCustomiser(FusekiCliCustomiser)} instead
     * </p>
     * @param argModule Arguments module
     * @deprecated Register a {@link org.apache.jena.fuseki.main.sys.FusekiCliCustomiser} via
     * {@link #addCustomiser(FusekiCliCustomiser)} instead.
     */
    @Deprecated
    public static void addArgModule(ArgModuleGeneral argModule) { additionalArgs.add(argModule); }

    private static final List<FusekiCliCustomiser> CUSTOMISERS = new ArrayList<>();
    /**
     * Registers a CLI customiser
     * <p>
     * A CLI customiser can add one/more custom arguments into the Fuseki Server CLI arguments and then can apply those
     * to the Fuseki server being built during the processing of {@link #processModulesAndArgs()}.  This allows for
     * custom arguments that directly affect how the Fuseki server is built to be created.
     * </p>
     * @param customiser CLI customiser
     */
    public static void addCustomiser(FusekiCliCustomiser customiser) {
        if (customiser != null) {
            CUSTOMISERS.add(customiser);
        }
    }

    /**
     * Resets any previously registered CLI customisers
     */
    public static void resetCustomisers() {
        CUSTOMISERS.clear();
    }

    // private static ModLocation modLocation = new ModLocation();
    private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

    private final FusekiServer.Builder builder;
    // Default
    private boolean useTDB2 = true;

    // Instance variables used only to track non-builder related aspects of CLI setup for later logging
    private boolean empty = false;
    private String datasetDescription = null, datasetPath = null;

    // -- Programmatic ways to create a server

    /** Build, but do not start, a server based on command line syntax. */
    public static FusekiServer build(String... args) {
        FusekiServer.Builder builder = builder(args);
        return builder.build();
    }

    /**
     * Create a {@link org.apache.jena.fuseki.main.FusekiServer.Builder} which has
     * been setup according to the command line arguments.
     * The builder can be further modified.
     */
    public static FusekiServer.Builder builder(String... args) {
        // Parses command line, sets arguments.
        FusekiMain inner = new FusekiMain(args);
        // Process command line args according to the argument specified, this populates
        // the private instance builder directly
        inner.process();
        return inner.builder;
    }

    /**
     * Create a server and run, within the same JVM.
     * This is the command line entry point.
     */

    static void run(String... argv) {
        JenaSystem.init();
        new FusekiMain(argv).mainRun();
    }

    // --

    protected FusekiMain(FusekiServer.Builder builder, String... argv) {
        super(argv);

        this.builder = builder != null ? builder : FusekiServer.create();

        for (FusekiCliCustomiser customiser : CUSTOMISERS) {
            customiser.customiseCli(this);
        }
    }

    protected FusekiMain(String... argv) {
        this(null, argv);

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
    }

    static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName";

    @Override
    protected String getSummary() {
        return getCommandName() + " " + argUsage;
    }

    @Override
    protected void processModulesAndArgs() {
        Logger log = Fuseki.serverLog;

        builder.verbose(super.isVerbose());

        // Process any CLI customisers up front
        for (FusekiCliCustomiser customiser : CUSTOMISERS) {
            customiser.processCliArgs(this, builder);
        }

        boolean allowEmpty = contains(argEmpty) || contains(argSparqler);
        boolean allowUpdate = false;
        DatasetGraph dsg = null;

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
                this.datasetPath = DataAccessPoint.canonical(getPositionalArg(0));
        }

        this.datasetDescription = "<unset>";

        // ---- check: Invalid: --update + --conf
        if ( contains(argUpdate) && contains(argConfig) )
            throw new CmdException("--update and a configuration file does not make sense (control using the configuration file only)");
        allowUpdate = contains(argUpdate);

        boolean hasJettyConfigFile = contains(argJettyConfig);
        if (hasJettyConfigFile) {
            String jettyConfigFile = getValue(argJettyConfig);
            if ( !FileOps.exists(jettyConfigFile)) {
                throw new CmdException("Jetty config file not found: " + jettyConfigFile);
            }
            builder.jettyServerConfig(jettyConfigFile);
        }

        // ---- Port
        builder.port(defaultPort);

        if ( contains(argPort) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Cannot specify the port and also provide a Jetty configuration file");
            builder.port(portNumber(argPort));
        }

        if ( contains(argLocalhost) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Cannot specify 'localhost' and also provide a Jetty configuration file");
            builder.loopback(true);
        }

        // ---- Dataset
        // Only one of these is chosen from the checking above.

        // Which TDB to use to create a command line TDB database.
        if ( contains(argTDB1mode) )
            useTDB2 = false;
        if ( contains(argTDB2mode) )
            useTDB2 = true;

        if ( allowEmpty ) {
            this.empty = true;
            this.datasetDescription = "No dataset";
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
            this.datasetDescription = "Configuration: "+path.toAbsolutePath();
            builder.parseConfigFile(getValue(argConfig));
        }

        // Ways to set up a dataset.
        if ( contains(argMem) ) {
            this.datasetDescription = "in-memory";
            // Only one setup should be called by the test above but to be safe
            // and in case of future changes, clear the configuration.
            dsg = DatasetGraphFactory.createTxnMem();
            // Always allow, else you can't do very much!
            allowUpdate = true;
        }

        if ( contains(argFile) ) {
            List<String> filenames = getValues(argFile);
            this.datasetDescription = "in-memory, with files loaded";
            // Update is not enabled by default for --file
            allowUpdate = contains(argUpdate);
            dsg = DatasetGraphFactory.createTxnMem();

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
                DatasetGraph finalDsg = dsg;
                Txn.executeWrite(dsg, ()-> {
                    try {
                        log.info("Dataset: in-memory: load file: " + filename);
                        RDFDataMgr.read(finalDsg, filename);
                    } catch (RiotException ex) {
                        throw new CmdException("Failed to load file: " + filename);
                    }
                });
            }
        }

        if ( contains(argMemTDB) ) {
            String tag = useTDB2 ? "TDB2" : "TDB1";
            this.datasetDescription = tag+" dataset in-memory";
            dsg = useTDB2
                               ? DatabaseMgr.createDatasetGraph()
                               : TDB1Factory.createDatasetGraph();
            allowUpdate = true;
        }

        if ( contains(argTDB) ) {
            String directory = getValue(argTDB);
            validateDirectory(directory);
            if ( IO.isEmptyDirectory(directory) ) {
                if ( useTDB2 ) {
                    dsg = setupTDB2(directory);
                }
                else {
                    dsg = setupTDB1(directory);
                }
            } else if ( TDBOps.isTDB1(directory) ) {
                dsg = setupTDB1(directory);
            } else if ( TDBOps.isTDB2(directory) ) {
                dsg = setupTDB2(directory);
            } else {
                throw new CmdException("Directory not a database: " + directory);
            }
        }

        if ( contains(assemblerDescDecl) ) {
            this.datasetDescription = "Assembler: "+ getValue(assemblerDescDecl);
            // Need to add service details.
            Dataset ds = modDataset.createDataset();
            dsg = ds.asDatasetGraph();
        }

        if ( contains(argRDFS) ) {
            String rdfsVocab = getValue(argRDFS);
            if ( !FileOps.exists(rdfsVocab) )
                throw new CmdException("No such file for RDFS: "+rdfsVocab);
            Graph rdfsGraph = RDFDataMgr.loadGraph(rdfsVocab);
            this.datasetDescription = this.datasetDescription + " (with RDFS)";
            dsg = RDFSFactory.datasetRDFS(dsg, rdfsGraph);
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
            builder.staticFileBase(filebase).addServlet("/sparql", new SPARQL_QueryGeneral());
        }

        if ( contains(argGeneralQuerySvc) ) {
            String z = getValue(argGeneralQuerySvc);
            if ( ! z.startsWith("/") )
                z = "/"+z;
            // Add SPARQL_QueryGeneral as a general servlet, not reached by the service router.
            builder.addServlet(z, new SPARQL_QueryGeneral());
        }

        if ( contains(argValidators) || contains(argSparqler) ) {
            // Validators.
            builder.addServlet("/$/validate/query",  new QueryValidator());
            builder.addServlet("/$/validate/update", new UpdateValidator());
            builder.addServlet("/$/validate/iri",    new IRIValidator());
            builder.addServlet("/$/validate/data",   new DataValidator());
        }

        // -- Server setup.

        if ( contains(argContextPath) ) {
            String contextPath = getValue(argContextPath);
            contextPath = sanitizeContextPath(contextPath);
            if ( contextPath != null )
                builder.contextPath(contextPath);
        }

        if ( contains(argBase) ) {
            // Static files.
            String filebase = getValue(argBase);
            if ( ! FileOps.exists(filebase) ) {
                throw new CmdException("File area not found: "+filebase);
            }
            builder.staticFileBase(filebase);
        }

        if ( contains(argPasswdFile) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify a password file and also provide a Jetty configuration file");
            builder.passwordFile(getValue(argPasswdFile));
        }

        if ( contains(argRealm) )
            builder.realm(getValue(argRealm));

        if ( contains(argHttpsPort) && ! contains(argHttps) )
            throw new CmdException("https port given but not certificate details via --"+argHttps.getKeyName());

        if ( contains(argHttps) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify \"https\" and also provide a Jetty configuration file");
            builder.https(contains(argHttpsPort) ? portNumber(argHttpsPort) : defaultHttpsPort, getValue(argHttps));
        }

        if ( contains(argAuth) ) {
            if ( hasJettyConfigFile )
                throw new CmdException("Can't specify authentication and also provide a Jetty configuration file");
            String schemeStr = getValue(argAuth);
            builder.auth(AuthScheme.scheme(schemeStr));
        }

        boolean withModules = hasValueOfTrue(argEnableModules);
        if ( withModules ) {
            // Use the discovered ones.
            FusekiAutoModules.enable(true);
            builder.fusekiModules(FusekiAutoModules.load());
        } else {
            // Disabled module discovery.
            FusekiAutoModules.enable(false);
            builder.fusekiModules(FusekiModules.empty());
        }

        if ( contains(argCORS) ) {
            String corsConfigFile = getValue(argCORS);
            if (!FileOps.exists(corsConfigFile))
                throw new CmdException("CORS config file not found: " + corsConfigFile);
            builder.enableCors(true, corsConfigFile);
        } else {
            builder.enableCors(!contains(argNoCORS));
        }

        builder.enablePing(contains(argWithPing))
               .enableStats(contains(argWithStats))
               .enableMetrics(contains(argWithMetrics))
               .enableCompact(contains(argWithCompact));

        if ( ! this.empty ) {
            // Config file was already applied earlier
            if ( !contains(argConfig) )
                // One dataset.
                builder.add(this.datasetPath, dsg, allowUpdate);
        }
    }

    private DatasetGraph setupTDB2(String directory) {
        DatasetGraph dsg;
        this.datasetDescription = "TDB2 dataset: location=" + directory;
        dsg = DatabaseMgr.connectDatasetGraph(directory);
        return dsg;
    }

    private DatasetGraph setupTDB1(String directory) {
        DatasetGraph dsg;
        this.datasetDescription = "TDB1 dataset: location="+ directory;
        dsg = TDB1Factory.createDatasetGraph(directory);
        return dsg;
    }

    private int portNumber(ArgDecl arg) {
        String portStr = getValue(arg);
        if ( portStr.isEmpty() )
            return -1;
        try {
            return Integer.parseInt(portStr);
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

    private static void validateDirectory(String directory) {
        File dir = Path.of(directory).toFile();
        if ( ! dir.exists() )
            throw new CmdException("Directory does not exist: " + directory);
        if ( ! dir.isDirectory() )
            throw new CmdException("Not a directory: " + directory);
        if ( ! dir.canRead() )
            throw new CmdException("Directory not readable: "+directory) ;
        if ( ! dir.canWrite() )
            throw new CmdException("Directory not writeable: "+directory) ;
    }

    @Override
    protected void exec() {
        try {
            Logger log = Fuseki.serverLog;
            FusekiMainInfo.logServerCode(log);
            FusekiServer server = this.builder.build();
            infoCmd(server, log);
            try {
                server.start();
            } catch (FusekiException ex) {
                if ( ex.getCause() instanceof BindException ) {
                    if ( !contains(argJettyConfig) )
                        Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port="+portNumber(argPort));
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

    /**
     * Returns the internal builder that gets populated by {@link #processModulesAndArgs()}
     * @return Builder
     */
    protected FusekiServer.Builder builder() {
        return FusekiServer.create();
    }

    /** Information from the command line setup */
    private void infoCmd(FusekiServer server, Logger log) {
        if ( super.isQuiet() )
            return;

        if ( this.empty ) {
            FmtLog.info(log, "No SPARQL datasets services");
        } else {
            if ( this.datasetPath == null && !contains(argConfig) )
                log.error("No dataset path nor server configuration file");
        }

        DataAccessPointRegistry dapRegistry = DataAccessPointRegistry.get(server.getServletContext());
        if ( this.datasetPath != null ) {
            if ( dapRegistry.size() != 1 )
                log.error("Expected only one dataset in the DataAccessPointRegistry");
        }

        // Log details on startup.
        if ( ! super.isQuiet() )
            FusekiCoreInfo.logServerCmdSetup(log, super.isVerbose(), dapRegistry,
                                             this.datasetPath, this.datasetDescription, getValue(argConfig),
                                             builder.staticFileBase());
    }

    @Override
    protected String getCommandName() {
        return "fuseki";
    }
}
