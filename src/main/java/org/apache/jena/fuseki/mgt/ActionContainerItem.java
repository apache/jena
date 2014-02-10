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

import static java.lang.String.format ;

import java.io.* ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Locale ;
import java.util.Map ;

import javax.servlet.ServletException ;
import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.build.Builder ;
import org.apache.jena.fuseki.build.Template ;
import org.apache.jena.fuseki.build.TemplateFunctions ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.servlets.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.web.HttpSC ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.uuid.JenaUUID ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** Base for actions that are container and also have action on items */ 
public abstract class ActionContainerItem extends ActionCtl {
    
    public ActionContainerItem() { super() ; }
    
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
//        doCommon(request, response);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
//        doCommon(request, response);
//    }
//    
//    @Override
//    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        doCommon(request, response);
//    }
//    
//    @Override
//    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        doCommon(request, response);
//    }

    @Override
    final
    protected void perform(HttpAction action) {
        String method = action.request.getMethod() ;
        if ( method.equals(METHOD_GET) )
            execGet(action) ;
        else if ( method.equals(METHOD_POST) )
            execPost(action) ;
        else if ( method.equals(METHOD_DELETE) )
            execDelete(action) ;
        else
            ServletOps.error(HttpSC.METHOD_NOT_ALLOWED_405) ;
    }

    protected void execGet(HttpAction action) {
        JsonValue v ;
        if ( isContainerAction(action)  )
            v = execGetContainer(action) ;
        else
            v = execGetItem(action) ;
        if ( v == null )
            ServletOps.success(action);
        else
            sendJsonReponse(action, v);
    }
    
    // ServlerOps?
    protected static void sendJsonReponse(HttpAction action, JsonValue v) {
        try {
            HttpServletResponse response = action.response ;
            ServletOutputStream out = response.getOutputStream() ;
            response.setContentType(WebContent.contentTypeJSON);
            response.setCharacterEncoding(WebContent.charsetUTF8) ;
            JSON.write(out, v) ;
            out.println() ; 
            out.flush() ;
            ServletOps.success(action);
        } catch (IOException ex) { ServletOps.errorOccurred(ex) ; }
    }
    
    /** GET request on the container - respond with JSON, or null for plain 200 */  
    protected abstract JsonValue execGetContainer(HttpAction action) ;
    /** GET request on an item in the container - repond with JSON, or null for plain 200 */  
    protected abstract JsonValue execGetItem(HttpAction action) ;

    private void execPost(HttpAction action) {
        JsonValue v ;
        if ( isContainerAction(action) )
            v = execPostContainer(action) ;
        else
            v = execPostItem(action) ;
        if ( v == null )
            ServletOps.success(action);
        else
            sendJsonReponse(action, v);
    }
    
    /** POST request on an item in the container - respond with JSON, or null for plain 200 */  
    protected abstract JsonValue execPostContainer(HttpAction action) ;
    /** POST request on an item in the container - respond with JSON, or null for plain 200 */  
    protected abstract JsonValue execPostItem(HttpAction action) ;

    // Must be an item
    protected abstract void execDelete(HttpAction action) ;

}
