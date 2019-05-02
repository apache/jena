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

package org.apache.jena.fuseki.ctl;

import static java.lang.String.format ;
import static org.apache.jena.riot.WebContent.charsetUTF8 ;
import static org.apache.jena.riot.WebContent.contentTypeTextPlain ;

import java.io.IOException ;
import java.util.Iterator ;
import java.util.List ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.servlets.HttpAction ;

public class ActionStats extends ActionContainerItem
{
    public ActionStats() { super() ; } 

    // This does not consult the system database for dormant etc.
    protected JsonValue execCommonContainer(HttpAction action) {            
        action.log.info(format("[%d] GET stats all", action.id)) ;
        return generateStats(action.getDataAccessPointRegistry()) ;
    }

    public static JsonObject generateStats(DataAccessPointRegistry registry) {
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("top") ;
        builder.key(ServerConst.datasets) ;
        builder.startObject("datasets") ;
        registry.forEach((name, access)->statsDataset(builder, access));
        builder.finishObject("datasets") ;
        builder.finishObject("top") ;
        return builder.build().getAsObject() ;
    }
    
    protected JsonValue execCommonItem(HttpAction action) {
        action.log.info(format("[%d] GET stats dataset %s", action.id, action.getDatasetName())) ;
        
        JsonBuilder builder = new JsonBuilder() ;
        String datasetPath = DataAccessPoint.canonical(action.getDatasetName()) ;
        builder.startObject("TOP") ;
        
        builder.key(ServerConst.datasets) ;
        builder.startObject("datasets") ;
        statsDataset(builder, datasetPath, action.getDataAccessPointRegistry()) ;
        builder.finishObject("datasets") ;
        
        builder.finishObject("TOP") ;
        return builder.build() ;
    }
    
    public static JsonObject generateStats(DataAccessPoint access) {
        JsonBuilder builder = new JsonBuilder() ;
        statsDataset(builder, access) ;
        return builder.build().getAsObject() ;
    }
    
    private void statsDataset(JsonBuilder builder, String name, DataAccessPointRegistry registry) {
        DataAccessPoint access = registry.get(name) ;
        statsDataset(builder, access);
    }
    
    private static void statsDataset(JsonBuilder builder, DataAccessPoint access) {
        // Object started
        builder.key(access.getName()) ;
        DataService dSrv = access.getDataService() ;
        builder.startObject("counters") ;
        
        builder.key(CounterName.Requests.getName()).value(dSrv.getCounters().value(CounterName.Requests)) ;
        builder.key(CounterName.RequestsGood.getName()).value(dSrv.getCounters().value(CounterName.RequestsGood)) ;
        builder.key(CounterName.RequestsBad.getName()).value(dSrv.getCounters().value(CounterName.RequestsBad)) ;
        
        builder.key(ServerConst.endpoints).startObject("endpoints") ;
        
        for ( Operation operName : dSrv.getOperations() ) {
            List<Endpoint> endpoints = access.getDataService().getEndpoints(operName) ;
            
            for ( Endpoint endpoint : endpoints ) {
                // Endpoint names are unique for a given service.
                builder.key(endpoint.getName()) ;
                builder.startObject() ;
                
                operationCounters(builder, endpoint);
                builder.key(ServerConst.operation).value(operName.getName()) ;
                builder.key(ServerConst.description).value(operName.getDescription());
                
                builder.finishObject() ;
            }
        }
        builder.finishObject("endpoints") ;
        builder.finishObject("counters") ;
    }

    private static void operationCounters(JsonBuilder builder, Endpoint operation) {
        for (CounterName cn : operation.getCounters().counters()) {
            Counter c = operation.getCounters().get(cn) ;
            builder.key(cn.getName()).value(c.value()) ;
        }
    }

    private void statsTxt(HttpServletResponse resp, DataAccessPointRegistry registry) throws IOException
    {
        ServletOutputStream out = resp.getOutputStream() ;
        resp.setContentType(contentTypeTextPlain);
        resp.setCharacterEncoding(charsetUTF8) ;

        Iterator<String> iter = registry.keys().iterator() ;
        while(iter.hasNext())
        {
            String ds = iter.next() ;
            DataAccessPoint desc = registry.get(ds) ;
            statsTxt(out, desc) ;
            if ( iter.hasNext() )
                out.println() ;
        }
        out.flush() ;
    }
    
    private void statsTxt(ServletOutputStream out, DataAccessPoint desc) throws IOException
    {
        DataService dSrv = desc.getDataService() ;
        out.println("Dataset: "+desc.getName()) ;
        out.println("    Requests      = "+dSrv.getCounters().value(CounterName.Requests)) ;
        out.println("    Good          = "+dSrv.getCounters().value(CounterName.RequestsGood)) ;
        out.println("    Bad           = "+dSrv.getCounters().value(CounterName.RequestsBad)) ;

        out.println("  SPARQL Query:") ;
        out.println("    Request       = "+counter(dSrv, Operation.Query, CounterName.Requests)) ;
        out.println("    Good          = "+counter(dSrv, Operation.Query, CounterName.RequestsGood)) ;
        out.println("    Bad requests  = "+counter(dSrv, Operation.Query, CounterName.RequestsBad)) ;
        out.println("    Timeouts      = "+counter(dSrv, Operation.Query, CounterName.QueryTimeouts)) ;
        out.println("    Bad exec      = "+counter(dSrv, Operation.Query, CounterName.QueryExecErrors)) ;
        out.println("    IO Errors     = "+counter(dSrv, Operation.Query, CounterName.QueryIOErrors)) ;

        out.println("  SPARQL Update:") ;
        out.println("    Request       = "+counter(dSrv, Operation.Update, CounterName.Requests)) ;
        out.println("    Good          = "+counter(dSrv, Operation.Update, CounterName.RequestsGood)) ;
        out.println("    Bad requests  = "+counter(dSrv, Operation.Update, CounterName.RequestsBad)) ;
        out.println("    Bad exec      = "+counter(dSrv, Operation.Update, CounterName.UpdateExecErrors)) ;
        
        out.println("  Upload:") ;
        out.println("    Requests      = "+counter(dSrv, Operation.Upload, CounterName.Requests)) ;
        out.println("    Good          = "+counter(dSrv, Operation.Upload, CounterName.RequestsGood)) ;
        out.println("    Bad           = "+counter(dSrv, Operation.Upload, CounterName.RequestsBad)) ;
        
        out.println("  SPARQL Graph Store Protocol:") ;
        out.println("    GETs          = "+gspValue(dSrv, CounterName.HTTPget)      + " (good="+gspValue(dSrv, CounterName.HTTPgetGood)+"/bad="+gspValue(dSrv, CounterName.HTTPgetBad)+")") ;
        out.println("    PUTs          = "+gspValue(dSrv, CounterName.HTTPput)      + " (good="+gspValue(dSrv, CounterName.HTTPputGood)+"/bad="+gspValue(dSrv, CounterName.HTTPputBad)+")") ;
        out.println("    POSTs         = "+gspValue(dSrv, CounterName.HTTPpost)     + " (good="+gspValue(dSrv, CounterName.HTTPpostGood)+"/bad="+gspValue(dSrv, CounterName.HTTPpostBad)+")") ;
        out.println("    PATCHs        = "+gspValue(dSrv, CounterName.HTTPpatch)    + " (good="+gspValue(dSrv, CounterName.HTTPpatchGood)+"/bad="+gspValue(dSrv, CounterName.HTTPpatchBad)+")") ;
        out.println("    DELETEs       = "+gspValue(dSrv, CounterName.HTTPdelete)   + " (good="+gspValue(dSrv, CounterName.HTTPdeleteGood)+"/bad="+gspValue(dSrv, CounterName.HTTPdeleteBad)+")") ;
        out.println("    HEADs         = "+gspValue(dSrv, CounterName.HTTPhead)     + " (good="+gspValue(dSrv, CounterName.HTTPheadGood)+"/bad="+gspValue(dSrv, CounterName.HTTPheadBad)+")") ;
    }
    
    private long counter(DataService dSrv, Operation operation, CounterName cName) {
        return 0 ;
    }
    
    private long gspValue(DataService dSrv, CounterName cn) {
        return  counter(dSrv, Operation.GSP_RW, cn) +
                counter(dSrv, Operation.GSP_R, cn) ;
    }
    
    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        return execCommonContainer(action);
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        return execCommonItem(action);
    }

    @Override
    protected JsonValue execGetContainer(HttpAction action) {
        return execCommonContainer(action);
    }
    
    @Override
    protected JsonValue execGetItem(HttpAction action) {
        return execCommonItem(action);
    }    
}


