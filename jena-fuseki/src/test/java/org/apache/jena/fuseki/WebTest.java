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

package org.apache.jena.fuseki;

import java.io.IOException ;
import java.io.InputStream ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.client.methods.HttpUriRequest ;
import org.apache.http.impl.client.DefaultHttpClient ;
import org.apache.jena.atlas.io.IO ;

public class WebTest extends BaseTest
{
    
    public static void exec_get(String url, int ... expectedResponseCodes)
    {
        HttpUriRequest httpRequest = new HttpGet(url) ;
        exec(httpRequest, expectedResponseCodes) ;
    }
    
    public static void exec(HttpUriRequest httpRequest, int...expectedResponseCodes)
    {
        HttpClient httpclient = new DefaultHttpClient() ;
        try {
            HttpResponse response = httpclient.execute(httpRequest) ;
            int responseCode = response.getStatusLine().getStatusCode() ;
            String responseMessage = response.getStatusLine().getReasonPhrase() ;
            
            // Drain the body 
            HttpEntity entity = response.getEntity() ;
            if ( entity != null )
            {
                InputStream instream = entity.getContent() ;
                byte[] bytes = IO.readWholeFile(instream) ;
                instream.close() ;
            }

            boolean found = false ;
            for ( int expected : expectedResponseCodes)
                if ( expected == responseCode )
                    return ;
            fail("Reponse: "+responseCode+" : Expected : "+expectedResponseCodes) ;
        } catch (IOException ex)
        { IO.exception(ex) ; }
    }
}
