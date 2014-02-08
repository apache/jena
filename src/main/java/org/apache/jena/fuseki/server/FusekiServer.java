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

import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.FileOps ;
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
    public static void init(ServerInitialConfig initialSetup, String configDir) {
        FileOps.ensureDir(Fuseki.configDirName) ;
        FileOps.ensureDir(Fuseki.systemFileArea) ;
        //FileOps.ensureDir(Fuseki.systemDatabaseName) ; 

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
        } else
            throw new FusekiConfigException("Invalid ServerInitialConfig") ;
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
    
    // ----
    private static DataAccessPoint defaultConfiguration( String name, DatasetGraph dsg, boolean updatable) {
        name = DataAccessPoint.canonical(name) ;
        DataAccessPoint dap = new DataAccessPoint(name) ;
        DataService ds = Builder.buildDataService(dsg, updatable) ;
        dap.setDataService(ds) ;
        return dap ;
    }
}

