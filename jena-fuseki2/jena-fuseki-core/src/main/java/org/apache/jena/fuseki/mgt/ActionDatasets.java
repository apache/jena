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

package org.apache.jena.fuseki.mgt;

import static java.lang.String.format ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import javax.servlet.http.HttpServletRequest ;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.build.DatasetDescriptionRegistry;
import org.apache.jena.fuseki.build.FusekiBuilder;
import org.apache.jena.fuseki.build.FusekiConst;
import org.apache.jena.fuseki.build.Template;
import org.apache.jena.fuseki.build.TemplateFunctions;
import org.apache.jena.fuseki.ctl.ActionContainerItem;
import org.apache.jena.fuseki.ctl.JsonDescription;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.FusekiVocab;
import org.apache.jena.fuseki.server.ServerConst;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.fuseki.system.Upload;
import org.apache.jena.fuseki.webapp.FusekiSystem;
import org.apache.jena.fuseki.webapp.SystemState;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.shared.uuid.JenaUUID ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.web.HttpSC ;

public class ActionDatasets extends ActionContainerItem {
    
    private static Dataset system = SystemState.getDataset() ;
    private static DatasetGraphTransaction systemDSG = SystemState.getDatasetGraph() ; 
    
    static private Property pServiceName = FusekiVocab.pServiceName ;
    static private Property pStatus = FusekiVocab.pStatus ;

    private static final String paramDatasetName    = "dbName" ;
    private static final String paramDatasetType    = "dbType" ;
    private static final String tDatabaseTDB        = "tdb" ;
    private static final String tDatabaseTDB2       = "tdb2" ;
    private static final String tDatabaseMem        = "mem" ;

    public ActionDatasets() { super() ; }
    
    // ---- GET : return details of dataset or datasets.
    @Override
    protected JsonValue execGetContainer(HttpAction action) { 
        action.log.info(format("[%d] GET datasets", action.id)) ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("D") ;
        builder.key(ServerConst.datasets) ;
        JsonDescription.arrayDatasets(builder, action.getDataAccessPointRegistry());
        builder.finishObject("D") ;
        return builder.build() ;
    }

    @Override
    protected JsonValue execGetItem(HttpAction action) {
        action.log.info(format("[%d] GET dataset %s", action.id, action.getDatasetName())) ;
        JsonBuilder builder = new JsonBuilder() ;
        DataAccessPoint dsDesc = action.getDataAccessPointRegistry().get(action.getDatasetName()) ;
        if ( dsDesc == null )
            ServletOps.errorNotFound("Not found: dataset "+action.getDatasetName());
        JsonDescription.describe(builder, dsDesc) ;
        return builder.build() ;
    }
    
    // ---- POST 
    
    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        JenaUUID uuid = JenaUUID.generate() ;
        DatasetDescriptionRegistry registry = new DatasetDescriptionRegistry() ;
        
        ContentType ct = FusekiLib.getContentType(action) ;
        
        boolean hasParams = action.request.getParameterNames().hasMoreElements();
        
        if ( ct == null && ! hasParams ) {
            ServletOps.errorBadRequest("Bad request - Content-Type or both parameters dbName and dbType required");
            // Or do "GET over POST"
            //return execGetContainer(action);
        }
        
        boolean committed = false ;
        // Also acts as a concurrency lock
        system.begin(ReadWrite.WRITE) ;
        String systemFileCopy = null ;
        String configFile = null ;
            
        try {
            // Where to build the templated service/database. 
            Model model = ModelFactory.createDefaultModel() ;
            StreamRDF dest = StreamRDFLib.graph(model.getGraph()) ;
    
            if ( hasParams || WebContent.isHtmlForm(ct) )
                assemblerFromForm(action, dest) ;
            else if ( WebContent.isMultiPartForm(ct) )
                assemblerFromUpload(action, dest) ;
            else
                assemblerFromBody(action, dest) ;
            
            // ----
            // Keep a persistent copy immediately.  This is not used for
            // anything other than being "for the record".
            systemFileCopy = FusekiSystem.dirFileArea.resolve(uuid.asString()).toString() ;
            try ( OutputStream outCopy = IO.openOutputFile(systemFileCopy) ) {
                RDFDataMgr.write(outCopy, model, Lang.TURTLE) ;
            }
            // ----

            // Process configuration.
            Statement stmt = getOne(model, null, pServiceName, null) ;
            if ( stmt == null ) {
                StmtIterator sIter = model.listStatements(null, pServiceName, (RDFNode)null ) ;
                if ( ! sIter.hasNext() )
                    ServletOps.errorBadRequest("No name given in description of Fuseki service") ;
                sIter.next() ;
                if ( sIter.hasNext() )
                    ServletOps.errorBadRequest("Multiple names given in description of Fuseki service") ;
                throw new InternalErrorException("Inconsistent: getOne didn't fail the second time") ;
            }
                
            if ( ! stmt.getObject().isLiteral() )
                ServletOps.errorBadRequest("Found "+FmtUtils.stringForRDFNode(stmt.getObject())+" : Service names are strings, then used to build the external URI") ;

            Resource subject = stmt.getSubject() ;
            Literal object = stmt.getObject().asLiteral() ;
            
            if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
                action.log.warn(format("[%d] Service name '%s' is not a string", action.id, FmtUtils.stringForRDFNode(object)));
            
            String datasetPath ;
            {   // Check the name provided.
                String datasetName = object.getLexicalForm() ;
                
                // ---- Check and canonicalize name.
                if ( datasetName.isEmpty() )
                    ServletOps.error(HttpSC.BAD_REQUEST_400, "Empty dataset name") ;
                if ( StringUtils.isBlank(datasetName) )
                    ServletOps.error(HttpSC.BAD_REQUEST_400, format("Whitespace dataset name: '%s'", datasetName)) ;
                if ( datasetName.contains(" ") )
                    ServletOps.error(HttpSC.BAD_REQUEST_400, format("Bad dataset name (contains spaces) '%s'",datasetName)) ;
                if ( datasetName.equals("/") )
                    ServletOps.error(HttpSC.BAD_REQUEST_400, format("Bad dataset name '%s'",datasetName)) ;
                datasetPath = DataAccessPoint.canonical(datasetName) ;
            }
            action.log.info(format("[%d] Create database : name = %s", action.id, datasetPath)) ;
            // ---- Check whether it already exists 
            if ( action.getDataAccessPointRegistry().isRegistered(datasetPath) )
                // And abort.
                ServletOps.error(HttpSC.CONFLICT_409, "Name already registered "+datasetPath) ;
            
            configFile = FusekiSystem.generateConfigurationFilename(datasetPath) ;
            List<String> existing = FusekiSystem.existingConfigurationFile(datasetPath) ;
            if ( ! existing.isEmpty() )
                ServletOps.error(HttpSC.CONFLICT_409, "Configuration file for '"+datasetPath+"' already exists") ;

            // Write to configuration directory.
            try ( OutputStream outCopy = IO.openOutputFile(configFile) ) {
                RDFDataMgr.write(outCopy, model, Lang.TURTLE) ;
            }

            // Currently do nothing with the system database.
            // In the future ... maybe ...
//            Model modelSys = system.getNamedModel(gn.getURI()) ;
//            modelSys.removeAll(null, pStatus, null) ;
//            modelSys.add(subject, pStatus, FusekiVocab.stateActive) ;
            
            // Need to be in Resource space at this point.
            DataAccessPoint ref = FusekiBuilder.buildDataAccessPoint(subject, registry) ;
            ref.getDataService().goActive();
            action.getDataAccessPointRegistry().register(datasetPath, ref) ;
            action.getResponse().setContentType(WebContent.contentTypeTextPlain); 
            ServletOps.success(action) ;
            system.commit();
            committed = true ;
            
        } catch (IOException ex) { IO.exception(ex); }
        finally { 
            if ( ! committed ) {
                if ( systemFileCopy != null ) FileOps.deleteSilent(systemFileCopy);
                if ( configFile != null ) FileOps.deleteSilent(configFile);
                system.abort() ; 
            }
            system.end() ; 
        }
        return null ;
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        String name = action.getDatasetName() ;
        if ( name == null )
            name = "''" ;
        action.log.info(format("[%d] POST dataset %s", action.id, name)) ;
        
        if ( action.getDataAccessPoint() == null )
            ServletOps.errorNotFound("Not found: dataset "+action.getDatasetName());
        
        DataService dSrv = action.getDataService() ;
        if ( dSrv == null )
            // If not set explicitly, take from DataAccessPoint
            dSrv = action.getDataAccessPoint().getDataService() ;
        
        String s = action.request.getParameter("state") ;
        if ( s == null || s.isEmpty() )
            ServletOps.errorBadRequest("No state change given") ;

        // setDatasetState is a transaction on the persistent state of the server. 
        if ( s.equalsIgnoreCase("active") ) {
            action.log.info(format("[%d] REBUILD DATASET %s", action.id, name)) ;
            setDatasetState(name, FusekiVocab.stateActive) ;
            dSrv.goActive() ; 
            // DatasetGraph dsg = ???? ;
            //dSrv.activate(dsg) ; 
            //dSrv.activate() ;
        } else if ( s.equalsIgnoreCase("offline") ) {
            action.log.info(format("[%d] OFFLINE DATASET %s", action.id, name)) ;
            //DataAccessPoint access = action.getDataAccessPoint() ;
            //access.goOffline() ;
            dSrv.goOffline() ;  // Affects the target of the name. 
            setDatasetState(name, FusekiVocab.stateOffline) ;  
            //dSrv.offline() ;
        } else if ( s.equalsIgnoreCase("unlink") ) {
            action.log.info(format("[%d] UNLINK ACCESS NAME %s", action.id, name)) ;
            //DataAccessPoint access = action.getDataAccessPoint() ;
            ServletOps.errorNotImplemented("unlink: dataset"+action.getDatasetName());
            //access.goOffline() ;
            // Registry?
        }
        else
            ServletOps.errorBadRequest("State change operation '"+s+"' not recognized");
        return null ;
    }

    // ---- DELETE
    
    @Override
    protected void execDeleteItem(HttpAction action) {
        // Does not exist?
        String name = action.getDatasetName() ;
        if ( name == null )
            name = "" ;
        action.log.info(format("[%d] DELETE ds=%s", action.id, name)) ;

        if ( ! action.getDataAccessPointRegistry().isRegistered(name) )
            ServletOps.errorNotFound("No such dataset registered: "+name);

        // This acts as a lock. 
        systemDSG.begin(ReadWrite.WRITE) ;
        boolean committed = false ;

        try {
            // Here, go offline.
            // Need to reference count operations when they drop to zero
            // or a timer goes off, we delete the dataset.

            DataAccessPoint ref = action.getDataAccessPointRegistry().get(name) ;
            
            // Redo check inside transaction.
            if ( ref == null )
                ServletOps.errorNotFound("No such dataset registered: "+name);
            
            String filename = name.startsWith("/") ? name.substring(1) : name;
            List<String> configurationFiles = FusekiSystem.existingConfigurationFile(filename);
            if  ( configurationFiles.size() != 1 ) {
                // This should not happen.
                action.log.warn(format("[%d] There are %d configuration files, not one.", action.id, configurationFiles.size()));
                ServletOps.errorOccurred(
                    format(
                        "There are %d configuration files, not one. Delete not performed; clearup of the filesystem needed.",
                        action.id, configurationFiles.size()));
            }
            
            String cfgPathname = configurationFiles.get(0);
            
            // Delete configuration file.
            // Once deleted, server restart will not have the database. 
            FileOps.deleteSilent(cfgPathname);

            // Get before removing.
            DataService dataService = ref.getDataService();
            
            // Make it invisible in this running server.
            action.getDataAccessPointRegistry().remove(name);

            // Delete the database for real only when it is in the server "run/databases"
            // area. Don't delete databases that reside elsewhere. We do delete the
            // configuration file, so the databases will not be associated with the server
            // anymore.
            
            // JENA-1586: Remove from current running Fuseki server.

            boolean isTDB1 = org.apache.jena.tdb.sys.TDBInternal.isTDB1(dataService.getDataset());
            boolean isTDB2 = org.apache.jena.tdb2.sys.TDBInternal.isTDB2(dataService.getDataset());

            dataService.shutdown();
            // JENA-1481: Really delete files.
            if ( ( isTDB1 || isTDB2 ) ) {
                // Delete databases created by the UI, or the admin operation, which are
                // in predictable, unshared location on disk.
                // There may not be any database files, the in-memory case.
                Path pDatabase = FusekiSystem.dirDatabases.resolve(filename);
                if ( Files.exists(pDatabase)) {
                    try {
                        if ( Files.isSymbolicLink(pDatabase)) {
                            action.log.info(format("[%d] Database is a symbolic link, not removing files", action.id, pDatabase));
                        } else {
                            IO.deleteAll(pDatabase);
                            action.log.info(format("[%d] Deleted database files %s", action.id, pDatabase));
                        }
                    } catch (RuntimeIOException ex) {
                        action.log.error(format("[%d] Error while deleting database files %s: %s", action.id, pDatabase, ex.getMessage()), ex);
                        // But we have managed to remove it from the running server, and removed its configuration, so declare victory. 
                    }
                }
            }
            
            // -- System database
            // Find graph associated with this dataset name.
            // (Statically configured databases aren't in the system database.)
            Node n = NodeFactory.createLiteral(DataAccessPoint.canonical(name)) ;
            Quad q = getOne(systemDSG, null, null, pServiceName.asNode(), n) ;
//            if ( q == null )
//                ServletOps.errorBadRequest("Failed to find dataset for '"+name+"'");
            if ( q != null ) {
                Node gn = q.getGraph() ;
                //action.log.info("SHUTDOWN NEEDED"); // To ensure it goes away?
                systemDSG.deleteAny(gn, null, null, null) ;
            }
            systemDSG.commit() ;
            committed = true ;
            ServletOps.success(action) ;
        } finally { 
            if ( ! committed ) systemDSG.abort() ; 
            systemDSG.end() ; 
        }
    }

    private static void assemblerFromBody(HttpAction action, StreamRDF dest) {
        bodyAsGraph(action, dest) ;
    }

    private static Map<String, String> dbTypeToTemplate = new HashMap<>();
    static {
        dbTypeToTemplate.put(tDatabaseTDB,  Template.templateTDB1_FN);
        dbTypeToTemplate.put(tDatabaseTDB2, Template.templateTDB2_FN);
        dbTypeToTemplate.put(tDatabaseMem,  Template.templateTIM_MemFN);
    }
    
    private static void assemblerFromForm(HttpAction action, StreamRDF dest) {
        String dbType = action.getRequest().getParameter(paramDatasetType) ;
        String dbName = action.getRequest().getParameter(paramDatasetName) ;
        if ( StringUtils.isBlank(dbType) || StringUtils.isBlank(dbName) )
            ServletOps.errorBadRequest("Required parameters: dbName and dbType");
        
        Map<String, String> params = new HashMap<>() ;
        
        if ( dbName.startsWith("/") )
            params.put(Template.NAME, dbName.substring(1)) ;
        else
            params.put(Template.NAME, dbName) ;
        FusekiSystem.addGlobals(params); 
        
        //action.log.info(format("[%d] Create database : name = %s, type = %s", action.id, dbName, dbType )) ;
        
        String template = dbTypeToTemplate.get(dbType.toLowerCase(Locale.ROOT));
        if ( template == null )
                ServletOps.errorBadRequest(format("dbType can be only '%s', '%s' or '%s'", tDatabaseTDB, tDatabaseTDB2, tDatabaseMem)) ;
        
        String syntax =  TemplateFunctions.templateFile(template, params, Lang.TTL) ;
        RDFParser.create().source(new StringReader(syntax)).base("http://base/").lang(Lang.TTL).parse(dest);
    }

    private static void assemblerFromUpload(HttpAction action, StreamRDF dest) {
        Upload.fileUploadWorker(action, dest);
    }

    // Persistent state change.
    private static void setDatasetState(String name, Resource newState) {
        boolean committed = false ;
        system.begin(ReadWrite.WRITE) ;
        try {
            String dbName = name ;
            if ( dbName.startsWith("/") )
                dbName = dbName.substring(1) ;
            
            String update =  StrUtils.strjoinNL
                (FusekiConst.PREFIXES,
                 "DELETE { GRAPH ?g { ?s fu:status ?state } }",
                 "INSERT { GRAPH ?g { ?s fu:status "+FmtUtils.stringForRDFNode(newState)+" } }",
                 "WHERE {",
                 "   GRAPH ?g { ?s fu:name '"+dbName+"' ; ",
                 "                 fu:status ?state .",
                 "   }",
                 "}"
                 ) ;
            UpdateRequest req =  UpdateFactory.create(update) ;
            UpdateAction.execute(req, system);
            system.commit();
            committed = true ;
        } finally { 
            if ( ! committed ) system.abort() ; 
            system.end() ; 
        }
    }
    
    // ---- Auxiliary functions

    private static Quad getOne(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.findNG(g, s, p, o) ;
        if ( ! iter.hasNext() )
            return null ;
        Quad q = iter.next() ;
        if ( iter.hasNext() )
            return null ;
        return q ;
    }
    
    private static Statement getOne(Model m, Resource s, Property p, RDFNode o) {
        StmtIterator iter = m.listStatements(s, p, o) ;
        if ( ! iter.hasNext() )
            return null ;
        Statement stmt = iter.next() ;
        if ( iter.hasNext() )
            return null ;
        return stmt ;
    }
    
    // XXX Merge with Upload.incomingData
    
    private static void bodyAsGraph(HttpAction action, StreamRDF dest) {
        HttpServletRequest request = action.request ;
        String base = ActionLib.wholeRequestURL(request) ;
        ContentType ct = FusekiLib.getContentType(request) ;
        Lang lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
        if ( lang == null ) {
            ServletOps.errorBadRequest("Unknown content type for triples: " + ct) ;
            return ;
        }
        InputStream input = null ;
        try { input = request.getInputStream() ; } 
        catch (IOException ex) { IO.exception(ex) ; }

        // Don't log - assemblers are typically small.
        // Adding this to the log confuses things.
        // Reserve logging for data uploads. 
//        long len = request.getContentLengthLong() ;
//        if ( action.verbose ) {
//            if ( len >= 0 )
//                alog.info(format("[%d]   Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s", action.id, len,
//                                ct.getContentType(), ct.getCharset(), lang.getName())) ;
//            else
//                alog.info(format("[%d]   Body: Content-Type=%s, Charset=%s => %s", action.id, ct.getContentType(),
//                                ct.getCharset(), lang.getName())) ;
//        }
        dest.prefix("root", base+"#");
        ActionLib.parse(action, dest, input, lang, base) ;
    }
}
