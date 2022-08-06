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

package org.apache.jena.fuseki.cmd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModDatasetAssembler;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.jetty.JettyServerConfig;
import org.apache.jena.fuseki.mgt.Template;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.apache.jena.fuseki.webapp.FusekiServerListener;
import org.apache.jena.fuseki.webapp.FusekiWebapp;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.sys.Names;
import org.slf4j.Logger;

/**
 * Handles the fuseki command, used to start a Fuseki Webapp server.
 */
public class FusekiCmd {
    // This allows us to set logging before calling FusekiCmdInner
    // FusekiCmdInner inherits from CmdMain which statically sets logging.
    // By java classloading, super class statics run before the
    // statics of a class are run.

    static {
        FusekiEnv.mode = FusekiEnv.INIT.STANDALONE;
        FusekiEnv.setEnvironment();
        FusekiLogging.setLogging(FusekiEnv.FUSEKI_BASE);
    }

    static public void main(String... argv) {
        FusekiCmdInner.innerMain(argv);
    }

    static class FusekiCmdInner extends CmdARQ {
        private static ArgDecl argMem           = new ArgDecl(ArgDecl.NoValue,  "mem");
        // This does not apply to empty in-memory setups.
        private static ArgDecl argUpdate        = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate");

        private static ArgDecl argFile          = new ArgDecl(ArgDecl.HasValue, "file");
        private static ArgDecl argTDB1mode      = new ArgDecl(ArgDecl.NoValue,  "tdb1");
        private static ArgDecl argTDB2mode      = new ArgDecl(ArgDecl.NoValue,  "tdb2");
        private static ArgDecl argMemTDB        = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB", "tdbmem");
        // And not ModLocation.
        private static ArgDecl argTDB           = new ArgDecl(ArgDecl.HasValue, "loc", "location", "tdb");
        private static ArgDecl argAssemblerDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset");

        // RDFS vocabulary applied to command line defined dataset.
        private static ArgDecl argRDFS          = new ArgDecl(ArgDecl.HasValue, "rdfs");

        private static ArgDecl argPort          = new ArgDecl(ArgDecl.HasValue, "port");
        private static ArgDecl argLocalhost     = new ArgDecl(ArgDecl.NoValue,  "localhost", "local");
        private static ArgDecl argTimeout       = new ArgDecl(ArgDecl.HasValue, "timeout");
        private static ArgDecl argFusekiConfig  = new ArgDecl(ArgDecl.HasValue, "config", "conf");
        private static ArgDecl argJettyConfig   = new ArgDecl(ArgDecl.HasValue, "jetty-config");
        private static ArgDecl argGZip          = new ArgDecl(ArgDecl.HasValue, "gzip");

        // Deprecated.  Use shiro.
        private static ArgDecl argBasicAuth     = new ArgDecl(ArgDecl.HasValue, "basic-auth");

        private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

        static public void innerMain(String... argv) {
            JenaSystem.init();
            // Do explicitly so it happens after subsystem initialization.
            Fuseki.init();
            new FusekiCmdInner(argv).mainRun();
        }

        private JettyServerConfig   jettyServerConfig = new JettyServerConfig();
        {
            jettyServerConfig.port = 3030;
            jettyServerConfig.contextPath = "/";
            jettyServerConfig.jettyConfigFile = null;
            jettyServerConfig.enableCompression = true;
            jettyServerConfig.verboseLogging = false;
        }

        private final FusekiArgs cmdLine = new FusekiArgs();
        private boolean useTDB2 = true;

        public FusekiCmdInner(String... argv) {
            super(argv);

            getUsage().startCategory("Fuseki");
            addModule(modDataset);
            add(argMem, "--mem",
                "Create an in-memory, non-persistent dataset for the server");
            add(argFile, "--file=FILE",
                "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file");
            add(argTDB1mode, "--tdb1",
                "Use TDB1 for command line persistent datasets (default is TDB2)");
            add(argTDB2mode, "--tdb2",
                "Use TDB2 for command line persistent datasets (default is TDB2)");
            add(argTDB, "--loc=DIR",
                "Use an existing TDB database (or create if does not exist)");
            add(argMemTDB, "--memTDB",
                "Create an in-memory, non-persistent dataset using TDB (testing only)");

            // This has proven confusing because it is like --conf.
//            add(argAssemblerDecl, "--desc",
//                "Assembler description of a single database");

            add(argRDFS, "--rdfs=FILE",
                "Apply RDFS on top of the dataset");
            add(argPort, "--port",
                "Listen on this port number");
            // Set via jetty config file.
            add(argLocalhost, "--localhost",
                "Listen only on the localhost interface");
            add(argTimeout, "--timeout=",
                "Global timeout applied to queries (value in ms) -- format is X[,Y] ");
            add(argUpdate, "--update",
                "Allow updates (via SPARQL Update and SPARQL HTTP Update)");
            add(argFusekiConfig, "--config=",
                "Use a configuration file to determine the services");
            add(argJettyConfig, "--jetty-config=FILE",
                "Set up the server (not services) with a Jetty XML file");
            add(argBasicAuth);
            add(argGZip, "--gzip=on|off",
                "Enable GZip compression (HTTP Accept-Encoding) if request header set");

            super.modVersion.addClass(TDB.class);
            super.modVersion.addClass(Fuseki.class);
        }

        private static String argUsage = "[--config=FILE] [--mem|--loc=TDBdatabase|--file=FILE] [--port PORT] /DatasetPathName";

        @Override
        protected String getSummary() {
            return getCommandName() + " " + argUsage;
        }

        @Override
        protected void processModulesAndArgs() {
            if ( super.isVerbose() || super.isDebug() ) {
                jettyServerConfig.verboseLogging = true;
                // Output is still at level INFO (currently)
            }

            cmdLine.quiet = super.isQuiet();
            cmdLine.verbose = super.isVerbose();

            // Any final tinkering with FUSEKI_HOME and FUSEKI_BASE (run area), e.g. arguments like --home, --base, then ....
//            if ( contains(argHome) )
//                Fuseki.configLog.warn("--home ignored (use enviroment variables $FUSEKI_HOME and $FUSEKI_BASE)");

            FusekiEnv.resetEnvironment();

            Logger log = Fuseki.serverLog;

            if ( contains(argFusekiConfig) ) {
                cmdLine.fusekiCmdLineConfigFile = getValue(argFusekiConfig);
                cmdLine.datasetDescription = "Configuration: "+cmdLine.fusekiCmdLineConfigFile;
            }

            // ---- Datasets
            // Check one and only way is defined.
            int x = 0;

            if ( contains(argMem) )
                x++;
            if ( contains(argFile) )
                x++;
            if ( contains(argAssemblerDecl) )
                x++;
            if ( contains(argTDB) )
                x++;
            if ( contains(argMemTDB) )
                x++;

            if ( cmdLine.fusekiCmdLineConfigFile != null ) {
                if ( x >= 1 )
                    throw new CmdException("Dataset specified on the command line but a configuration file also given.");
            } else {
                // No configuration file.  0 or 1 legal.
                if ( x > 1 )
                    throw new CmdException("Multiple ways providing a dataset. Only one of --mem, --file, --loc or --desc");
            }

            boolean cmdlineConfigPresent = ( x != 0 );
            if ( cmdlineConfigPresent && getPositional().size() == 0 )
                throw new CmdException("Missing service name");

            if ( cmdLine.fusekiCmdLineConfigFile != null && getPositional().size() > 0 )
                throw new CmdException("Service name will come from --conf; no command line service name allowed");


            if ( !cmdlineConfigPresent && getPositional().size() > 0 )
                throw new CmdException("Service name given but no configuration argument to match (e.g. --mem, --loc/--tdb, --file)");

            if ( cmdlineConfigPresent && getPositional().size() > 1 )
                throw new CmdException("Multiple dataset path names given");

            if ( ! cmdlineConfigPresent && cmdLine.fusekiCmdLineConfigFile == null ) {
                // Turn command line argument into an absolute file name.
                FusekiEnv.setEnvironment();
                Path cfg = FusekiEnv.FUSEKI_BASE.resolve(FusekiWebapp.DFT_CONFIG).toAbsolutePath();
                if ( Files.exists(cfg) )
                    cmdLine.fusekiServerConfigFile = cfg.toString();
            }

            if ( ! cmdlineConfigPresent && contains(argRDFS) )
                throw new CmdException("Need to define RDFS setup in the configuration file.");

            // ---- General settings.

            cmdLine.allowUpdate = contains(argUpdate);

            // This influences later settings.
            if ( contains(argLocalhost) ) {
                cmdLine.allowUpdate = true;
                jettyServerConfig.loopback = true;
            }

            if ( contains(argTimeout) ) {
                String str = getValue(argTimeout);
                ARQ.getContext().set(ARQ.queryTimeout, str);
            }

            // Which TDB to use to create a command line TDB database.
            if ( contains(argTDB1mode) )
                useTDB2 = false;
            if ( contains(argTDB2mode) )
                useTDB2 = true;

            // ---- Per storage/configuration

            if ( contains(argMem) ) {
                cmdLine.datasetDescription = "in-memory";
                // Only one setup should be called by the test above but to be safe
                // and in case of future changes, clear the configuration.
                cmdLine.reset();
                cmdLine.templateFile = Template.templateTIM_MemFN;
                // Always allow.
                cmdLine.allowUpdate = true;
            }

            if ( contains(argFile) ) {
                List<String> filenames = getValues(argFile);
                // Directly populate the dataset.
                cmdLine.reset();
                cmdLine.dsg = DatasetGraphFactory.createTxnMem();
                // Update is not enabled by default for --file
                cmdLine.allowUpdate = contains(argUpdate);
                cmdLine.datasetDescription = "in-memory, with files loaded";
                for ( String filename : filenames ) {
                    String pathname = filename;
                    if ( filename.startsWith("file:") )
                        pathname = filename.substring("file:".length());
                    if ( !FileOps.exists(filename) )
                        throw new CmdException("File not found: " + filename);
                    Lang language = RDFLanguages.filenameToLang(filename);
                    if ( language == null )
                        throw new CmdException("Can't guess language for file: " + filename);
                    Txn.executeWrite(cmdLine.dsg, ()->{
                        try {
                            log.info("Dataset: in-memory: load file: " + filename);
                            RDFDataMgr.read(cmdLine.dsg, filename);
                        } catch (RiotException ex) {
                            throw new CmdException("Failed to load file: " + filename);
                        }
                    });
                }
            }

            if ( contains(argMemTDB) ) {
                cmdLine.reset();
                cmdLine.templateFile = useTDB2 ? Template.templateTDB2_MemFN : Template.templateTDB1_MemFN;
                cmdLine.params.put(Template.DIR, Names.memName);
                // Always allow.
                cmdLine.allowUpdate = true;
                cmdLine.datasetDescription = useTDB2 ? "TDB2 dataset (in-memory)" : "TDB1 dataset (in-memory)";
            }

            if ( contains(argTDB) ) {
                String dir = getValue(argTDB);
                cmdLine.params.put(Template.DIR, dir);
                cmdLine.reset();
                cmdLine.allowUpdate = contains(argUpdate);
                WebappDSGSetup.setup(dir, useTDB2, cmdLine);
            }

            if ( contains(argRDFS) ) {
                String rdfsVocab = getValue(argRDFS);
                if ( !FileOps.exists(rdfsVocab) )
                    throw new CmdException("No such file for RDFS: "+rdfsVocab);
                cmdLine.rdfsGraph = rdfsVocab;
                cmdLine.datasetDescription = cmdLine.datasetDescription+ " (with RDFS)";
            }

            // This is has proven confusing because it is like --conf.
//            // Otherwise
//            if ( contains(argAssemblerDecl) ) {
//                cmdLine.datasetDescription = "Assembler: "+ modDataset.getAssemblerFile();
//                // Need to add service details.
//                Dataset ds = modDataset.createDataset();
//                //cmdLineDataset.dsg = ds.asDatasetGraph();
//            }

            if ( cmdlineConfigPresent ) {
                cmdLine.datasetPath = getPositionalArg(0);
                if ( cmdLine.datasetPath.length() > 0 && !cmdLine.datasetPath.startsWith("/") )
                    throw new CmdException("Dataset path name must begin with a /: " + cmdLine.datasetPath);
                if ( ! cmdLine.allowUpdate )
                    Fuseki.serverLog.info("Running in read-only mode for "+cmdLine.datasetPath);
                // Include the dataset name as NAME for any templates.
                cmdLine.params.put(Template.NAME,  cmdLine.datasetPath);
            }

            // ---- Jetty server
            if ( contains(argBasicAuth) )
                Fuseki.configLog.warn("--basic-auth ignored: Use Apache Shiro security - see shiro.ini");

            if ( contains(argPort) ) {
                String portStr = getValue(argPort);
                try {
                    jettyServerConfig.port = Integer.parseInt(portStr);
                } catch (NumberFormatException ex) {
                    throw new CmdException(argPort.getKeyName() + " : bad port number: " + portStr);
                }
            }

            if ( contains(argJettyConfig) ) {
                jettyServerConfig.jettyConfigFile = getValue(argJettyConfig);
                if ( !FileOps.exists(jettyServerConfig.jettyConfigFile) )
                    throw new CmdException("No such file: " + jettyServerConfig.jettyConfigFile);
            }

            if ( contains(argGZip) ) {
                if ( !hasValueOfTrue(argGZip) && !hasValueOfFalse(argGZip) )
                    throw new CmdException(argGZip.getNames().get(0) + ": Not understood: " + getValue(argGZip));
                jettyServerConfig.enableCompression = super.hasValueOfTrue(argGZip);
            }
        }

        @Override
        protected void exec() {
            try {
                runFuseki(cmdLine, jettyServerConfig);
            } catch (FusekiException ex) {
                throw new TerminationException(1);
            }
        }

        @Override
        protected String getCommandName() {
            return "fuseki";
        }
    }

    /** Configure and run a Fuseki server - this function does not return except for error starting up */
    public static void runFuseki(FusekiArgs serverConfig, JettyServerConfig jettyConfig) {
        FusekiServerListener.initialSetup = serverConfig;
        JettyFusekiWebapp.initializeServer(jettyConfig);
        JettyFusekiWebapp.instance.start();
        JettyFusekiWebapp.instance.join();
    }
}
