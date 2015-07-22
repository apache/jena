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

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.io.StringReader ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.build.Builder ;
import org.apache.jena.fuseki.build.Template ;
import org.apache.jena.fuseki.build.TemplateFunctions ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.servlets.* ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
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
    private static final String tDatabasetTDB       = "tdb" ;
    private static final String tDatabasetMem       = "mem" ;

    public ActionDatasets() { super() ; }
    
    // ---- GET : return details of dataset or datasets.
    @Override
    protected JsonValue execGetContainer(HttpAction action) { 
        action.log.info(format("[%d] GET datasets", action.id)) ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("D") ;
        builder.key(JsonConst.datasets) ;
        JsonDescription.arrayDatasets(builder, DataAccessPointRegistry.get());
        builder.finishObject("D") ;
        return builder.build() ;
    }

    @Override
    protected JsonValue execGetItem(HttpAction action) {
        action.log.info(format("[%d] GET dataset %s", action.id, action.getDatasetName())) ;
        JsonBuilder builder = new JsonBuilder() ;
        DataAccessPoint dsDesc = DataAccessPointRegistry.get().get(action.getDatasetName()) ;
        if ( dsDesc == null )
            ServletOps.errorNotFound("Not found: dataset "+action.getDatasetName());
        JsonDescription.describe(builder, dsDesc) ;
        return builder.build() ;
    }
    
    // ---- POST 
    
    // DB less version
    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        JenaUUID uuid = JenaUUID.generate() ;
        String newURI = uuid.asURI() ;
        Node gn = NodeFactory.createURI(newURI) ;
        
        ContentType ct = FusekiLib.getContentType(action) ;
        
        boolean committed = false ;
        // Also acts as a concurrency lock
        system.begin(ReadWrite.WRITE) ;
        String filename1 = null ;
        String filename2 = null ;
            
        try {
            // Where to build the templated service/database. 
            Model model = ModelFactory.createDefaultModel() ;
            StreamRDF dest = StreamRDFLib.graph(model.getGraph()) ;
    
            if ( WebContent.isHtmlForm(ct) )
                assemblerFromForm(action, dest) ;
            else if ( WebContent.isMultiPartForm(ct) )
                assemblerFromUpload(action, dest) ;
            else
                assemblerFromBody(action, dest) ;
            
            // Keep a persistent copy imediately.  This is not used for
            // anything other than being "for the record".
            filename1 = FusekiServer.dirFileArea.resolve(uuid.asString()).toString() ;
            try ( OutputStream outCopy = IO.openOutputFile(filename1) ) {
                RDFDataMgr.write(outCopy, model, Lang.TURTLE) ;
            }

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
            
            String datasetName = object.getLexicalForm() ;
            String datasetPath = DataAccessPoint.canonical(datasetName) ;
            action.log.info(format("[%d] Create database : name = %s", action.id, datasetPath)) ;
            
            if ( DataAccessPointRegistry.get().isRegistered(datasetPath) )
                // And abort.
                ServletOps.error(HttpSC.CONFLICT_409, "Name already registered "+datasetPath) ;

            // Copy to the configuration directory for server start up next time.
            filename2 = datasetPath.substring(1) ;        // Without "/"
            filename2 = FusekiServer.dirConfiguration.resolve(filename2).toString()+".ttl" ;
            if ( FileOps.exists(filename2) )
                ServletOps.error(HttpSC.INTERNAL_SERVER_ERROR_500, "Configuration file of that name already exists "+filename2) ;

            try ( OutputStream outCopy = IO.openOutputFile(filename2) ) {
                RDFDataMgr.write(outCopy, model, Lang.TURTLE) ;
            }

            // Currently do nothing with the system database.
            // In the future ... maybe ...
//            Model modelSys = system.getNamedModel(gn.getURI()) ;
//            modelSys.removeAll(null, pStatus, null) ;
//            modelSys.add(subject, pStatus, FusekiVocab.stateActive) ;
            
            // Need to be in Resource space at this point.
            DataAccessPoint ref = Builder.buildDataAccessPoint(subject) ;
            DataAccessPointRegistry.register(datasetPath, ref) ;
            action.getResponse().setContentType(WebContent.contentTypeTextPlain); 
            ServletOutputStream out = action.getResponse().getOutputStream() ;
            ServletOps.success(action) ;
            system.commit();
            committed = true ;
            
        } catch (IOException ex) { IO.exception(ex); }
        finally { 
            if ( ! committed ) {
                if ( filename1 != null ) FileOps.deleteSilent(filename1);
                if ( filename2 != null ) FileOps.deleteSilent(filename2);
                system.abort() ; 
            }
            system.end() ; 
        }
        return null ;
    }
    
//    //@Override
//    // The system database version.  
//    // Keep for easy replacement until comfortable new way is stable. 
//    protected JsonValue execPostContainer1(HttpAction action) {
//        JenaUUID uuid = JenaUUID.generate() ;
//        String newURI = uuid.asURI() ;
//        Node gn = NodeFactory.createURI(newURI) ;
//        
//        ContentType ct = FusekiLib.getContentType(action) ;
//        
//        boolean committed = false ;
//        system.begin(ReadWrite.WRITE) ;
//        try {
//            Model model = system.getNamedModel(gn.getURI()) ;
//            StreamRDF dest = StreamRDFLib.graph(model.getGraph()) ;
//    
//            if ( WebContent.isHtmlForm(ct) )
//                assemblerFromForm(action, dest) ;
//            else if ( WebContent.isMultiPartForm(ct) )
//                assemblerFromUpload(action, dest) ;
//            else
//                assemblerFromBody(action, dest) ;
//            
//            // Keep a persistent copy.
//            String filename = FusekiServer.dirFileArea.resolve(uuid.asString()).toString() ;
//            try ( OutputStream outCopy = new FileOutputStream(filename) ) {
//                RDFDataMgr.write(outCopy, model, Lang.TURTLE) ;
//            }
//            
//            Statement stmt = getOne(model, null, pServiceName, null) ;
//            if ( stmt == null ) {
//                StmtIterator sIter = model.listStatements(null, pServiceName, (RDFNode)null ) ;
//                if ( ! sIter.hasNext() )
//                    ServletOps.errorBadRequest("No name given in description of Fuseki service") ;
//                sIter.next() ;
//                if ( sIter.hasNext() )
//                    ServletOps.errorBadRequest("Multiple names given in description of Fuseki service") ;
//                throw new InternalErrorException("Inconsistent: getOne didn't fail the second time") ;
//            }
//                
//            if ( ! stmt.getObject().isLiteral() )
//                ServletOps.errorBadRequest("Found "+FmtUtils.stringForRDFNode(stmt.getObject())+" : Service names are strings, then used to build the external URI") ;
//            
//            Resource subject = stmt.getSubject() ;
//            Literal object = stmt.getObject().asLiteral() ;
//            
//            if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
//                action.log.warn(format("[%d] Service name '%s' is not a string", action.id, FmtUtils.stringForRDFNode(object)));
//    
//            String datasetName = object.getLexicalForm() ;
//            String datasetPath = DataAccessPoint.canonical(datasetName) ;
//            action.log.info(format("[%d] Create database : name = %s", action.id, datasetPath)) ;
//            
//            if ( DataAccessPointRegistry.get().isRegistered(datasetPath) )
//                // And abort.
//                ServletOps.error(HttpSC.CONFLICT_409, "Name already registered "+datasetPath) ;
//                
//            model.removeAll(null, pStatus, null) ;
//            model.add(subject, pStatus, FusekiVocab.stateActive) ;
//            
//            // Need to be in Resource space at this point.
//            DataAccessPoint ref = Builder.buildDataAccessPoint(subject) ;
//            DataAccessPointRegistry.register(datasetPath, ref) ;
//            action.getResponse().setContentType(WebContent.contentTypeTextPlain); 
//            ServletOutputStream out = action.getResponse().getOutputStream() ;
//            ServletOps.success(action) ;
//            system.commit();
//            committed = true ;
//            
//        } catch (IOException ex) { IO.exception(ex); }
//        finally { 
//            if ( ! committed ) system.abort() ; 
//            system.end() ; 
//        }
//        return null ;
//    }

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
            DataAccessPoint access = action.getDataAccessPoint() ;
            //access.goOffline() ;
            dSrv.goOffline() ;  // Affects the target of the name. 
            setDatasetState(name, FusekiVocab.stateOffline) ;  
            //dSrv.offline() ;
        } else if ( s.equalsIgnoreCase("unlink") ) {
            action.log.info(format("[%d] UNLINK ACCESS NAME %s", action.id, name)) ;
            DataAccessPoint access = action.getDataAccessPoint() ;
            ServletOps.errorNotImplemented("unlink: dataset"+action.getDatasetName());
            //access.goOffline() ;
            // Registry?
        }
        else
            ServletOps.errorBadRequest("State change operation '"+s+"' not recognized");
        return null ;
    }

    private void assemblerFromBody(HttpAction action, StreamRDF dest) {
        bodyAsGraph(action, dest) ;
    }

    private void assemblerFromForm(HttpAction action, StreamRDF dest) {
        String dbType = action.getRequest().getParameter(paramDatasetType) ;
        String dbName = action.getRequest().getParameter(paramDatasetName) ;
        if ( dbType == null || dbName == null )
            ServletOps.errorBadRequest("Required parameters: dbName and dbType");
        
        Map<String, String> params = new HashMap<>() ;
        
        if ( dbName.startsWith("/") )
            params.put(Template.NAME, dbName.substring(1)) ;
        else
            params.put(Template.NAME, dbName) ;
        FusekiServer.addGlobals(params); 
        
        //action.log.info(format("[%d] Create database : name = %s, type = %s", action.id, dbName, dbType )) ;
        if ( ! dbType.equals(tDatabasetTDB) && ! dbType.equals(tDatabasetMem) )
            ServletOps.errorBadRequest(format("dbType can be only '%s' or '%s'", tDatabasetTDB, tDatabasetMem)) ;
        
        String template = null ;
        if ( dbType.equalsIgnoreCase(tDatabasetTDB))
            template = TemplateFunctions.templateFile(Template.templateTDBFN, params, Lang.TTL) ;
        if ( dbType.equalsIgnoreCase(tDatabasetMem))
            template = TemplateFunctions.templateFile(Template.templateMemFN, params, Lang.TTL) ;
        RDFDataMgr.parse(dest, new StringReader(template), "http://base/", Lang.TTL) ;
    }

    private void assemblerFromUpload(HttpAction action, StreamRDF dest) {
        Upload.fileUploadWorker(action, dest);
    }

    // ---- DELETE

    @Override
    protected void execDeleteItem(HttpAction action) {
//      if ( isContainerAction(action) ) {
//      ServletOps.errorBadRequest("DELETE only applies to a specific dataset.") ;
//      return ;
//  }
  
        // Does not exist?
        String name = action.getDatasetName() ;
        if ( name == null )
            name = "" ;
        action.log.info(format("[%d] DELETE ds=%s", action.id, name)) ;

        if ( ! DataAccessPointRegistry.get().isRegistered(name) )
            ServletOps.errorNotFound("No such dataset registered: "+name);

        systemDSG.begin(ReadWrite.WRITE) ;
        boolean committed = false ;
        try {
            // Here, go offline.
            // Need to reference count operations when they drop to zero
            // or a timer goes off, we delete the dataset.
            
            DataAccessPoint ref = DataAccessPointRegistry.get().get(name) ;
            // Redo check inside transaction.
            if ( ref == null )
                ServletOps.errorNotFound("No such dataset registered: "+name);

            // Make it invisible to the outside.
            DataAccessPointRegistry.get().remove(name) ;
            
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

    // Persistent state change.
    private void setDatasetState(String name, Resource newState) {
        boolean committed = false ;
        system.begin(ReadWrite.WRITE) ;
        try {
            String dbName = name ;
            if ( dbName.startsWith("/") )
                dbName = dbName.substring(1) ;
            
            String update =  StrUtils.strjoinNL
                (SystemState.PREFIXES,
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

        int len = request.getContentLength() ;
//        if ( verbose ) {
//            if ( len >= 0 )
//                alog.info(format("[%d]   Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s", action.id, len,
//                                ct.getContentType(), ct.getCharset(), lang.getName())) ;
//            else
//                alog.info(format("[%d]   Body: Content-Type=%s, Charset=%s => %s", action.id, ct.getContentType(),
//                                ct.getCharset(), lang.getName())) ;
//        }
        dest.prefix("root", base+"#");
        ActionSPARQL.parse(action, dest, input, lang, base) ;
    }
}
