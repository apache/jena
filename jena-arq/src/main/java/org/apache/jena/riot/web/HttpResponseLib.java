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

package org.apache.jena.riot.web;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.lang.RDFParserOutputLib ;
import org.apache.jena.riot.system.SinkRDF ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;

/** A collection of handlers for response handling.
 * @see HttpOp#execHttpGet(String, String, java.util.Map)
 * @see HttpOp#execHttpPost(String, String, ContentProducer, String, java.util.Map)
 * 
 */
public class HttpResponseLib
{
    static abstract class AbstractGraphReader implements HttpCaptureResponse<Graph>
    {
        private Graph graph = null ;
        @Override
        final public void handle(String contentType, String baseIRI, HttpResponse response)
        {
            try {
                Graph g = GraphFactory.createDefaultGraph() ;
                HttpEntity entity = response.getEntity() ;
                MediaType mt = MediaType.create(response.getFirstHeader(HttpNames.hContentType).getValue()) ;
                mt.getCharset() ;
                SinkRDF dest = RDFParserOutputLib.graph(g) ; 
                InputStream in = entity.getContent() ;
                LangRIOT parser = createParser(in, baseIRI, dest) ;
                parser.parse() ;
                in.close() ;
                this.graph = g ; 
            } catch (IOException ex) { IO.exception(ex) ; }
        }
    
        @Override
        public Graph get() { return graph ; }
        
        abstract protected LangRIOT createParser(InputStream in, String baseIRI, SinkRDF dest) ;
    }

    public static HttpResponseHandler httpDumpResponse = new HttpResponseHandler()
    {
        @Override
        public void handle(String contentType, String baseIRI, HttpResponse response)
        {
            try {
                HttpEntity entity = response.getEntity() ;
                InputStream in = entity.getContent() ;
                int l ;
                byte buffer[] = new byte[1024] ;
                while( (l=in.read(buffer)) != -1 )
                {
                    System.out.print(new String(buffer, 0, l, "UTF-8")) ;
                }
                    
    
                in.close() ;
            } catch (IOException ex)
            {
                ex.printStackTrace(System.err) ;
            }
        }
    } ;
    
    public static HttpResponseHandler nullResponse = new HttpResponseHandler() {
        @Override
        public void handle(String contentType, String baseIRI, HttpResponse response)
        {}
    } ;
    
    public static HttpCaptureResponse<Graph> graphReaderTurtle = new AbstractGraphReader()
    {
        @Override
        protected LangRIOT createParser(InputStream in, String baseIRI, SinkRDF dest)
        {
            return RiotReader.createParserTurtle(in, baseIRI, dest) ;
        }
    } ;
    public static HttpCaptureResponse<Graph> graphReaderNTriples = new AbstractGraphReader()
    {
        @Override
        protected LangRIOT createParser(InputStream in, String baseIRI, SinkRDF dest)
        {
            return RiotReader.createParserNTriples(in, dest) ;
        }
    } ;
    public static HttpCaptureResponse<Graph> graphReaderRDFXML = new AbstractGraphReader()
    {
        @Override
        protected LangRIOT createParser(InputStream in, String baseIRI, SinkRDF dest)
        {
            return RiotReader.createParserRDFXML(in, baseIRI, dest) ;
        }
    } ;
    
    public static ResultsFormat contentTypeToResultSet(String contentType) { return mapContentTypeToResultSet.get(contentType) ; }
    private static final Map<String, ResultsFormat> mapContentTypeToResultSet = new HashMap<String, ResultsFormat>() ;
    static
    {
        mapContentTypeToResultSet.put(WebContent.contentTypeResultsXML, ResultsFormat.FMT_RS_XML) ;
        mapContentTypeToResultSet.put(WebContent.contentTypeResultsJSON, ResultsFormat.FMT_RS_JSON) ;
        mapContentTypeToResultSet.put(WebContent.contentTypeTextTSV, ResultsFormat.FMT_RS_TSV) ;
    }

    /** Response handling for SPARQL result sets. */
    public static class HttpCaptureResponseResultSet implements HttpCaptureResponse<ResultSet>
    {    
        ResultSet rs = null ;
        @Override
        public void handle(String contentType, String baseIRI, HttpResponse response) throws IOException
        {
            MediaType mt = MediaType.create(contentType) ;
            ResultsFormat fmt = mapContentTypeToResultSet.get(contentType) ; // contentTypeToResultSet(contentType) ;
            InputStream in = response.getEntity().getContent() ;
            rs = ResultSetFactory.load(in, fmt) ;
            // Force reading
            rs = ResultSetFactory.copyResults(rs) ;
        }

        @Override
        public ResultSet get()
        {
            return rs ;
        }
    }
}
