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
import java.io.FilenameFilter ;
import java.io.IOException ;
import java.io.StringReader ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.nio.file.StandardCopyOption ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.build.Builder ;
import org.apache.jena.fuseki.build.FusekiConfig ;
import org.apache.jena.fuseki.build.Template ;
import org.apache.jena.fuseki.build.TemplateFunctions ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.sys.Names ;

public class FusekiServer
{
    /** Root of the Fuseki installation for fixed files. */ 
    public static Path FUSEKI_HOME = null ;
    /** Root of the varying files in this deployment. Often $FUSEKI_HOME/run */ 
    public static Path FUSEKI_BASE = null ;

    private static FilenameFilter filterConfig = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("config") ;
        }
    } ;
    
    // Relative names of directories
    private static final String        runArea                  = "run" ;
    private static final String        databasesLocationBase    = "databases" ;
    private static final String        backupDirNameBase        = "backups" ;
    private static final String        configDirNameBase        = "configuration" ;
    private static final String        logsNameBase             = "logs" ;
    private static final String        systemDatabaseNameBase   = "system" ;
    private static final String        systemFileAreaBase       = "system_files" ;
    private static final String        templatesNameBase        = "templates" ;
    
    // --- Set during server initialization

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

    private static boolean            initialized       = false ;
    
    public synchronized static void init() {
        if ( initialized )
            return ;
        initialized = true ;
        
        if ( FUSEKI_HOME == null ) {
            // Make absolute
            String x1 = System.getenv("FUSEKI_HOME") ;
            if ( x1 != null )
                FUSEKI_HOME = Paths.get(x1) ;
            else
                FUSEKI_HOME = Paths.get("") ;
        }
            
        if ( FUSEKI_BASE == null ) {
            String x2 = System.getenv("FUSEKI_BASE") ;
            if ( x2 != null )
                FUSEKI_BASE = Paths.get(x2) ;
            else
                FUSEKI_BASE = FUSEKI_HOME.resolve(runArea) ;
        }

        FUSEKI_HOME = FUSEKI_HOME.toAbsolutePath() ;
        FUSEKI_BASE = FUSEKI_BASE.toAbsolutePath() ;
        
        Fuseki.configLog.info("FUSEKI_HOME="+FUSEKI_HOME.toString());
        Fuseki.configLog.info("FUSEKI_BASE="+FUSEKI_BASE.toString());
        
        mustExist(FUSEKI_HOME) ;
        
        // Copy in defaults?
        Path dirTemplatesMasters = makePath(FUSEKI_HOME, templatesNameBase) ;
        mustExist(dirTemplatesMasters) ;
        
        if ( Files.isRegularFile(FUSEKI_BASE) ) 
            throw new FusekiConfigException("FUSEKI_BASE exists but is a file") ;
        boolean initFusekiBase = ! Files.exists(FUSEKI_BASE) || emptyDir(FUSEKI_BASE) ;
        
        ensureDir(FUSEKI_BASE) ;

        dirTemplates        = makePath(FUSEKI_BASE, templatesNameBase) ;
        ensureDir(dirTemplates) ; 

        dirDatabases        = makePathEnsureDir(FUSEKI_BASE, databasesLocationBase) ;
        dirBackups          = makePathEnsureDir(FUSEKI_BASE, backupDirNameBase) ;
        dirConfiguration    = makePathEnsureDir(FUSEKI_BASE, configDirNameBase) ;
        dirLogs             = makePathEnsureDir(FUSEKI_BASE, logsNameBase) ;
        dirSystemDatabase   = makePathEnsureDir(FUSEKI_BASE, systemDatabaseNameBase) ;
        dirFileArea         = makePathEnsureDir(FUSEKI_BASE, systemFileAreaBase) ;
        
        String dftShiroIniFile = "shiro.ini" ;
        if ( initFusekiBase ) { 
            Fuseki.configLog.info("Initializing FUSEKI_BASE") ;
            copyFile(FUSEKI_HOME.resolve(dftShiroIniFile), FUSEKI_BASE.resolve(dftShiroIniFile)) ; 
            copyFileFilter(dirTemplatesMasters, dirTemplates, filterConfig) ;
        }
    }

    private static boolean emptyDir(Path dir) {
        return dir.toFile().list().length <= 2 ;
    }
    
    private static void copyFile(Path src, Path dst) {
        try {
            Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES) ;
        } catch (IOException e) {
            IO.exception("Failed to copy file "+src, e);
            e.printStackTrace();
        }
    }

    private static void copyFileFilter(Path srcDir, Path dstDir, FilenameFilter filterConfig) {
        String[] files = srcDir.toFile().list(filterConfig) ;
        for ( String fn : files ) {
            try {
                Path src = srcDir.resolve(fn) ;
                Path dst = dstDir.resolve(fn) ;
                Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES) ;
            } catch (IOException e) {
                IO.exception("Failed to copy directory of files "+srcDir, e);
                e.printStackTrace();
            }
        }
    }

    public static void initializeDataAccessPoints(ServerInitialConfig initialSetup, String configDir) {
        List<DataAccessPoint> configFileDBs = findDatasets(initialSetup) ;
        List<DataAccessPoint> directoryDBs =  FusekiConfig.readConfigurationDirectory(configDir) ;
        List<DataAccessPoint> systemDBs =     FusekiConfig.readSystemDatabase(SystemState.getDataset()) ;
        
        List<DataAccessPoint> datapoints = new ArrayList<DataAccessPoint>() ;
        datapoints.addAll(configFileDBs) ;
        datapoints.addAll(directoryDBs) ;
        datapoints.addAll(systemDBs) ;
        
        // Having found them, set them all running.
        enable(datapoints);
    }

    private static void enable(List<DataAccessPoint> datapoints) {
        for ( DataAccessPoint dap : datapoints ) {
            Fuseki.configLog.info("Register: "+dap.getName()) ;
            DataAccessPointRegistry.register(dap.getName(), dap); 
        }
    }

    private static List<DataAccessPoint> findDatasets(ServerInitialConfig params) { 
        // Has a side effect of global context setting
        // when processing a config file.
        // Compatibility.
        
        List<DataAccessPoint> datasets = DS.list() ;
        if ( params == null )
            return datasets ;

        if ( params.fusekiConfigFile != null ) {
            Fuseki.configLog.info("Configuration file: " + params.fusekiConfigFile) ;
            List<DataAccessPoint> cmdLineDatasets = FusekiConfig.readConfigFile(params.fusekiConfigFile) ;
            datasets.addAll(cmdLineDatasets) ;
        } else if ( params.dsg != null ) {
            DataAccessPoint dap = defaultConfiguration(params.datasetPath, params.dsg, params.allowUpdate) ;
            datasets.add(dap) ;
        } else if ( params.templateFile != null ) {
            Fuseki.configLog.info("Template file: " + params.templateFile) ;
            String dir = params.params.get(Template.DIR) ;
            if ( dir != null ) {
                if ( Lib.equal(dir, Names.memName) ) {
                    Fuseki.configLog.info("TDB dataset: in-memory") ;
                } else {
                    if ( !FileOps.exists(dir) )
                        throw new CmdException("Directory not found: " + dir) ;
                    Fuseki.configLog.info("TDB dataset: directory=" + dir) ;
                }
            }

            DataAccessPoint dap = configFromTemplate(params.templateFile, params.datasetPath, params.params) ;
            datasets.add(dap) ;
        }
        // No datasets is valid.
        return datasets ;
    }
    
    private static DataAccessPoint configFromTemplate(String templateFile, 
                                                      String datasetPath, 
                                                      Map<String, String> params) {
        datasetPath = DataAccessPoint.canonical(datasetPath) ;
        
        // DRY -- ActionDatasets (and others?)
        if ( params == null ) {
            params = new HashMap<>() ;
            params.put(Template.NAME, datasetPath) ;
        } else {
            if ( ! params.containsKey(Template.NAME) ) {
                Fuseki.configLog.warn("No NAME found in template parameters (added)") ;
                params.put(Template.NAME, datasetPath) ;   
            }
        }
        
        String str = TemplateFunctions.templateFile(templateFile, params) ;
        Lang lang = RDFLanguages.filenameToLang(str, Lang.TTL) ;
        StringReader sr =  new StringReader(str) ;
        Model model = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(model, sr, datasetPath, lang);
        
        // Find DataAccessPoint
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
        DataAccessPoint dap = Builder.buildDataAccessPoint(subject) ;
        return dap ;
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
    
    private static DataAccessPoint defaultConfiguration( String name, DatasetGraph dsg, boolean updatable) {
        name = DataAccessPoint.canonical(name) ;
        DataAccessPoint dap = new DataAccessPoint(name) ;
        DataService ds = Builder.buildDataService(dsg, updatable) ;
        dap.setDataService(ds) ;
        return dap ;
    }
    
    // ---- Helpers

    /** Ensure a directory exists, creating it if necessary.
     */
    private static void  ensureDir(Path directory) {
        File dir = directory.toFile() ;
        if ( ! dir.exists() )
            dir.mkdirs() ;
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
    
    private static boolean exists(Path directory) {
        File dir = directory.toFile() ;
        return dir.exists() ;
    }

    private static Path makePathEnsureDir(Path root , String relName ) {
        Path p = makePath(root, relName) ;
        ensureDir(p);
        return p ;
    }
    
    private static Path makePath(Path root , String relName ) {
        Path path = root.resolve(relName) ;
        // Must exist
//        try { path = path.toRealPath() ; }
//        catch (IOException e) { IO.exception(e) ; }
        return path ;
    }
}
