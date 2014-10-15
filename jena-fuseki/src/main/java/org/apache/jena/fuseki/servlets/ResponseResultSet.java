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
import static org.apache.jena.atlas.lib.Lib.equal ;
import static org.apache.jena.fuseki.servlets.ServletBase.errorBadRequest ;
import static org.apache.jena.fuseki.servlets.ServletBase.errorOccurred ;
import static org.apache.jena.fuseki.servlets.ServletBase.log ;

import java.io.IOException ;
import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.commons.lang.StringUtils ;
import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.riot.ResultSetMgr ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.resultset.ResultSetLang ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.core.Prologue ;

/** This is the content negotiation for each kind of SPARQL query result */ 
public class ResponseResultSet
{
    private static Logger xlog = LoggerFactory.getLogger(ResponseResultSet.class) ;
    private static Logger slog = ServletBase.log ;

    // Short names for "output="
    private static final String contentOutputJSON          = "json" ;
    private static final String contentOutputXML           = "xml" ;
    private static final String contentOutputSPARQL        = "sparql" ;
    private static final String contentOutputText          = "text" ;
    private static final String contentOutputCSV           = "csv" ;
    private static final String contentOutputTSV           = "tsv" ;
    private static final String contentOutputThrift        = "thrift" ;
    
    public static Map<String,String> shortNamesResultSet = new HashMap<String, String>() ;
    static {
        // Some short names.  keys are lowercase.
        ResponseOps.put(shortNamesResultSet, contentOutputJSON,   WebContent.contentTypeResultsJSON) ;
        ResponseOps.put(shortNamesResultSet, contentOutputSPARQL, WebContent.contentTypeResultsXML) ;
        ResponseOps.put(shortNamesResultSet, contentOutputXML,    WebContent.contentTypeResultsXML) ;
        ResponseOps.put(shortNamesResultSet, contentOutputText,   WebContent.contentTypeTextPlain) ;
        ResponseOps.put(shortNamesResultSet, contentOutputCSV,    WebContent.contentTypeTextCSV) ;
        ResponseOps.put(shortNamesResultSet, contentOutputTSV,    WebContent.contentTypeTextTSV) ;
        ResponseOps.put(shortNamesResultSet, contentOutputThrift, WebContent.contentTypeResultsThrift) ;
    }
    
    interface OutputContent { void output(ServletOutputStream out) ; }

    public static void doResponseResultSet(HttpAction action, Boolean booleanResult)
    {
        doResponseResultSet$(action, null, booleanResult, null, DEF.rsOfferTable) ;
    }

    public static void doResponseResultSet(HttpAction action, ResultSet resultSet, Prologue qPrologue)
    {
        doResponseResultSet$(action, resultSet, null, qPrologue, DEF.rsOfferTable) ;
    }
    
    // If we refactor the conneg into a single function, we can split boolean and result set handling. 
    
    // One or the other argument must be null
    private static void doResponseResultSet$(HttpAction action,
                                             ResultSet resultSet, Boolean booleanResult, 
                                             Prologue qPrologue, 
                                             AcceptList contentTypeOffer) 
    {
        HttpServletRequest request = action.request ;
        HttpServletResponse response = action.response ;
        long id = action.id ;
        
        if ( resultSet == null && booleanResult == null )
        {
            xlog.warn("doResponseResult: Both result set and boolean result are null") ; 
            throw new FusekiException("Both result set and boolean result are null") ;
        }
        
        if ( resultSet != null && booleanResult != null )
        {
            xlog.warn("doResponseResult: Both result set and boolean result are set") ; 
            throw new FusekiException("Both result set and boolean result are set") ;
        }

        String mimeType = null ; 
        MediaType i = ConNeg.chooseContentType(request, contentTypeOffer, DEF.acceptRSXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;
        
        // Override content type
        // Does &output= override?
        // Requested output type by the web form or &output= in the request.
        String outputField = ResponseOps.paramOutput(request, shortNamesResultSet) ;    // Expands short names
        if ( outputField != null )
            mimeType = outputField ;
        
        String serializationType = mimeType ;           // Choose the serializer based on this.
        String contentType = mimeType ;                 // Set the HTTP respose header to this.
             
        // Stylesheet - change to application/xml.
        final String stylesheetURL = ResponseOps.paramStylesheet(request) ;
        if ( stylesheetURL != null && equal(serializationType,WebContent.contentTypeResultsXML) )
            contentType = WebContent.contentTypeXML ;
        
        // Force to text/plain?
        String forceAccept = ResponseOps.paramForceAccept(request) ;
        if ( forceAccept != null )
            contentType = WebContent.contentTypeTextPlain ;

        // Better : dispatch on MediaType
        // Fuseki2 uses the SPARQL parser/write registry.
        if ( equal(serializationType, WebContent.contentTypeResultsXML) )
            sparqlXMLOutput(action, contentType, resultSet, stylesheetURL, booleanResult) ;
        else if ( equal(serializationType, WebContent.contentTypeResultsJSON) )
            jsonOutput(action, contentType, resultSet, booleanResult) ;
        else if ( equal(serializationType, WebContent.contentTypeTextPlain) )
            textOutput(action, contentType, resultSet, qPrologue, booleanResult) ;
        else if ( equal(serializationType, WebContent.contentTypeTextCSV) ) 
            csvOutput(action, contentType, resultSet, booleanResult) ;
        else if (equal(serializationType, WebContent.contentTypeTextTSV) )
            tsvOutput(action, contentType, resultSet, booleanResult) ;
        else if (equal(serializationType, WebContent.contentTypeResultsThrift) )
            thriftOutput(action, contentType, resultSet, booleanResult) ;
        else
            errorBadRequest("Can't determine output serialization: "+serializationType) ;
    }
    
    
    public static void setHttpResponse(HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse,
                                       String contentType, String charset) 
    {
        // ---- Set up HTTP Response
        // Stop caching (not that ?queryString URLs are cached anyway)
        if ( true )
        {
            httpResponse.setHeader("Cache-Control", "no-cache") ;
            httpResponse.setHeader("Pragma", "no-cache") ;
        }
        // See: http://www.w3.org/International/O-HTTP-charset.html
        if ( contentType != null )
        {
            if ( charset != null && ! isXML(contentType) )
                contentType = contentType+"; charset="+charset ;
            log.trace("Content-Type for response: "+contentType) ;
            httpResponse.setContentType(contentType) ;
        }
    }

    private static boolean isXML(String contentType)
    {
        return contentType.equals(WebContent.contentTypeRDFXML)
            || contentType.equals(WebContent.contentTypeResultsXML)
            || contentType.equals(WebContent.contentTypeXML) ; 
    }

    private static void sparqlXMLOutput(HttpAction action, String contentType, final ResultSet resultSet, final String stylesheetURL, final Boolean booleanResult)
    {
        OutputContent proc = 
            new OutputContent(){
            @Override
            public void output(ServletOutputStream out)
            {
                if ( resultSet != null )
                    ResultSetFormatter.outputAsXML(out, resultSet, stylesheetURL) ;
                if ( booleanResult != null )
                    ResultSetFormatter.outputAsXML(out, booleanResult, stylesheetURL) ;
            }} ;
            output(action, contentType, null, proc) ;
        }
    
    private static void jsonOutput(HttpAction action, String contentType, final ResultSet resultSet, final Boolean booleanResult)
    {
        OutputContent proc = new OutputContent(){
            @Override
            public void output(ServletOutputStream out)
            {
                if ( resultSet != null )
                    ResultSetFormatter.outputAsJSON(out, resultSet) ;
                if (  booleanResult != null )
                    ResultSetFormatter.outputAsJSON(out, booleanResult ) ;
            }
        } ;
        
        try {
            String callback = ResponseOps.paramCallback(action.request) ;
            ServletOutputStream out = action.response.getOutputStream() ;

            if ( callback != null )
            {
                callback = StringUtils.replaceChars(callback, "\r", "") ;
                callback = StringUtils.replaceChars(callback, "\n", "") ;
                out.print(callback) ;
                out.println("(") ;
            }

            output(action, contentType, WebContent.charsetUTF8, proc) ;

            if ( callback != null )
                out.println(")") ;
        } catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    private static void textOutput(HttpAction action, String contentType, final ResultSet resultSet, final Prologue qPrologue, final Boolean booleanResult)
    {
        // Text is not streaming.
        OutputContent proc =  new OutputContent(){
            @Override
            public void output(ServletOutputStream out)
            {
                if ( resultSet != null )
                    ResultSetFormatter.out(out, resultSet, qPrologue) ;
                if (  booleanResult != null )
                    ResultSetFormatter.out(out, booleanResult ) ;
            }
        };

        output(action, contentType, WebContent.charsetUTF8, proc) ;
    }

    private static void csvOutput(HttpAction action, String contentType, final ResultSet resultSet, final Boolean booleanResult) {
        OutputContent proc = new OutputContent(){
            @Override
            public void output(ServletOutputStream out)
            {
                if ( resultSet != null )
                    ResultSetFormatter.outputAsCSV(out, resultSet) ;
                if (  booleanResult != null )
                    ResultSetFormatter.outputAsCSV(out, booleanResult ) ;
            }
        } ;
        output(action, contentType, WebContent.charsetUTF8, proc) ; 
    }

    private static void tsvOutput(HttpAction action, String contentType, final ResultSet resultSet, final Boolean booleanResult) {
        OutputContent proc = new OutputContent(){
            @Override
            public void output(ServletOutputStream out)
            {
                if ( resultSet != null )
                    ResultSetFormatter.outputAsTSV(out, resultSet) ;
                if (  booleanResult != null )
                    ResultSetFormatter.outputAsTSV(out, booleanResult ) ;
            }
        } ;
        output(action, contentType, WebContent.charsetUTF8, proc) ; 
    }

    private static void thriftOutput(HttpAction action, String contentType, final ResultSet resultSet, final Boolean booleanResult) {
        OutputContent proc = new OutputContent(){
            @Override
            public void output(ServletOutputStream out)
            {
                if ( resultSet != null )
                    ResultSetMgr.write(out, resultSet, ResultSetLang.SPARQLResultSetThrift) ;
                if ( booleanResult != null )
                    slog.error("Can't write boolen result in thrift") ;
            }
        } ;
        output(action, contentType, WebContent.charsetUTF8, proc) ; 
    }

    private static void output(HttpAction action, String contentType, String charset, OutputContent proc) 
    {
        try {
            setHttpResponse(action.request, action.response, contentType, charset) ; 
            action.response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = action.response.getOutputStream() ;
            try
            {
                proc.output(out) ;
                out.flush() ;
            } catch (QueryCancelledException ex) {
                // Bother.  Status code 200 already sent.
                slog.info(format("[%d] Query Cancelled - results truncated (but 200 already sent)", action.id)) ;
                out.println() ;
                out.println("##  Query cancelled due to timeout during execution   ##") ;
                out.println("##  ****          Incomplete results           ****   ##") ;
                out.flush() ;
                // No point raising an exception - 200 was sent already.  
                //errorOccurred(ex) ;
            }
        // Includes client gone.
        } catch (IOException ex) 
        { errorOccurred(ex) ; }
        // Do not call httpResponse.flushBuffer(); here - Jetty closes the stream if it is a gzip stream
        // then the JSON callback closing details can't be added. 
    }
}
