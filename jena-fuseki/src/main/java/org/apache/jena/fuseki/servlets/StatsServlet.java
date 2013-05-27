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

package org.apache.jena.fuseki.servlets;

import java.io.IOException ;
import java.io.PrintWriter ;
import java.util.Iterator ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;

public class StatsServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        //throws ServletException, IOException
    {
        try {
            PrintWriter out = resp.getWriter() ;
            resp.setContentType("text/plain");
            
            Iterator<String> iter = DatasetRegistry.get().keys() ;
            while(iter.hasNext())
            {
                String ds = iter.next() ;
                DatasetRef desc = DatasetRegistry.get().get(ds) ;
                stats(out, desc) ;
                if ( iter.hasNext() )
                    out.println() ;
            }
            
            out.flush() ;
        } catch (IOException e)
        { }
    }
    
    private void stats(PrintWriter out, DatasetRef desc)
    {
        out.println("Dataset: "+desc.name) ;
        out.println("    Requests   = "+desc.countServiceRequests) ;
        out.println("    Good       = "+desc.countServiceRequestsOK) ;
        out.println("    Bad        = "+desc.countServiceRequestsBad) ;
        out.println("  SPARQL Query:") ;
        out.println("    OK         = "+desc.countQueryOK) ;
        out.println("    Bad Syntax = "+desc.countQueryBadSyntax) ;
        out.println("    Timeouts   = "+desc.countQueryTimeout) ;
        out.println("    Bad exec   = "+desc.countQueryBadExecution);
        out.println("  SPARQL Update:") ;
//        out.println("    Updates    = "+desc.update.    
//        out.println("  Upload:") ;
//        out.println("    Uploads    = "+desc.countUpload) ;
//        out.println("  SPARQL Graph Store Protocol:") ;
//        out.println("    GETs       = "+desc.countGET) ;
//        out.println("    POSTs      = "+desc.countPOST) ;
//        out.println("    PUTs       = "+desc.countPUT) ;
//        out.println("    DELETEs    = "+desc.countDELETE) ;
        //out.println("   HEADs     = "+desc.countHEAD) ;
    }
    
}

