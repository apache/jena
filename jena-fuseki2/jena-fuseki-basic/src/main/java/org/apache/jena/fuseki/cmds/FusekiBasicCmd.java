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

package org.apache.jena.fuseki.cmds;

import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;
import arq.cmdline.ModDatasetAssembler;
import jena.cmd.ArgDecl;
import jena.cmd.CmdException;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.lib.DateTimeUtils ;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.servlets.SPARQL_QueryGeneral ;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.validation.DataValidator ;
import org.apache.jena.fuseki.validation.IRIValidator ;
import org.apache.jena.fuseki.validation.QueryValidator ;
import org.apache.jena.fuseki.validation.UpdateValidator ;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.transaction.TransactionManager;
import org.apache.jena.tdb2.DatabaseMgr;
import org.slf4j.Logger;

/** Fuseki command that runs a Fuseki server with no UI, just SPARQL services.
 * <p>
 * Use {@code --conf=} for multiple datasets and specific service names. 
 * <p>
 * The command line dataset setup only supports a single dataset.
 */

public class FusekiBasicCmd {
    // Own module (or optional dependency on jena-cmds)
    // Needs jena-cmds.
    
    static {
        FusekiLogging.setLogging();
    }

    static public void main(String... argv) {
        FusekiCmdInner.innerMain(argv);
    }
    
    /** Dataset setup (command line, config file) for a dataset (or several if config file) */
    static class ServerConfig {
        public int port;
        // Dataset name on the command line.
        public String    datasetPath      = null; 
        // Command line --update.
        public boolean   allowUpdate      = false;
        
        // This is set ...
        public DatasetGraph dsg           = null;
        // ... or this.
        public String serverConfig        = null;
        

        // Allow there to be no registered datasets without it being an error.
        // which is "return  dsg==null && serverConfig==null;"
        public boolean empty              = false ;
        // Setup for SPARQLer - validators and general query engine, some pages. 
        public boolean sparqler           = false ;
        public boolean validators         = false ;
        public boolean loopback           = false;
        public String datasetDescription;
        public String contentDirectory    = null;
    }
    
    static class FusekiCmdInner extends CmdARQ {
        private static int defaultPort = 3030;
        
        private static ArgDecl  argMem          = new ArgDecl(ArgDecl.NoValue,  "mem");
        private static ArgDecl  argUpdate       = new ArgDecl(ArgDecl.NoValue,  "update", "allowUpdate");
        private static ArgDecl  argFile         = new ArgDecl(ArgDecl.HasValue, "file");

        private static ArgDecl  argTDB2mode     = new ArgDecl(ArgDecl.NoValue,  "tdb2");
        private static ArgDecl  argMemTDB       = new ArgDecl(ArgDecl.NoValue,  "memtdb", "memTDB", "tdbmem");
        private static ArgDecl  argTDB          = new ArgDecl(ArgDecl.HasValue, "loc", "location", "tdb");
        
        // No SPARQL dataset or services
        private static ArgDecl  argEmpty        = new ArgDecl(ArgDecl.NoValue,  "empty", "no-dataset");
        private static ArgDecl  argPort         = new ArgDecl(ArgDecl.HasValue, "port");
        private static ArgDecl  argLocalhost    = new ArgDecl(ArgDecl.NoValue,  "localhost", "local");
        private static ArgDecl  argTimeout      = new ArgDecl(ArgDecl.HasValue, "timeout");
        private static ArgDecl  argConfig       = new ArgDecl(ArgDecl.HasValue, "config", "conf");
        private static ArgDecl  argGZip         = new ArgDecl(ArgDecl.HasValue, "gzip");
        private static ArgDecl  argBase         = new ArgDecl(ArgDecl.HasValue, "base", "files");
        private static ArgDecl  argSparqler     = new ArgDecl(ArgDecl.HasValue, "sparqler");
        private static ArgDecl  argValidators   = new ArgDecl(ArgDecl.NoValue,  "validators");
        // private static ModLocation modLocation = new ModLocation();
        private static ModDatasetAssembler modDataset      = new ModDatasetAssembler();

        static public void innerMain(String... argv) {
            JenaSystem.init();
            new FusekiCmdInner(argv).mainRun();
        }

        private final ServerConfig serverConfig  = new ServerConfig();
        private boolean useTDB2;
        
        public FusekiCmdInner(String... argv) {
            super(argv);

            if ( false )
                // Consider ...
                TransactionManager.QueueBatchSize = TransactionManager.QueueBatchSize / 2;

            getUsage().startCategory("Fuseki");
            addModule(modDataset);
            add(argMem, "--mem",
                "Create an in-memory, non-persistent dataset for the server");
            add(argFile, "--file=FILE",
                "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file");
            add(argTDB2mode, "--tdb2",
                "Create command line persistent datasets with TDB2");
            add(argTDB, "--loc=DIR",
                "Use an existing TDB database (or create if does not exist)");
            add(argMemTDB, "--memTDB",
                "Create an in-memory, non-persistent dataset using TDB (testing only)");
//            add(argEmpty, "--empty",
//                "Run with no datasets and services (validators only)");
            add(argEmpty); // Hidden for now.
            add(argPort, "--port",
                "Listen on this port number");
            add(argLocalhost, "--localhost",
                "Listen only on the localhost interface");
            add(argTimeout, "--timeout=",
                "Global timeout applied to queries (value in ms) -- format is X[,Y] ");
            add(argUpdate, "--update",
                "Allow updates (via SPARQL Update and SPARQL HTTP Update)");
            add(argConfig, "--config=",
                "Use a configuration file to determine the services");
            add(argGZip, "--gzip=on|off",
                "Enable GZip compression (HTTP Accept-Encoding) if request header set");
            add(argBase, "--base=DIR",
                "Directory for static content");
            add(argSparqler, "--sparqler=DIR",
                "Run with SPARQLer services Directory for static content");
            add(argValidators, "--validators", "Install validators");

            super.modVersion.addClass(TDB.class);
            super.modVersion.addClass(Fuseki.class);
        }

        static String argUsage = "[--config=FILE] [--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName";

        @Override
        protected String getSummary() {
            return getCommandName() + " " + argUsage;
        }

        @Override
        protected void processModulesAndArgs() {
            int x = 0;

            Logger log = Fuseki.serverLog;

            // ---- Checking

            if ( contains(argMem) )             
                x++;
            if ( contains(argFile) )
                x++;
            if ( contains(ModAssembler.assemblerDescDecl) )
                x++;
            if ( contains(argTDB) )
                x++;
            if ( contains(argMemTDB) )
                x++;
            if ( contains(argConfig) )
                x++;

            boolean allowEmpty = contains(argEmpty) || contains(argSparqler);
            
            
            if ( x == 0 && ! allowEmpty )
                throw new CmdException("No dataset specified on the command line.");

            if ( x > 1 )
                throw new CmdException("Multiple ways providing a dataset. Only one of --mem, --file, --loc or --desc");
            
            if ( x > 0 && allowEmpty )
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
            
            if ( contains(argPort) ) {
                String portStr = getValue(argPort);
                try {
                    int port = Integer.parseInt(portStr);
                    serverConfig.port = port;
                } catch (NumberFormatException ex) {
                    throw new CmdException(argPort.getKeyName() + " : bad port number: " + portStr);
                }
            }
            if ( contains(argLocalhost) )
                serverConfig.loopback = true;

            // ---- Dataset
            // Only one of these is choose from the checking above.
            
            // Which TDB to use to create a command line TDB database. 
            useTDB2 = contains(argTDB2mode);
            String tag = useTDB2 ? "TDB2" : "TDB";
            
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
                String filename = getValue(argFile);
                String pathname = filename;
                if ( filename.startsWith("file:") )
                    pathname = filename.substring("file:".length());

                serverConfig.datasetDescription = "file:"+filename;
                if ( !FileOps.exists(pathname) )
                    throw new CmdException("File not found: " + filename);
                serverConfig.dsg = DatasetGraphFactory.createTxnMem();
                
                // INITIAL DATA.
                Lang language = RDFLanguages.filenameToLang(filename);
                if ( language == null )
                    throw new CmdException("Can't guess language for file: " + filename);
                Txn.executeWrite(serverConfig.dsg,  ()->RDFDataMgr.read(serverConfig.dsg, filename));
            }

//            if ( contains(argMemTDB) ) {
//                //log.info("TDB dataset: in-memory") ;
//                cmdLineConfig.reset();
//                cmdLineConfig.argTemplateFile = useTDB2 ? Template.templateTDB2_MemFN : Template.templateTDB1_MemFN ;
//                cmdLineConfig.params.put(Template.DIR, Names.memName) ;
//                // Always allow.
//                cmdLineConfig.allowUpdate = true ;
//                cmdLineConfig.datasetDescription = useTDB2 ? "TDB2 dataset (in-memory)" : "TDB dataset (in-memory)";
//            }
//
//            if ( contains(argTDB) ) {
//                cmdLineConfig.reset();
//                cmdLineConfig.argTemplateFile = 
//                    useTDB2 ? Template.templateTDB2_DirFN : Template.templateTDB1_DirFN;
//                String dir = getValue(argTDB) ;
//                cmdLineConfig.params.put(Template.DIR, dir) ;
//                cmdLineConfig.datasetDescription = useTDB2 ? "TDB2 dataset: "+dir : "TDB dataset: "+dir;
//            }
            
            if ( contains(argMemTDB) ) {
                serverConfig.datasetDescription = tag+" dataset in-memory";
                serverConfig.dsg =
                    useTDB2
                    ? DatabaseMgr.createDatasetGraph()
                    : TDBFactory.createDatasetGraph();
                serverConfig.allowUpdate = true;
            }

            if ( contains(argTDB) ) {
                String dir = getValue(argTDB);
                serverConfig.datasetDescription = tag+" dataset: "+dir;
                serverConfig.dsg = 
                    useTDB2
                    ? DatabaseMgr.connectDatasetGraph(dir)
                    : TDBFactory.createDatasetGraph(dir);
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
            
            if ( contains(argValidators) ) {
                serverConfig.validators = true;
            }
                
            if ( contains(argSparqler) ) {
                String filebase = getValue(argSparqler);
                if ( ! FileOps.exists(filebase) )
                    throw new CmdException("File area not found: "+filebase); 
                serverConfig.contentDirectory = filebase;
                serverConfig.sparqler = true;
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

//            if ( contains(argGZip) ) {
//                if ( !hasValueOfTrue(argGZip) && !hasValueOfFalse(argGZip) )
//                    throw new CmdException(argGZip.getNames().get(0) + ": Not understood: " + getValue(argGZip));
//                jettyServerConfig.enableCompression = super.hasValueOfTrue(argGZip);
//            }
        }

//        private static String sort_out_dir(String path) {
//            path.replace('\\', '/');
//            if ( !path.endsWith("/") )
//                path = path + "/";
//            return path;
//        }

        @Override
        protected void exec() {
            try {
                FusekiServer server = buildServer(serverConfig);
                info(server, serverConfig);
                try {
                    server.start();
                } catch (FusekiException ex) {
                    if ( ex.getCause() instanceof BindException ) {
                        Fuseki.serverLog.error("Failed to start server: "+ex.getCause().getMessage()+ ": port="+serverConfig.port) ;
                        System.exit(1);
                    }
                    throw ex;
                } catch (Exception ex) {
                    throw new FusekiException("Failed to start server: " + ex.getMessage(), ex) ;
                }
                server.join();
                System.exit(0);
            }
            catch (AssemblerException ex) {
                if ( ex.getCause() != null )
                    System.err.println(ex.getCause().getMessage());
                else
                    System.err.println(ex.getMessage());
            }
        }

        // ServerConfig -> Setup the builder.
        private static FusekiServer buildServer(ServerConfig serverConfig) {
            FusekiServer.Builder builder = FusekiServer.create();
            // Loopback.
            builder.setPort(serverConfig.port);
            builder.setLoopback(serverConfig.loopback);
            
            if ( serverConfig.validators ) {
                if ( serverConfig.sparqler )
                    builder.addServlet("/sparql",  new SPARQL_QueryGeneral());
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
                builder.setStaticFileBase(serverConfig.contentDirectory) ;

            return builder.build();
        }

        private void info(FusekiServer server, ServerConfig serverConfig) {
            if ( super.isQuiet() )
                return;

            Logger log = Fuseki.serverLog;

            String version = Fuseki.VERSION;
            String buildDate = Fuseki.BUILD_DATE ;

            if ( version != null && version.equals("${project.version}") )
                version = null ;
            if ( buildDate != null && buildDate.equals("${build.time.xsd}") )
                buildDate = DateTimeUtils.nowAsXSDDateTimeString() ;
            
            String name = Fuseki.NAME;
            name = name +" (basic server)";
            
            if ( version != null ) {
                if ( Fuseki.developmentMode && buildDate != null )
                    FmtLog.info(log, "%s %s %s", name, version, buildDate) ;
                else
                    FmtLog.info(log, "%s %s", name, version);
            }
            
            // Dataset -> Endpoints
            Map<String, List<String>> mapDatasetEndpoints = description(DataAccessPointRegistry.get(server.getServletContext()));
            
            if ( serverConfig.empty ) {
                FmtLog.info(log, "No SPARQL datasets services"); 
            } else {
                if ( serverConfig.datasetPath == null && serverConfig.serverConfig == null )
                    log.error("No dataset path nor server configuration file");
            }
            
            if ( serverConfig.datasetPath != null ) {
                if ( mapDatasetEndpoints.size() != 1 ) {
                    System.err.println(serverConfig.datasetPath);
                    log.error("Expected only one dataset");
                }
                List<String> endpoints = mapDatasetEndpoints.get(serverConfig.datasetPath); 
                FmtLog.info(log,  "Dataset Type = %s", serverConfig.datasetDescription);
                FmtLog.info(log,  "Path = %s; Services = %s", serverConfig.datasetPath, endpoints);
            }
            if ( serverConfig.serverConfig != null ) {
                // May be many datasets and services.
                FmtLog.info(log,  "Configuration file %s", serverConfig.serverConfig);
                mapDatasetEndpoints.forEach((path, endpoints)->{
                    FmtLog.info(log,  "Path = %s; Services = %s", path, endpoints);
                });
            }
            
            if ( serverConfig.contentDirectory != null )
                FmtLog.info(log,  "Static files = %s", serverConfig.contentDirectory);
                
            if ( super.isVerbose() )
                PlatformInfo.logDetailsVerbose(log);
            else if ( !super.isQuiet() )
                PlatformInfo.logDetails(log);
        }

        private static Map<String, List<String>> description(DataAccessPointRegistry reg) {
            Map<String, List<String>> desc = new LinkedHashMap<>();
            reg.forEach((ds,dap)->{
                List<String> endpoints = new ArrayList<>();
                desc.put(ds, endpoints);
                DataService dSrv = dap.getDataService();
                dSrv.getOperations().forEach((op)->{
                    dSrv.getEndpoints(op).forEach(ep-> {
                        String x = ep.getEndpoint();
                        if ( x.isEmpty() )
                            x = "quads";
                        endpoints.add(x);   
                    });
                });
            });
            return desc;
        }
        
        @Override
        protected String getCommandName() {
            return "fuseki";
        }
    }
}
