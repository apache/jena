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
import java.util.Map ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.velocity.Template ;
import org.apache.velocity.VelocityContext ;
import org.apache.velocity.app.VelocityEngine ;
import org.apache.velocity.runtime.RuntimeConstants ;
import org.apache.velocity.runtime.RuntimeServices ;
import org.apache.velocity.runtime.log.LogChute ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


/** Simple servlet that uses <a href="http://velocity.apache.org/">Velocity</a>
 *  to format pages.  It isolates the use of velocity by taking a configuration map. 
 *  Use with a servlet mapping of "*.vm" or some such extension.
 */
public class SimpleVelocityServlet extends HttpServlet
{
    private static Logger log = LoggerFactory.getLogger(SimpleVelocityServlet.class) ;
    private String docbase ;
    private VelocityEngine velocity ;
    
    private Object functions = null ;
    private String functionsName = null ;
    private final Map<String, Object> datamodel ;
    
    public SimpleVelocityServlet(String base, Map<String, Object> datamodel)
    {
        setDocBase(base) ;
        this.datamodel = datamodel ;
        velocity = new VelocityEngine();
        // Just plain set the logger.  No initialize phaff around reflection calls and newInstance() 
        velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new SimpleSLF4JLogChute(log)) ;
//        velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
//          SimpleSLF4JLogChute.class.getName() );
//        velocity.setProperty("runtime.log.logsystem.log4j.logger",
//            "FOO");
        velocity.init();
    }
    
    public Object getFunctions()
    {
        return functions ;
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
            VelocityContext context = new VelocityContext(datamodel) ;
            // TODO - better?
            String path = path(req) ;
            path = docbase+path ;
            Template temp = velocity.getTemplate(path) ;
            context.put("request", req) ;
            resp.setCharacterEncoding("UTF-8") ;
            Writer out = resp.getWriter() ;
            temp.merge(context, out);
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
    
    /** Velocity logger to SLF4J */ 
    static class SimpleSLF4JLogChute implements LogChute
    {

        private Logger logger ;

        SimpleSLF4JLogChute(Logger log )
        {
            this.logger = log ; 
        }
        
        @Override
        public void init(RuntimeServices rs) throws Exception
        { }

        @Override
        public void log(int level, String message)
        {
            log(level, message, null) ;
//            switch(level)
//            {
//                case LogChute.TRACE_ID:
//                case LogChute.DEBUG_ID:
//                case LogChute.INFO_ID:
//                case LogChute.WARN_ID:
//                case LogChute.ERROR_ID:
//            }
        }

        @Override
        public void log(int level, String message, Throwable t)
        {
            switch (level)
            {
                case LogChute.TRACE_ID : logger.trace(message, t) ; return ;
                case LogChute.DEBUG_ID : logger.debug(message, t) ; return ;
                case LogChute.INFO_ID :  logger.info(message, t) ;  return ;
                case LogChute.WARN_ID :  logger.warn(message, t) ;  return ;
                case LogChute.ERROR_ID : logger.error(message, t) ; return ;
            }
        }

        @Override
        public boolean isLevelEnabled(int level)
        {
            switch(level)
            {
                case LogChute.TRACE_ID:   return logger.isTraceEnabled() ;
                case LogChute.DEBUG_ID:   return logger.isDebugEnabled() ;
                case LogChute.INFO_ID:    return logger.isInfoEnabled() ;
                case LogChute.WARN_ID:    return logger.isWarnEnabled() ;
                case LogChute.ERROR_ID:   return logger.isErrorEnabled() ;
            }
            return true ;
        }

    }
}

