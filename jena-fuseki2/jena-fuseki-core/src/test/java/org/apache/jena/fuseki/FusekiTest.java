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

package org.apache.jena.fuseki;

import java.io.IOException ;
import java.util.Objects ;

import org.apache.http.HttpResponse ;
import org.apache.http.StatusLine ;
import org.apache.http.client.methods.HttpOptions ;
import org.apache.http.client.methods.HttpUriRequest ;
import org.apache.http.impl.client.CloseableHttpClient ;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext ;
import org.apache.http.util.EntityUtils ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.riot.web.HttpResponseLib ;
import org.apache.jena.web.HttpSC ;
import org.junit.Assert ;

public class FusekiTest {

    /** Check whether str is a comma separated list of expected (unordered) */
    public static void assertStringList(String str, String... expected) {
        str = str.replace(" ", "") ;
        String[] x = str.split(",") ;
        for ( String ex : expected ) {
            Assert.assertTrue("Got: "+str+" - Does not contain "+ex, containsStr(ex, x)) ;
        }
        for ( String s : x ) {
            Assert.assertTrue("Got: "+str+" - Not expected "+s, containsStr(s, expected)) ;
        }
    }

    /** Is v in the list of strings? */
    public static boolean containsStr(String v, String[] strings) {
        for ( String s: strings ) {
            if ( Objects.equals(v, s)) 
                return true ;
        }
        return false ;
    }

    /** Do an HTTP Options. */
    public static String execOptions(String url) {
        // Prepare and execute
        try ( CloseableHttpClient httpClient = HttpClients.createDefault() ) {
            HttpUriRequest request = new HttpOptions(url) ;
            HttpResponse response = httpClient.execute(request, (HttpContext)null);

            // Response
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (HttpSC.isClientError(statusCode) || HttpSC.isServerError(statusCode)) {
                // Error responses can have bodies so it is important to clear up.
                String contentPayload = "" ;
                if ( response.getEntity() != null )
                    contentPayload = EntityUtils.toString(response.getEntity()) ;
                throw new HttpException(statusCode, statusLine.getReasonPhrase(), contentPayload);
            }
            HttpResponseLib.nullResponse.handle(url, response);
            return response.getFirstHeader(HttpNames.hAllow).getValue() ;
        } catch (IOException ex) { 
            throw new HttpException(ex);
        }
    }
    
    public static void exec404(Runnable action) {
        execWithHttpException(HttpSC.NOT_FOUND_404, action) ;
    }
    
    public static void execWithHttpException(int statusCode, Runnable action) { 
        try {
            action.run();
            Assert.fail("Expected HttpException") ;
        } catch (HttpException ex) {
            // -1 : any status code in HttpException 
            if ( statusCode > 0 )
                Assert.assertEquals(statusCode, ex.getResponseCode()) ;
                
        }
    }
}

