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

package org.apache.jena.riot;

import static java.lang.String.format ;

import java.io.IOException ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.StatusLine ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.MediaType ;
import org.openjena.riot.web.HttpNames ;

public class HttpOp2
{

    static private AtomicLong counter = new AtomicLong(0) ;

    public static TypedInputStreamHttp execHttpGet(String url, String acceptHeader)
    {
        try {
            long id = counter.incrementAndGet() ;
            String requestURI = determineRequestURI(url) ;
            String baseIRI = determineBaseIRI(requestURI) ;
    
            HttpGet httpget = new HttpGet(requestURI);
            if ( WebReader2.log.isDebugEnabled() )
                WebReader2.log.debug(format("[%d] %s %s",id ,httpget.getMethod(),httpget.getURI().toString())) ;
            // Accept
            if ( acceptHeader != null )
                httpget.addHeader(HttpNames.hAccept, acceptHeader) ;
            
            // Execute
            HttpClient httpclient = new SystemDefaultHttpClient();        // Pool?
            HttpResponse response = httpclient.execute(httpget) ;
            
            // Response
            StatusLine statusLine = response.getStatusLine() ;
            if ( statusLine.getStatusCode() == 404 )
            {
                WebReader2.log.debug(format("[%d] %s %s",id, statusLine.getStatusCode(), statusLine.getReasonPhrase())) ;
                return null ;
            }
            if ( statusLine.getStatusCode() >= 400 )
            {
                WebReader2.log.debug(format("[%d] %s %s",id, statusLine.getStatusCode(), statusLine.getReasonPhrase())) ;
                throw new HttpException(statusLine.getStatusCode()+" "+statusLine.getReasonPhrase()) ;
            }
    
            HttpEntity entity = response.getEntity() ;
            if ( entity == null )
            {
                // No content in the return.  Probably a mistake, but not guaranteed.
                if ( WebReader2.log.isDebugEnabled() )
                    WebReader2.log.debug(format("[%d] %d %s :: (empty)",id, statusLine.getStatusCode(), statusLine.getReasonPhrase())) ;
                return null ;
            }
                
            MediaType mt = MediaType.create(entity.getContentType().getValue()) ;
            if ( WebReader2.log.isDebugEnabled() )
                WebReader2.log.debug(format("[%d] %d %s :: %s",id, statusLine.getStatusCode(), statusLine.getReasonPhrase() , mt)) ;
                
            return new TypedInputStreamHttp(entity.getContent(), mt,
                                       httpclient.getConnectionManager()) ;
        }
        catch (IOException ex) { IO.exception(ex) ; return null ; }
    }

    private static String determineRequestURI(String url)
    {
        String requestURI = url ;
        if ( requestURI.contains("#") )
        {
            // No frag ids.
            int i = requestURI.indexOf('#') ;
            requestURI = requestURI.substring(0,i) ;
        }
        return requestURI ;
    }

    private static String determineBaseIRI(String requestURI)
    {
        // Technically wrong, but including the query string is "unhelpful"
        String baseIRI = requestURI ;
        if ( requestURI.contains("?") )
        {
            // No frag ids.
            int i = requestURI.indexOf('?') ;
            baseIRI = requestURI.substring(0,i) ;
        }
        return baseIRI ;
    }

}

