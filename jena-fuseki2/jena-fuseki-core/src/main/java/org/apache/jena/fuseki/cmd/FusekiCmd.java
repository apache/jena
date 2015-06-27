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

package org.apache.jena.fuseki.cmd ;

import java.util.List ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLogging ;
import org.apache.jena.fuseki.build.Template ;
import org.apache.jena.fuseki.jetty.JettyFuseki ;
import org.apache.jena.fuseki.jetty.JettyServerConfig ;
import org.apache.jena.fuseki.server.FusekiEnv ;
import org.apache.jena.fuseki.server.FusekiServerListener ;
import org.apache.jena.fuseki.server.ServerInitialConfig ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.sys.Names ;
import org.apache.jena.tdb.transaction.TransactionManager ;
import org.slf4j.Logger ;
import jena.cmd.ArgDecl ;
import jena.cmd.CmdException ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModDatasetAssembler ;

public class FusekiCmd {
    // This allows us to set logging before calling FusekiCmdInner
    // FusekiCmdInner inherits from CmdMain which statically sets logging.
    // By java classloading, super class statics run before the 
    // statics of a class are run.

    static {
        FusekiEnv.mode = FusekiEnv.INIT.STANDALONE ;
        FusekiLogging.setLogging() ;
    }

    static public void main(String... argv) {
        FusekiCmdInner.innerMain(argv);
    }
    
    static class FusekiCmdInner extends CmdARQ {
        // --mgt. --mgtPort  :: Legacy.
        private static ArgDecl  argMgt          = new ArgDecl(ArgDecl.NoValue, "mgt") ;
        private static ArgDecl  argMgtPort      = new ArgDecl(ArgDecl.HasValue, "mgtPort", "mgtport") ;
        
        // --home :: Legacy - do not use.
        private static ArgDecl  argHome         = new ArgDecl(ArgDecl.HasValue, "home") ;
        private static ArgDecl  argPages        = new ArgDecl(ArgDecl.HasValue, "pages") ;

        private static ArgDecl  argMem          = new ArgDecl(ArgDecl.NoValue, "mem") ;
        private static ArgDecl  argAllowUpdate  = new ArgDecl(ArgDecl.NoValue, "update", "allowUpdate") ;
        private static ArgDecl  argFile         = new ArgDecl(ArgDecl.HasValue, "file") ;
        private static ArgDecl  argMemTDB       = new ArgDecl(ArgDecl.NoValue, "memtdb", "memTDB") ;
        private static ArgDecl  argTDB          = new ArgDecl(ArgDecl.HasValue, "loc", "location") ;
        private static ArgDecl  argPort         = new ArgDecl(ArgDecl.HasValue, "port") ;
        private static ArgDecl  argLocalhost    = new ArgDecl(ArgDecl.NoValue, "localhost", "local") ;
        private static ArgDecl  argTimeout      = new ArgDecl(ArgDecl.HasValue, "timeout") ;
        private static ArgDecl  argFusekiConfig = new ArgDecl(ArgDecl.HasValue, "config", "conf") ;
        private static ArgDecl  argJettyConfig  = new ArgDecl(ArgDecl.HasValue, "jetty-config") ;
        private static ArgDecl  argGZip         = new ArgDecl(ArgDecl.HasValue, "gzip") ;

        // Deprecated.  Use shiro.
        private static ArgDecl  argBasicAuth    = new ArgDecl(ArgDecl.HasValue, "basic-auth") ;

        // private static ModLocation modLocation = new ModLocation() ;
        private static ModDatasetAssembler modDataset      = new ModDatasetAssembler() ;

        // fuseki [--mem|--desc assembler.ttl] [--port PORT] **** /datasetURI

        static public void innerMain(String... argv) {
            // Just to make sure ...
            ARQ.init() ;
            TDB.init() ;
            Fuseki.init() ;
            new FusekiCmdInner(argv).mainRun() ;
        }

        private JettyServerConfig   jettyServerConfig = new JettyServerConfig() ;
        {
            jettyServerConfig.port = 3030 ;
            jettyServerConfig.contextPath = "/" ;
            jettyServerConfig.jettyConfigFile = null ;
            jettyServerConfig.pages = Fuseki.PagesStatic ;
            jettyServerConfig.enableCompression = true ;
            jettyServerConfig.verboseLogging = false ;
        }

        private ServerInitialConfig cmdLineDataset  = new ServerInitialConfig() ;

        public FusekiCmdInner(String... argv) {
            super(argv) ;

            if ( false )
                // Consider ...
                TransactionManager.QueueBatchSize = TransactionManager.QueueBatchSize / 2 ;

            getUsage().startCategory("Fuseki") ;
            addModule(modDataset) ;
            add(argMem, "--mem", "Create an in-memory, non-persistent dataset for the server") ;
            add(argFile, "--file=FILE",
                "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file") ;
            add(argTDB, "--loc=DIR", "Use an existing TDB database (or create if does not exist)") ;
            add(argMemTDB, "--memTDB", "Create an in-memory, non-persistent dataset using TDB (testing only)") ;
            add(argPort, "--port", "Listen on this port number") ;
            add(argPages, "--pages=DIR", "Set of pages to serve as static content") ;
            // Set via jetty config file.
            add(argLocalhost, "--localhost", "Listen only on the localhost interface") ;
            add(argTimeout, "--timeout=", "Global timeout applied to queries (value in ms) -- format is X[,Y] ") ;
            add(argAllowUpdate, "--update", "Allow updates (via SPARQL Update and SPARQL HTTP Update)") ;
            add(argFusekiConfig, "--config=", "Use a configuration file to determine the services") ;
            add(argJettyConfig, "--jetty-config=FILE", "Set up the server (not services) with a Jetty XML file") ;
            add(argBasicAuth) ;
            //add(argMgt,     "--mgt",          "Enable the management commands") ;
            add(argMgt) ; // Legacy
            add(argMgtPort) ; // Legacy
            //add(argMgtPort, "--mgtPort=port", "Port for management optations") ;
            //add(argHome, "--home=DIR", "Root of Fuseki installation (overrides environment variable FUSEKI_HOME)") ;
            add(argGZip, "--gzip=on|off", "Enable GZip compression (HTTP Accept-Encoding) if request header set") ;

            //add(argUber) ;
            // add(argGSP) ;

            super.modVersion.addClass(TDB.class) ;
            super.modVersion.addClass(Fuseki.class) ;
        }

        static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName" ;

        @Override
        protected String getSummary() {
            return getCommandName() + " " + argUsage ;
        }

        @Override
        protected void processModulesAndArgs() {
            int x = 0 ;

            Logger log = Fuseki.serverLog ;

            if ( contains(argFusekiConfig) )
                cmdLineDataset.fusekiCmdLineConfigFile = getValue(argFusekiConfig) ;

            ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;

            // ---- Datasets

            if ( contains(argMem) )             
                x++ ;
            if ( contains(argFile) )
                x++ ;
            if ( contains(assemblerDescDecl) )
                x++ ;
            if ( contains(argTDB) )
                x++ ;
            if ( contains(argMemTDB) )
                x++ ;

            if ( cmdLineDataset.fusekiCmdLineConfigFile != null ) {
                if ( x >= 1 )
                    throw new CmdException("Dataset specified on the command line but a configuration file also given.") ;
            } else {
                // No configuration file.  0 or 1 legal.
                if ( x > 1 )
                    throw new CmdException("Multiple ways providing a dataset. Only one of --mem, --file, --loc or --desc") ;
            }
            
            boolean cmdlineConfigPresent = ( x != 0 ) ;

            if ( contains(argMem) ) {
                log.info("Dataset: in-memory") ;
                cmdLineDataset = new ServerInitialConfig() ;
                cmdLineDataset.argTemplateFile = Template.templateMemFN ; 
            }

            if ( contains(argFile) ) {
                String filename = getValue(argFile) ;
                log.info("Dataset: in-memory: load file: " + filename) ;
                if ( !FileOps.exists(filename) )
                    throw new CmdException("File not found: " + filename) ;

                // Directly populate the dataset.
                cmdLineDataset = new ServerInitialConfig() ;
                cmdLineDataset.dsg = DatasetGraphFactory.createMem() ;

                // INITIAL DATA.
                Lang language = RDFLanguages.filenameToLang(filename) ;
                if ( language == null )
                    throw new CmdException("Can't guess language for file: " + filename) ;
                RDFDataMgr.read(cmdLineDataset.dsg, filename) ;
            }

            if ( contains(argMemTDB) ) {
                //log.info("TDB dataset: in-memory") ;
                cmdLineDataset = new ServerInitialConfig() ;
                cmdLineDataset.argTemplateFile = Template.templateTDBMemFN ;
                cmdLineDataset.params.put(Template.DIR, Names.memName) ;
            }

            if ( contains(argTDB) ) {
                cmdLineDataset = new ServerInitialConfig() ;
                cmdLineDataset.argTemplateFile = Template.templateTDBDirFN ;
                String dir = getValue(argTDB) ;
                cmdLineDataset.params.put(Template.DIR, dir) ;
            }

            // Otherwise
            if ( contains(assemblerDescDecl) ) {
                log.info("Dataset from assembler") ;
                // Need to add service details.
                Dataset ds = modDataset.createDataset() ;
                //cmdLineDataset.dsg = ds.asDatasetGraph() ;
            }
            
            if ( cmdlineConfigPresent && getPositional().size() == 0 )
                throw new CmdException("Missing service name") ;
            if ( !cmdlineConfigPresent && getPositional().size() > 0 )
                throw new CmdException("Service name given but no configuration argument to match") ;

            if ( cmdLineDataset != null ) {
                if ( getPositional().size() > 1 )
                    throw new CmdException("Multiple dataset path names given") ;
                if ( getPositional().size() == 1 ) {
                    cmdLineDataset.datasetPath = getPositionalArg(0) ;
                    if ( cmdLineDataset.datasetPath.length() > 0 && !cmdLineDataset.datasetPath.startsWith("/") )
                        throw new CmdException("Dataset path name must begin with a /: " + cmdLineDataset.datasetPath) ;
                    cmdLineDataset.allowUpdate = contains(argAllowUpdate) ;
                    if ( ! cmdLineDataset.allowUpdate )
                        Fuseki.serverLog.info("Running in read-only mode for "+cmdLineDataset.datasetPath) ;
                    // Include the dataset name as NAME for any templates.
                    cmdLineDataset.params.put(Template.NAME,  cmdLineDataset.datasetPath) ;
                }
            }

            // ---- Jetty server
            if ( contains(argBasicAuth) )
                Fuseki.configLog.warn("--basic-auth ignored: Use Apache Shiro security - see shiro.ini") ;

            if ( contains(argPort) ) {
                String portStr = getValue(argPort) ;
                try {
                    jettyServerConfig.port = Integer.parseInt(portStr) ;
                } catch (NumberFormatException ex) {
                    throw new CmdException(argPort.getKeyName() + " : bad port number: " + portStr) ;
                }
            }

            if ( contains(argMgt) )
                Fuseki.configLog.warn("Fuseki v2: Management functions are always enabled.  --mgt not needed.") ; 
            
            if ( contains(argMgtPort) )
                Fuseki.configLog.warn("Fuseki v2: Management functions are always on the same port as the server.  --mgtPort ignored.") ; 

//            if ( contains(argMgt) ) {
//                jettyServerConfig.mgtPort = 0 ;
//                if (  contains(argMgtPort) ) {
//                    String mgtPortStr = getValue(argMgtPort) ;
//                    try {
//                        jettyServerConfig.mgtPort = Integer.parseInt(mgtPortStr) ;
//                    } catch (NumberFormatException ex) {
//                        throw new CmdException("--"+argMgtPort.getKeyName() + " : bad port number: " + mgtPortStr) ;
//                    }
//                }
//            }

            if ( contains(argLocalhost) )
                jettyServerConfig.loopback = true ;

            if ( contains(argTimeout) ) {
                String str = getValue(argTimeout) ;
                ARQ.getContext().set(ARQ.queryTimeout, str) ;
            }

            if ( contains(argJettyConfig) ) {
                jettyServerConfig.jettyConfigFile = getValue(argJettyConfig) ;
                if ( !FileOps.exists(jettyServerConfig.jettyConfigFile) )
                    throw new CmdException("No such file: " + jettyServerConfig.jettyConfigFile) ;
            }

            if ( contains(argBasicAuth) ) {
                jettyServerConfig.authConfigFile = getValue(argBasicAuth) ;
                if ( !FileOps.exists(jettyServerConfig.authConfigFile) )
                    throw new CmdException("No such file: " + jettyServerConfig.authConfigFile) ;
            }

            if ( contains(argHome) ) {
                Fuseki.configLog.warn("--home ignored (use enviroment variables $FUSEKI_HOME and $FUSEKI_BASE)") ;
//                List<String> args = super.getValues(argHome) ;
//                homeDir = args.get(args.size() - 1) ;
            }

            if ( contains(argPages) ) {
                List<String> args = super.getValues(argPages) ;
                jettyServerConfig.pages = args.get(args.size() - 1) ;
            }

            if ( contains(argGZip) ) {
                if ( !hasValueOfTrue(argGZip) && !hasValueOfFalse(argGZip) )
                    throw new CmdException(argGZip.getNames().get(0) + ": Not understood: " + getValue(argGZip)) ;
                jettyServerConfig.enableCompression = super.hasValueOfTrue(argGZip) ;
            }
        }

        private static String sort_out_dir(String path) {
            path.replace('\\', '/') ;
            if ( !path.endsWith("/") )
                path = path + "/" ;
            return path ;
        }

        @Override
        protected void exec() {
            FusekiServerListener.initialSetup = cmdLineDataset ;
            // For standalone, command line use ...
            JettyFuseki.initializeServer(jettyServerConfig) ;
            JettyFuseki.instance.start() ;
            JettyFuseki.instance.join() ;
            System.exit(0) ;
        }

        @Override
        protected String getCommandName() {
            return "fuseki" ;
        }
    }
}