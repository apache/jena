/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import java.io.IOException ;
import java.io.InputStream ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.client.methods.HttpUriRequest ;
import org.apache.http.impl.client.DefaultHttpClient ;
import org.openjena.atlas.io.IO ;

public class WebTest
{
    static void exec_get(String url, int expectedResponseCode)
    {
        HttpUriRequest httpGet = new HttpGet(url) ;
        HttpClient httpclient = new DefaultHttpClient() ;
        try {
            HttpResponse response = httpclient.execute(httpGet) ;
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
            org.junit.Assert.assertEquals("("+responseCode+", "+responseMessage+")", expectedResponseCode, responseCode) ;
        } catch (IOException ex)
        { IO.exception(ex) ; }
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */