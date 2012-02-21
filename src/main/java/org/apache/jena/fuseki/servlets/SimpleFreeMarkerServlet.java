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

package org.apache.jena.fuseki.servlets;

import java.io.Writer ;
import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import freemarker.template.Configuration ;
import freemarker.template.Template ;

/** Simple servlet that uses <a href="http://freemarker.sf.net/">FreeMarker</a>
 *  to format pages.  
 *  Use with a servlet mapping of "*.ftl" or socme such extension.
 *  Has a single object for methods and statics - in the template, it's calls 
 *  
 */
public class SimpleFreeMarkerServlet extends HttpServlet
{
    private String docbase ;
    private Configuration cfg = new Configuration();
    
    private Object functions = null ;
    private String functionsName = null ;
    
    private Map<String, Object> datamodel = new HashMap<String, Object>();


    public SimpleFreeMarkerServlet(String base, Map<String, Object> datamodel)
    {
        setDocBase(base) ;
        setDataModel(datamodel) ;
    }
    
    public Object getFunctions()
    {
        return functions ;
    }

    private void setDataModel(Map<String, Object> datamodel)
    {
        this.datamodel = datamodel ;
    }

    public Configuration getConfiguration()
    {
        return cfg ;
    }

    public String getDocBase()
    {
        return docbase ;
    }

    public void setDocBase(String docbase)
    {
        this.docbase = docbase ;
    }
    
    // See also 
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    {
        process(req, resp) ;
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    {
        process(req, resp) ;
    }

    private void process(HttpServletRequest req, HttpServletResponse resp)
    { 
        try {
            // TODO - better?
            String path = path(req) ;
            path = docbase+path ;
            Template temp = cfg.getTemplate(path);
            resp.setCharacterEncoding("UTF-8") ;
            Writer out = resp.getWriter() ;
            if ( datamodel != null )
                datamodel.put("request", req) ;
            temp.process(datamodel, out);
            out.flush();
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
        }
    }
    
    private String path(HttpServletRequest request)
    {     
        String path = request.getPathInfo();
        if (path != null) return path;
        path = request.getServletPath();
        if (path != null) return path;
        return null ;
    }

    @Override
    public String getServletInfo()
    {
        return "Lightweight FreeMarker Servlet";
    }
}

