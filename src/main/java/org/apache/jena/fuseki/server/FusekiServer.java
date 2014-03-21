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
import java.io.StringReader ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.InternalErrorException ;
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

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public class FusekiServer
{
    /** Root of the Fuseki installation for fixed files. */ 
    public static Path FUSEKI_HOME = null ;
    /** Root of the varying files in this deployment. Often $FUSEKI_HOME/run */ 
    public static Path FUSEKI_BASE = null ;

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
        }
        if ( FUSEKI_BASE == null ) {
            String x2 = System.getenv("FUSEKI_BASE") ;
            if ( x2 != null )
                FUSEKI_BASE = Paths.get(x2) ;
            else
                FUSEKI_BASE = FUSEKI_HOME.resolve(runArea) ;
        }

        mustExist(FUSEKI_HOME) ;
        dirTemplates        = makePath(FUSEKI_HOME, templatesNameBase) ;
        mustExist(dirTemplates) ;

        ensureDir(FUSEKI_BASE) ;
        dirBackups          = makePath(FUSEKI_BASE, backupDirNameBase) ;
        dirConfiguration    = makePath(FUSEKI_BASE, configDirNameBase) ;
        dirLogs             = makePath(FUSEKI_BASE, logsNameBase) ;
        dirSystemDatabase   = makePath(FUSEKI_BASE, systemDatabaseNameBase) ;
        dirFileArea         = makePath(FUSEKI_BASE, systemFileAreaBase) ;
        ensureDir(dirBackups) ;
        ensureDir(dirConfiguration) ;
        ensureDir(dirLogs) ;
        ensureDir(dirSystemDatabase) ;
        ensureDir(dirFileArea) ;
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
        
        String str = TemplateFunctions.template(templateFile, params) ;
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

    private static void ensureDir(Path directory) {
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

    private static Path makePath(Path root , String relName ) {
        Path path = root.resolve(relName) ;
        // Must exist
//        try { path = path.toRealPath() ; }
//        catch (IOException e) { IO.exception(e) ; }
        return path ;
    }
}
