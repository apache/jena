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

import java.io.* ;
import java.nio.channels.FileChannel ;
import java.util.List ;

import javax.servlet.ServletException ;
import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.server.FusekiConfig ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.SPARQL_ServletBase ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;


public class DatasetsServlet extends SPARQL_ServletBase /* rename */ {
    //private static Logger log = Fuseki.adminLog ;

    // XXX Rewrite for (renamed) SPARQL_ServletBase
    
    public DatasetsServlet() {}
    
    // LOG
    
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
    
    @Override
    protected void validate(HttpAction action) {
    }

    @Override
    protected void perform(HttpAction action) {
        String name = mapRequestToDataset(action) ;
        
    }
    

    protected void execGet(HttpServletRequest request, HttpServletResponse response) {
        log.info("Admin: GET "+request.getRequestURI()); // +Query string.
        JsonBuilder builder = new JsonBuilder() ;
        String name = mapRequestToDataset(request) ;
        if ( name == null ) {
            // All
            builder.startObject() ;
            builder.key("datasets") ;
            JsonDescription.arrayDatasets(builder, DatasetRegistry.get());
            builder.finishObject() ;
        } else {
            String datasetPath = DatasetRef.canocialDatasetPath(name) ;
            DatasetRef dsDesc = DatasetRegistry.get().get(datasetPath) ;
            if ( dsDesc == null )
                errorNotFound("Not found: dataset "+name);
            JsonDescription.describe(builder, dsDesc) ;
        }
        JsonValue v = builder.build() ;
        try {
            ServletOutputStream out = response.getOutputStream() ;
            response.setContentType(WebContent.contentTypeJSON);
            response.setCharacterEncoding(WebContent.charsetUTF8) ;
            JSON.write(out, v) ;
            out.println() ; 
            out.flush() ;
        } catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    @Deprecated()
    static String mapRequestToDataset(HttpServletRequest request) {
        String pathInfo = request.getPathInfo() ;
        if ( pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") )
            return null ;
        String name = pathInfo ;
        // pathInfo starts with a "/"
        int idx = pathInfo.lastIndexOf('/') ;
        if ( idx > 0 )
            name = name.substring(idx) ;
        // Returns "/name"
        return name ; 
    }
    
    // Null means no name given.
    @Override
    protected String mapRequestToDataset(HttpAction action) {
        // Find the part after the name used to dispatch this request. 
        System.out.println("context path = '"+action.request.getContextPath()+"'") ;
        System.out.println("pathinfo     = '"+action.request.getPathInfo()+"'") ;
        System.out.println("servlet path = '"+action.request.getServletPath()+"'") ;
        

//        log.info("context path = "+action.request.getContextPath()) ;
//        log.info("pathinfo     = "+action.request.getPathInfo()) ;
//        log.info("servlet path = "+action.request.getServletPath()) ;
        // if /name
        //action.request.getServletPath() ;
        // if /*
        String pathInfo = action.request.getPathInfo() ;
        if ( pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") )
            return null ;
        String name = pathInfo ;
        // pathInfo starts with a "/"
        int idx = pathInfo.lastIndexOf('/') ;
        if ( idx > 0 )
            name = name.substring(idx) ;
        // Returns "/name"
        return name ; 
    }
    
    protected void execPost(HttpServletRequest request, HttpServletResponse response) {
        log.info("Admin: POST "+request.getRequestURI()); // +Query string.
        String name = mapRequestToDataset(request) ;
        if ( name != null ) {
            errorBadRequest("POST only to the container") ;
            return ;
        }
        
        Model m = ModelFactory.createDefaultModel() ;
        StreamRDF dest = StreamRDFLib.graph(m.getGraph()) ;
        bodyAsGraph(request, dest) ;
        List<DatasetRef> refs = FusekiConfig.readConfiguration(m);
        
        for (DatasetRef dsDesc : refs) {
            String datasetPath = dsDesc.name ;
            if ( DatasetRegistry.get().isRegistered(datasetPath) )
                // Remove?
                errorBadRequest("Already registered: " + dsDesc.name) ;
            SPARQLServer.registerDataset(datasetPath, dsDesc) ;
        }
        try {
            String n = IO.uniqueFilename(FusekiConfig.configurationsDirectory, "assem", "ttl") ;
            OutputStream out = new FileOutputStream(n) ;
            out = new BufferedOutputStream(out) ;
            RDFDataMgr.write(out, m, Lang.TURTLE) ;
            out.close() ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }

    protected void execDelete(HttpServletRequest request, HttpServletResponse response) {
        // Find DS.
        log.info("Admin: DELETE "+request.getRequestURI()); // +Query string.
        String name = mapRequestToDataset(request) ;
        if ( name == null ) {
            errorBadRequest("DELETE only to the container entries.") ;
            return ;
        }
        
        //  Canoncial name
        if ( ! DatasetRegistry.get().isRegistered(name) )
            errorBadRequest("Not found: " + name) ;
        
        DatasetRef dsRef = DatasetRegistry.get().get(name) ;
        DatasetRegistry.get().remove(name) ;
        // No longer routeable
        dsRef.gracefulShutdown() ;
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
    
    protected static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;

    private static void bodyAsGraph(HttpServletRequest request, StreamRDF dest) {
        String base = wholeRequestURL(request) ;
        ContentType ct = FusekiLib.getContentType(request) ;
        Lang lang = WebContent.contentTypeToLang(ct.getContentType()) ;
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
//                log.info(format("[%d]   Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s", action.id, len,
//                                ct.getContentType(), ct.getCharset(), lang.getName())) ;
//            else
//                log.info(format("[%d]   Body: Content-Type=%s, Charset=%s => %s", action.id, ct.getContentType(),
//                                ct.getCharset(), lang.getName())) ;
//        }
        dest.prefix("root", base+"#");
        parse(dest, input, lang, base) ;
         
    }

    public static void parse(StreamRDF dest, InputStream input, Lang lang, String base) {
        // Need to adjust the error handler.
//        try { RDFDataMgr.parse(dest, input, base, lang) ; }
//        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
        LangRIOT parser = RiotReader.createParser(input, lang, base, dest) ;
        parser.getProfile().setHandler(errorHandler) ;
        try { parser.parse() ; } 
        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
    }
}

