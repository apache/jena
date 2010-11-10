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
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.server.DatasetRegistry ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public class SPARQL_Upload extends SPARQL_ServletBase 
{
    // RENAME
    // Reserve : /system/ /fuseki/ /mgt/ /admin/
    
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
    }
    
    @Override
    protected void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response)
    {
        validate(request) ;
        HttpActionUpload action = new HttpActionUpload(id, dsg, request, response, verbose_debug) ;
        
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if ( ! isMultipart )
            error(HttpSC.BAD_REQUEST_400 , "Not a file upload") ;
        ServletFileUpload upload = new ServletFileUpload();
        try {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String fieldName = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField())
                {
                    // TODO This will be the IRI.
                    String value = Streams.asString(stream) ;
                    //System.out.println("Form field " + fieldName + " with value " + Streams.asString(stream) + " detected.");
                } else {
//                    System.out.println("File field " + fieldName + " with file name "
//                                       + item.getName() + " detected.");
                    // Process the input stream
                    String name = item.getName() ; 
                    serverlog.info(format("[%d] Upload: Filename: %s", action.id, name)) ;

                    // We read into a in-memory graph, then (if successful) update the dataset.
                    Graph graph = GraphFactory.createDefaultGraph() ;
                    Sink<Triple> sink = new SinkTriplesToGraph(graph) ;
                    // TODO Content-type.
                    try {
                        LangRIOT parser = RiotReader.createParserTurtle(stream, null, sink) ;
                        parser.getProfile().setHandler(ErrorHandlerFactory.errorHandlerNoLogging) ;
                        parser.parse() ;
                    } catch (Exception ex) { errorBadRequest(ex.getMessage()) ; }
                    
                    int x = graph.size() ;
                    // Only default graph.
                    // TODO named Destination
                    action.beginWrite() ;
                    try {
                        dsg.getDefaultGraph().getBulkUpdateHandler().add(graph) ;
                    } finally { action.endWrite() ; }
                    
                    serverlog.info(format("[%d] Upload: %d triples", action.id, x)) ;
                    
                    response.setContentType("text/plain") ;
                    response.getOutputStream().print("Triples = "+x) ;
                    success(action) ;
                }
            }
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