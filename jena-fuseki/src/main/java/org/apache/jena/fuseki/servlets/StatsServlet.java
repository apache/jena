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

import org.apache.jena.fuseki.server.CounterName ;
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
        out.println("    Requests      = "+desc.counters.value(CounterName.DatasetRequests)) ;
        out.println("    Good          = "+desc.counters.value(CounterName.DatasetRequestsGood)) ;
        out.println("    Bad           = "+desc.counters.value(CounterName.DatasetRequestsBad)) ;

        out.println("  SPARQL Query:") ;
        out.println("    Request       = "+desc.query.counters.value(CounterName.QueryRequests)) ;
        out.println("    Good          = "+desc.query.counters.value(CounterName.QueryRequestsGood)) ;
        out.println("    Bad requests  = "+desc.query.counters.value(CounterName.QueryRequestsBad)) ;
        out.println("    Timeouts      = "+desc.query.counters.value(CounterName.QueryTimeouts)) ;
        out.println("    Bad exec      = "+desc.query.counters.value(CounterName.QueryExecErrors)) ;

        out.println("  SPARQL Update:") ;
        out.println("    Request       = "+desc.update.counters.value(CounterName.UpdateRequests)) ;
        out.println("    Good          = "+desc.update.counters.value(CounterName.UpdateRequestsGood)) ;
        out.println("    Bad requests  = "+desc.update.counters.value(CounterName.UpdateRequestsBad)) ;
        out.println("    Bad exec      = "+desc.update.counters.value(CounterName.UpdateExecErrors)) ;
        
        out.println("  Upload:") ;
        out.println("    Requests      = "+desc.upload.counters.value(CounterName.UploadRequests)) ;
        out.println("    Good          = "+desc.upload.counters.value(CounterName.UploadRequestsGood)) ;
        out.println("    Bad           = "+desc.upload.counters.value(CounterName.UploadRequestsBad)) ;
        
        out.println("  SPARQL Graph Store Protocol:") ;
        out.println("    GETs          = "+gspValue(desc, CounterName.GSPget)+ " (good="+gspValue(desc, CounterName.GSPgetGood)+"/bad="+gspValue(desc, CounterName.GSPgetBad)+")") ;
        out.println("    PUTs          = "+gspValue(desc, CounterName.GSPput)+ " (good="+gspValue(desc, CounterName.GSPputGood)+"/bad="+gspValue(desc, CounterName.GSPputBad)+")") ;
        out.println("    POSTs         = "+gspValue(desc, CounterName.GSPpost)+ " (good="+gspValue(desc, CounterName.GSPpostGood)+"/bad="+gspValue(desc, CounterName.GSPpostBad)+")") ;
        out.println("    DELETEs       = "+gspValue(desc, CounterName.GSPdelete)+ " (good="+gspValue(desc, CounterName.GSPdeleteGood)+"/bad="+gspValue(desc, CounterName.GSPdeleteBad)+")") ;
        out.println("    HEADs         = "+gspValue(desc, CounterName.GSPhead)+ " (good="+gspValue(desc, CounterName.GSPheadGood)+"/bad="+gspValue(desc, CounterName.GSPheadBad)+")") ;
    }
    
    private long gspValue(DatasetRef desc, CounterName cn) {
        long x1 = desc.readGraphStore.counters.value(cn) ;
        long x2 = desc.readWriteGraphStore.counters.value(cn) ;
        return x1+x2 ;
    }
    
    
}


