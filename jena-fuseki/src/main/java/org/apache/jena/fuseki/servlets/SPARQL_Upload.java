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
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.http.HttpSC ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.iri.IRI ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.ContentType ;
import org.openjena.riot.* ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

public class SPARQL_Upload extends SPARQL_ServletBase 
{
    private static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;
    
    private class HttpActionUpload extends HttpAction {
        public HttpActionUpload(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, desc, request, response, verbose) ;
        }
    }
    
    public SPARQL_Upload(boolean verbose_debug)
    {
        super(PlainRequestFlag.REGULAR, verbose_debug) ;
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
        
        ServletFileUpload upload = new ServletFileUpload();
        // Locking only needed over the insert into dataset
        try {
            String graphName = null ;
            Graph graphTmp = GraphFactory.createGraphMem() ;
            Node gn = null ;
            String name = null ;  
            ContentType ct = null ;
            Lang lang = null ;
            int tripleCount = 0 ;
            
            FileItemIterator iter = upload.getItemIterator(request);
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
                            gn = Node.createURI(graphName) ;
                        }
                    }
                    else if ( fieldName.equals(HttpNames.paramDefaultGraphURI) )
                        graphName = null ;
                    else
                        // Add file type?
                        log.info(format("[%d] Upload: Field="+fieldName+" - ignored")) ;
                } else {
                    // Process the input stream
                    name = item.getName() ; 
                    if ( name == null || name.equals("") || name.equals("UNSET FILE NAME") ) 
                        errorBadRequest("No name for content - can't determine RDF syntax") ;
                    
                    String contentTypeHeader = item.getContentType() ;
                    ct = ContentType.parse(contentTypeHeader) ;
                    
                    lang = FusekiLib.langFromContentType(ct.getContentType()) ;
                    if ( lang == null )
                        lang = Lang.guess(name) ;
                    if ( lang == null )
                        // Desperate.
                        lang = Lang.RDFXML ;
                    
                    String base = "http://example/upload-base/" ;
                    // We read into a in-memory graph, then (if successful) update the dataset.
                    // This isolates errors.
                    Sink<Triple> sink = new SinkTriplesToGraph(graphTmp) ;
                    LangRIOT parser = RiotReader.createParserTriples(stream, lang, base, sink) ;
                    parser.getProfile().setHandler(errorHandler) ;
                    try {
                        parser.parse() ;
                    } 
                    catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
                    finally { sink.close() ; }
                    
                    tripleCount = graphTmp.size() ;
                    //DatasetGraph dsgTmp = DatasetGraphFactory.create(graphTmp) ;
                }
            }    
                
            if ( graphName == null )
                graphName = "default" ;
            log.info(format("[%d] Upload: Filename: %s, Content-Type=%s, Charset=%s => (%s,%s,%d triple(s))", 
                                      action.id, name,  ct.getContentType(), ct.getCharset(), graphName, lang.getName(), tripleCount)) ;

            // Delay updating until all form fields processed to get the graph name 
            action.beginWrite() ;
            try {
                if ( graphName.equals(HttpNames.valueDefault) ) 
                    action.getActiveDSG().getDefaultGraph().getBulkUpdateHandler().add(graphTmp) ;
                else
                    action.getActiveDSG().getGraph(gn).getBulkUpdateHandler().add(graphTmp) ;
                action.commit() ;
            } finally { action.endWrite() ; }
                    
            response.setContentType("text/plain") ;
            response.getOutputStream().print("Triples = "+tripleCount) ;
            success(action) ;
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)
        {
            errorOccurred(ex) ;
            return ;
        }
    }

    private void validate(HttpServletRequest request)
    {}

    @Override
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        errorOccurred("requestNoQueryString") ;
        return true ;
    }

}
