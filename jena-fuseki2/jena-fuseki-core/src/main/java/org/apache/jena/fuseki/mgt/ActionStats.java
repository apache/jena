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
import static org.apache.jena.riot.WebContent.charsetUTF8 ;
import static org.apache.jena.riot.WebContent.contentTypeTextPlain ;

import java.io.IOException ;
import java.util.Iterator ;
import java.util.List ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.fuseki.servlets.HttpAction ;

public class ActionStats extends ActionContainerItem
{
    public ActionStats() { super() ; } 
    
    // This does not consult the system database for dormant etc.
    @Override
    protected JsonValue execGetContainer(HttpAction action) { 
        action.log.info(format("[%d] GET stats all", action.id)) ;
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("top") ;
        
        builder.key(JsonConst.datasets) ;
        builder.startObject("datasets") ;
        for ( String ds : DataAccessPointRegistry.get().keys() )
            statsDataset(builder, ds) ; 
        builder.finishObject("datasets") ;
        
        builder.finishObject("top") ;
        return builder.build() ;
    }

    @Override
    protected JsonValue execGetItem(HttpAction action) {
        action.log.info(format("[%d] GET stats dataset %s", action.id, action.getDatasetName())) ;
        
        JsonBuilder builder = new JsonBuilder() ;
        String datasetPath = DataAccessPoint.canonical(action.getDatasetName()) ;
        builder.startObject("TOP") ;
        
        builder.key(JsonConst.datasets) ;
        builder.startObject("datasets") ;
        statsDataset(builder, datasetPath) ;
        builder.finishObject("datasets") ;
        
        builder.finishObject("TOP") ;
        return builder.build() ;
    }

    private void statsDataset(JsonBuilder builder, String ds) {
        // Object started
        builder.key(ds) ;
        
        DataAccessPoint access = DataAccessPointRegistry.get().get(ds) ;
        DataService dSrv = access.getDataService() ;
        builder.startObject("counters") ;
        
        builder.key(CounterName.Requests.name()).value(dSrv.getCounters().value(CounterName.Requests)) ;
        builder.key(CounterName.RequestsGood.name()).value(dSrv.getCounters().value(CounterName.RequestsGood)) ;
        builder.key(CounterName.RequestsBad.name()).value(dSrv.getCounters().value(CounterName.RequestsBad)) ;
        
        
        // Build the operation -> endpoint list map.
        
//      MultiMap<OperationName, Endpoint> map = MultiMap.createMapList() ;
//      for ( OperationName operName : dSrv.getOperations() ) {
//          List<Endpoint> endpoints = access.getDataService().getOperation(operName) ;
//          for ( Endpoint endpoint : endpoints )
//              map.put(operName, endpoint) ; 
//      }
        
        
        builder.key(JsonConst.endpoints).startObject("endpoints") ;
        
        for ( OperationName operName : dSrv.getOperations() ) {
            List<Endpoint> endpoints = access.getDataService().getOperation(operName) ;
//            System.err.println(operName+" : "+endpoints.size()) ;
//            for ( Endpoint endpoint : endpoints )
//                System.err.println("  "+endpoint.getEndpoint()) ;
            
            for ( Endpoint endpoint : endpoints ) {
                
                // Endpoint names are unique but not services.
                
                builder.key(endpoint.getEndpoint()) ;
                builder.startObject() ;
                
                operationCounters(builder, endpoint);
                builder.key(JsonConst.operation).value(operName.name()) ;
                builder.key(JsonConst.description).value(operName.getDescription()) ;
                
                builder.finishObject() ;
            }
        }
        builder.finishObject("endpoints") ;
        builder.finishObject("counters") ;

    }

    private void operationCounters(JsonBuilder builder, Endpoint operation) {
        for (CounterName cn : operation.getCounters().counters()) {
            Counter c = operation.getCounters().get(cn) ;
            builder.key(cn.name()).value(c.value()) ;
        }
    }

    private void statsTxt(HttpServletResponse resp) throws IOException
    {
        ServletOutputStream out = resp.getOutputStream() ;
        resp.setContentType(contentTypeTextPlain);
        resp.setCharacterEncoding(charsetUTF8) ;

        Iterator<String> iter = DataAccessPointRegistry.get().keys().iterator() ;
        while(iter.hasNext())
        {
            String ds = iter.next() ;
            DataAccessPoint desc = DataAccessPointRegistry.get().get(ds) ;
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
        out.println("    Request       = "+counter(dSrv, OperationName.Query, CounterName.Requests)) ;
        out.println("    Good          = "+counter(dSrv, OperationName.Query, CounterName.RequestsGood)) ;
        out.println("    Bad requests  = "+counter(dSrv, OperationName.Query, CounterName.RequestsBad)) ;
        out.println("    Timeouts      = "+counter(dSrv, OperationName.Query, CounterName.QueryTimeouts)) ;
        out.println("    Bad exec      = "+counter(dSrv, OperationName.Query, CounterName.QueryExecErrors)) ;
        out.println("    IO Errors     = "+counter(dSrv, OperationName.Query, CounterName.QueryIOErrors)) ;

        out.println("  SPARQL Update:") ;
        out.println("    Request       = "+counter(dSrv, OperationName.Update, CounterName.Requests)) ;
        out.println("    Good          = "+counter(dSrv, OperationName.Update, CounterName.RequestsGood)) ;
        out.println("    Bad requests  = "+counter(dSrv, OperationName.Update, CounterName.RequestsBad)) ;
        out.println("    Bad exec      = "+counter(dSrv, OperationName.Update, CounterName.UpdateExecErrors)) ;
        
        out.println("  Upload:") ;
        out.println("    Requests      = "+counter(dSrv, OperationName.Upload, CounterName.Requests)) ;
        out.println("    Good          = "+counter(dSrv, OperationName.Upload, CounterName.RequestsGood)) ;
        out.println("    Bad           = "+counter(dSrv, OperationName.Upload, CounterName.RequestsBad)) ;
        
        out.println("  SPARQL Graph Store Protocol:") ;
        out.println("    GETs          = "+gspValue(dSrv, CounterName.HTTPget)+ " (good="+gspValue(dSrv, CounterName.HTTPgetGood)+"/bad="+gspValue(dSrv, CounterName.HTTPGetBad)+")") ;
        out.println("    PUTs          = "+gspValue(dSrv, CounterName.HTTPput)+ " (good="+gspValue(dSrv, CounterName.HTTPputGood)+"/bad="+gspValue(dSrv, CounterName.HTTPputBad)+")") ;
        out.println("    POSTs         = "+gspValue(dSrv, CounterName.HTTPpost)+ " (good="+gspValue(dSrv, CounterName.HTTPpostGood)+"/bad="+gspValue(dSrv, CounterName.HTTPpostBad)+")") ;
        out.println("    DELETEs       = "+gspValue(dSrv, CounterName.HTTPdelete)+ " (good="+gspValue(dSrv, CounterName.HTTPdeleteGood)+"/bad="+gspValue(dSrv, CounterName.HTTPdeleteBad)+")") ;
        out.println("    HEADs         = "+gspValue(dSrv, CounterName.HTTPhead)+ " (good="+gspValue(dSrv, CounterName.HTTPheadGood)+"/bad="+gspValue(dSrv, CounterName.HTTPheadBad)+")") ;
    }
    
    private long counter(DataService dSrv, OperationName opName, CounterName cName) {
        return 0 ;
    }
    
    private long gspValue(DataService dSrv, CounterName cn) {
        return  counter(dSrv, OperationName.GSP, cn) +
                counter(dSrv, OperationName.GSP_R, cn) ;
    }

    // We shouldn't get here - no doPost above.
    
    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        throw new InternalError(METHOD_POST+" container") ;
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        throw new InternalError(METHOD_POST+" item") ;
    }
}


