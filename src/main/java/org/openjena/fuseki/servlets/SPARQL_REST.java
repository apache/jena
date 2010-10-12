/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;
import static org.openjena.fuseki.HttpNames.HEADER_LASTMOD ;
import static org.openjena.fuseki.HttpNames.METHOD_DELETE ;
import static org.openjena.fuseki.HttpNames.METHOD_GET ;
import static org.openjena.fuseki.HttpNames.METHOD_HEAD ;
import static org.openjena.fuseki.HttpNames.METHOD_OPTIONS ;
import static org.openjena.fuseki.HttpNames.METHOD_PATCH ;
import static org.openjena.fuseki.HttpNames.METHOD_POST ;
import static org.openjena.fuseki.HttpNames.METHOD_PUT ;
import static org.openjena.fuseki.HttpNames.METHOD_TRACE ;

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
import org.openjena.fuseki.conneg.TypedOutputStream ;
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

            MediaType mediaType = contentNegotationRDF(action) ; 
            TypedOutputStream out = new TypedOutputStream(action.response.getOutputStream(), mediaType) ;
            Lang lang = FusekiLib.langFromContentType(mediaType.getContentType()) ;

            if ( action.verbose )
            {
                serverlog.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                                      action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
            }

            action.lock.enterCriticalSection(Lock.READ) ;
            try {
                // If we want to set the Content-Length, we need to buffer.
                //response.setContentLength(??) ;
                RDFWriter writer = FusekiLib.chooseWriter(lang) ;
                Model model = ModelFactory.createModelForGraph(action.target.graph()) ;
                writer.write(model, action.response.getOutputStream(), null) ;
                success(action) ;
            } finally { action.lock.leaveCriticalSection() ; }
        } catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    protected void doHead(HttpActionREST action)
    {
        if ( ! action.target.alreadyExisted )
        {
            successNotFound(action) ;
            return ;
        }
        MediaType mediaType = contentNegotationRDF(action) ;
        success(action) ;
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
        boolean existedBefore = action.target.alreadyExisted ; 
        DatasetGraph body = parseBody(action) ;
        action.lock.enterCriticalSection(Lock.WRITE) ;
        try {
            clearGraph(action.target) ;
            //deleteGraph(target) ;   // Opps. Deletes the target!
            addDataInto(body.getDefaultGraph(), action.target) ;
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
        boolean existedBefore = action.target.alreadyExisted ; 
        DatasetGraph body = parseBody(action) ;
        action.lock.enterCriticalSection(Lock.WRITE) ;
        try {
            addDataInto(body.getDefaultGraph(), action.target) ;
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

//    private TypedStream createTypedStream(MediaType contentType)
//    {
//        MediaType contentType = contentNegotationRDF(request) ;
//        String charset = null ;
//        
//        if ( ! DEF.acceptRDFXML.equals(contentType) )
//            charset = ConNeg.chooseCharset(request, DEF.charsetOffer, DEF.charsetUTF8).getType() ;
//        
//        String contentTypeStr = contentType.getMediaType() ;
//        try {
//            TypedStream ts = new TypedStream(request.getInputStream(),
//                                             contentTypeStr,
//                                             charset) ;
//            return ts ;
//        } catch (IOException ex) { errorOccurred(ex) ; return null ; }
//    }

    private MediaType contentNegotationRDF(HttpActionREST action)
    {
        MediaType mt = ConNeg.chooseContentType(action.request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        if ( mt == null )
            return null ;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType());
        if ( mt.getCharset() != null )
        action.response.setCharacterEncoding(mt.getCharset()) ;
        return mt ;
    }
    
    private MediaType contentNegotationQuads(HttpServletRequest request)
    {
        return ConNeg.chooseContentType(request, DEF.quadsOffer, DEF.acceptTriG) ;
    }

    // Auxilliary functionality.
    
    private void deleteGraph(HttpActionREST action)
    {
        if ( action.target.isDefault )
            action.target.graph().getBulkUpdateHandler().removeAll() ;
        else
            action.dsg.removeGraph(action.target.graphName) ;
    }

    private void clearGraph(Target target)
    {
        if ( target.isGraphSet() )
            target.graph().getBulkUpdateHandler().removeAll() ;
    }

    private void addDataInto(Graph data, Target dest)
    {   
        Graph g = dest.graph() ;
        g.getBulkUpdateHandler().add(data) ;
    }

    private DatasetGraph parseBody(HttpActionREST action)
    {
        // DRY - separate out conneg.
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
        
        if ( dftGraph )
            return Target.createDefault(dsg) ;
        
        // Named graph
        String base = SPARQL_ServletBase.wholeRequestURL(request) ;
        String absUri = IRIResolver.resolveString(uri, base) ;
        Node gn = Node.createURI(absUri) ;
        boolean alreadyExists ;
        dsg.getLock().enterCriticalSection(Lock.READ) ;
        try {
                alreadyExists = dsg.containsGraph(gn) ;
            return Target.createNamed(dsg, alreadyExists, absUri, gn) ;
        } finally { dsg.getLock().leaveCriticalSection() ; }
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
    private static final class Target
    {
        final boolean isDefault ;
        final boolean alreadyExisted ;
        final DatasetGraph dsg ;
        // May be null, then  
        private Graph _graph ;
        final String name ;
        final Node graphName ;
        
        static Target createNamed(DatasetGraph dsg, boolean alreadyExisted, String name, Node graphName)
        {
            return new Target(false, dsg, alreadyExisted, name, graphName) ;
        }

        static Target createDefault(DatasetGraph dsg)
        {
            return new Target(true, dsg, true, null, null) ;
        }

        //private Target(boolean isDefault, Graph graph, String name, Node graphName)
        private Target(boolean isDefault, DatasetGraph dsg, boolean alreadyExisted, String name, Node graphName)
        {
            this.isDefault = isDefault ;
            this.alreadyExisted = alreadyExisted ;
            this.dsg = dsg ;
            this._graph = null ;
            this.name  = name ;
            this.graphName = graphName ;

//            if ( graph == null )
//                throw new IllegalArgumentException("Inconsistent: no graph") ;

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

        public Graph graph()
        {
            if ( isGraphSet() )
            {
                if ( isDefault ) 
                    _graph = dsg.getDefaultGraph() ;
                else
                    _graph = dsg.getGraph(graphName) ;
            }
            return _graph ;
        }
        
        public boolean isGraphSet()
        {
            return _graph == null ;
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