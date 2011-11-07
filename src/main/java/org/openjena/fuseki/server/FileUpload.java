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

package org.openjena.fuseki.server;

import java.io.IOException ;
import java.io.InputStream ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.commons.fileupload.util.Streams ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.out.SinkTripleOutput ;

import com.hp.hpl.jena.graph.Triple ;

public class FileUpload extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if ( ! isMultipart )
        {
            response.sendError(HttpSC.BAD_REQUEST_400 , "Not a file upload") ;
            return ;
        }
        
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        try {

            // Parse the request
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    System.out.println("Form field " + name + " with value "
                                       + Streams.asString(stream) + " detected.");
                } else {
                    System.out.println("File field " + name + " with file name "
                                       + item.getName() + " detected.");
                    // Process the input stream
                    response.setContentType("text/plain") ;

                    Sink<Triple> sink = new SinkTripleOutput(response.getOutputStream()) ;
                    LangRIOT parser = RiotReader.createParserTurtle(stream, null, sink) ;
                    parser.parse() ;
                    
                    response.getOutputStream().println("----------------------") ;
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            response.sendError(HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            return ;
        }
    }
}
