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

import java.util.Enumeration ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.sparql.core.DatasetGraph ;

public abstract class SPARQL_GSP extends ActionREST
{
    protected final static Target determineTarget(HttpAction action) {
        // Delayed until inside a transaction.
        if ( action.getActiveDSG() == null )
            ServletOps.errorOccurred("Internal error : No action graph (not in a transaction?)") ;
        
        boolean dftGraph = getOneOnly(action.request, HttpNames.paramGraphDefault) != null ;
        String uri = getOneOnly(action.request, HttpNames.paramGraph) ;
        
        if ( !dftGraph && uri == null ) {
            // No params - direct naming.
            if ( ! Fuseki.GSP_DIRECT_NAMING )
                ServletOps.errorBadRequest("Neither default graph nor named graph specified") ;
            
            // Direct naming.
            String directName = action.request.getRequestURL().toString() ;
            if ( action.request.getRequestURI().equals(action.getDatasetName()) )
                // No name (should have been a quads operations).
                ServletOps.errorBadRequest("Neither default graph nor named graph specified and no direct name") ;
            Node gn = NodeFactory.createURI(directName) ;
            return namedTarget(action, directName) ;
        }
        
        if ( dftGraph )
            return Target.createDefault(action.getActiveDSG()) ;
        
        // Named graph
        if ( uri.equals(HttpNames.valueDefault ) )
            // But "named" default
            return Target.createDefault(action.getActiveDSG()) ;
        
        // Strictly, a bit naughty on the URI resolution.  But more sensible. 
        // Base is dataset.
        
        // XXX Remove any service.
        
        String base = action.request.getRequestURL().toString() ; //wholeRequestURL(request) ;
        // Make sure it ends in "/", ie. dataset as container.
        if ( action.request.getQueryString() != null && ! base.endsWith("/") )
            base = base + "/" ;
        String absUri = null ;
        try { absUri = IRIResolver.resolveString(uri, base) ; }
        catch (RiotException ex) {
            // Bad IRI
            ServletOps.errorBadRequest("Bad IRI: "+ex.getMessage()) ;
        }
        return namedTarget(action, absUri) ;
    }
    
    private static Target namedTarget(HttpAction action, String graphName) {
        Node gn = NodeFactory.createURI(graphName) ;
        return Target.createNamed(action.getActiveDSG(), graphName, gn) ;
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

        /** Create a new Target which is like the original but aimed at a different DatasetGraph */
        static Target retarget(Target target, DatasetGraph dsg) {
            Target target2 = new Target(target, dsg) ;
            target2._graph = null ;
            return target2 ;
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

        private Target(Target other, DatasetGraph dsg) {
            this.isDefault  = other.isDefault ;
            this.dsg        = dsg ; //other.dsg ;
            this._graph     = other._graph ;
            this.name       = other.name ;
            this.graphName  = other.graphName ;
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
//      protected static ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(log) ;

        public String toString()
        {
            if ( isDefault ) return "default" ;
            return name ;
        }
    }

    public SPARQL_GSP()
    { super() ; }

    @Override
    protected void validate(HttpAction action)
    {
        HttpServletRequest request = action.request ;
        if ( request.getQueryString() == null ) {
            //errorBadRequest("No query string") ;
            return ;
        }
        
        String g = request.getParameter(HttpNames.paramGraph) ;
        String d = request.getParameter(HttpNames.paramGraphDefault) ;
        
        if ( g != null && d !=null )
            ServletOps.errorBadRequest("Both ?default and ?graph in the query string of the request") ;
        
        if ( g == null && d == null )
            ServletOps.errorBadRequest("Neither ?default nor ?graph in the query string of the request") ;
        
        int x1 = SPARQL_Protocol.countParamOccurences(request, HttpNames.paramGraph) ;
        int x2 = SPARQL_Protocol.countParamOccurences(request, HttpNames.paramGraphDefault) ;
        
        if ( x1 > 1 )
            ServletOps.errorBadRequest("Multiple ?default in the query string of the request") ;
        if ( x2 > 1 )
            ServletOps.errorBadRequest("Multiple ?graph in the query string of the request") ;
        
        Enumeration<String> en = request.getParameterNames() ;
        for ( ; en.hasMoreElements() ; )
        {
            String h = en.nextElement() ;
            if ( ! HttpNames.paramGraph.equals(h) && ! HttpNames.paramGraphDefault.equals(h) )
                ServletOps.errorBadRequest("Unknown parameter '"+h+"'") ;
            // one of ?default and &graph
            if ( request.getParameterValues(h).length != 1 )
                ServletOps.errorBadRequest("Multiple parameters '"+h+"'") ;
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
            ServletOps.errorBadRequest("Multiple occurrences of '"+name+"'") ;
        return values[0] ;
    }
}
