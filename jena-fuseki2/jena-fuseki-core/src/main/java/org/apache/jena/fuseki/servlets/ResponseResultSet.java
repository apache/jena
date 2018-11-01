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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;
import static org.apache.jena.riot.WebContent.*;

import java.io.IOException ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Objects;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.system.ConNeg;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFormatter ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.resultset.XMLOutput;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** This is the content negotiation for each kind of SPARQL query result */
public class ResponseResultSet
{
    private static Logger xlog = LoggerFactory.getLogger(ResponseResultSet.class) ;

    // Short names for "output="
    private static final String contentOutputJSON          = "json" ;
    private static final String contentOutputXML           = "xml" ;
    private static final String contentOutputSPARQL        = "sparql" ;
    private static final String contentOutputText          = "text" ;
    private static final String contentOutputCSV           = "csv" ;
    private static final String contentOutputTSV           = "tsv" ;
    private static final String contentOutputThrift        = "thrift" ;

    public static Map<String,String> shortNamesResultSet = new HashMap<>() ;
    static {
        // Some short names.  keys are lowercase.
        ResponseOps.put(shortNamesResultSet, contentOutputJSON,   contentTypeResultsJSON) ;
        ResponseOps.put(shortNamesResultSet, contentOutputSPARQL, contentTypeResultsXML) ;
        ResponseOps.put(shortNamesResultSet, contentOutputXML,    contentTypeResultsXML) ;
        ResponseOps.put(shortNamesResultSet, contentOutputText,   contentTypeTextPlain) ;
        ResponseOps.put(shortNamesResultSet, contentOutputCSV,    contentTypeTextCSV) ;
        ResponseOps.put(shortNamesResultSet, contentOutputTSV,    contentTypeTextTSV) ;
        ResponseOps.put(shortNamesResultSet, contentOutputThrift, contentTypeResultsThrift) ;
    }

    interface OutputContent { void output(ServletOutputStream out) throws IOException; }

    public static void doResponseResultSet(HttpAction action, Boolean booleanResult) {
        doResponseResultSet$(action, null, booleanResult, null, DEF.rsOfferBoolean) ;
    }

    public static void doResponseResultSet(HttpAction action, ResultSet resultSet, Prologue qPrologue) {
        doResponseResultSet$(action, resultSet, null, qPrologue, DEF.rsOfferTable) ;
    }

    // One or the other argument must be null
    private static void doResponseResultSet$(HttpAction action, 
                                             ResultSet resultSet, Boolean booleanResult,
                                             Prologue qPrologue, AcceptList contentTypeOffer) {
        HttpServletRequest request = action.request ;
        HttpServletResponse response = action.response ;
        long id = action.id ;

        if ( resultSet == null && booleanResult == null ) {
            xlog.warn("doResponseResult: Both result set and boolean result are null") ;
            throw new FusekiException("Both result set and boolean result are null") ;
        }

        if ( resultSet != null && booleanResult != null ) {
            xlog.warn("doResponseResult: Both result set and boolean result are set") ;
            throw new FusekiException("Both result set and boolean result are set") ;
        }

        String mimeType = null ;
        // -- Conneg
        MediaType i = ConNeg.chooseContentType(request, contentTypeOffer, DEF.acceptRSXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;

        // -- Override content type from conneg.
        // Does &output= override?
        // Requested output type by the web form or &output= in the request.
        String outputField = ResponseOps.paramOutput(request, shortNamesResultSet) ;    // Expands short names
        if ( outputField != null )
            mimeType = outputField ;

        String serializationType = mimeType ;           // Choose the serializer based on this.
        String contentType = mimeType ;                 // Set the HTTP respose header to this.

        // -- Stylesheet - change to application/xml.
        final String stylesheetURL = ResponseOps.paramStylesheet(request) ;
        if ( stylesheetURL != null && Objects.equals(serializationType,contentTypeResultsXML) )
            contentType = contentTypeXML ;

        // Force to text/plain?
        String forceAccept = ResponseOps.paramForceAccept(request) ;
        if ( forceAccept != null )
            contentType = contentTypeTextPlain ;

        // Some kind of general dispatch is neater but there are quite a few special cases.
        // text/plain is special because there is no ResultSetWriter for it (yet). 
        // Text plain is special because of the formatting by prologue.
        // text/plain is not a registered result set language. 
        //
        // JSON is special because of ?callback
        //
        // XML is special because of
        // (1) charset is a feature of XML, not the response 
        // (2) ?stylesheet=
        //
        // Thrift is special because
        // (1) charset is meaningless
        // (2) there is no boolean result form.

        if ( Objects.equals(serializationType, contentTypeTextPlain) ) {
            textOutput(action, contentType, resultSet, qPrologue, booleanResult) ;
            return;
        }

        Lang lang = WebContent.contentTypeToLangResultSet(serializationType);
        if (lang == null )
            ServletOps.errorBadRequest("Not recognized for SPARQL results: "+serializationType) ;
        if ( ! ResultSetWriterRegistry.isRegistered(lang) )
            ServletOps.errorBadRequest("No results writer for "+serializationType);

        Context cxt = action.getContext().copy();
        String charset = charsetUTF8;
        String jsonCallback = null;
        
        if ( Objects.equals(serializationType, contentTypeResultsXML) ) {
            charset = null;
            XMLOutput.setStylesheetURL(cxt, stylesheetURL);
        }
        if ( Objects.equals(serializationType, contentTypeResultsJSON) ) {
            jsonCallback = ResponseOps.paramCallback(action.request) ;
        }
        if (Objects.equals(serializationType, WebContent.contentTypeResultsThrift) ) {
            if ( booleanResult != null )
                ServletOps.errorBadRequest("Can't write a boolean result in thrift") ;
            charset = null;
        }
        
        //Finally, the general case
        generalOutput(action, lang, contentType, charset, cxt, jsonCallback, resultSet, booleanResult) ;
    }

    private static void textOutput(HttpAction action, String contentType, ResultSet resultSet, Prologue qPrologue, Boolean booleanResult) {
        // Text is not streaming.
        OutputContent proc = (ServletOutputStream out) -> { 
            if ( resultSet != null )
                ResultSetFormatter.out(out, resultSet, qPrologue) ;
            if (  booleanResult != null )
                ResultSetFormatter.out(out, booleanResult.booleanValue()) ;
        };

        output(action, contentType, charsetUTF8, proc) ;
    }

    /** Any format */
    private static void generalOutput(HttpAction action, Lang rsLang, 
                                      String contentType, String charset,
                                      Context context, String callback,
                                      ResultSet resultSet, Boolean booleanResult) {
        ResultsWriter rw = ResultsWriter.create()
            .lang(rsLang)
            .context(context)
            .build();
        OutputContent proc = (ServletOutputStream out) -> {
            if ( callback != null ) {
                String callbackFunction = callback;
                callbackFunction = callbackFunction.replace("\r", "") ;
                callbackFunction = callbackFunction.replace("\n", "") ;
                out.print(callbackFunction) ;
                out.println("(") ;
            }
            if ( resultSet != null )
                rw.write(out, resultSet) ;
            if ( booleanResult != null )
                rw.write(out, booleanResult.booleanValue()) ;
            if ( callback != null )
                out.println(")") ;
        } ;
        output(action, contentType, charset, proc) ;
    }

    // Sett HTTP response Execute OutputContent inside 
    private static void output(HttpAction action, String contentType, String charset, OutputContent proc) {
        try {
            ResponseOps.setHttpResponse(action, contentType, charset) ;
            action.response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = action.response.getOutputStream() ;
            try {
                proc.output(out) ;
                out.flush() ;
            } catch (QueryCancelledException ex) {
                // Status code 200 may have already been sent.
                // We can try to set the HTTP response code anyway.
                // Breaking the results is the best we can do to indicate the timeout. 
                action.response.setStatus(HttpSC.BAD_REQUEST_400);
                action.log.info(format("[%d] Query Cancelled - results truncated (but 200 may have already been sent)", action.id)) ;
                out.println() ;
                out.println("##  Query cancelled due to timeout during execution   ##") ;
                out.println("##  ****          Incomplete results           ****   ##") ;
                out.flush() ;
                // No point raising an exception - 200 was sent already.
                //errorOccurred(ex) ;
            }
        // Includes client gone.
        } catch (IOException ex) { ServletOps.errorOccurred(ex) ; }
        // Do not call httpResponse.flushBuffer() at this point. JSON callback closing details haven't been added.
        // Jetty closes the stream if it is a gzip stream.
    }
}
