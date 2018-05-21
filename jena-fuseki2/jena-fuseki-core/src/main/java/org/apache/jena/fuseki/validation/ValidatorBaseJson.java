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

package org.apache.jena.fuseki.validation;

import static java.lang.String.format ;

import java.io.OutputStream ;
import java.util.Enumeration ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.fuseki.servlets.ActionErrorException ;
import org.apache.jena.fuseki.servlets.ActionLib ;
import org.apache.jena.fuseki.servlets.ServletBase ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.fuseki.validation.json.ValidationAction;

import static org.apache.jena.riot.WebContent.* ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;

/** Validation base for JSON output */ 
public abstract class ValidatorBaseJson extends ServletBase
{
    private static Logger vLog = Fuseki.validationLog ;

    public static final String respService      = "X-Service" ;
    
    @Override
    public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    { execute(httpRequest, httpResponse) ; }

    @Override
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    { execute(httpRequest, httpResponse) ; }
    
    protected void execute(HttpServletRequest request, HttpServletResponse response) {
        long id = allocRequestId(request, response) ;
        ValidationAction action = new ValidationAction(id, vLog, request, response, false) ;
        printRequest(action) ;
        action.setStartTime() ;
        
        response = action.response ;
        initResponse(request, response) ;
        
        try {
            JsonObject obj = execute(action) ;
            action.statusCode = HttpSC.OK_200 ;
            action.message = "OK" ;
            response.setCharacterEncoding(charsetUTF8);
            response.setContentType(contentTypeJSON);
            //response.setContentType(WebContent.contentTypeTextPlain);
            action.response.setStatus(HttpSC.OK_200) ;
            OutputStream out = response.getOutputStream() ; 
            JSON.write(out, obj);
        } catch (ActionErrorException ex) {
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
            if ( ex.getMessage() != null )
                ServletOps.responseSendError(response, ex.getRC(), ex.getMessage()) ;
            else
                ServletOps.responseSendError(response, ex.getRC()) ;
        } catch (Throwable th) {
            ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, "Internal Error") ;
        }
        action.setFinishTime() ;
        printResponse(action) ;
    }
    
    static void initResponse(HttpServletRequest request, HttpServletResponse response)
    {
        setCommonHeaders(response) ;
        String method = request.getMethod() ;
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) || HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
            setVaryHeader(response) ;
    }
    
    static void printRequest(ValidationAction action)
    {
        String url = ActionLib.wholeRequestURL(action.request) ;
        String method = action.request.getMethod() ;

        action.log.info(format("[%d] %s %s", action.id, method, url)) ;
        if ( action.verbose ) {
            Enumeration<String> en = action.request.getHeaderNames() ;
            for (; en.hasMoreElements();) {
                String h = en.nextElement() ;
                Enumeration<String> vals = action.request.getHeaders(h) ;
                if (!vals.hasMoreElements())
                    action.log.info(format("[%d]   %s", action.id, h)) ;
                else {
                    for (; vals.hasMoreElements();)
                        action.log.info(format("[%d]   %-20s %s", action.id, h, vals.nextElement())) ;
                }
            }
        }
    }
    
    static void printResponse(ValidationAction action)
    {
        long time = action.getTime() ;
        
        if ( action.verbose )
        {
//            if ( action.contentType != null )
//                log.info(format("[%d]   %-20s %s", action.id, HttpNames.hContentType, action.contentType)) ;
//            if ( action.contentLength != -1 )
//                log.info(format("[%d]   %-20s %d", action.id, HttpNames.hContentLengh, action.contentLength)) ;
//            for ( Map.Entry<String, String> e: action.headers.entrySet() )
//                log.info(format("[%d]   %-20s %s", action.id, e.getKey(), e.getValue())) ;
        }

        String timeStr = fmtMillis(time) ;

        if ( action.message == null )
            action.log.info(String.format("[%d] %d %s (%s) ", action.id, action.statusCode, HttpSC.getMessage(action.statusCode), timeStr)) ;
        else
            action.log.info(String.format("[%d] %d %s (%s) ", action.id, action.statusCode, action.message, timeStr)) ;
    }
    
    static String fmtMillis(long time)
    {
        // Millis only? seconds only?
        if ( time < 1000 )
            return String.format("%,d ms", time) ;
        return String.format("%,.3f s", time/1000.0) ;
    }
    
    protected abstract JsonObject execute(ValidationAction action) ;
    
    protected abstract String validatorName() ;

    static void setHeaders(HttpServletResponse httpResponse, String vName)
    {
        httpResponse.setCharacterEncoding(charsetUTF8) ;
        httpResponse.setContentType(contentTypeJSON) ;
        httpResponse.setHeader(respService, "Jena Fuseki Validator / "+vName+": http://jena.apache.org/") ;
    }

    protected static String getArg(ValidationAction action, String paramName) {
        String arg = getArgOrNull(action, paramName) ;
        if ( arg == null ) {
            ServletOps.error(HttpSC.BAD_REQUEST_400, "No parameter given: " + paramName) ;
            return null ;
        }
        return arg ;
    }

    protected static String getArgOrNull(ValidationAction action, String paramName) {
        String[] args = getArgs(action, paramName) ;

        if ( args == null || args.length == 0 )
            return null ;

        if ( args.length > 1 ) {
            ServletOps.error(HttpSC.BAD_REQUEST_400, "Too many ("+args.length+") parameter values: "+paramName) ;
            return null ;
        }
        
        return args[0] ;
    }
    
    protected static String[] getArgs(ValidationAction action, String paramName) {
        String[] args = action.request.getParameterValues(paramName) ;
        if ( args == null || args.length == 0 )
            return null ;
        return args ;
    }
}    

