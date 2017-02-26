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

package org.apache.jena.fuseki.server;

import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.StringReader ;
import java.net.URL ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.StandardCopyOption ;
import java.util.* ;

import jena.cmd.CmdException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.build.* ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.tdb.sys.Names ;

public class FusekiServer
{
    // Initialization of FUSEKI_HOME and FUSEKI_BASE is done in FusekiEnv.setEnvironment()
    // so that the code is independent of any logging.  FusekiLogging can use
    // initialized values of FUSEKI_BASE while looking forlog4j configuration.
    
    /** Root of the Fuseki installation for fixed files. 
     * This may be null (e.g. running inside a web application container) */ 
    //public static Path FUSEKI_HOME = null ;
    
    /** Root of the varying files in this deployment. Often $FUSEKI_HOME/run.
     * This is not null - it may be /etc/fuseki, which must be writable.
     */ 
    //public static Path FUSEKI_BASE = null ;
    
    // Relative names of directories in the FUSEKI_BASE area.
    public static final String     runArea                  = FusekiEnv.ENV_runArea ;
    public static final String     databasesLocationBase    = "databases" ;
    // Place to put Lucene text and spatial indexes.
    //private static final String        databaseIndexesDir       = "indexes" ;
      
    public static final String     backupDirNameBase        = "backups" ;
    public static final String     configDirNameBase        = "configuration" ;
    public static final String     logsNameBase             = "logs" ;
    public static final String     systemDatabaseNameBase   = "system" ;
    public static final String     systemFileAreaBase       = "system_files" ;
    public static final String     templatesNameBase        = "templates" ;
    // This name is in web.xml as well.
    public static final String     DFT_SHIRO_INI            = "shiro.ini" ; 
    // In FUSEKI_BASE
    public static final String     DFT_CONFIG               = "config.ttl" ;
    
    /** Directory for TDB databases - this is known to the assembler templates */
    public static Path        dirDatabases       = null ;
    
    /** Directory for writing backups */
    public static Path        dirBackups         = null ;

    /** Directory for assembler files */
    public static Path        dirConfiguration   = null ;
    
    /** Directory for assembler files */
    public static Path        dirLogs            = null ;

    /** Directory for system database */
    public static Path        dirSystemDatabase  = null ;

    /** Directory for files uploaded (e.g upload assmbler descriptions); not data uploads. */
    public static Path        dirFileArea        = null ;
    
    /** Directory for assembler files */
    public static Path        dirTemplates       = null ;

    private static boolean    initialized        = false ;
    // Marks the end of successful initialization.
    /*package*/static boolean serverInitialized  = false ;

    /*package*/ synchronized static void formatBaseArea() {
        if ( initialized )
            return ;
        initialized = true ;
        try {
            FusekiEnv.setEnvironment() ;
            Path FUSEKI_HOME = FusekiEnv.FUSEKI_HOME ;
            Path FUSEKI_BASE = FusekiEnv.FUSEKI_BASE ;
            
            Fuseki.init() ;
            Fuseki.configLog.info("FUSEKI_HOME="+ ((FUSEKI_HOME==null) ? "unset" : FUSEKI_HOME.toString())) ;
            Fuseki.configLog.info("FUSEKI_BASE="+FUSEKI_BASE.toString());

            // ----  Check FUSEKI_HOME and FUSEKI_BASE
            // If FUSEKI_HOME exists, it may be FUSEKI_BASE.

            if ( FUSEKI_HOME != null ) {
                if ( ! Files.isDirectory(FUSEKI_HOME) )
                    throw new FusekiConfigException("FUSEKI_HOME is not a directory: "+FUSEKI_HOME) ;
                if ( ! Files.isReadable(FUSEKI_HOME) )
                    throw new FusekiConfigException("FUSEKI_HOME is not readable: "+FUSEKI_HOME) ;
            }

            if ( Files.exists(FUSEKI_BASE) ) {
                if ( ! Files.isDirectory(FUSEKI_BASE) )
                    throw new FusekiConfigException("FUSEKI_BASE is not a directory: "+FUSEKI_BASE) ;
                if ( ! Files.isWritable(FUSEKI_BASE) )
                    throw new FusekiConfigException("FUSEKI_BASE is not writable: "+FUSEKI_BASE) ;
            } else {
                ensureDir(FUSEKI_BASE);
            }

            // Ensure FUSEKI_BASE has the assumed directories.
            dirTemplates        = writeableDirectory(FUSEKI_BASE, templatesNameBase) ;
            dirDatabases        = writeableDirectory(FUSEKI_BASE, databasesLocationBase) ;
            dirBackups          = writeableDirectory(FUSEKI_BASE, backupDirNameBase) ;
            dirConfiguration    = writeableDirectory(FUSEKI_BASE, configDirNameBase) ;
            dirLogs             = writeableDirectory(FUSEKI_BASE, logsNameBase) ;
            dirSystemDatabase   = writeableDirectory(FUSEKI_BASE, systemDatabaseNameBase) ;
            dirFileArea         = writeableDirectory(FUSEKI_BASE, systemFileAreaBase) ;
            //Possible intercept point

            // ---- Initialize with files.

            if ( Files.isRegularFile(FUSEKI_BASE) ) 
                throw new FusekiConfigException("FUSEKI_BASE exists but is a file") ;

            // Copy missing files into FUSEKI_BASE
            copyFileIfMissing(null, DFT_SHIRO_INI, FUSEKI_BASE) ;
            copyFileIfMissing(null, DFT_CONFIG, FUSEKI_BASE) ;
            for ( String n : Template.templateNames ) {
                copyFileIfMissing(null, n, FUSEKI_BASE) ;
            }

            serverInitialized = true ;
        } catch (RuntimeException ex) {
            Fuseki.serverLog.error("Exception in server initialization", ex) ;
            throw ex ;
        }
    }
    
    /** Copy a file from src to dst under name fn.
     * If src is null, try as a classpath resource
     * @param src   Source directory, or null meaning use java resource. 
     * @param fn    File name, a relative path.
     * @param dst   Destination directory.
     * 
     */
    private static void copyFileIfMissing(Path src, String fn, Path dst) {
        
        Path dstFile = dst.resolve(fn) ;
        if ( Files.exists(dstFile) )
            return ;
        
        // fn may be a path.
        if ( src != null ) {
            try {
                Files.copy(src.resolve(fn), dstFile, StandardCopyOption.COPY_ATTRIBUTES) ;
            } catch (IOException e) {
                IO.exception("Failed to copy file "+src, e);
                e.printStackTrace();
            }
        } else {
            try {
                // Get from the file from area "org/apache/jena/fuseki/server"  (our package)
                URL url = FusekiServer.class.getResource(fn) ;
                if ( url == null )
                    throw new FusekiConfigException("Field to find resource '"+fn+"'") ; 
                InputStream in = url.openStream() ;
                Files.copy(in, dstFile) ;
            }
            catch (IOException e) {
                IO.exception("Failed to copy file from resource: "+src, e);
                e.printStackTrace();
            }
        }
    }

    public static void initializeDataAccessPoints(DataAccessPointRegistry registry, ServerInitialConfig initialSetup, String configDir) {
        List<DataAccessPoint> configFileDBs = initServerConfiguration(initialSetup) ;
        List<DataAccessPoint> directoryDBs =  FusekiConfig.readConfigurationDirectory(configDir) ;
        List<DataAccessPoint> systemDBs =     FusekiConfig.readSystemDatabase(SystemState.getDataset()) ;
        
        List<DataAccessPoint> datapoints = new ArrayList<>() ;
        datapoints.addAll(configFileDBs) ;
        datapoints.addAll(directoryDBs) ;
        datapoints.addAll(systemDBs) ;
        
        // Having found them, set them all running.
        enable(registry, datapoints);
    }

    private static void enable(DataAccessPointRegistry registry, List<DataAccessPoint> datapoints) {
        for ( DataAccessPoint dap : datapoints ) {
            Fuseki.configLog.info("Register: "+dap.getName()) ;
            registry.register(dap.getName(), dap); 
        }
    }

    private static List<DataAccessPoint> initServerConfiguration(ServerInitialConfig params) { 
        // Has a side effect of global context setting
        // when processing a config file.
        // Compatibility.
        
        List<DataAccessPoint> datasets = new ArrayList<>();
        if ( params == null )
            return datasets ;

        if ( params.fusekiCmdLineConfigFile != null ) {
            List<DataAccessPoint> confDatasets = processServerConfigFile(params.fusekiCmdLineConfigFile) ;
            datasets.addAll(confDatasets) ;
        }
        else if ( params.fusekiServerConfigFile != null ) {
            List<DataAccessPoint> confDatasets = processServerConfigFile(params.fusekiServerConfigFile) ;
            datasets.addAll(confDatasets) ;
        }
        else if ( params.dsg != null ) {
            DataAccessPoint dap = datasetDefaultConfiguration(params.datasetPath, params.dsg, params.allowUpdate) ;
            datasets.add(dap) ;
        } else if ( params.argTemplateFile != null ) {
            DataAccessPoint dap = configFromTemplate(params.argTemplateFile, params.datasetPath, params.allowUpdate, params.params) ;
            datasets.add(dap) ;
        }
        // No datasets is valid.
        return datasets ;
    }
    
    private static List<DataAccessPoint> processServerConfigFile(String configFilename) {
        if ( ! FileOps.exists(configFilename) ) {
            Fuseki.configLog.warn("Configuration file '" + configFilename+"' does not exist") ;
            return Collections.emptyList(); 
        }
        Fuseki.configLog.info("Configuration file: " + configFilename) ;
        return FusekiConfig.readServerConfigFile(configFilename) ;
    }
    
    private static DataAccessPoint configFromTemplate(String templateFile, String datasetPath, 
                                                      boolean allowUpdate, Map<String, String> params) {
        DatasetDescriptionRegistry registry = FusekiServer.registryForBuild() ; 
        // ---- Setup
        if ( params == null ) {
            params = new HashMap<>() ;
            params.put(Template.NAME, datasetPath) ;
        } else {
            if ( ! params.containsKey(Template.NAME) ) {
                Fuseki.configLog.warn("No NAME found in template parameters (added)") ;
                params.put(Template.NAME, datasetPath) ;   
            }
        }
        //-- Logging
        Fuseki.configLog.info("Template file: " + templateFile) ;
        String dir = params.get(Template.DIR) ;
        if ( dir != null ) {
            if ( Objects.equals(dir, Names.memName) ) {
                Fuseki.configLog.info("TDB dataset: in-memory") ;
            } else {
                if ( !FileOps.exists(dir) )
                    throw new CmdException("Directory not found: " + dir) ;
                Fuseki.configLog.info("TDB dataset: directory=" + dir) ;
            }
        }
        //-- Logging
        
        datasetPath = DataAccessPoint.canonical(datasetPath) ;
        
        // DRY -- ActionDatasets (and others?)
        addGlobals(params); 

        String str = TemplateFunctions.templateFile(templateFile, params, Lang.TTL) ;
        Lang lang = RDFLanguages.filenameToLang(str, Lang.TTL) ;
        StringReader sr =  new StringReader(str) ;
        Model model = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(model, sr, datasetPath, lang);
        
        // ---- DataAccessPoint
        Statement stmt = getOne(model, null, FusekiVocab.pServiceName, null) ;
        if ( stmt == null ) {
            StmtIterator sIter = model.listStatements(null, FusekiVocab.pServiceName, (RDFNode)null ) ;
            if ( ! sIter.hasNext() )
                ServletOps.errorBadRequest("No name given in description of Fuseki service") ;
            sIter.next() ;
            if ( sIter.hasNext() )
                ServletOps.errorBadRequest("Multiple names given in description of Fuseki service") ;
            throw new InternalErrorException("Inconsistent: getOne didn't fail the second time") ;
        }
        Resource subject = stmt.getSubject() ;
        if ( ! allowUpdate ) {
            // Opportunity for more sophisticated "read-only" mode.
            //  1 - clean model, remove "fu:serviceUpdate", "fu:serviceUpload", "fu:serviceReadGraphStore", "fu:serviceReadWriteGraphStore"
            //  2 - set a flag on DataAccessPoint
        }
        DataAccessPoint dap = FusekiBuilder.buildDataAccessPoint(subject, registry) ;
        return dap ;
    }
    
    public static void addGlobals(Map<String, String> params) {
        if ( params == null ) {
            Fuseki.configLog.warn("FusekiServer.addGlobals : params is null", new Throwable()) ;
            return ;
        }
        
        if ( ! params.containsKey("FUSEKI_BASE") )
            params.put("FUSEKI_BASE", pathStringOrElse(FusekiEnv.FUSEKI_BASE, "unset")) ;
        if ( ! params.containsKey("FUSEKI_HOME") )
            params.put("FUSEKI_HOME", pathStringOrElse(FusekiEnv.FUSEKI_HOME, "unset")) ;
    }

    private static String pathStringOrElse(Path path, String dft) {
        if ( path == null )
            return dft ;
        return path.toString() ;
    }
    
    // DRY -- ActionDatasets (and others?)
    private static Statement getOne(Model m, Resource s, Property p, RDFNode o) {
        StmtIterator iter = m.listStatements(s, p, o) ;
        if ( ! iter.hasNext() )
            return null ;
        Statement stmt = iter.next() ;
        if ( iter.hasNext() )
            return null ;
        return stmt ;
    }
    
    private static DataAccessPoint datasetDefaultConfiguration( String name, DatasetGraph dsg, boolean allowUpdate) {
        name = DataAccessPoint.canonical(name) ;
        DataService ds = FusekiBuilder.buildDataService(dsg, allowUpdate) ;
        DataAccessPoint dap = new DataAccessPoint(name, ds) ;
        return dap ;
    }
    
    // ---- Helpers

    /** Ensure a directory exists, creating it if necessary.
     */
    private static void ensureDir(Path directory) {
        File dir = directory.toFile() ;
        if ( ! dir.exists() ) {
            boolean b = dir.mkdirs() ;
            if ( ! b )
                throw new FusekiConfigException("Failed to create directory: "+directory) ;
        }
        else if ( ! dir.isDirectory())
            throw new FusekiConfigException("Not a directory: "+directory) ;
    }

    private static void mustExist(Path directory) {
        File dir = directory.toFile() ;
        if ( ! dir.exists() )
            throw new FusekiConfigException("Does not exist: "+directory) ; 
        if ( ! dir.isDirectory())
            throw new FusekiConfigException("Not a directory: "+directory) ;
    }
    
    private static boolean emptyDir(Path dir) {
        return dir.toFile().list().length <= 2 ;
    }
    
    private static boolean exists(Path directory) {
        File dir = directory.toFile() ;
        return dir.exists() ;
    }

    private static Path writeableDirectory(Path root , String relName ) {
        Path p = makePath(root, relName) ;
        ensureDir(p);
        if ( ! Files.isWritable(p) )
            throw new FusekiConfigException("Not writable: "+p) ;
        return p ;
    }
    
    private static Path makePath(Path root , String relName ) {
        Path path = root.resolve(relName) ;
        // Must exist
//        try { path = path.toRealPath() ; }
//        catch (IOException e) { IO.exception(e) ; }
        return path ;
    }

    /**
     * <ul>
     * <li>GLOBAL: sharing across all descriptions
     * <li>FILE: sharing within files but not across files.
     * </ul>
     */
    enum DatasetDescriptionScope { GLOBAL, FILE }
    private static DatasetDescriptionRegistry globalDatasets = new DatasetDescriptionRegistry() ;
    private static DatasetDescriptionScope policyDatasetDescriptionScope = DatasetDescriptionScope.FILE ;
    
    /** Call this once per configuration file. */
    public static DatasetDescriptionRegistry registryForBuild() {
        switch (policyDatasetDescriptionScope) {
            case FILE :     return new DatasetDescriptionRegistry() ;
            case GLOBAL :   return globalDatasets ;
            default:        throw new InternalErrorException() ;
        }
    }
}
