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

package org.apache.jena.fuseki.http;

import com.hp.hpl.jena.sparql.modify.request.Target ;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateRemote
{
    /** @deprecated Use  UpdateExecutionFactory.createRemote */
    @Deprecated 
    public static void executeClear(String serviceURL)
    {
        Update clear = new UpdateDrop(Target.ALL) ;
        UpdateRemote.execute(clear, serviceURL) ;
    }
    
    /** @deprecated Use  UpdateExecutionFactory.createRemote */
    @Deprecated 
    public static void execute(Update request, String serviceURL)
    {
        execute(new UpdateRequest(request) , serviceURL) ;
    }
    
    /** @deprecated Use  UpdateExecutionFactory.createRemote */
    @Deprecated 
    public static void execute(UpdateRequest request, String serviceURL)
    {
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, serviceURL) ;
        proc.execute() ;

//        HttpPost httpPost = new HttpPost(serviceURL) ;
//        ByteArrayOutputStream b_out = new ByteArrayOutputStream() ;
//        IndentedWriter out = new IndentedWriter(b_out) ; 
//        UpdateWriter.output(request, out) ;
//        out.flush() ;
//        byte[] bytes = b_out.toByteArray() ;
//        AbstractHttpEntity reqEntity = new ByteArrayEntity(bytes) ;
//        reqEntity.setContentType(WebContent.contentTypeSPARQLUpdate) ;
//        reqEntity.setContentEncoding(HTTP.UTF_8) ;
//        httpPost.setEntity(reqEntity) ;
//        HttpClient httpclient = new DefaultHttpClient() ;
//
//        try
//        {
//            HttpResponse response = httpclient.execute(httpPost) ;
//            int responseCode = response.getStatusLine().getStatusCode() ;
//            String responseMessage = response.getStatusLine().getReasonPhrase() ;
//            
//            if ( responseCode == HttpSC.NO_CONTENT_204 )
//                return ;
//            if ( responseCode == HttpSC.OK_200 )
//                // But what was the content?
//                // TODO read body 
//                return ; 
//            throw new UpdateException(responseCode+" "+responseMessage) ;
//        } catch (IOException ex)
//        {
//            throw new UpdateException(ex) ;
//        }
            
    }
}
