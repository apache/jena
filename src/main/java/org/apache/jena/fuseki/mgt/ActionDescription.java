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

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.riot.WebContent ;
import org.eclipse.jetty.server.Server ;

/** Description of datasets for a server */ 
public class ActionDescription extends ActionCtl
{
    public ActionDescription() { super() ; }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp); }
    
    @Override
    protected void perform(HttpAction action) 
    {
        try {
            description(action) ;
            ServletOps.success(action) ;
        }
        catch (IOException e) {
            ServletOps.errorOccurred(e) ;
        }
    }
    
    private void description(HttpAction action) throws IOException {
        ServletOutputStream out = action.response.getOutputStream() ;
        action.response.setContentType(WebContent.contentTypeJSON);
        action.response.setCharacterEncoding(WebContent.charsetUTF8) ;
        
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
        builder
            .key("version").value(Fuseki.VERSION) ;
    }
    
    private int port(Server jettyServer) {
        return jettyServer.getConnectors()[0].getPort() ;
    }

    private void describeDataset(JsonBuilder builder) {
        builder.key("datasets") ;
        JsonDescription.arrayDatasets(builder, DatasetRegistry.get());
    }

}

