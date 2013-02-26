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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;

import java.io.IOException ;
import java.io.InputStream ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.commons.fileupload.util.Streams ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.web.HttpSC ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

public class SPARQL_Upload extends SPARQL_ServletBase 
{
    private static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;
    
    private static class HttpActionUpload extends HttpAction {
        public HttpActionUpload(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, desc, request, response, verbose) ;
        }
    }
    
    public SPARQL_Upload(boolean verbose_debug)
    {
        super(verbose_debug) ;
    }

    // Methods to respond to.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        doCommon(request, response) ;
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
    {
        response.setHeader(HttpNames.hAllow, "OPTIONS,POST");
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }
    
    @Override
    protected void perform(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response)
    {
        // Only allows one file in the upload.
        
        validate(request) ;
        HttpActionUpload action = new HttpActionUpload(id, desc, request, response, verbose_debug) ;
        
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if ( ! isMultipart )
            error(HttpSC.BAD_REQUEST_400 , "Not a file upload") ;
        
        long tripleCount = -1 ;
        action.beginWrite() ;
        try {
            Graph graphTmp = GraphFactory.createDefaultGraph() ;
            String graphName = upload(action, graphTmp, "http://example/upload-base/") ;
            tripleCount = graphTmp.size() ;
            
            log.info(format("[%d] Upload: Graph: %s (%d triple(s))", 
                            action.id, graphName,  tripleCount)) ;
            
            Node gn = graphName.equals(HttpNames.valueDefault)
                ? Quad.defaultGraphNodeGenerated 
                : Node.createURI(graphName) ;
                
            FusekiLib.addDataInto(graphTmp, action.getActiveDSG(), gn) ;
            tripleCount = graphTmp.size();
            action.commit() ;
        } catch (RuntimeException ex)
        {
            // If anything went wrong, try to backout.
            try { action.abort() ; } catch (Exception ex2) {}
            errorOccurred(ex.getMessage()) ;
        } 
        finally { action.endWrite() ; }
        try {
            response.setContentType("text/plain") ;
            response.getOutputStream().print("Triples = "+tripleCount) ;
            success(action) ;
        }
        catch (Exception ex) { errorOccurred(ex) ; }
    }
    
    static public Graph upload(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response, String destination)
    {
        HttpActionUpload action = new HttpActionUpload(id, desc, request, response, false) ;
        // We read into a in-memory graph, then (if successful) update the dataset.
        // This isolates errors.
        Graph graphTmp = GraphFactory.createDefaultGraph() ;
        String graphName = upload(action, graphTmp, destination) ;
        return graphTmp ;
    }
    
    /** @return any graph name found.
     */
    
    static private String upload(HttpActionUpload action, Graph graphDst, String base)
    {
        ServletFileUpload upload = new ServletFileUpload();
        // Locking only needed over the insert into the dataset
        String graphName = null ;
        String name = null ;  
        ContentType ct = null ;
        Lang lang = null ;
        int tripleCount = 0 ;

        try {
            FileItemIterator iter = upload.getItemIterator(action.request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String fieldName = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField())
                {
                    // Graph name.
                    String value = Streams.asString(stream, "UTF-8") ;
                    if ( fieldName.equals(HttpNames.paramGraph) )
                    {
                        graphName = value ;
                        if ( graphName != null && ! graphName.equals(HttpNames.valueDefault) )
                        {
                            IRI iri = IRIResolver.parseIRI(value) ;
                            if ( iri.hasViolation(false) )
                                errorBadRequest("Bad IRI: "+graphName) ;
                            if ( iri.getScheme() == null )
                                errorBadRequest("Bad IRI: no IRI scheme name: "+graphName) ;
                            if ( iri.getScheme().equalsIgnoreCase("http") || iri.getScheme().equalsIgnoreCase("https")) 
                            {
                                // Redundant??
                                if ( iri.getRawHost() == null ) 
                                    errorBadRequest("Bad IRI: no host name: "+graphName) ;
                                if ( iri.getRawPath() == null || iri.getRawPath().length() == 0 )
                                    errorBadRequest("Bad IRI: no path: "+graphName) ;
                                if ( iri.getRawPath().charAt(0) != '/' )
                                    errorBadRequest("Bad IRI: Path does not start '/': "+graphName) ;
                            } 
                        }
                    }
                    else if ( fieldName.equals(HttpNames.paramDefaultGraphURI) )
                        graphName = null ;
                    else
                        // Add file type?
                        log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName)) ;
                } else {
                    // Process the input stream
                    name = item.getName() ; 
                    if ( name == null || name.equals("") || name.equals("UNSET FILE NAME") ) 
                        errorBadRequest("No name for content - can't determine RDF syntax") ;

                    String contentTypeHeader = item.getContentType() ;
                    ct = ContentType.parse(contentTypeHeader) ;

                    lang = WebContent.contentTypeToLang(ct.getContentType()) ;
                    if ( lang == null )
                        lang = RDFLanguages.filenameToLang(name) ;
                    if ( lang == null )
                        // Desperate.
                        lang = RDFLanguages.RDFXML ;

                    StreamRDF dest = StreamRDFLib.graph(graphDst) ;
                    LangRIOT parser = RiotReader.createParser(stream, lang, base, dest) ;
                    parser.getProfile().setHandler(errorHandler) ;
                    log.info(format("[%d] Upload: Filename: %s, Content-Type=%s, Charset=%s => %s", 
                                    action.id, name,  ct.getContentType(), ct.getCharset(), lang.getName())) ;
                    try { parser.parse() ; }
                    catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
                }
            }    

            if ( graphName == null )
                graphName = "default" ;
            return graphName ;
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)            { errorOccurred(ex) ; return null ; }
    }            

    @Override
    protected void validate(HttpServletRequest request)
    {}
}
