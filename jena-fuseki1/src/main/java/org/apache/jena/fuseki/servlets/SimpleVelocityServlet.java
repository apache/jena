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

import java.io.IOException ;
import java.io.Writer ;
import java.util.Map ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.velocity.VelocityContext ;
import org.apache.velocity.app.VelocityEngine ;
import org.apache.velocity.runtime.RuntimeConstants ;
import org.apache.velocity.runtime.RuntimeServices ;
import org.apache.velocity.runtime.log.LogChute ;
import org.apache.velocity.runtime.log.NullLogChute ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


/** Simple servlet that uses <a href="http://velocity.apache.org/">Velocity</a>
 *  to format pages.  It isolates the use of velocity by taking a configuration map. 
 *  Use with a servlet mapping of "*.vm" or some such extension.
 */
public class SimpleVelocityServlet extends HttpServlet
{
    //private static Logger log = LoggerFactory.getLogger(SimpleVelocityServlet.class) ;
    /* Velocity logging
     * Instead of internal velocity logging, we catch the exceptions, 
     * log the message ourselves. This gives a celaner log file without
     * loosing information that the application could use.  
     */
    
    private static Logger vlog = LoggerFactory.getLogger("Velocity") ;
    private static LogChute velocityLog = new NullLogChute() ;
    //private static LogChute velocityLog = new SimpleSLF4JLogChute(vlog) ;
    
    private String docbase ;
    private VelocityEngine velocity ;
    private String functionsName = null ;
    private final Map<String, Object> datamodel ;
    
    public SimpleVelocityServlet(String base, Map<String, Object> datamodel)
    {
        this.docbase = base ;
        this.datamodel = datamodel ;
        velocity = new VelocityEngine();
        // Turn off logging - catch exceptions and log ourselves
        velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, velocityLog) ;
        velocity.setProperty( RuntimeConstants.INPUT_ENCODING, "UTF-8" ) ;
        velocity.setProperty( RuntimeConstants.FILE_RESOURCE_LOADER_PATH, base) ;
        velocity.init();
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
        try
        {
            resp.setContentType("text/html") ;
            resp.setCharacterEncoding("UTF-8") ;
            Writer out = resp.getWriter() ;
            String path = path(req) ;
            VelocityContext vc = SimpleVelocity.createContext(datamodel) ;
            vc.put("request", req) ;
            SimpleVelocity.process(docbase, path, out, vc) ;
        } catch (IOException ex)
        {
            vlog.warn("IOException", ex) ;
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
        return "Lightweight Velocity Servlet";
    }
    
    /** Velocity logger to SLF4J */ 
    static class SimpleSLF4JLogChute implements LogChute
    {
        // Uusally for debugging only.
        private Logger logger ;

        SimpleSLF4JLogChute( Logger log )
        {
            this.logger = log ; 
        }
        
        @Override
        public void init(RuntimeServices rs) throws Exception
        { }

        @Override
        public void log(int level, String message)
        {
            if ( logger == null ) return ;
            switch(level)
            {
                case LogChute.TRACE_ID : logger.trace(message) ; return ;
                case LogChute.DEBUG_ID : logger.debug(message) ; return ;
                case LogChute.INFO_ID :  logger.info(message) ;  return ;
                case LogChute.WARN_ID :  logger.warn(message) ;  return ;
                case LogChute.ERROR_ID : logger.error(message) ; return ;
            }
        }

        @Override
        public void log(int level, String message, Throwable t)
        {
            if ( logger == null ) return ;
            // Forget the stack trace - velcoity internal - long - unhelpful to application. 
            t = null ;
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

