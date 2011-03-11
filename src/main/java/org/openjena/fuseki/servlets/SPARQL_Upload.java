/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;

import java.io.IOException ;
import java.io.InputStream ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.commons.fileupload.util.Streams ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.FusekiLib ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.conneg.ContentType ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.server.DatasetRegistry ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public class SPARQL_Upload extends SPARQL_ServletBase 
{
    private static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(serverlog) ;
    
    private class HttpActionUpload extends HttpAction {
        public HttpActionUpload(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, dsg, request, response, verbose) ;
        }
    }
    
    public SPARQL_Upload(boolean verbose_debug)
    {
        super(PlainRequestFlag.REGULAR, verbose_debug) ;
    }

    @Override
    protected String mapRequestToDataset(String uri)
    {
        // MgtServlet
        String uri2 = mapRequestToDataset(uri, HttpNames.ServiceUpload) ;
        if ( uri2 != null && ! "".equals(uri2) )
            return uri2 ;
        if ( DatasetRegistry.get().size() == 1 )
            // Managing a single dataset.
            return DatasetRegistry.get().keys().next();
        return null ;
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
    protected void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response)
    {
        // Only allows one file in the upload.
        
        validate(request) ;
        HttpActionUpload action = new HttpActionUpload(id, dsg, request, response, verbose_debug) ;
        
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
                    String value = Streams.asString(stream) ;
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
                            if ( iri.getRawHost() == null )
                                errorBadRequest("Bad IRI: no host name: "+graphName) ;
                            if ( iri.getRawPath() != null && iri.getRawPath().length() > 0 && iri.getRawPath().charAt(0) != '/' )
                                errorBadRequest("Bad IRI: Pat does not start '/': "+graphName) ;
                            gn = Node.createURI(graphName) ;
                        }
                    }
                    // Add file type?
                    else
                        serverlog.info(format("[%d] Upload: Field="+fieldName+" - ignored")) ;
                    //System.out.println("Form field " + fieldName + " with value " + Streams.asString(stream) + " detected.");
                } else {
//                    System.out.println("File field " + fieldName + " with file name "
//                                       + item.getName() + " detected.");
                    // Process the input stream
                    name = item.getName() ; 
                    if ( name == null || name.equals("") || name.equals("UNSET FILE NAME") ) 
                        errorBadRequest("No name for content - can't determine RDF syntax") ;
                    
                    String contentTypeHeader = item.getContentType() ;
                    ct = ContentType.parse(contentTypeHeader) ;
                    
                    lang = FusekiLib.langFromContentType(ct.contentType) ;
                    if ( lang == null )
                        lang = Lang.guess(name) ;
                    if ( lang == null )
                        // Desparate.
                        lang = Lang.RDFXML ;
                    
                    String base = "http://example/upload-base/" ;
                    // We read into a in-memory graph, then (if successful) update the dataset.
                    Sink<Triple> sink = new SinkTriplesToGraph(graphTmp) ;
                    LangRIOT parser = RiotReader.createParserTriples(stream, lang, base, sink) ;
                    parser.getProfile().setHandler(errorHandler) ;
                    try {
                        parser.parse() ;
                    } catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
                    tripleCount = graphTmp.size() ;
                    //DatasetGraph dsgTmp = DatasetGraphFactory.create(graphTmp) ;
                }
            }    
                
            if ( graphName == null )
                graphName = "default" ;
            serverlog.info(format("[%d] Upload: Filename: %s, Content-Type=%s, Charset=%s => (%s,%s,%d triple(s))", 
                                      action.id, name,  ct.contentType, ct.charset, graphName, lang.getName(), tripleCount)) ;

            // Delay updating until all form fields processed to get the graph name 
            action.beginWrite() ;
            try {
                if ( graphName.equals(HttpNames.valueDefault) ) 
                    dsg.getDefaultGraph().getBulkUpdateHandler().add(graphTmp) ;
                else
                    dsg.getGraph(gn).getBulkUpdateHandler().add(graphTmp) ;
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */