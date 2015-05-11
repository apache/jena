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

package org.apache.jena.fuseki.mgt;

import java.io.IOException ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;
import javax.servlet.http.HttpSession ;

import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.web.HttpSC ;

/** Log-in and choose dataset */
public class ActionDataset extends HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
//        request.getRemoteUser() ;
//        request.getUserPrincipal() ;
        
        String dataset = FusekiLib.safeParameter(request, "dataset") ;
        HttpSession session = request.getSession(true) ;
        session.setAttribute("dataset", dataset) ;
        session.setMaxInactiveInterval(15*60) ; // 10 mins
        
        boolean known = DatasetRegistry.get().isRegistered(dataset) ;
        if ( !known )
        {
            response.sendError(HttpSC.BAD_REQUEST_400, "No such dataset: "+dataset) ;
            return ;
        }
        
        // Redirect to GET page.
		response.setHeader(HttpNames.hLocation, PageNames.pageAfterLogin) ;
		response.setStatus(HttpSC.SEE_OTHER_303) ;
        
//        Cookie cookie = new Cookie("org.apache.jena.fuseki.session", dataset) ;
//        // 24 hours.
//        cookie.setMaxAge(24*60*60) ;
        
    }

}
