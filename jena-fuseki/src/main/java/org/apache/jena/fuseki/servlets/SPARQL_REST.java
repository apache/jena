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
import java.util.Locale ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileUtils ;

public abstract class SPARQL_REST extends SPARQL_ServletBase
{
    protected static Logger classLog = LoggerFactory.getLogger(SPARQL_REST.class) ;
    
    protected static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;

    protected final static Target determineTarget(HttpAction action) 
    {
        // Delayed until inside a transaction.
        if ( action.getActiveDSG() == null )
                errorOccurred("Internal error : No action graph (not in a transaction?)") ;
        
        boolean dftGraph = getOneOnly(action.request, HttpNames.paramGraphDefault) != null ;
        String uri = getOneOnly(action.request, HttpNames.paramGraph) ;
        
        if ( !dftGraph && uri == null )
        {
            // Direct naming or error.
            uri = action.request.getRequestURL().toString() ;
            if ( action.request.getRequestURI().equals(action.getDatasetRef().name) )
                // No name 
                errorBadRequest("Neither default graph nor named graph specified; no direct name") ;
        }
        
        if ( dftGraph )
            return Target.createDefault(action.getActiveDSG()) ;
        
        // Named graph
        if ( uri.equals(HttpNames.valueDefault ) )
            // But "named" default
            return Target.createDefault(action.getActiveDSG()) ;
        
        // Strictly, a bit naughty on the URI resolution.  But more sensible. 
        // Base is dataset.
        String base = action.request.getRequestURL().toString() ; //wholeRequestURL(request) ;
        // Make sure it ends in "/", ie. dataset as container.
        if ( action.request.getQueryString() != null && ! base.endsWith("/") )
            base = base + "/" ;
        
        String absUri = IRIResolver.resolveString(uri, base) ;
        Node gn = NodeFactory.createURI(absUri) ;
        return Target.createNamed(action.getActiveDSG(), absUri, gn) ;
    }
    

    // struct for target
    protected static final class Target
    {
        final boolean isDefault ;
        final DatasetGraph dsg ;
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
    { super(verbose) ; }

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
    protected void perform(HttpAction action)
    {
        dispatch(action) ;
    }

    private void dispatch(HttpAction action)
    {
        HttpServletRequest req = action.request ;
        HttpServletResponse resp = action.response ;
        String method = req.getMethod().toUpperCase(Locale.ENGLISH) ;

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
        
    protected abstract void doGet(HttpAction action) ;
    protected abstract void doHead(HttpAction action) ;
    protected abstract void doPost(HttpAction action) ;
    protected abstract void doPatch(HttpAction action) ;
    protected abstract void doDelete(HttpAction action) ;
    protected abstract void doPut(HttpAction action) ;
    protected abstract void doOptions(HttpAction action) ;

    protected static void deleteGraph(HttpAction action)
    {
        Target target = determineTarget(action) ;
        if ( target.isDefault )
            target.graph().clear() ;
        else
            action.getActiveDSG().removeGraph(target.graphName) ;
    }

    protected static void clearGraph(Target target)
    {
        if ( ! target.isGraphSet() )
        {
            Graph g = target.graph() ;
            g.clear() ;
        }
    }

    protected static void addDataInto(Graph data, HttpAction action)
    {   
        try
        {
            Target dest = determineTarget(action) ;
            FusekiLib.addDataInto(data, dest.dsg, dest.graphName) ;
            
//            Graph g = dest.graph() ;
//            
//            if (g == null)
//            {
//                if (dest.isDefault) errorOccurred("Dataset does not have a default graph") ;
//                log.info(format("[%d] Creating in-memory graph for <%s>", action.id, dest.graphName)) ;
//                // Not default graph.
//                // Not an autocreate dataset - create something.
//                g = GraphFactory.createDefaultGraph() ;
//                dest.dsg.addGraph(dest.graphName, g) ;
//            }
//            GraphUtil.addInto(g, data) ;
        } catch (RuntimeException ex)
        {
            // If anything went wrong, try to backout.
            action.abort() ;
            errorOccurred(ex.getMessage()) ;
            return ;
        }
    }
    
    protected static DatasetGraph parseBody(HttpAction action)
    {
        String contentTypeHeader = action.request.getContentType() ;
        
        if ( contentTypeHeader == null )
            errorBadRequest("No content type: "+contentTypeHeader) ;
            // lang = Lang.guess(action.request.getRequestURI()) ;
        
        ContentType ct = ContentType.parse(contentTypeHeader) ;
        
        String base = wholeRequestURL(action.request) ;
        
        // Use WebContent names
        if ( WebContent.contentTypeMultiFormData.equalsIgnoreCase(ct.getContentType()) )
        {
//            //log.warn("multipart/form-data not supported (yet)") ;
//            error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "multipart/form-data not supported") ;
            Graph graphTmp = SPARQL_Upload.upload(action, base) ;
            return DatasetGraphFactory.create(graphTmp) ;
        }
        
        if (WebContent.contentTypeMultiMixed.equals(ct.getContentType()) )
        {
            //log.warn("multipart/mixed not supported") ;
            error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "multipart/mixed not supported") ;
            return null ;
        }

        int len = action.request.getContentLength() ;
        Lang lang = WebContent.contentTypeToLang(ct.getContentType()) ;
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
                    // Without content length, reading to end of file is occassionaly fraught.
                    // Reason unknown - maybe some client mishandling of the stream. 
                    String x = FileUtils.readWholeFileAsUTF8(input) ;
                    input = new ByteArrayInputStream(x.getBytes("UTF-8")) ; 
                }
            }
            
            return parse(action, lang, base, input) ;
        } catch (IOException ex) { errorOccurred(ex) ; return null ; }
    }

    private static DatasetGraph parse(HttpAction action, Lang lang, String base, InputStream input)
    {
        Graph graphTmp = GraphFactory.createGraphMem() ;
        
        StreamRDF dest = StreamRDFLib.graph(graphTmp) ;
        LangRIOT parser = RiotReader.createParser(input, lang, base, dest) ;
        parser.getProfile().setHandler(errorHandler) ;
        try { parser.parse() ; } 
        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
        DatasetGraph dsgTmp = DatasetGraphFactory.create(graphTmp) ;
        return dsgTmp ;
    }
    
    @Override
    protected void validate(HttpAction action)
    {
        HttpServletRequest request = action.request ;
        // Direct naming.
        if ( request.getQueryString() == null )
            //errorBadRequest("No query string") ;
            return ;
        
        String g = request.getParameter(HttpNames.paramGraph) ;
        String d = request.getParameter(HttpNames.paramGraphDefault) ;
        
        if ( g != null && d !=null )
            errorBadRequest("Both ?default and ?graph in the query string of the request") ;
        
        if ( g == null && d == null )
            errorBadRequest("Neither ?default nor ?graph in the query string of the request") ;
        
        int x1 = SPARQL_Protocol.countParamOccurences(request, HttpNames.paramGraph) ;
        int x2 = SPARQL_Protocol.countParamOccurences(request, HttpNames.paramGraphDefault) ;
        
        if ( x1 > 1 )
            errorBadRequest("Multiple ?default in the query string of the request") ;
        if ( x2 > 1 )
            errorBadRequest("Multiple ?graph in the query string of the request") ;
        
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
