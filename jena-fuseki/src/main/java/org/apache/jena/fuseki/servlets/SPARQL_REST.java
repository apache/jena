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

import static org.apache.jena.fuseki.HttpNames.* ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Enumeration ;
import java.util.Locale ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.CounterName ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.riot.system.StreamRDF ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public abstract class SPARQL_REST extends SPARQL_ServletBase
{
    protected static Logger classLog = LoggerFactory.getLogger(SPARQL_REST.class) ;
    
    protected static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;

    protected final static Target determineTarget(HttpAction action) {
        // Delayed until inside a transaction.
        if ( action.getActiveDSG() == null )
            errorOccurred("Internal error : No action graph (not in a transaction?)") ;
        
        boolean dftGraph = getOneOnly(action.request, HttpNames.paramGraphDefault) != null ;
        String uri = getOneOnly(action.request, HttpNames.paramGraph) ;
        
        if ( !dftGraph && uri == null ) {
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

        static Target createNamed(DatasetGraph dsg, String name, Node graphName) {
            return new Target(false, dsg, name, graphName) ;
        }

        static Target createDefault(DatasetGraph dsg) {
            return new Target(true, dsg, null, null) ;
        }

        private Target(boolean isDefault, DatasetGraph dsg, String name, Node graphName) {
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
        public Graph graph() {
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

    public SPARQL_REST()
    { super() ; }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Direct all verbs to our common framework.
        doCommon(request, response) ;
    }
    
    private void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD)) return ;
        if (lastModified >= 0) resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }
    
    @Override
    protected void perform(HttpAction action) {
        dispatch(action) ;
    }

    private void dispatch(HttpAction action) {
        HttpServletRequest req = action.request ;
        HttpServletResponse resp = action.response ;
        String method = req.getMethod().toUpperCase(Locale.ROOT) ;

        if (method.equals(METHOD_GET))
            doGet$(action);
        else if (method.equals(METHOD_HEAD))
            doHead$(action);
        else if (method.equals(METHOD_POST))
            doPost$(action);
        else if (method.equals(METHOD_PATCH))
            doPatch$(action) ;
        else if (method.equals(METHOD_OPTIONS))
            doOptions$(action) ;
        else if (method.equals(METHOD_TRACE))
            //doTrace(action) ;
            errorMethodNotAllowed("TRACE") ;
        else if (method.equals(METHOD_PUT))
            doPut$(action) ;   
        else if (method.equals(METHOD_DELETE))
            doDelete$(action) ;
        else
            errorNotImplemented("Unknown method: "+method) ;
    }

    // Counter wrappers
    
    protected void doGet$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPget) ;
        try {
            doGet(action) ;
            incCounter(action.srvRef, CounterName.GSPgetGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPgetBad) ;
            throw ex ;
        }
    }

    protected void doHead$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPhead) ;
        try {
            doHead(action) ;
            incCounter(action.srvRef, CounterName.GSPheadGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPheadBad) ;
            throw ex ;
        }
    }

    protected void doPost$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPpost) ;
        try {
            doPost(action) ;
            incCounter(action.srvRef, CounterName.GSPpostGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPpostBad) ;
            throw ex ;
        }
    }

    protected void doPatch$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPpatch) ;
        try {
            doPatch(action) ;
            incCounter(action.srvRef, CounterName.GSPpatchGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPpatchBad) ;
            throw ex ;
        }
    }

    protected void doDelete$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPdelete) ;
        try {
            doDelete(action) ;
            incCounter(action.srvRef, CounterName.GSPdeleteGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPdeleteBad) ;
            throw ex ;
        }
    }

    protected void doPut$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPput) ;
        try {
            doPut(action) ;
            incCounter(action.srvRef, CounterName.GSPputGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPputBad) ;
            throw ex ;
        }
    }

    protected void doOptions$(HttpAction action) {
        incCounter(action.srvRef, CounterName.GSPoptions) ;
        try {
            doOptions(action) ;
            incCounter(action.srvRef, CounterName.GSPoptionsGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.srvRef, CounterName.GSPoptionsBad) ;
            throw ex ;
        }
    }
    
    protected abstract void doGet(HttpAction action) ;
    protected abstract void doHead(HttpAction action) ;
    protected abstract void doPost(HttpAction action) ;
    protected abstract void doPatch(HttpAction action) ;
    protected abstract void doDelete(HttpAction action) ;
    protected abstract void doPut(HttpAction action) ;
    protected abstract void doOptions(HttpAction action) ;
    
    // @@ Move to SPARQL_ServletBase
    // Check for all RiotReader
    public static void parse(HttpAction action, StreamRDF dest, InputStream input, Lang lang, String base) {
        try {
            ReaderRIOT r = RDFDataMgr.createReader(lang) ;
            if ( r == null )
                errorBadRequest("No parser for language '"+lang.getName()+"'") ;
            r.setErrorHandler(errorHandler); 
            r.read(input, base, null, dest, null) ; 
        } 
        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
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
