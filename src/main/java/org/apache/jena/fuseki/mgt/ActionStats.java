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

import java.io.IOException ;
import java.util.Iterator ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.* ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.riot.WebContent ;

public class ActionStats extends ActionCtl
{
    public ActionStats() { super() ; } 
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp); 
    }
    
    @Override
    protected void perform(HttpAction action) {
        execGet(action) ;
    }

    protected void execGet(HttpAction action) {
        JsonValue v ;
        if (action.dsRef.name == null )
            v = execGetContainer(action) ;
        else
            v = execGetDataset(action) ;
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
    
    // This does not consult the system database for dormant etc.
    private JsonValue execGetContainer(HttpAction action) { 
        action.log.info(format("[%d] GET stats all", action.id)) ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("top") ;

        builder.key("server") ;
        builder.startObject("server") ;
        builder.key("host").value(action.request.getLocalName()+":"+action.request.getLocalPort()) ;
        builder.finishObject("server") ;

        builder.key("datasets") ;
        builder.startObject("datasets") ;
        for ( String ds : DatasetRegistry.get().keys() )
            statsDataset(builder, ds) ; 
        builder.finishObject("datasets") ;
        
        builder.finishObject("top") ;
        return builder.build() ;
    }

    private JsonValue execGetDataset(HttpAction action) {
        action.log.info(format("[%d] GET stats dataset %s", action.id, action.dsRef.name)) ;
        
        JsonBuilder builder = new JsonBuilder() ;
        String datasetPath = DatasetRef.canocialDatasetPath(action.dsRef.name) ;
        builder.startObject("TOP") ;
        
        builder.key("datasets") ;
        builder.startObject("datasets") ;
        statsDataset(builder, datasetPath) ;
        builder.finishObject("datasets") ;
        
        builder.finishObject("TOP") ;
        return builder.build() ;
    }

    private void statsDataset(JsonBuilder builder, String ds) {
        // Object started
        builder.key(ds) ;
        
        DatasetRef desc = DatasetRegistry.get().get(ds) ;
        builder.startObject("counters") ;
        
        builder.key(CounterName.Requests.name()).value(desc.getCounters().value(CounterName.Requests)) ;
        builder.key(CounterName.RequestsGood.name()).value(desc.getCounters().value(CounterName.RequestsGood)) ;
        builder.key(CounterName.RequestsBad.name()).value(desc.getCounters().value(CounterName.RequestsBad)) ;

        
        builder.key("services").startObject("services") ;
        for ( ServiceRef srvRef : desc.getServiceRefs() ) {
            builder.key(srvRef.name).startObject("service") ;
            statsService(builder, srvRef) ;

            
            builder.key("endpoints") ;
            builder.startArray() ;
            for ( String ep : srvRef.endpoints)
                builder.value(ep) ;
            builder.finishArray() ;
            
            builder.finishObject("service") ;
        }
        builder.finishObject("services") ;
        builder.finishObject("counters") ;

    }

    private void statsService(JsonBuilder builder, ServiceRef srvRef) {
        for (CounterName cn : srvRef.getCounters().counters()) {
            Counter c = srvRef.getCounters().get(cn) ;
            builder.key(cn.name()).value(c.value()) ;
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


