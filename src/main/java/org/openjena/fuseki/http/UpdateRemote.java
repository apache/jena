/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.http;

import java.io.ByteArrayOutputStream ;
import java.io.IOException ;

import org.apache.http.HttpResponse ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.entity.AbstractHttpEntity ;
import org.apache.http.entity.ByteArrayEntity ;
import org.apache.http.impl.client.DefaultHttpClient ;
import org.apache.http.protocol.HTTP ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.sparql.modify.request.Target ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateException ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateRemote
{
    /** Reset the remote repository by execution a "DROP ALL" command on it */
    public static void executeClear(String serviceURL)
    {
        Update clear = new UpdateDrop(Target.ALL) ;
        UpdateRemote.execute(clear, serviceURL) ;
    }
    
    public static void execute(Update request, String serviceURL)
    {
        execute(new UpdateRequest(request) , serviceURL) ;
    }
    
    public static void execute(UpdateRequest request, String serviceURL)
    {
        HttpPost httpPost = new HttpPost(serviceURL) ;
        ByteArrayOutputStream b_out = new ByteArrayOutputStream() ;
        IndentedWriter out = new IndentedWriter(b_out) ; 
        UpdateWriter.output(request, out) ;
        out.flush() ;
        byte[] bytes = b_out.toByteArray() ;
        AbstractHttpEntity reqEntity = new ByteArrayEntity(bytes) ;
        reqEntity.setContentType(WebContent.contentTypeSPARQLUpdate) ;
        reqEntity.setContentEncoding(HTTP.UTF_8) ;
        httpPost.setEntity(reqEntity) ;
        HttpClient httpclient = new DefaultHttpClient() ;

        try
        {
            HttpResponse response = httpclient.execute(httpPost) ;
            int responseCode = response.getStatusLine().getStatusCode() ;
            String responseMessage = response.getStatusLine().getReasonPhrase() ;
            
            if ( responseCode == HttpSC.NO_CONTENT_204 )
                return ;
            if ( responseCode == HttpSC.OK_200 )
                // But what was the content?
                // TODO read body 
                return ; 
            throw new UpdateException(responseCode+" "+responseMessage) ;
        } catch (IOException ex)
        {
            throw new UpdateException(ex) ;
        }
            
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