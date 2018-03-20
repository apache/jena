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

import static java.lang.String.format;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.servlets.ResponseResultSet.OutputContent;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Responsible for handling JSON response output.
 */
public class ResponseJson 
{

    // Loggers
    private static Logger xlog = LoggerFactory.getLogger(ResponseJson.class) ;

    /**
     * Outputs a JSON query result
     *
     * @param action HTTP action
     * @param jsonItem a ResultSetJsonStream instance
     */
    public static void doResponseJson(HttpAction action, Iterator<JsonObject> jsonItem)
    {
        if ( jsonItem == null)
        {
            xlog.warn("doResponseJson: Result set is null") ; 
            throw new FusekiException("Result set is null") ;
        }

        jsonOutput(action, jsonItem) ;
    }

    private static void jsonOutput(HttpAction action, final Iterator<JsonObject> jsonItems)
    {
        OutputContent proc = new OutputContent()
        {
            @Override
            public void output(ServletOutputStream out)
            {
                if (jsonItems != null)
                    ResultSetFormatter.output(out, jsonItems) ;
            }
        } ;

        try
        {
            String callback = ResponseOps.paramCallback(action.request) ;
            ServletOutputStream out = action.response.getOutputStream() ;

            if ( callback != null )
            {
                callback = StringUtils.replaceChars(callback, "\r", "") ;
                callback = StringUtils.replaceChars(callback, "\n", "") ;
                out.print(callback) ;
                out.println("(") ;
            }

            output(action, "application/json", WebContent.charsetUTF8, proc) ;

            if ( callback != null )
                out.println(")") ;
        } catch (IOException ex) { ServletOps.errorOccurred(ex) ; }
    }

    private static void output(HttpAction action, String contentType, String charset, OutputContent proc) 
    {
        try
        {
            setHttpResponse(action.request, action.response, contentType, charset) ; 
            action.response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = action.response.getOutputStream() ;
            try
            {
                proc.output(out) ;
                out.flush() ;
            }
            catch (QueryCancelledException ex)
            {
                // Bother.  Status code 200 already sent.
                xlog.info(format("[%d] Query Cancelled - results truncated (but 200 already sent)", action.id)) ;
                out.println() ;
                out.println("##  Query cancelled due to timeout during execution   ##") ;
                out.println("##  ****          Incomplete results           ****   ##") ;
                out.flush() ;
                // No point raising an exception - 200 was sent already.  
                //errorOccurred(ex) ;
            }
        // Includes client gone.
        } catch (IOException ex) 
        { ServletOps.errorOccurred(ex) ; }
        // Do not call httpResponse.flushBuffer(); here - Jetty closes the stream if it is a gzip stream
        // then the JSON callback closing details can't be added. 
    }

    public static void setHttpResponse(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            String contentType, String charset) 
    {
        // ---- Set up HTTP Response
        // Stop caching (not that ?queryString URLs are cached anyway)
        if ( true )
        {
            httpResponse.setHeader("Cache-Control", "no-cache") ;
            httpResponse.setHeader("Pragma", "no-cache") ;
        }
        // See: http://www.w3.org/International/O-HTTP-charset.html
        if ( contentType != null )
        {
            if ( charset != null )
                contentType = contentType+"; charset="+charset ;
            xlog.trace("Content-Type for response: "+contentType) ;
            httpResponse.setContentType(contentType) ;
        }
    }

}
