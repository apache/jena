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

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import static org.apache.jena.riot.WebContent.* ;

public class ActionPing extends ActionCtl
{
    public ActionPing() { super() ; } 
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp); 
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp); 
    }
    

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp); 
    }

    @Override
    protected void perform(HttpAction action) {
        try {
            perform$(action) ;
            ServletOps.success(action);
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    protected void perform$(HttpAction action) throws IOException {
        HttpServletResponse response = action.response ;
        ServletOutputStream out = action.response.getOutputStream() ;
        response.setContentType(contentTypeTextPlain);
        response.setCharacterEncoding(charsetUTF8) ;
        
    }
}


