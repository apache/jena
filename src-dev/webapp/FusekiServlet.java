/**
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

package webapp;

import javax.servlet.ServletContext ;
import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.servlets.ActionLib ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.SPARQL_UberServlet ;

/** Various fixes to make the uber servlet work in named webapp contexts */ 
public class FusekiServlet extends SPARQL_UberServlet.AccessByConfig {
    
    @Override 
    public void init() throws ServletException {
        Log.info(this, getServletContext().getServletContextName()) ;
        super.init() ;
    }
    
    
    @Override public void doGet(HttpServletRequest request, HttpServletResponse response) {
        /*
         * getContextPath--
         *   Either "/path" or "" for root app 
         *   Canonical form is via ServletContext
         * getServletPath--
         *   Either "/path" or "" for matching "/*"
         */
        Log.info(this, "URI                     = '"+request.getRequestURI()) ;
        Log.info(this, "Context path            = '"+request.getContextPath()+"'") ;
        Log.info(this, "Servlet path            = '"+request.getServletPath()+"'") ;
        ServletContext cxt = this.getServletContext() ;
        Log.info(this, "ServletContext path     = '"+cxt.getContextPath()+"'") ;
        //Log.info(this, "ServletContextName path = '"+cxt.getServletContextName()+"'") ;
        super.doGet(request, response);
    }
    
    @Override
    protected String mapRequestToDataset(HttpAction action) 
    {
        String uri = action.request.getRequestURI() ;
        String x = removeContextPath(uri) ;
        return ActionLib.mapRequestToDatasetLongest$(action.request.getRequestURI()) ;
    }
    
    /** Find the graph (direct naming) or service name */
    @Override
    protected String findTrailing(String uri, String dsname) 
    {
        String x = removeContextPath(uri) ;
        return super.findTrailing(x, dsname) ;
    }

    /* 
     * The context path can be:
     * "" for the root context
     * "/webapp" for named contexts
     * so:
     * "/dataset/server" becomes "/dataset/server"
     * "/webapp/dataset/server" becomes "/dataset/server"
     */
    private String removeContextPath(String uri) {
        String contextPath = getServletContext().getContextPath() ;
        if ( contextPath == null )
            return uri ;
        if ( contextPath.isEmpty())
            return uri ;
        String x = uri ;
        if ( uri.startsWith(contextPath) )
            x = uri.substring(contextPath.length()) ;
        //log.info("removeContext: uri = "+uri+" contextPath="+contextPath+ "--> x="+x) ;
        return x ;
    }

}
