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

package org.apache.jena.fuseki.mgt;

import java.io.IOException ;
import java.io.PrintStream ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.web.HttpSC ;
import org.eclipse.jetty.server.Server ;

/** Description of datasets for a server */ 
public class DescriptionServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws /*ServletException,*/ IOException
    {
        try {
            // Conneg etc.
            description(req, resp) ;
        } catch (IOException e)
        { 
            resp.sendError(HttpSC.INTERNAL_SERVER_ERROR_500, e.getMessage()) ;
            resp.setContentType(WebContent.contentTypeTextPlain) ;
            resp.setCharacterEncoding(WebContent.charsetUTF8) ;
            ServletOutputStream out = resp.getOutputStream() ;
            PrintStream x = new PrintStream(out) ;
            e.printStackTrace(x);
            x.flush() ;
        }
    }
    
    private void description(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        ServletOutputStream out = resp.getOutputStream() ;
        resp.setContentType(WebContent.contentTypeJSON);
        resp.setCharacterEncoding(WebContent.charsetUTF8) ;
        
        JsonBuilder builder = new JsonBuilder() ; 
        builder.startObject() ;
        describeServer(builder) ;
        describeDataset(builder) ;
        builder.finishObject() ;
        
        JsonValue v = builder.build() ;
        JSON.write(out, v) ;
        out.println() ; 
        out.flush() ;
    }

    private void describeServer(JsonBuilder builder) {
        builder
            .key("server")
            .startObject()
            //.key("hostname").value(req.getLocalName())
            .key("port").value(port(Fuseki.getJettyServer()))
            .finishObject() ;
        builder
            .key("admin")
            .startObject()
            //.key("hostname").value(req.getLocalName())
            .key("port").value(port(Fuseki.getJettyMgtServer()))
            .finishObject() ;
    }
    
    private int port(Server jettyServer ) {
        return jettyServer.getConnectors()[0].getPort() ;
    }

    private void describeDataset(JsonBuilder builder) {
        builder.key("datasets") ;
        builder.startArray() ;
        for ( String ds : DatasetRegistry.get().keys() ) {
            DatasetRef desc = DatasetRegistry.get().get(ds) ;
            JsonDescription.describe(builder, desc) ;
        }
        builder.finishArray() ;
    }

}

