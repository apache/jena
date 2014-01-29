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

package org.apache.jena.fuseki.server ;

import static java.lang.String.format ;
import static org.apache.jena.fuseki.Fuseki.serverLog ;

import java.io.File ;
import java.io.FileFilter ;
import java.io.FilenameFilter ;
import java.lang.reflect.Method ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.riot.RDFDataMgr ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class FusekiConfig {
    static {
        Fuseki.init() ;
    }
    
    private static Logger log      = Fuseki.configLog ;

    private static FileFilter visibleFilesX = null ; 
        
    private static FilenameFilter visibleFiles = 
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if ( name.startsWith(".") )
                    return false ;
                File f = new File(dir, name) ;
                return f.isFile() ;
            }
        } 
    ;

    // ---- DatasetRef used where there isn't a real Dataset e.g. the SPARQL processor.
    
    /**
     * Return the DataService (read-only) for when there is no dataset, just a
     * SPARQL Query processor
     */
    public static DataService serviceOnlyDatasetRef() {
        return DataService.dummy ;
    }

    /** Setup the server configuration based on ServerInitialConfig (from command line) */ 
    public static List<DataAccessPoint> defaultConfiguration(ServerInitialConfig params) {
        if ( params.fusekiConfigFile != null )
            log.warn("Configuration file found while processing command line dataset configuration") ;
        
        // Use a template assembler.
        
        DataService dbSvc = DataService.create(params.dsg) ;
        DataAccessPoint dataAccess = new DataAccessPoint(params.datasetPath) ;
        dataAccess.setDataService(dbSvc); 
        dbSvc.addEndpoint(OperationName.Query, DEF.ServiceQuery);
        dbSvc.addEndpoint(OperationName.Query, DEF.ServiceQueryAlt) ;
        
        if ( params.allowUpdate ) {
            dbSvc.addEndpoint(OperationName.Update, DEF.ServiceUpdate) ;
            dbSvc.addEndpoint(OperationName.Upload, DEF.ServiceUpload) ;
            dbSvc.addEndpoint(OperationName.GSP, DEF.ServiceData) ;
        } else
            dbSvc.addEndpoint(OperationName.GSP_R, DEF.ServiceData) ;

        return Arrays.asList(dataAccess) ; 
    }

//    /** Read one of more dataset descriptions in a model */
//    public static List<DatasetRef> readConfiguration(Model m) {
//        additionalRDF(m) ;
//        List<Resource> services = getByType(FusekiVocab.fusekiService, m) ; 
//            
//        if ( services.size() == 0 ) {
//            log.error("No services found") ;
//            throw new FusekiConfigException() ;
//        }
//        
//        // Remove?
//        if ( services.size() > 1 ) {
//            log.error("Multiple services found") ;
//            throw new FusekiConfigException() ;
//        }
//        
//        List<DatasetRef> refs = new ArrayList<DatasetRef>() ;
//        
//        Resource service = services.get(0) ;
//        String name = ((Literal)getOne(service, "fuseki:name")).getLexicalForm() ;
//        log.info("name = "+name); 
//        DatasetRef dsDesc = processService(service) ;
//        String datasetPath = dsDesc.name ;
//        refs.add(dsDesc) ;
//        return refs ;
//    }
    
    /** Has side effects in server setup */
    public static List<DataAccessPoint> readConfigFile(String filename) {
        // Old-style config file.
        Model model = FileManager.get().loadModel(filename) ;
        additionalRDF(model) ;
        server(model) ;
        return servicesAndDatasets(model) ;
    }

    public static List<DataAccessPoint> readConfigurationDirectory(String dir) {
        List<DataAccessPoint> dataServiceRef = new ArrayList<DataAccessPoint>() ;
        File d = new File(dir) ;
        String[] aFiles = d.list(visibleFiles) ;
        if ( aFiles == null ) {
            log.warn("Not found: directory for assembler files for services: '"+dir+"'") ;
            return Collections.emptyList() ;
        }
        for ( String assemFile : aFiles ) {
            Model m = RDFDataMgr.loadModel(assemFile) ;
            // Same code as ActionDatasets
        }
        
        return dataServiceRef ;
    }
        
        
    public static List<DataAccessPoint> readSystemDatabase(Dataset ds) {
        String qs = StrUtils.strjoinNL
            (SystemState.PREFIXES ,
             "SELECT * {" ,
             "  GRAPH ?g {",
             "     ?s fu:name ?name ;" ,
             "        fu:status ?status ." ,
             "  }",
             "}"
             ) ;
        
        List<DataAccessPoint> refs = new ArrayList<DataAccessPoint>() ;
        
        ResultSet rs = query(qs, ds) ;
        
//        ResultSetFormatter.out(rs); 
//        ((ResultSetRewindable)rs).reset();
        
        for ( ; rs.hasNext() ; ) {
            QuerySolution row = rs.next() ;
            Resource s = row.getResource("s") ;
            // The result set was copied so we need to find the model again.
            Resource g = row.getResource("g") ;
            Resource rStatus = row.getResource("status") ;
            DatasetStatus status = DatasetStatus.status(rStatus) ;
            Model m = ds.getNamedModel(g.getURI()) ;
            s = m.wrapAsResource(s.asNode()) ;
            //String name = row.getLiteral("name").getLexicalForm() ;
            DataAccessPoint ref = processService(s) ;
            refs.add(ref) ;
        }
        return refs ;
    }
    
    // ---- Old style config file processing
    
    private static void server(Model model) {
        // Find one server.
        List<Resource> servers = getByType(FusekiVocab.tServer, model) ;
        if ( servers.size() == 0 )
            throw new FusekiConfigException("No server found (no resource with type "
                                            + strForResource(FusekiVocab.tServer)) ;
        if ( servers.size() > 1 )
            throw new FusekiConfigException(servers.size()
                                            + " servers found (must be exactly one in a configuration file)") ;

        // ---- Server
        Resource server = servers.get(0) ;
        processServer(server) ;
    }
    
    private static List<DataAccessPoint> servicesAndDatasets(Model model) {
        // Old style configuration file : server to services.
        // ---- Services
        ResultSet rs = query("SELECT * { ?s fu:services [ list:member ?member ] }", model) ;
        if ( !rs.hasNext() )
            log.warn("No services found") ;

        List<DataAccessPoint> accessPoints = new ArrayList<DataAccessPoint>() ;

        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            Resource svc = soln.getResource("member") ;
            DataAccessPoint acc = processService(svc) ;
            accessPoints.add(acc) ;
        }
        return accessPoints ;
    }

    private static void processServer(Resource server) {
        // Global, currently.
        AssemblerUtils.setContext(server, Fuseki.getContext()) ;

        StmtIterator sIter = server.listProperties(JA.loadClass) ;
        for ( ; sIter.hasNext() ; ) {
            Statement s = sIter.nextStatement() ;
            RDFNode rn = s.getObject() ;
            String className = null ;
            if ( rn instanceof Resource ) {
                String uri = ((Resource)rn).getURI() ;
                if ( uri == null ) {
                    log.warn("Blank node for class to load") ;
                    continue ;
                }
                String javaScheme = "java:" ;
                if ( !uri.startsWith(javaScheme) ) {
                    log.warn("Class to load is not 'java:': " + uri) ;
                    continue ;
                }
                className = uri.substring(javaScheme.length()) ;
            }
            if ( rn instanceof Literal )
                className = ((Literal)rn).getLexicalForm() ;
            /* Loader. */loadAndInit(className) ;
        }
    }


    // ---- DatasetRef used where there isn't a real Dataset e.g. the SPARQL processor.

    private static void loadAndInit(String className) {
        try {
            Class<? > classObj = Class.forName(className) ;
            log.info("Loaded " + className) ;
            Method initMethod = classObj.getMethod("init") ;
            initMethod.invoke(null) ;
        }
        catch (ClassNotFoundException ex) {
            log.warn("Class not found: " + className) ;
        }
        catch (Exception e) {
            throw new FusekiConfigException(e) ;
        }
    }

    /** Build a DatasetRef from an assember starting at Resource svc */
    public static DataAccessPoint processService(Resource svc) {
        log.info("Service: " + nodeLabel(svc)) ;
        
        String name = ((Literal)getOne(svc, "fu:name")).getLexicalForm() ;
        
        Resource datasetDesc = ((Resource)getOne(svc, "fu:dataset")) ;
        // Check if it is in the model.
        if ( !datasetDesc.hasProperty(RDF.type) )
            throw new FusekiConfigException("No rdf:type for dataset " + nodeLabel(datasetDesc)) ;
        Dataset ds = (Dataset)Assembler.general.open(datasetDesc) ;
        // Assembler to do all this. 
        DataService dataService = new DataService(svc, ds.asDatasetGraph()) ;
        
        DataAccessPoint dataAccess = new DataAccessPoint(name) ;
        dataAccess.setDataService(dataService) ;
        DatasetRegistry.get().put(name, dataAccess) ; 
        
        log.info("  name = " + dataAccess.getName()) ;

        addServiceEP(dataAccess, dataService, OperationName.Query,  svc,    "fu:serviceQuery") ;
        addServiceEP(dataAccess, dataService, OperationName.Update, svc,    "fu:serviceUpdate") ;
        addServiceEP(dataAccess, dataService, OperationName.Upload, svc,    "fu:serviceUpload") ;
        addServiceEP(dataAccess, dataService, OperationName.GSP_R,  svc,    "fu:serviceReadGraphStore") ;
        addServiceEP(dataAccess, dataService, OperationName.GSP,    svc,    "fu:serviceReadWriteGraphStore") ;
        // XXX 
//        // Extract timeout overriding configuration if present.
//        if ( svc.hasProperty(FusekiVocab.pAllowTimeoutOverride) ) {
//            sDesc.allowTimeoutOverride = svc.getProperty(FusekiVocab.pAllowTimeoutOverride).getObject().asLiteral().getBoolean() ;
//            if ( svc.hasProperty(FusekiVocab.pMaximumTimeoutOverride) ) {
//                sDesc.maximumTimeoutOverride = (int)(svc.getProperty(FusekiVocab.pMaximumTimeoutOverride).getObject().asLiteral().getFloat() * 1000) ;
//            }
//        }

        return dataAccess ;
    }

    
    /** Initial configuration - for all the datasets, call the per dataset initization  */ 
    public static void configureDatasets(List<DataAccessPoint> datasets) {
        for (DataAccessPoint dsDesc : datasets)
            configureOneDataset(dsDesc) ;
    }

    public static void configureOneDataset(DataAccessPoint dsDesc) {
        registerDataset(dsDesc.getName(), dsDesc) ;
        //addJMX(dsDesc) ;
    }
    
    /** Register a DatasetRef, which should no already be registered */  
    
    public static void registerDataset(String datasetPath, DataAccessPoint dataAccess) {
        if ( DatasetRegistry.get().isRegistered(datasetPath) )
            throw new FusekiConfigException("Already registered: key = "+datasetPath) ;
        DatasetRegistry.get().put(datasetPath, dataAccess) ;
        serverLog.info(format("Register dataset path = %s", datasetPath)) ;
        //addJMX(dsDesc) ;
    }
    
//    public static void addJMX() {
//        DatasetRegistry registry = DatasetRegistry.get() ;
//        for (String ds : registry.keys()) {
//            DatasetRef dsRef = registry.get(ds) ;
//            addJMX(dsRef) ;
//        }
//    }

    private static void addJMX(DataAccessPoint dsRef) {
//        String x = datasetNames ;
//        // if ( x.startsWith("/") )
//        // x = x.substring(1) ;
//        ARQMgt.register(Fuseki.PATH + ".dataset:name=" + x, dsRef) ;
//        // For all endpoints
//        for (ServiceRef sRef : dsRef.getServiceRefs()) {
//            ARQMgt.register(Fuseki.PATH + ".dataset:name=" + x + "/" + sRef.name, sRef) ;
//        }
    }

    public static void removeJMX() {
//        DatasetRegistry registry = DatasetRegistry.get() ;
//        for (String ds : registry.keys()) {
//            DatasetRef ref = registry.get(ds) ;
//            removeJMX(ref) ;
//        }
    }

//    private static void removeJMX(DatasetRef dsRef) {
//        String x = dsRef.getName() ;
//        ARQMgt.unregister(Fuseki.PATH + ".dataset:name=" + x) ;
//        for (ServiceRef sRef : dsRef.getServiceRefs()) {
//            ARQMgt.unregister(Fuseki.PATH + ".dataset:name=" + x + "/" + sRef.name) ;
//        }
//    }

    // XXX Move to utils
    private static Model additionalRDF(Model m) {
        String x1 = StrUtils.strjoinNL
            ( SystemState.PREFIXES, 
              "INSERT                    { [] ja:loadClass 'com.hp.hpl.jena.tdb.TDB' }",
              "WHERE { FILTER NOT EXISTS { [] ja:loadClass 'com.hp.hpl.jena.tdb.TDB' } }"
             ) ;
        String x2 = StrUtils.strjoinNL
            (SystemState.PREFIXES,
             "INSERT DATA {",
             "   tdb:DatasetTDB  rdfs:subClassOf  ja:RDFDataset .",
             "   tdb:GraphTDB    rdfs:subClassOf  ja:Model .",
             "}" 
             ) ;
        execute(m, x1) ;
        execute(m, x2) ;
        return m ;
    }

    private static void execute(Model m, String x) {
        UpdateRequest req = UpdateFactory.create(x) ;
        UpdateAction.execute(req, m);
    }

    // Helpers
    private static RDFNode getOne(Resource svc, String property) {
        String ln = property.substring(property.indexOf(':') + 1) ;
        ResultSet rs = query("SELECT * { ?svc " + property + " ?x}", svc.getModel(), "svc", svc) ;
        if ( !rs.hasNext() )
            throw new FusekiConfigException("No " + ln + " for service " + nodeLabel(svc)) ;
        RDFNode x = rs.next().get("x") ;
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple " + ln + " for service " + nodeLabel(svc)) ;
        return x ;
    }

    public static List<Resource> getByType(Resource type, Model m) {
        ResIterator rIter = m.listSubjectsWithProperty(RDF.type, type) ;
        return Iter.toList(rIter) ;
    }

    private static void addServiceEP(DataAccessPoint dataAccessPoint, DataService dataService, OperationName opName, Resource svc, String property) {
        ResultSet rs = query("SELECT * { ?svc " + property + " ?ep}", svc.getModel(), "svc", svc) ;
        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            String epName = soln.getLiteral("ep").getLexicalForm() ;
            Operation operation = new Operation(opName, epName) ;
            dataService.addEndpoint(opName, epName); 
            log.info("  " + opName.name + " = " + dataAccessPoint.getName() + "/" + epName) ;
        }
    }

//    // ---- Helper code
//    
    private static ResultSet query(String string, Model m) {
        return query(string, m, null, null) ;
    }

    private static ResultSet query(String string, Model m, String varName, RDFNode value) {
        Query query = QueryFactory.create(SystemState.PREFIXES + string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null )
            initValues = querySolution(varName, value) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, m, initValues) ;
        ResultSet rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
        qExec.close() ;
        return rs ;
    }

    private static ResultSet query(String string, Dataset ds) {
        return query(string, ds, null, null) ;
    }

    private static ResultSet query(String string, Dataset ds, String varName, RDFNode value) {
        Query query = QueryFactory.create(SystemState.PREFIXES + string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null )
            initValues = querySolution(varName, value) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds, initValues) ;
        ResultSet rs = ResultSetFactory.copyResults(qExec.execSelect()) ;
        qExec.close() ;
        return rs ;
    }

    private static QuerySolutionMap querySolution(String varName, RDFNode value) {
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        querySolution(qsm, varName, value) ;
        return qsm ;
    }

    private static QuerySolutionMap querySolution(QuerySolutionMap qsm, String varName, RDFNode value) {
        qsm.add(varName, value) ;
        return qsm ;
    }

    // Node presentation
    private static String nodeLabel(RDFNode n) {
        if ( n == null )
            return "<null>" ;
        if ( n instanceof Resource )
            return strForResource((Resource)n) ;

        Literal lit = (Literal)n ;
        return lit.getLexicalForm() ;
    }

    // XXX Lib
    public static String strForResource(Resource r) {
        return strForResource(r, r.getModel()) ;
    }

    // XXX Lib
    public static String strForResource(Resource r, PrefixMapping pm) {
        if ( r == null )
            return "NULL " ;
        if ( r.hasProperty(RDFS.label) ) {
            RDFNode n = r.getProperty(RDFS.label).getObject() ;
            if ( n instanceof Literal )
                return ((Literal)n).getString() ;
        }

        if ( r.isAnon() )
            return "<<blank node>>" ;

        if ( pm == null )
            pm = r.getModel() ;

        return strForURI(r.getURI(), pm) ;
    }

    private static String strForURI(String uri, PrefixMapping pm) {
        if ( pm != null ) {
            String x = pm.shortForm(uri) ;

            if ( !x.equals(uri) )
                return x ;
        }
        return "<" + uri + ">" ;
    }
}
