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

import org.apache.velocity.Template ;
import org.apache.velocity.VelocityContext ;
import org.apache.velocity.app.VelocityEngine ;
import org.apache.velocity.exception.MethodInvocationException ;
import org.apache.velocity.exception.ParseErrorException ;
import org.apache.velocity.exception.ResourceNotFoundException ;
import org.apache.velocity.runtime.RuntimeConstants ;
import org.apache.velocity.runtime.log.LogChute ;
import org.apache.velocity.runtime.log.NullLogChute ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class SimpleVelocity
{
    private static LogChute velocityLogChute = new NullLogChute() ;
    private static Logger velocityLog = LoggerFactory.getLogger("Velocity");

    /** Process a template */
    public static void process(String base, String path, Writer out, Map<String, Object> params)
    {
        process(base, path, out, createContext(params)) ;
    }
    
    /** Process a template */
    public static void process(String base, String path, Writer out, VelocityContext context)
    {
        VelocityEngine velocity = new VelocityEngine() ;
        // Turn off logging - catch exceptions and log ourselves
        velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, velocityLogChute) ;
        velocity.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8") ;
        velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, base) ;
        velocity.init() ;
        try {
            Template temp = velocity.getTemplate(path) ;
            temp.merge(context, out) ;
            out.flush();
        } 
        catch (ResourceNotFoundException ex) { velocityLog.error("Resource not found: "+ex.getMessage()) ; }
        catch (ParseErrorException ex)       { velocityLog.error("Parse error ("+path+") : "+ex.getMessage()) ; }
        catch (MethodInvocationException ex) { velocityLog.error("Method invocation exception ("+path+") : "+ex.getMessage()) ; }
        catch (IOException ex)               { velocityLog.warn("IOException", ex) ; }
    }
    
    public static VelocityContext createContext(Map<String, Object> params)
    {
        // Velocity requires a mutable map.
        // Scala leads to immutable maps ... be safe and copy.
        VelocityContext context = new VelocityContext() ;
        for ( Map.Entry<String, Object> e : params.entrySet() )
            context.put(e.getKey(), e.getValue()) ;
        return context ;
    }
    
}
