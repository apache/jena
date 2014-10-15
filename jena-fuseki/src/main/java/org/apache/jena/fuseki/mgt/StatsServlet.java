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
import java.util.Iterator ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonArray ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.riot.WebContent ;

public class StatsServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        //throws ServletException, IOException
    {
        try {
            // Conneg etc.
            statsJSON(req, resp) ;
        } catch (IOException e)
        { }
    }
    
    private void statsJSON(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        ServletOutputStream out = resp.getOutputStream() ;
        resp.setContentType(WebContent.contentTypeJSON);
        resp.setCharacterEncoding(WebContent.charsetUTF8) ;

        /*
         * { "server" : ....   
         *    "datasets" : {
         *       "ds1": { counters... }
         *       GSP stucture?
         *         
         */
        
        JsonObject obj = new JsonObject() ;
        JsonObject datasets = new JsonObject() ;
        JsonObject server = new JsonObject() ;
        server.put("host", req.getLocalName()+":"+req.getLocalPort()) ;
        
        for ( String ds : DatasetRegistry.get().keys() )
            statsJSON(datasets, ds) ; 
        
        obj.put("server", server) ;
        obj.put("datasets", datasets) ;
        
        JSON.write(out, obj) ;
        out.flush() ;
    }
    
    private void statsJSON(JsonObject datasets, String ds) {
        DatasetRef desc = DatasetRegistry.get().get(ds) ;
        JsonObject stats = new JsonObject() ;
        datasets.put(ds, stats) ;
        stats.put(CounterName.Requests.name(),      desc.getCounters().value(CounterName.Requests)) ;
        stats.put(CounterName.RequestsGood.name(),  desc.getCounters().value(CounterName.RequestsGood)) ;
        stats.put(CounterName.RequestsBad.name(),   desc.getCounters().value(CounterName.RequestsBad)) ;
        JsonObject services = new JsonObject() ;

//        JsonArray endpoints = new JsonArray() ;
//        services.put("endpoints", endpoints) ;
//        JsonArray srvNames = new JsonArray() ;
//        services.put("names", srvNames) ;
        
        // There can be several endpoints for one service.
        for ( ServiceRef srvRef : desc.getServiceRefs() ) {
            JsonObject epStats = new JsonObject() ;
            statsJSON(epStats, srvRef) ;
            services.put(srvRef.name, epStats) ;
            JsonArray endpoints = new JsonArray() ;
            epStats.put("endpoints", endpoints) ;
            for ( String ep : srvRef.endpoints) {
                endpoints.add(ep) ;
            }
        }
        stats.put("services", services) ;
    }

    private void statsJSON(JsonObject epStats, ServiceRef srvRef) {
        for (CounterName cn : srvRef.getCounters().counters()) {
            Counter c = srvRef.getCounters().get(cn) ;
            epStats.put(cn.name(), c.value()) ;
        }
    }

    private void statsTxt(HttpServletResponse resp) throws IOException
    {
        ServletOutputStream out = resp.getOutputStream() ;
        resp.setContentType(WebContent.contentTypeTextPlain);
        resp.setCharacterEncoding(WebContent.charsetUTF8) ;

        Iterator<String> iter = DatasetRegistry.get().keys().iterator() ;
        while(iter.hasNext())
        {
            String ds = iter.next() ;
            DatasetRef desc = DatasetRegistry.get().get(ds) ;
            statsTxt(out, desc) ;
            if ( iter.hasNext() )
                out.println() ;
        }
        out.flush() ;
    }
    private void statsTxt(ServletOutputStream out, DatasetRef desc) throws IOException
    {
        out.println("Dataset: "+desc.name) ;
        out.println("    Requests      = "+desc.getCounters().value(CounterName.Requests)) ;
        out.println("    Good          = "+desc.getCounters().value(CounterName.RequestsGood)) ;
        out.println("    Bad           = "+desc.getCounters().value(CounterName.RequestsBad)) ;

        out.println("  SPARQL Query:") ;
        out.println("    Request       = "+desc.query.getCounters().value(CounterName.Requests)) ;
        out.println("    Good          = "+desc.query.getCounters().value(CounterName.RequestsGood)) ;
        out.println("    Bad requests  = "+desc.query.getCounters().value(CounterName.RequestsBad)) ;
        out.println("    Timeouts      = "+desc.query.getCounters().value(CounterName.QueryTimeouts)) ;
        out.println("    Bad exec      = "+desc.query.getCounters().value(CounterName.QueryExecErrors)) ;

        out.println("  SPARQL Update:") ;
        out.println("    Request       = "+desc.update.getCounters().value(CounterName.Requests)) ;
        out.println("    Good          = "+desc.update.getCounters().value(CounterName.RequestsGood)) ;
        out.println("    Bad requests  = "+desc.update.getCounters().value(CounterName.RequestsBad)) ;
        out.println("    Bad exec      = "+desc.update.getCounters().value(CounterName.UpdateExecErrors)) ;
        
        out.println("  Upload:") ;
        out.println("    Requests      = "+desc.upload.getCounters().value(CounterName.Requests)) ;
        out.println("    Good          = "+desc.upload.getCounters().value(CounterName.RequestsGood)) ;
        out.println("    Bad           = "+desc.upload.getCounters().value(CounterName.RequestsBad)) ;
        
        out.println("  SPARQL Graph Store Protocol:") ;
        out.println("    GETs          = "+gspValue(desc, CounterName.GSPget)+ " (good="+gspValue(desc, CounterName.GSPgetGood)+"/bad="+gspValue(desc, CounterName.GSPgetBad)+")") ;
        out.println("    PUTs          = "+gspValue(desc, CounterName.GSPput)+ " (good="+gspValue(desc, CounterName.GSPputGood)+"/bad="+gspValue(desc, CounterName.GSPputBad)+")") ;
        out.println("    POSTs         = "+gspValue(desc, CounterName.GSPpost)+ " (good="+gspValue(desc, CounterName.GSPpostGood)+"/bad="+gspValue(desc, CounterName.GSPpostBad)+")") ;
        out.println("    DELETEs       = "+gspValue(desc, CounterName.GSPdelete)+ " (good="+gspValue(desc, CounterName.GSPdeleteGood)+"/bad="+gspValue(desc, CounterName.GSPdeleteBad)+")") ;
        out.println("    HEADs         = "+gspValue(desc, CounterName.GSPhead)+ " (good="+gspValue(desc, CounterName.GSPheadGood)+"/bad="+gspValue(desc, CounterName.GSPheadBad)+")") ;
    }
    
    private long gspValue(DatasetRef desc, CounterName cn) {
        long x1 = desc.readGraphStore.getCounters().value(cn) ;
        long x2 = desc.readWriteGraphStore.getCounters().value(cn) ;
        return x1+x2 ;
    }
    
    
}


