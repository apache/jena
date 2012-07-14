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
import static org.apache.jena.fuseki.HttpNames.HEADER_LASTMOD ;
import static org.apache.jena.fuseki.HttpNames.METHOD_DELETE ;
import static org.apache.jena.fuseki.HttpNames.METHOD_GET ;
import static org.apache.jena.fuseki.HttpNames.METHOD_HEAD ;
import static org.apache.jena.fuseki.HttpNames.METHOD_OPTIONS ;
import static org.apache.jena.fuseki.HttpNames.METHOD_PATCH ;
import static org.apache.jena.fuseki.HttpNames.METHOD_POST ;
import static org.apache.jena.fuseki.HttpNames.METHOD_PUT ;
import static org.apache.jena.fuseki.HttpNames.METHOD_TRACE ;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.Enumeration ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.ContentType ;
import org.openjena.atlas.web.MediaType ;
import org.openjena.riot.* ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import org.openjena.riot.system.IRIResolver ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileUtils ;

public abstract class SPARQL_REST extends SPARQL_ServletBase
{
    protected static Logger classLog = LoggerFactory.getLogger(SPARQL_REST.class) ;
    
    protected static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;

    protected class HttpActionREST extends HttpAction {
        private Target _target = null ; 
        protected HttpActionREST(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, desc, request, response, verbose) ;
        }

        protected final boolean hasTarget()
        {
            return 
                request.getParameter(HttpNames.paramGraphDefault) == null &&
                request.getParameter(HttpNames.paramGraph) == null ;
        }
        
        protected final Target getTarget() 
        {
            if ( _target == null )
                _target = targetGraph(request, super.getActiveDSG() ) ;
            return _target ;
        }
    }
    
    // struct for target
    protected static final class Target
    {
        final boolean isDefault ;
        final DatasetGraph dsg ;
        // May be null, then  
        private Graph _graph ;
        final String name ;
        final Node graphName ;

        static Target createNamed(DatasetGraph dsg, String name, Node graphName)
        {
            return new Target(false, dsg, name, graphName) ;
        }

        static Target createDefault(DatasetGraph dsg)
        {
            return new Target(true, dsg, null, null) ;
        }

        private Target(boolean isDefault, DatasetGraph dsg, String name, Node graphName)
        {
            this.isDefault = isDefault ;
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

        /** Get a graph for the action - this may create a graph in the dataset - this is not a test for graph existence */
        public Graph graph()
        {
            if ( ! isGraphSet() )
            {
                if ( isDefault ) 
                    _graph = dsg.getDefaultGraph() ;
                else
                    _graph = dsg.getGraph(graphName) ;
            }
            return _graph ;
        }

        public boolean exists()
        {
            if ( isDefault ) return true ;
            return dsg.containsGraph(graphName) ;
        }

        public boolean isGraphSet()
        {
            return _graph != null ;
        }

        @Override
        public String toString()
        {
            if ( isDefault ) return "default" ;
            return name ;
        }
    }

    public SPARQL_REST(boolean verbose)
    { super(PlainRequestFlag.DIFFERENT, verbose) ; }

    public SPARQL_REST()
    { this(false) ; }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Direct all verbs to our common framework.
        doCommon(request, response) ;
    }
    
    private void maybeSetLastModified(HttpServletResponse resp, long lastModified)
    {
        if (resp.containsHeader(HEADER_LASTMOD)) return ;
        if (lastModified >= 0) resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }
    
    @Override
    protected void perform(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response)
    {
        validate(request) ;
        HttpActionREST action = new HttpActionREST(id, desc, request, response, verbose_debug) ;
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
            doPatch(action) ;
        else if (method.equals(METHOD_OPTIONS))
            doOptions(action) ;
        else if (method.equals(METHOD_TRACE))
            //doTrace(action) ;
            errorMethodNotAllowed("TRACE") ;
        else if (method.equals(METHOD_PUT))
            doPut(action) ;   
        else if (method.equals(METHOD_DELETE))
            doDelete(action) ;
        else
            errorNotImplemented("Unknown method: "+method) ;
    }
        
    protected abstract void doGet(HttpActionREST action) ;
    protected abstract void doHead(HttpActionREST action) ;
    protected abstract void doPost(HttpActionREST action) ;
    protected abstract void doPatch(HttpActionREST action) ;
    protected abstract void doDelete(HttpActionREST action) ;
    protected abstract void doPut(HttpActionREST action) ;
    protected abstract void doOptions(HttpActionREST action) ;

    @Override
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        errorBadRequest("No query string") ;
        return false ;
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

    protected static MediaType contentNegotationRDF(HttpActionREST action)
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
    
    protected static MediaType contentNegotationQuads(HttpActionREST action)
    {
        return ConNeg.chooseContentType(action.request, DEF.quadsOffer, DEF.acceptTriG) ;
    }

    // Auxilliary functionality.
    
    protected static void deleteGraph(HttpActionREST action)
    {
        if ( action.getTarget().isDefault )
            action.getTarget().graph().getBulkUpdateHandler().removeAll() ;
        else
            action.getActiveDSG().removeGraph(action.getTarget().graphName) ;
    }

    protected static void clearGraph(Target target)
    {
        if ( ! target.isGraphSet() )
        {
            Graph g = target.graph() ;
            g.getBulkUpdateHandler().removeAll() ;
        }
    }

    protected static void addDataInto(Graph data, HttpActionREST action)
    {   
        Target dest = action.getTarget() ;
        Graph g = dest.graph() ;
        if ( g == null )
        {
            if ( dest.isDefault )
                errorOccurred("Dataset does not have a default graph") ;
            log.info(format("[%d] Creating in-memory graph for <%s>", action.id, dest.graphName)) ;
            // Not default graph.
            // Not an autocreate dataset - create something.
            g = GraphFactory.createDefaultGraph() ;
            dest.dsg.addGraph(dest.graphName, g) ;
        }
        g.getBulkUpdateHandler().add(data) ;
    }

    protected static DatasetGraph parseBody(HttpActionREST action)
    {
        // DRY - separate out conneg.
        // This is reader code as for client GET.
        // ---- ContentNeg / Webreader.
        String contentTypeHeader = action.request.getContentType() ;
        if ( contentTypeHeader == null )
            errorBadRequest("No content type: "+contentTypeHeader) ;
            // lang = Lang.guess(action.request.getRequestURI()) ;
        
        ContentType ct = ContentType.parse(contentTypeHeader) ;
        int len = action.request.getContentLength() ;
        Lang lang = FusekiLib.langFromContentType(ct.getContentType()) ;
        if ( lang == null )
        {
            errorBadRequest("Unknown content type for triples: "+contentTypeHeader) ;
            return null ;
        }

        if ( action.verbose )
        {
            if ( len >= 0 )
                log.info(format("[%d]   Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s", 
                                      action.id, len, ct.getContentType(), ct.getCharset(), lang.getName())) ;
            else
                log.info(format("[%d]   Body: Content-Type=%s, Charset=%s => %s", 
                                          action.id, ct.getContentType(), ct.getCharset(), lang.getName())) ;
        }
        
        try {
            InputStream input = action.request.getInputStream() ;
            String base = wholeRequestURL(action.request) ;
            
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
            
            return parse(action, lang, base, input) ;
        } catch (IOException ex) { errorOccurred(ex) ; return null ; }
    }

    private static DatasetGraph parse(HttpActionREST action, Lang lang, String base, InputStream input)
    {
        Graph graphTmp = GraphFactory.createGraphMem() ;
        Sink<Triple> sink = new SinkTriplesToGraph(graphTmp) ;
        LangRIOT parser = RiotReader.createParserTriples(input, lang, base, sink) ;
        parser.getProfile().setHandler(errorHandler) ;
        try {
            parser.parse() ;
        } 
        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
        finally { sink.close() ; }
        DatasetGraph dsgTmp = DatasetGraphFactory.create(graphTmp) ;
        
        return dsgTmp ;
    }
    
    protected static void validate(HttpServletRequest request)
    {
        String g = request.getParameter(HttpNames.paramGraph) ;
        String d = request.getParameter(HttpNames.paramGraphDefault) ;
        
        if ( g != null && d !=null )
            errorBadRequest("Both ?default and ?graph in the query string of the request") ;
        
        if ( g == null && d == null )
            errorBadRequest("Neither ?default nor ?graph in the query string of the request") ;
        
        @SuppressWarnings("unchecked")
        Enumeration<String> en = request.getParameterNames() ;
        for ( ; en.hasMoreElements() ; )
        {
            String h = en.nextElement() ;
            if ( ! HttpNames.paramGraph.equals(h) && ! HttpNames.paramGraphDefault.equals(h) )
                errorBadRequest("Unknown parameter '"+h+"'") ;
            // one of ?default and &graph
            if ( request.getParameterValues(h).length != 1 )
                errorBadRequest("Multiple parameters '"+h+"'") ;
        }
    }

    protected static Target targetGraph(HttpServletRequest request, DatasetGraph dsg)
    {
        boolean dftGraph = getOneOnly(request, HttpNames.paramGraphDefault) != null ;
        String uri = getOneOnly(request, HttpNames.paramGraph) ;
        
        if ( !dftGraph && uri == null )
            errorBadRequest("Neither default graph nor named graph specificed") ;
        
        if ( dftGraph )
            return Target.createDefault(dsg) ;
        
        // Named graph
        if ( uri.equals(HttpNames.valueDefault ) )
            // But "named" default
            return Target.createDefault(dsg) ;
        
        // Strictly, a bit naughthy on the URI resolution.  But more sensible. 
        // Base is dataset.
        String base = request.getRequestURL().toString() ; //wholeRequestURL(request) ;
        // Make sure it ends in "/", ie. dataset as container.
        if ( request.getQueryString() != null && ! base.endsWith("/") )
            base = base + "/" ;
        
        String absUri = IRIResolver.resolveString(uri, base) ;
        Node gn = Node.createURI(absUri) ;
        return Target.createNamed(dsg, absUri, gn) ;
    }
    
    protected static String getOneOnly(HttpServletRequest request, String name)
    {
        String[] values = request.getParameterValues(name) ;
        if ( values == null )
            return null ;
        if ( values.length == 0 )
            return null ;
        if ( values.length > 1 )
            errorBadRequest("Multiple occurrences of '"+name+"'") ;
        return values[0] ;
    }
}
