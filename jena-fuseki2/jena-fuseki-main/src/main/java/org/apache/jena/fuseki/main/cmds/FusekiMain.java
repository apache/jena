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
import static org.apache.jena.fuseki.main.cmds.SetupType.*;

import java.net.BindException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModDatasetAssembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.cmd.*;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.main.sys.FusekiServerArgsCustomiser;
import org.apache.jena.fuseki.main.sys.InitFusekiMain;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.FusekiCoreInfo;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral;
import org.apache.jena.fuseki.validation.*;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;

public class FusekiMain extends CmdARQ {

    /** Default HTTP port when running from the command line. */
    public static int defaultPort          = 3030;
    /** Default HTTPS port when running from the command line. */
    public static int defaultHttpsPort     = 3043;

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
     * This function does not return.
     * See also {@link #build} to create and return a server.
     */
    public static void run(String... argv) {
        JenaSystem.init();
        InitFusekiMain.init();
        new FusekiMain(argv).mainRun();
    }

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

    // Use modules found by the ServiceLoader. Currently, no-op.
    private static ArgDecl  argEnableModules  = new ArgDecl(ArgDecl.HasValue,  "modules", "fuseki-modules");

    private static ArgDecl  argAuth         = new ArgDecl(ArgDecl.HasValue, "auth");

    private static ArgDecl  argHttps        = new ArgDecl(ArgDecl.HasValue, "https");
    private static ArgDecl  argHttpsPort    = new ArgDecl(ArgDecl.HasValue, "httpsPort", "httpsport", "sport");

    private static ArgDecl  argPasswdFile   = new ArgDecl(ArgDecl.HasValue, "passwd");
    private static ArgDecl  argRealm        = new ArgDecl(ArgDecl.HasValue, "realm");

    // Same as --empty --validators --general=/sparql, --files=ARG

    private static ArgDecl  argSparqler     = new ArgDecl(ArgDecl.HasValue, "sparqler");

    private static ArgDecl  argValidators   = new ArgDecl(ArgDecl.NoValue,  "validators");

    private ModDatasetAssembler modDataset  = new ModDatasetAssembler();
    private List<FusekiServerArgsCustomiser> customiseServerArgs;
    private final ServerArgs serverArgs     = new ServerArgs();

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
        // Process command line args according to the arguments specified.
        fusekiMain.process();
        // Apply serverArgs to a builder.
        FusekiServer.Builder builder = FusekiServer.create();
        fusekiMain.applyServerArgs(builder, fusekiMain.serverArgs);
        return builder;
    }

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
        ArgCustomizers.addCustomiser(customiser);
    }

    /**
     * Registers CLI customisers.
     * <p>
     * CLI customisers can add one/more custom arguments into the Fuseki Server CLI arguments and then can apply those
     * to the Fuseki server being built during the processing of {@link #processModulesAndArgs()}.  This allows for
     * custom arguments that directly affect how the Fuseki server is built to be created.
     * </p>
     * @see #addCustomiser(FusekiServerArgsCustomiser)
     */
    public static void addCustomisers(FusekiModules customiserSet) {
        Objects.requireNonNull(customiserSet);
        customiserSet.forEach(customiser->ArgCustomizers.addCustomiser(customiser));
    }

    /**
     * Resets any previously registered CLI customisers
     */
    public static void resetCustomisers() {
        ArgCustomizers.resetCustomisers();
    }

    private void applyCustomisers(Consumer<FusekiServerArgsCustomiser> action) {
        for (FusekiServerArgsCustomiser customiser : customiseServerArgs) {
            action.accept(customiser);
        }
    }

    // Customiser Lifecycle
    //
    // :: Setup args
    // -- Called at the end of FusekiMain.argumentsSetup() after the standard arguments have been added.
    // ---- FusekiServerArgsCustomiser.serverArgsModify(CmdLineGeneral, ServerArgs)
    //
    // :: Get values
    // -- End of FusekiMain.processModulesAndArgs
    // ---- FusekiServerArgsCustomiser.serverArgsPrepare(CmdGeneral fusekiCmd, ServerArgs serverArgs)

    // :: Prepare server builder
    // -- End of applyServerArgs
    // ---- FusekiServerArgsCustomiser.serverArgsBuilder(FusekiServer.Builder serverBuilder, Model configModel)

    // == Enter the build lifecycle in builder.build().
    //    Decide FusekiModules for the build.
    // --> then into FusekiBuildCycle.prepare(FusekiServer.Builder serverBuilder, Set<String> datasetNames, Model configModel) { }

    protected FusekiMain(String... argv) {
        super(argv);
        // Snapshot for this FusekiMain instance
        customiseServerArgs = List.copyOf(ArgCustomizers.get());
        argumentsSetup();
    }

    private void argumentsSetup() {
        modVersion.addClass("Fuseki", Fuseki.class);

        getUsage().startCategory("Fuseki");
        add(argConfig, "--config=FILE",
            "Use a configuration file to determine the services");
        // ---- Describe the dataset on the command line.
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
        add(argRDFS, "--rdfs=FILE",
            "Apply RDFS on top of the dataset");
        add(argUpdate, "--update",
                "Allow updates (via SPARQL Update and SPARQL HTTP Update)");
        addModule(modDataset);

        // ---- Server setup
        add(argEmpty); // Hidden
        add(argPort, "--port",
            "Listen on this port number");
        add(argLocalhost, "--localhost",
            "Listen only on the localhost interface");
        add(argGZip, "--gzip=on|off",
                "Enable GZip compression (HTTP Accept-Encoding) if request header set");
        add(argBase, "--base=DIR",
            "Directory for static content");
        add(argContextPath, "--contextPath=PATH",
            "Context path for the server");
        add(argHttps, "--https=CONF",
                "https certificate access details. JSON file { \"cert\":FILE , \"passwd\"; SECRET } ");
        add(argHttpsPort, "--httpsPort=NUM",
                "https port (default port is 3043)");
        add(argPasswdFile, "--passwd=FILE",
                "Password file");
        add(argTimeout, "--timeout=",
                "Global timeout applied to queries (value in ms) -- format is X[,Y] ");
        // ---- Servlets
        add(argSparqler, "--sparqler=DIR",
            "Run with SPARQLer services Directory for static content");
        add(argValidators, "--validators",
            "Install validators");
        add(argGeneralQuerySvc, "--general=PATH",
            "Add a general SPARQL endpoint (without a dataset) at /PATH");

        add(argAuth, "--auth=[basic|digest]",
            "Run the server using basic or digest authentication");
        add(argJettyConfig, "--jetty=FILE",
            "jetty.xml server configuration");
        add(argCORS, "--cors=FILE", "Configure CORS settings from file");
        add(argNoCORS, "--no-cors", "Disable CORS");

        add(argWithPing,    "--ping",       "Enable /$/ping");
        add(argWithStats,   "--stats",      "Enable /$/stats");
        add(argWithMetrics, "--metrics",    "Enable /$/metrics");
        add(argWithCompact, "--compact",    "Enable /$/compact/*");

        add(argEnableModules, "--modules=true|false", "Enable Fuseki autoloaded modules");

        applyCustomisers(customiser -> customiser.serverArgsModify(this, serverArgs));
    }

    static String argUsage = "[--config=FILE|--mem|--loc=DIR|--file=FILE] [--port PORT] /DatasetPathName";

    @Override
    protected String getSummary() {
        return getCommandName() + " " + argUsage;
    }

    @Override
    protected void processModulesAndArgs() {
        Logger log = Fuseki.serverLog;
        serverArgs.verboseLogging = super.isVerbose();
        if ( ! serverArgs.bypassStdArgs )
            processStdArguments(log);
        // Have customisers process their own arguments.
        applyCustomisers(customiser->customiser.serverArgsPrepare(this, serverArgs));
    }

    private void processStdArguments(Logger log) {

        // ---- Command line definition of setup
        // One dataset
        // or a config file
        // or a "standard setup" e.g.SPARQLer
        // or empty allowed
        int numDefinitions = 0;
        SetupType setup = UNSET;

        if ( contains(argMem) ) {
            setup = MEM;
            numDefinitions++;
        }
        if ( contains(argFile) ) {
            setup = FILE;
            numDefinitions++;
        }
        if ( contains(assemblerDescDecl) ) {
            setup = ASSEM;
            numDefinitions++;
        }
        if ( contains(argTDB) ) {
            setup = TDB;
            numDefinitions++;
        }
        if ( contains(argMemTDB) ) {
            setup = MEMTDB;
            numDefinitions++;
        }
        if ( contains(argConfig) ) {
            setup = CONF;
            numDefinitions++;
        }
        if ( contains(argEmpty) ) {
            setup = NONE;
            //numDefinitions++;
        }
        if ( contains(argSparqler) ) {
            setup = SPARQLer;
            //numDefinitions++;
        }

        // ---- Validation

        if ( setup == UNSET && serverArgs.allowEmpty )
            setup = NONE;

        // Starting empty.
        boolean startEmpty = ( setup == NONE || setup == SPARQLer );

        if ( numDefinitions > 1 )
            throw new CmdException("Multiple ways providing a dataset. Only one of --mem, --file, --loc or --conf");

        if ( startEmpty && numDefinitions > 0 )
            throw new CmdException("Dataset provided but 'no dataset' flag given");

        if ( startEmpty && ! getPositional().isEmpty() )
            throw new CmdException("Dataset name provided but 'no dataset' flag given");

        if ( ! startEmpty && numDefinitions == 0 )
            throw new CmdException("No dataset or configuration specified on the command line");

        // Configuration file OR command line dataset
        if ( contains(argConfig) ) {
            // Invalid combination: --conf + arguments related to command line setup.
            if ( ! getPositional().isEmpty() )
                throw new CmdException("Can't have both a configuration file and a service name");
            if ( contains(argRDFS) )
                throw new CmdException("Need to define RDFS setup in the configuration file");
        } else {
            // No --conf
            if ( getPositional().size() > 1 )
                throw new CmdException("Multiple dataset path names given");
            if ( ! startEmpty && getPositional().size() == 0 ) {
                if ( setup == UNSET )
                    throw new CmdException("Missing dataset description and service name");
                else
                    throw new CmdException("Missing service name");
            }
            // Finally!
            if ( getPositional().size() == 1 )
                serverArgs.datasetPath = DataAccessPoint.canonical(getPositionalArg(0));
        }

        // ---- check: Invalid: --update + --conf
        if ( contains(argUpdate) && contains(argConfig) )
            throw new CmdException("--update and a configuration file does not make sense (control using the configuration file only)");
        boolean allowUpdate = contains(argUpdate);
        serverArgs.allowUpdate = allowUpdate;

        // -- Record the choice.
        serverArgs.setup = setup;
        serverArgs.datasetDescription = "<unset>";

        // ---- Dataset
        // A server has one of the command line dataset setups or a configuration file,
        // or "--empty" or "--sparqler"
        // Only one of these is chosen from the checking above.

        // Which TDB to use to create a command line TDB database.
        if ( contains(argTDB1mode) )
            useTDB2 = false;
        if ( contains(argTDB2mode) )
            useTDB2 = true;

        switch(setup) {
            case CONF->{
                serverArgs.serverConfigFile = getValue(argConfig);
            }
            case MEM->{
                serverArgs.dsgMaker = args->DSGSetup.setupMem(log, args);
            }
            case FILE->{
                List<String> filenames = getValues(argFile);
                serverArgs.dsgMaker = args->DSGSetup.setupFile(log, filenames, args);
            }
            case TDB->{
                String directory = getValue(argTDB);
                serverArgs.dsgMaker = args->DSGSetup.setupTDB(log, directory, useTDB2, args);
            }
            case NONE->{
                serverArgs.startEmpty = true;
                serverArgs.datasetDescription = "No dataset";
            }
            case ASSEM->{
                serverArgs.dsgMaker = args->DSGSetup.setupAssembler(log, modDataset, args);
            }
            case MEMTDB->{
                DSGSetup.setupMemTDB(log, useTDB2, serverArgs);
            }
            case UNSET->{
                throw new CmdException("Internal error");
            }
            case SPARQLer -> {
                String filebase = getValue(argSparqler);
                if ( !FileOps.exists(filebase) )
                    throw new CmdException("File area not found: " + filebase);
                serverArgs.contentDirectory = filebase;
                serverArgs.addGeneral = "/sparql";
                serverArgs.startEmpty = true;
                serverArgs.validators = true;
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + setup);
        }

        // ---- RDFS
        if ( contains(argRDFS) ) {
            String rdfsVocab = getValue(argRDFS);
            if ( !FileOps.exists(rdfsVocab) )
                throw new CmdException("No such file for RDFS: "+rdfsVocab);
            serverArgs.rdfsSchemaGraph = RDFDataMgr.loadGraph(rdfsVocab);
        }

        // ---- Misc features.
        if ( contains(argTimeout) ) {
            String str = getValue(argTimeout);
            ARQ.getContext().set(ARQ.queryTimeout, str);
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
            FusekiModules presetModules = serverArgs.fusekiModules;
            // Get auto modules from system-wide setup.
            FusekiModules autoModules = FusekiModules.getSystemModules();

            // Merge preset and auto-loaded modules into one FusekiModules instance.
            if ( presetModules == null ) {
                serverArgs.fusekiModules = autoModules;
            } else {
                List<FusekiModule> allModules = Stream.concat(
                        presetModules.asList().stream(),
                        autoModules.asList().stream())
                    .distinct()
                    .toList();
                serverArgs.fusekiModules = FusekiModules.create(allModules);
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
        FusekiCoreInfo.logCode(Fuseki.serverLog);
        FusekiServer server = execMakeServer();
        execStartServer(server);
    }

    private FusekiServer execMakeServer() {
        try {
            return makeServer(serverArgs);
        } catch (AssemblerException | FusekiException  ex) {
            if ( ex.getCause() != null )
                System.err.println(ex.getCause().getMessage());
            else
                System.err.println(ex.getMessage());
            throw new TerminationException(1);
        }
    }

    /** The method is blocking. */
    private void execStartServer(FusekiServer server) {
        infoCmd(server, Fuseki.serverLog);
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

    /**
     * Take a {@link ServerArgs} and make a {@Link FusekiServer}.
     * The server has not been started.
     */
    private FusekiServer makeServer(ServerArgs serverArgs) {
        FusekiServer.Builder builder = FusekiServer.create();
        applyServerArgs(builder, serverArgs);
        return builder.build();
    }

    /** Apply {@link ServerArgs} to a {@link FusekiServer.Builder}. */
    private void applyServerArgs(FusekiServer.Builder builder, ServerArgs serverArgs) {
        boolean commandLineSetup = ( serverArgs.dataset != null || serverArgs.dsgMaker != null );

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
            builder.addServlet("/$/validate/query",     new QueryValidator());
            builder.addServlet("/$/validate/update",    new UpdateValidator());
            builder.addServlet("/$/validate/iri",       new IRIValidator());
            builder.addServlet("/$/validate/langtag",   new LangTagValidator());
            builder.addServlet("/$/validate/data",      new DataValidator());
        }

        // Apply argument for the database services
        // if not empty
        //   If there is a config model - use that (ignore command line dataset)
        //   If there is a config file - load and use that (ignore command line dataset)
        //   Command line.
        if ( ! serverArgs.startEmpty ) {
            if (serverArgs.serverConfigModel != null ) {
                // -- Customiser has already set the configuration model
                builder.parseConfig(serverArgs.serverConfigModel);
                serverArgs.datasetDescription = "Configuration: provided";
            } else if ( serverArgs.serverConfigFile != null ) {
                // -- Configuration file.
                String file = serverArgs.serverConfigFile;
                if ( file.startsWith("file:") )
                    file = file.substring("file:".length());
                Path path = Path.of(file);
                IOX.checkReadableFile(file, msg->new CmdException(msg));

                serverArgs.datasetDescription = "Configuration: "+path.toAbsolutePath();
                serverArgs.serverConfigModel = RDFParser.source(path).toModel();
                // Add dataset and model declarations.
                AssemblerUtils.prepareForAssembler(serverArgs.serverConfigModel);

                // ... and perform server configuration
                builder.parseConfig(serverArgs.serverConfigModel);
            } else {
                // No serverConfigFile, no serverConfigModel.
                // -- A dataset setup by command line arguments.
                if ( serverArgs.datasetPath == null )
                    throw new CmdException("No URL path name for the dataset");
                // The dataset setup by command line arguments.
                // A customizer may have set the dataset.
                if ( serverArgs.dataset == null ) {
                    // The dsgMaker should set serverArgs.dataset and serverArgs.datasetDescription
                    serverArgs.dsgMaker.accept(serverArgs);
                }
                // This should have been set somehow by this point.
                if ( serverArgs.dataset == null )
                    // Internal error: should have happened during checking earlier.
                    throw new CmdException("Failed to set the dataset service");
                // RDFS -- Command line - add RDFS
                if ( serverArgs.rdfsSchemaGraph != null ) {
                    DSGSetup.setupRDFS(Fuseki.serverLog, serverArgs.rdfsSchemaGraph, serverArgs);
                }
                builder.add(serverArgs.datasetPath, serverArgs.dataset, serverArgs.allowUpdate);
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

        // Allow customisers to inspect and modify the builder.
        applyCustomisers(customiser->customiser.serverArgsBuilder(builder, serverArgs.serverConfigModel));
    }

    /** Information from the command line setup */
    private void infoCmd(FusekiServer server, Logger log) {
        if ( super.isQuiet() )
            return;

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
