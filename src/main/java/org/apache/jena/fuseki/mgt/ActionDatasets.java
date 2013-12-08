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

import java.io.* ;
import java.nio.channels.FileChannel ;
import java.util.Iterator ;
import java.util.Locale ;

import javax.servlet.ServletException ;
import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.web.HttpSC ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.uuid.JenaUUID ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class ActionDatasets extends ActionCtl {
    // XXX DatasetRef to include UUID : see execPostDataset
    // DatasetRef ref = processService(s) ;
    //   Needs to do the state.
    
    private static Dataset system = SystemState.dataset ;
    private static DatasetGraphTransaction systemDSG = SystemState.dsg ; 
    
    static private Property pServiceName = FusekiVocab.pServiceName ;
    static private Property pStatus = FusekiVocab.pStatus ;

    public ActionDatasets() { super() ; }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommon(request, response);
    }
    
//    @Override
//    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        doCommon(request, response);
//    }

    @Override
    protected void perform(HttpAction action) {
        String method = action.request.getMethod().toUpperCase(Locale.ROOT) ;
        if ( method.equals("GET") )
            execGet(action) ;
        else if ( method.equals("POST") )
            execPost(action) ;
        else if ( method.equals("DELETE") )
            execDelete(action) ;
        else
            error(HttpSC.METHOD_NOT_ALLOWED_405) ;
        
//      system.begin(ReadWrite.READ) ;
//      try { RDFDataMgr.write(System.out, system, Lang.TRIG); }
//      finally { system.end() ; }

    }

    protected void execGet(HttpAction action) {
        JsonValue v ;
        if (action.dsRef.name == null )
            v = execGetContainer(action) ;
        else
            v = execGetDataset(action) ;
        try {
            HttpServletResponse response = action.response ;
            ServletOutputStream out = response.getOutputStream() ;
            response.setContentType(WebContent.contentTypeJSON);
            response.setCharacterEncoding(WebContent.charsetUTF8) ;
            JSON.write(out, v) ;
            out.println() ; 
            out.flush() ;
        } catch (IOException ex) { errorOccurred(ex) ; }
        success(action);
    }
    
    // This does not consult the system database for dormant etc.
    private JsonValue execGetDataset(HttpAction action) { 
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject() ;
        builder.key("datasets") ;
        JsonDescription.arrayDatasets(builder, DatasetRegistry.get());
        builder.finishObject() ;
        return builder.build() ;
    }

    private JsonValue execGetContainer(HttpAction action) {
        action.log.info(format("[%d] GET ds=%s", action.id, action.dsRef.name)) ;
        JsonBuilder builder = new JsonBuilder() ;
        String datasetPath = DatasetRef.canocialDatasetPath(action.dsRef.name) ;
        DatasetRef dsDesc = DatasetRegistry.get().get(datasetPath) ;
        if ( dsDesc == null )
            errorNotFound("Not found: dataset "+action.dsRef.name);
        JsonDescription.describe(builder, dsDesc) ;
        return builder.build() ;
    }
    
    // POST container -> register new dataset
    // POST contains/name -> change the state of an existing entry.
    
    protected void execPost(HttpAction action) {
        if (action.dsRef.name == null )
            execPostContainer(action) ;
        else
            execPostDataset(action) ;
    }
    
    // An action on a dataset.
    // XXX extend to backup etc??
    private void execPostDataset(HttpAction action) {
        String name = action.dsRef.name ;
        if ( name == null )
            name = "" ;
        action.log.info(format("[%d] POST dataset %s", action.id, name)) ;
        
        if ( action.dsRef.dataset == null )
            errorNotFound("Not found: dataset "+action.dsRef.name);
        DatasetRef dsDesc = action.dsRef ;
        String s = action.request.getParameter("status") ;
        if ( s == null || s.isEmpty() )
            errorBadRequest("No state change given") ;

        // setDatasetState is a transaction on the pesistent state of the server. 
        if ( s.equalsIgnoreCase("active") ) {
            setDatasetState(name, FusekiVocab.stateActive) ;        
            dsDesc.activate() ;
        } else if ( s.equalsIgnoreCase("dormant") ) {
            setDatasetState(name, FusekiVocab.stateDormant) ;        
            dsDesc.dormant() ;
        } else
            errorBadRequest("New state '"+s+"' not recognized");
        success(action) ;
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
    
    private void execPostContainer(HttpAction action) {
        String newURI = JenaUUID.generate().asURI() ;
        Node gn = NodeFactory.createURI(newURI) ;
        
        //action.beginWrite() ;
        boolean committed = false ;
        system.begin(ReadWrite.WRITE) ;
        try {
            Model model = system.getNamedModel(newURI) ;
            StreamRDF dest = StreamRDFLib.graph(model.getGraph()) ;
            bodyAsGraph(action, dest) ;
            // Find name.  SPARQL?
            
            Statement stmt = getOne(model, null, pServiceName, null) ;
            if ( stmt == null ) {
                StmtIterator sIter = model.listStatements(null, pServiceName, (RDFNode)null ) ;
                if ( ! sIter.hasNext() )
                    errorBadRequest("No name given in description of Fuseki service") ;
                sIter.next() ;
                if ( sIter.hasNext() )
                    errorBadRequest("Multiple names given in description of Fuseki service") ;
                throw new InternalErrorException("Inconsistent: getOne didn't fail the second time") ;
            }
                
            if ( ! stmt.getObject().isLiteral() )
                errorBadRequest("Found "+FmtUtils.stringForRDFNode(stmt.getObject())+" : Service names are strings, then used to build the external URI") ;
            
            Resource subject = stmt.getSubject() ;
            Literal object = stmt.getObject().asLiteral() ;
            
            if ( object.getDatatype() != null && ! object.getDatatype().equals(XSDDatatype.XSDstring) )
                action.log.warn(format("[%d] Service name '%s' is not a string", action.id, FmtUtils.stringForRDFNode(object)));

            String datasetName = object.getLexicalForm() ;
            model.removeAll(null, pStatus, null) ;
            model.add(subject, pStatus, FusekiVocab.stateActive) ;
            
            String datasetPath = DatasetRef.canocialDatasetPath(datasetName) ;
            // Need to be in Resource space at this point.
            DatasetRef dsRef = FusekiConfig.processService(subject) ;
            SPARQLServer.registerDataset(datasetPath, dsRef) ;
            system.commit();
            committed = true ;
            success(action) ;
        } finally { 
            if ( ! committed ) system.abort() ; 
            system.end() ; 
        }
    }
    
    protected void execDelete(HttpAction action) {
        // Does not exist?
        String name = action.dsRef.name ;
        if ( name == null )
            name = "" ;
        action.log.info(format("[%d] DELETE ds=%s", action.id, name)) ;
        if ( action.dsRef.name == null ) {
            errorBadRequest("DELETE only to the container entries.") ;
            return ;
        }

        systemDSG.begin(ReadWrite.WRITE) ;
        boolean committed =false ;
        try {
            // Name to graph
            Quad q = getOne(SystemState.dsg, null, null, pServiceName.asNode(), null) ;
            if ( q == null )
                errorBadRequest("Failed to find dataset for '"+name+"'");
            Node gn = q.getGraph() ;
            
            DatasetRef dsRef = DatasetRegistry.get().get(name) ;
            dsRef.gracefulShutdown() ;
            DatasetRegistry.get().remove(name) ;
            // XXX or set to state deleted.
            systemDSG.deleteAny(gn, null, null, null) ;
            systemDSG.commit() ;
            committed = true ;
            success(action) ;
        } finally { 
            if ( ! committed ) systemDSG.abort() ; 
            systemDSG.end() ; 
        }
    }

    private Quad getOne(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.findNG(g, s, p, o) ;
        if ( ! iter.hasNext() )
            return null ;
        Quad q = iter.next() ;
        if ( iter.hasNext() )
            return null ;
        return q ;
    }
    
    private Statement getOne(Model m, Resource s, Property p, RDFNode o) {
        StmtIterator iter = m.listStatements(s, p, o) ;
        if ( ! iter.hasNext() )
            return null ;
        Statement stmt = iter.next() ;
        if ( iter.hasNext() )
            return null ;
        return stmt ;
    }
    
    private static void copyFile(File source, File dest) {
        try {
            @SuppressWarnings("resource")
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            @SuppressWarnings("resource")
            FileChannel destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destChannel.close();
        } catch (IOException ex) { IO.exception(ex); }
    }
    
    // XXX Merge with SPARQL_REST_RW.incomingData
    
    private static void bodyAsGraph(HttpAction action, StreamRDF dest) {
        HttpServletRequest request = action.request ;
        String base = wholeRequestURL(request) ;
        ContentType ct = FusekiLib.getContentType(request) ;
        Lang lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
        if ( lang == null ) {
            errorBadRequest("Unknown content type for triples: " + ct) ;
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
        parse(action, dest, input, lang, base) ;
         
    }

    // See SPARQL_REST for common code.
    public static void parse(HttpAction action, StreamRDF dest, InputStream input, Lang lang, String base) {
        // Need to adjust the error handler.
//        try { RDFDataMgr.parse(dest, input, base, lang) ; }
//        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
        LangRIOT parser = RiotReader.createParser(input, lang, base, dest) ;
        ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(action.log) ;
        parser.getProfile().setHandler(errorHandler) ;
        try { parser.parse() ; } 
        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
    }
}

