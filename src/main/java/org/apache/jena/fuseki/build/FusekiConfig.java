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

package org.apache.jena.fuseki.build ;

import java.io.File ;
import java.io.FilenameFilter ;
import java.lang.reflect.Method ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DatasetStatus ;
import org.apache.jena.fuseki.server.FusekiVocab ;
import org.apache.jena.fuseki.server.SystemState ;
import org.apache.jena.riot.RDFDataMgr ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.vocabulary.RDF ;

// XXX CLEAN ME

public class FusekiConfig {
    static { Fuseki.init() ; }
    
    private static Logger log = Fuseki.configLog ;
    
    private static FilenameFilter visibleFiles = 
        new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            if ( name.startsWith(".") )
                return false ;
            File f = new File(dir, name) ;
            return f.isFile() ;
        }
    } ;
    
    /** Has side effects in server setup */
    public static List<DataAccessPoint> readConfigFile(String filename) {
        // Old-style config file.
        Model model = RDFDataMgr.loadModel(filename) ;
        additionalRDF(model) ;
        server(model) ;
        return servicesAndDatasets(model) ;
    }

    private static void server(Model model) {
        // Find one server.
        List<Resource> servers = getByType(FusekiVocab.tServer, model) ;
        if ( servers.size() == 0 )
            throw new FusekiConfigException("No server found (no resource with type "
                + FusekiLib.strForResource(FusekiVocab.tServer)) ;
        if ( servers.size() > 1 )
            throw new FusekiConfigException(servers.size()
                                            + " servers found (must be exactly one in a configuration file)") ;

        // ---- Server
        Resource server = servers.get(0) ;
        processServer(server) ;
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
    
    private static List<DataAccessPoint> servicesAndDatasets(Model model) {
        // Old style configuration file : server to services.
        // ---- Services
        ResultSet rs = FusekiLib.query("SELECT * { ?s fu:services [ list:member ?member ] }", model) ;
        if ( !rs.hasNext() )
            log.warn("No services found") ;

        List<DataAccessPoint> accessPoints = new ArrayList<>() ;

        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            Resource svc = soln.getResource("member") ;
            DataAccessPoint acc = Builder.buildDataAccessPoint(svc) ;
            accessPoints.add(acc) ;
        }
        return accessPoints ;
    }
    
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

    
    // XXX Move to utils
    private static Model additionalRDF(Model m) {
        SystemState.init$();        // Why? mvn jetty:run-war
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

    public static List<Resource> getByType(Resource type, Model m) {
        ResIterator rIter = m.listSubjectsWithProperty(RDF.type, type) ;
        return Iter.toList(rIter) ;
    }
    
    // ---- Directory of assemblers
    
    public static List<DataAccessPoint> readConfigurationDirectory(String dir) {
        List<DataAccessPoint> dataServiceRef = new ArrayList<>() ;
        File d = new File(dir) ;
        String[] aFiles = d.list(visibleFiles) ;
        if ( aFiles == null ) {
            log.warn("Not found: directory for assembler files for services: '"+dir+"'") ;
            return Collections.emptyList() ;
        }
        for ( String assemFile : aFiles ) {
            Model m = RDFDataMgr.loadModel(assemFile) ;
            DataAccessPoint acc = readConfiguration(m) ; 
            dataServiceRef.add(acc) ;
        }

        return dataServiceRef ;
    }

    

    private static DataAccessPoint readConfiguration(Model m) {
        additionalRDF(m) ;
        List<Resource> services = getByType(FusekiVocab.fusekiService, m) ; 

        if ( services.size() == 0 ) {
            log.error("No services found") ;
            throw new FusekiConfigException() ;
        }

        // Remove?
        if ( services.size() > 1 ) {
            log.error("Multiple services found") ;
            throw new FusekiConfigException() ;
        }

        Resource service = services.get(0) ;
        DataAccessPoint acc = Builder.buildDataAccessPoint(service) ; 
        return acc ;
    }

    // ---- System database
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
        
        List<DataAccessPoint> refs = new ArrayList<>() ;
        
        ResultSet rs = FusekiLib.query(qs, ds) ;
        
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
            s = m.wrapAsResource(s.asNode()) ;  // Copy to m
            //String name = row.getLiteral("name").getLexicalForm() ;
            DataAccessPoint ref = Builder.buildDataAccessPoint(s) ;
            refs.add(ref) ;
        }
        return refs ;
    }
}
