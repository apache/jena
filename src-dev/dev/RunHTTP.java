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

package dev;

import java.io.IOException ;
import java.io.OutputStream ;
import java.net.URLEncoder ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.WebContent ;
import org.openjena.riot.web.ContentProducer ;
import org.openjena.riot.web.HttpOp ;
import org.openjena.riot.web.HttpResponseHandler ;
import org.openjena.riot.web.HttpResponseLib ;

import com.hp.hpl.jena.query.ResultSetFormatter ;

public class RunHTTP
{
    static String divider = "----------------------------------------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    static { Log.setLog4j() ; }

    public static void exit(int code)
    {
        System.out.flush() ;
        System.out.println("DONE") ;
        System.exit(code) ;
    }

    public static void main(String ... args) throws Exception
    {
        final String queryString1 =  "SELECT * { ?s ?p ?o } LIMIT 1" ;
        final String queryString2 =  "SELECT * { ?s ?p ?o } LIMIT 10" ;
        
        // GET graph
        Map<String, HttpResponseHandler> handlers = new HashMap<String, HttpResponseHandler>() ;
        // Chnage to one handler for all graph types.
        handlers.put(WebContent.contentTypeTurtle, HttpResponseLib.graphReaderTurtle) ;
        handlers.put(WebContent.contentTypeRDFXML, HttpResponseLib.graphReaderRDFXML) ;
        handlers.put("*", HttpResponseLib.httpDumpResponse) ;

        // Better design.
        HttpResponseLib.HttpCaptureResponseResultSet captureRS = new HttpResponseLib.HttpCaptureResponseResultSet() ;
        handlers.put(WebContent.contentTypeResultsXML, captureRS) ;             
        handlers.put(WebContent.contentTypeResultsJSON, captureRS) ;            
        handlers.put(WebContent.contentTypeTextTSV, captureRS) ;     
        
        List<String> acceptables = new ArrayList<String>() ;
        acceptables.add("text/turtle;q=0.8") ;
        acceptables.add("application/rdf+xml;q=0.1") ;
        String acceptHeader = Iter.asString(acceptables, " , ") ;
        
        HttpOp.execHttpGet("http://localhost:3030/ds/sparql?query="+URLEncoder.encode(queryString1,"UTF-8"),
                           acceptHeader,
                           handlers) ;
        
        ResultSetFormatter.out(captureRS.get()) ;

        
        ContentProducer cp = new ContentProducer() {
            //@Override
            public void writeTo(OutputStream outstream) throws IOException
            {
                outstream.write(StrUtils.asUTF8bytes(queryString2)) ;
                //outstream.flush() ;
            }} ;
         
        List<String> acceptResultSets = new ArrayList<String>() ;
        acceptResultSets.add(WebContent.contentTypeResultsJSON+";q=0.9") ;
        acceptResultSets.add(WebContent.contentTypeResultsXML+";q=0.5") ;
        acceptResultSets.add(WebContent.contentTypeTextTSV+";q=0.8") ;
        String acceptResultSet = Iter.asString(acceptResultSets, " , ") ;
            
        HttpOp.execHttpPost("http://localhost:3030/ds/sparql",
                            WebContent.contentTypeSPARQLQuery, cp,
                            acceptResultSet, handlers) ;
        ResultSetFormatter.out(captureRS.get()) ;
        exit(0) ;

    }
}
