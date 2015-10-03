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
import java.io.IOException ;
import java.lang.reflect.Method ;
import java.nio.file.DirectoryStream ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.assembler.JA ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiConfigException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DatasetStatus ;
import org.apache.jena.fuseki.server.FusekiVocab ;
import org.apache.jena.fuseki.server.SystemState ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.vocabulary.RDF ;
import org.slf4j.Logger ;

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
        if ( model.size() == 0 )
            return Collections.emptyList() ;
        additionalRDF(model) ;
        server(model) ;
        return servicesAndDatasets(model) ;
    }

    private static void server(Model model) {
        // Find one server.
        List<Resource> servers = getByType(FusekiVocab.tServer, model) ;
        if ( servers.size() == 0 )
            return ; 
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
        ResultSet rs = FusekiLib.query("SELECT * { ?s fu:services [ list:member ?service ] }", model) ;
        // If the old config.ttl file becomes just the server configuration file,
        // then don't warn here.
//        if ( !rs.hasNext() )
//            log.warn("No services found") ;

        List<DataAccessPoint> accessPoints = new ArrayList<>() ;

        if ( ! rs.hasNext() )
            // No "fu:services ( .... )" so try looking for services directly.
            // This means Fuseki2, service configuration files (no server section) work for --conf. 
            rs = FusekiLib.query("SELECT ?service { ?service a fu:Service }", model) ;

        for ( ; rs.hasNext() ; ) {
            QuerySolution soln = rs.next() ;
            Resource svc = soln.getResource("service") ;
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
              "INSERT                    { [] ja:loadClass 'org.apache.jena.tdb.TDB' }",
              "WHERE { FILTER NOT EXISTS { [] ja:loadClass 'org.apache.jena.tdb.TDB' } }"
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

    // XXX Move to a library
    private static List<Resource> getByType(Resource type, Model m) {
        ResIterator rIter = m.listSubjectsWithProperty(RDF.type, type) ;
        return Iter.toList(rIter) ;
    }
    
    // ---- Directory of assemblers
    
    /** Read service descriptions in the given directory */ 
    public static List<DataAccessPoint> readConfigurationDirectory(String dir) {
        Path pDir = Paths.get(dir).normalize() ;
        File dirFile = pDir.toFile() ;
        if ( ! dirFile.exists() ) {
            log.warn("Not found: directory for assembler files for services: '"+dir+"'") ;
            return Collections.emptyList() ;
        }
        if ( ! dirFile.isDirectory() ) {
            log.warn("Not a directory: '"+dir+"'") ;
            return Collections.emptyList() ;
        }
        // Files that are not hidden.
        DirectoryStream.Filter<Path> filter = (entry)-> {
                File f = entry.toFile() ;
                return ! f.isHidden() && f.isFile() ;
        } ;

        List<DataAccessPoint> dataServiceRef = new ArrayList<>() ;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pDir, filter)) {
            for ( Path p : stream ) {
                String fn = IRILib.filenameToIRI(p.toString()) ;
                log.info("Load configuration: "+fn);
                Model m = RDFDataMgr.loadModel(fn) ;
                DataAccessPoint acc = readConfiguration(m) ; 
                dataServiceRef.add(acc) ;
            }
        } catch (IOException ex) {
            log.warn("IOException:"+ex.getMessage(), ex);
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
    /** Read the system database */
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
        
        ds.begin(ReadWrite.WRITE) ;
        try {
            ResultSet rs = FusekiLib.query(qs, ds) ;

    //        ResultSetFormatter.out(rs); 
    //        ((ResultSetRewindable)rs).reset();

            for ( ; rs.hasNext() ; ) {
                QuerySolution row = rs.next() ;
                Resource s = row.getResource("s") ;
                Resource g = row.getResource("g") ;
                Resource rStatus = row.getResource("status") ;
                //String name = row.getLiteral("name").getLexicalForm() ;
                DatasetStatus status = DatasetStatus.status(rStatus) ;

                Model m = ds.getNamedModel(g.getURI()) ;
                // Rebase the resoure of the service description to the containing graph.
                Resource svc = m.wrapAsResource(s.asNode()) ;
                DataAccessPoint ref = Builder.buildDataAccessPoint(svc) ;
                refs.add(ref) ;
            }
            ds.commit(); 
            return refs ;
        } finally { ds.end() ; }
    }
}
