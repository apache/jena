/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;
import static org.openjena.fuseki.HttpNames.* ;
import static org.openjena.fuseki.HttpNames.METHOD_DELETE ;
import static org.openjena.fuseki.HttpNames.METHOD_GET ;
import static org.openjena.fuseki.HttpNames.METHOD_HEAD ;
import static org.openjena.fuseki.HttpNames.METHOD_POST ;
import static org.openjena.fuseki.HttpNames.METHOD_PUT ;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.Enumeration ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.DEF ;
import org.openjena.fuseki.FusekiLib ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.conneg.ConNeg ;
import org.openjena.fuseki.conneg.ContentType ;
import org.openjena.fuseki.conneg.MediaType ;
import org.openjena.fuseki.conneg.TypedStream ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRDFXML ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.system.IRIResolver ;
import org.openjena.riot.system.RiotLib ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileUtils ;

public class SPARQL_REST extends SPARQL_ServletBase
{
    private static Logger classLog = LoggerFactory.getLogger(SPARQL_REST.class) ;
    
    private static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(serverlog) ;

    class HttpActionREST extends HttpAction {
        final Target target ; 
        public HttpActionREST(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, dsg, request, response, verbose) ;
            this.target = targetGraph(request, dsg) ;
        }
    }
    
    public SPARQL_REST(boolean verbose)
    { super(PlainRequestFlag.DIFFERENT, verbose) ; }

    public SPARQL_REST()
    { this(false) ; }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doCommon(request, response) ;
    }
    
    private void maybeSetLastModified(HttpServletResponse resp, long lastModified)
    {
        if (resp.containsHeader(HEADER_LASTMOD)) return ;
        if (lastModified >= 0) resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }
    
    @Override
    protected void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response)
    {
        validate(request) ;
        HttpActionREST action = new HttpActionREST(id, dsg, request, response, verbose_debug) ;
        dispatch(action) ;
    }


    private void dispatch(HttpActionREST action)
    {
        HttpServletRequest req = action.request ;
        HttpServletResponse resp = action.response ;
        String method = req.getMethod().toUpperCase() ;

        // See HttpServlet.service.
        // We dispatch by REQUEST
        
        if (method.equals(METHOD_GET))
            doGet(action);
        else if (method.equals(METHOD_HEAD))
            doHead(action);
        else if (method.equals(METHOD_POST))
            doPost(action);
        else if (method.equals(METHOD_PATCH))
            //doPatch(action) ;
            errorNotImplemented() ;
        else if (method.equals(METHOD_OPTIONS))
            //doOptions(action) ;
            errorNotImplemented() ;
        else if (method.equals(METHOD_TRACE))
            //doTrace(action) ;
            errorNotImplemented() ;
        else if (method.equals(METHOD_PUT))
            doPut(action) ;   
        else if (method.equals(METHOD_DELETE))
            doDelete(action) ;
        else
            errorNotImplemented() ;
    }
        
    @Override
    protected String mapRequestToDataset(String uri)
    {
        String uri2 = mapRequestToDataset(uri, HttpNames.ServiceData) ;
        return (uri2 != null) ? uri2 : uri ; 
    }

    protected void doGet(HttpActionREST action)
    {
        try {
            // Creating target creates the graph in some datasets.
            if ( ! action.target.isDefault )
            {
                if ( ! action.dsg.containsGraph(action.target.graphName) )
                    SPARQL_ServletBase.errorNotFound("No such graph: "+action.target.name) ;
            }

            TypedStream stream = createTypedStream(action.request) ;
            action.response.setContentType(stream.getMediaType());
            action.response.setCharacterEncoding(stream.getCharset()) ;
            //response.setContentLength(0) ;

            if ( action.verbose )
            {
                // Not DRY
                Lang lang = FusekiLib.langFromContentType(stream.getMediaType()) ;
                if ( lang == null )
                    lang = Lang.RDFXML ;
                serverlog.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                                      action.id, stream.getMediaType(), stream.getCharset(), lang.getName())) ;
            }

            action.lock.enterCriticalSection(Lock.READ) ;
            try {
                // If we want to set the Content-Length, we need to buffer.
                RDFWriter writer = FusekiLib.chooseWriter(stream) ;
                Model model = ModelFactory.createModelForGraph(action.target.graph) ;
                writer.write(model, action.response.getOutputStream(), null) ;
                action.response.setStatus(HttpServletResponse.SC_OK);
            } finally { action.lock.leaveCriticalSection() ; }
        } catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    protected void doHead(HttpActionREST action)
    {
        boolean exists = (action.target.graph != null) ; 
        if ( exists )
            SPARQL_ServletBase.successNoContent(action) ;
        else
            SPARQL_ServletBase.successNotFound(action) ;
    }

    protected void doDelete(HttpActionREST action)
    {
        action.lock.enterCriticalSection(Lock.WRITE) ;
        try {
            deleteGraph(action) ;
            SPARQL_ServletBase.sync(action.dsg) ;
        } finally { action.lock.leaveCriticalSection() ; }
        SPARQL_ServletBase.successNoContent(action) ;
    }

    protected void doPut(HttpActionREST action)
    {
        boolean existedBefore = (action.target.graph != null) ; 
        DatasetGraph body = parseBody(action) ;
        action.lock.enterCriticalSection(Lock.WRITE) ;
        try {
            clearGraph(action.target) ;
            //deleteGraph(target) ;   // Opps. Deletes the target!
            addDataInto(body.getDefaultGraph(), action.target.graph) ;
            SPARQL_ServletBase.sync(action.dsg) ;
        } finally { action.lock.leaveCriticalSection() ; }
        // Differentiate: 201 Created or 204 No Content 
        if ( existedBefore )
            SPARQL_ServletBase.successNoContent(action) ;
        else
            SPARQL_ServletBase.successCreated(action) ;
    }

    protected void doPost(HttpActionREST action)
    {
        boolean existedBefore = (action.target.graph != null) ; 
        DatasetGraph body = parseBody(action) ;
        action.lock.enterCriticalSection(Lock.WRITE) ;
        try {
            addDataInto(body.getDefaultGraph(), action.target.graph) ;
            SPARQL_ServletBase.sync(action.dsg) ;
        } finally { action.lock.leaveCriticalSection() ; }
        if ( existedBefore )
            SPARQL_ServletBase.successNoContent(action) ;
        else
            SPARQL_ServletBase.successCreated(action) ;
    }

    @Override
    protected void requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        errorBadRequest("No query string") ;
    }

    private TypedStream createTypedStream(HttpServletRequest request)
    {
        MediaType contentType = ConNeg.chooseContentType(request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        String charset = null ;
        
        if ( ! DEF.acceptRDFXML.equals(contentType) )
            charset = ConNeg.chooseCharset(request, DEF.charsetOffer, DEF.charsetUTF8).getType() ;
        
        String contentTypeStr = contentType.getMediaType() ;
        try {
            TypedStream ts = new TypedStream(request.getInputStream(),
                                             contentTypeStr,
                                             charset) ;
            return ts ;
        } catch (IOException ex) { errorOccurred(ex) ; return null ; }
    }

    // Auxilliary functionality.
    
    private void deleteGraph(HttpActionREST action)
    {
        if ( action.target.isDefault )
            action.target.graph.getBulkUpdateHandler().removeAll() ;
        else
            action.dsg.removeGraph(action.target.graphName) ;
    }

    private void clearGraph(Target target)
    {
        target.graph.getBulkUpdateHandler().removeAll() ;
    }

    private void addDataInto(Graph data, Graph dest)
    {
        dest.getBulkUpdateHandler().add(data) ;
    }

    private DatasetGraph parseBody(HttpActionREST action)
    {
        // This is reader code as for client GET.
        // ---- ContentNeg / Webreader.
        String contentTypeHeader = action.request.getContentType() ;
        ContentType ct = FusekiLib.contentType(contentTypeHeader) ;
        int len = action.request.getContentLength() ;
        
        Lang lang = FusekiLib.langFromContentType(ct.contentType) ;
        if ( lang == null )
            SPARQL_ServletBase.errorBadRequest("Unknown: "+contentTypeHeader) ;
            //errorOccurred

        if ( action.verbose )
        {
            if ( len >= 0 )
                serverlog.info(format("[%d]    Body: len=%d, Content-Type=%s, Charset=%s => %s", 
                                      action.id, len, ct.contentType, ct.charset, lang.getName())) ;
            else
                serverlog.info(format("[%d]    Body: Content-Type=%s, Charset=%s => %s", 
                                      action.id, ct.contentType, ct.charset, lang.getName())) ;
        }
        
        try {
            InputStream input = action.request.getInputStream() ;
            String base =  SPARQL_ServletBase.wholeRequestURL(action.request) ;

            boolean buffering = false ;
            if ( buffering )
            {
                // Slurp the input : can be helpful for debugging.
                if ( len >= 0 )
                {
                    byte b[] = new byte[len] ;
                    input.read(b) ;
                    input = new ByteArrayInputStream(b) ; 
                }
                else
                {
                    // Without content length, reading to send of file is occassionaly fraught.
                    // Reason unknown - maybe some client mishandling of the stream. 
                    String x = FileUtils.readWholeFileAsUTF8(input) ;
                    System.out.println(x) ;
                    input = new ByteArrayInputStream(x.getBytes("UTF-8")) ; 
                }
            }
            Graph graphTmp = GraphFactory.createGraphMem() ;
            Sink<Triple> sink = new SinkTriplesToGraph(graphTmp) ;
            // Need ARQ upgrade
            // LangRIOT parser = RiotReader.createParserTriples(input, lang, base, sink) ;
            // parser.getProfile().setHandler(errorHandler) ;
            LangRIOT parser ;

            if ( lang.equals(Lang.RDFXML) )
            {
                String url = action.request.getRequestURL().toString() ;
                parser = LangRDFXML.create(action.request.getInputStream(), url, url, errorHandler, sink) ;
                parser.setProfile(RiotLib.profile(url, true, true, errorHandler)) ; 
            }
            else
            {
                parser = RiotReader.createParserTriples(input, lang, base, sink) ;
                parser.getProfile().setHandler(errorHandler) ;
            }

            parser.parse() ;
            DatasetGraph dsgTmp = DatasetGraphFactory.create(graphTmp) ;
            return dsgTmp ;
        } catch (IOException ex) { errorOccurred(ex) ; return null ; }
    }

    private static void validate(HttpServletRequest request)
    {
        @SuppressWarnings("unchecked")
        Enumeration<String> en = (Enumeration<String>)request.getParameterNames() ;
        for ( ; en.hasMoreElements() ; )
        {
            String h = en.nextElement() ;
            if ( ! HttpNames.paramGraph.equals(h) && ! HttpNames.paramGraphDefault.equals(h) )
                SPARQL_ServletBase.errorBadRequest("Unknown parameter '"+h+"'") ;
            if ( request.getParameterValues(h).length != 1 )
                SPARQL_ServletBase.errorBadRequest("Multiple parameters '"+h+"'") ;
        }
    }

    private static Target targetGraph(HttpServletRequest request, DatasetGraph dsg)
    {
        boolean dftGraph = getOneOnly(request, HttpNames.paramGraphDefault) != null ;
        String uri = getOneOnly(request, HttpNames.paramGraph) ;
        
        if ( !dftGraph && uri == null )
            SPARQL_ServletBase.errorBadRequest("Neither default graph nor named graph specificed") ;
        
        Node gn ;
        String absUri ;
        Graph g ;
        
        if ( uri != null )
        {
            String base = SPARQL_ServletBase.wholeRequestURL(request) ;
            absUri = IRIResolver.resolveString(uri, base) ;
            gn = Node.createURI(absUri) ;
            g = dsg.getGraph(gn) ;
        }
        else
        {
            absUri = null ;
            gn = null ;
            g = dsg.getDefaultGraph() ;
        }
        return new Target(dftGraph, g, absUri, gn) ;
    }
    
    private static String getOneOnly(HttpServletRequest request, String name)
    {
        String[] values = request.getParameterValues(name) ;
        if ( values == null )
            return null ;
        if ( values.length == 0 )
            return null ;
        if ( values.length > 1 )
            SPARQL_ServletBase.errorBadRequest("Multiple occurrences of '"+name+"'") ;
        return values[0] ;
    }
    
    // struct for target
    private static class Target
    {
        final boolean isDefault ;
        final Graph graph ;
        final String name ;
        final Node graphName ;
        Target(Graph graph, String name, Node graphName)
        {
            this(false, graph, name, graphName) ;
        }

        Target(Graph graph)
        {
            this(true, graph, null, null) ;
        }

        //private Target(boolean isDefault, Graph graph, String name, Node graphName)
        private Target(boolean isDefault, Graph graph, String name, Node graphName)
        {
            this.isDefault = isDefault ;
            this.graph = graph ;
            this.name  = name ;
            this.graphName = graphName ;

            if ( graph == null )
                throw new IllegalArgumentException("Inconsistent: no graph") ;

            if ( isDefault )
            {
                if ( name != null || graphName != null )
                    throw new IllegalArgumentException("Inconsistent: default and a graph name/node") ;       
            }
            else
            {
                if ( name == null || graphName == null )
                    throw new IllegalArgumentException("Inconsistent: not default and/or no graph name/node") ;
            }                
        }

        public String toString()
        {
            if ( isDefault ) return "default" ;
            return name ;
        }
    }

    // struct for exception return. 
    static class UpdateErrorException extends RuntimeException
    {
        final Throwable exception ;
        final String message ;
        final int rc ;
        UpdateErrorException(Throwable ex, String message, int rc)
        {
            this.exception = ex ;
            this.message = message ;
            this.rc = rc ;
        }
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