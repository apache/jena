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

package org.openjena.fuseki.servlets;

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.fuseki.DEF ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.FusekiException ;
import org.openjena.fuseki.FusekiLib ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.conneg.AcceptList ;
import org.openjena.fuseki.conneg.ConNeg ;
import org.openjena.fuseki.conneg.MediaType ;
import org.openjena.fuseki.conneg.TypedInputStream ;
import org.openjena.fuseki.conneg.WebLib ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.xmloutput.RDFXMLWriterI ;

/** This is the content negotiation for each kind of SPARQL query result */ 
public class ResponseQuery
{
    private static Logger log = LoggerFactory.getLogger(ResponseQuery.class) ;
    
    interface OutputContent { void output(ServletOutputStream out) ; }

    static AcceptList prefContentTypeResultSet     = DEF.rsOffer ; 
    static AcceptList prefContentTypeRDF           = DEF.rdfOffer ;

    public static void doResponseModel(Model model, HttpServletRequest request, HttpServletResponse response)
    {
        String mimeType = null ;        // Header request type 
        
        // TODO Use MediaType throughout.
        MediaType i = ConNeg.chooseContentType(request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;
        
        String writerMimeType = mimeType ;
        
        if ( mimeType == null )
        {
            Fuseki.requestLog.warn("Can't find MIME type for response") ;
            String x = WebLib.getAccept(request) ;
            String msg ;
            if ( x == null )
                msg = "No Accept: header" ;
            else
                msg = "Accept: "+x+" : Not understood" ;
            SPARQL_ServletBase.error(HttpSC.NOT_ACCEPTABLE_406, msg) ;
        }
        
        TypedInputStream ts = new TypedInputStream(null, mimeType, WebContent.charsetUTF8) ;
        Lang lang = FusekiLib.langFromContentType(ts.getMediaType()) ; 
        RDFWriter rdfw = FusekiLib.chooseWriter(lang) ;
             
        if ( rdfw instanceof RDFXMLWriterI )
            rdfw.setProperty("showXmlDeclaration", "true") ;
        
//        // Write locally to check it's possible.
//        // Time/space tradeoff.
//        try {
//            OutputStream out = new NullOutputStream() ;
//            rdfw.write(model, out, null) ;
//            IO.flush(out) ;
//        } catch (JenaException ex)
//        {
//            SPARQL_ServletBase.errorOccurred(ex) ;
//        }
        
        // Managed to write it locally
        try {
            setHttpResponse(request, response, ts.getMediaType(), ts.getCharset()) ; 
            response.setStatus(HttpSC.OK_200) ;
            rdfw.write(model, response.getOutputStream(), null) ;
            response.getOutputStream().flush() ;
        }
        catch (Exception ex) { SPARQL_ServletBase.errorOccurred(ex) ; }
    }
    
    // One or the other argument must be null
    public static void doResponseResultSet(final ResultSet resultSet, final Boolean booleanResult, HttpServletRequest request, HttpServletResponse response)
    {
        if ( resultSet == null && booleanResult == null )
        {
            log.warn("doResponseResult: Both result set and boolean result are null") ; 
            throw new FusekiException("Both result set and boolean result are null") ;
        }
        
        if ( resultSet != null && booleanResult != null )
        {
            log.warn("doResponseResult: Both result set and boolean result are set") ; 
            throw new FusekiException("Both result set and boolean result are set") ;
        }

        // Content negotiation
        String mimeType = null ; 
        MediaType i = ConNeg.chooseContentType(request, DEF.rsOffer, DEF.acceptRSXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;
        
        // Override content type
        // Does &output= override?
        // Requested output type by the web form or &output= in the request.
        String outputField = paramOutput(request) ;    // Expands short names
        if ( outputField != null )
            mimeType = outputField ;
        
        String serializationType = mimeType ;           // Choose the serializer based on this.
        String contentType = mimeType ;                 // Set the HTTP respose header to this.
             
        // Stylesheet - change to application/xml.
        final String stylesheetURL = paramStylesheet(request) ;
        if ( stylesheetURL != null && serializationType.equals(WebContent.contentTypeResultsXML))
            contentType = WebContent.contentTypeXML ;
        
        // Force to text/plain?
        String forceAccept = paramForceAccept(request) ;
        if ( forceAccept != null )
            contentType = forceAccept ;

        // Better : dispatch on MediaType
        // ---- Form: XML
        if ( serializationType.equals(WebContent.contentTypeResultsXML) )
        {
            try {
                output(contentType, null, new OutputContent()
                {
                    @Override
                    public void output(ServletOutputStream out)
                    {
                        if ( resultSet != null )
                            ResultSetFormatter.outputAsXML(out, resultSet, stylesheetURL) ;
                        if ( booleanResult != null )
                            ResultSetFormatter.outputAsXML(out, booleanResult.booleanValue(), stylesheetURL) ;
                    }
                }, request, response) ;
            }
            catch (Exception ex) { log.debug("Exception [SELECT/XML]"+ex, ex) ; } 
            return ;
        }

        // ---- Form: JSON
        if ( serializationType.equals(WebContent.contentTypeResultsJSON) )
        {
            try {
                jsonOutput(contentType, new OutputContent(){
                    @Override
                    public void output(ServletOutputStream out)
                    {
                        if ( resultSet != null )
                            ResultSetFormatter.outputAsJSON(out, resultSet) ;
                        if (  booleanResult != null )
                            ResultSetFormatter.outputAsJSON(out, booleanResult.booleanValue()) ;
                    }
                }, request, response) ;
            }
            // This catches things like NIO exceptions.
            catch (Exception ex) { log.debug("Exception [SELECT/JSON] "+ex, ex) ; } 
            return ;
        }

        // ---- Form: text
        if ( serializationType.equals(WebContent.contentTypeTextPlain) )
        {
            try {
                textOutput(contentType, new OutputContent(){
                    @Override
                    public void output(ServletOutputStream out)
                    {
                        if ( resultSet != null )
                            ResultSetFormatter.out(out, resultSet) ;
                        if (  booleanResult != null )
                            ResultSetFormatter.out(out, booleanResult.booleanValue()) ;
                    }
                }, request, response) ;
            }
//            catch (IOException ioEx)
//            {
//                if ( isEOFexception(ioEx) ) 
//                    log.warn("IOException[SELECT/Text] (ignored) "+ioEx, ioEx) ;
//                else
//                    log.debug("IOException [SELECT/Text] (ignored) "+ioEx, ioEx) ;
//            }
            // This catches things like NIO exceptions.
            catch (Exception ex) { log.debug("Exception [SELECT/Text] "+ex, ex) ; } 
            return ;
        }
        
        if ( serializationType.equals(WebContent.contentTypeTextCSV) || 
            serializationType.equals(WebContent.contentTypeTextTSV) )
        {
            try {
                OutputContent output ;
                if ( serializationType.equals(WebContent.contentTypeTextCSV) )
                {
                    output = new OutputContent(){
                        @Override
                        public void output(ServletOutputStream out)
                        {
                            if ( resultSet != null )
                                ResultSetFormatter.outputAsCSV(out, resultSet) ;
                            if (  booleanResult != null )
                                ResultSetFormatter.outputAsCSV(out, booleanResult.booleanValue()) ;
                        }
                    } ;
                }
                else
                {
                    output = new OutputContent(){
                        @Override
                        public void output(ServletOutputStream out)
                        {
                            if ( resultSet != null )
                                ResultSetFormatter.outputAsTSV(out, resultSet) ;
                            if (  booleanResult != null )
                                ResultSetFormatter.outputAsTSV(out, booleanResult.booleanValue()) ;
                        }
                    } ;
                }
                textOutput(contentType, output, request, response) ;
            }
//            catch (IOException ioEx)
//            {
//                if ( isEOFexception(ioEx) )
//                    log.warn("IOException[SELECT/CSV-TSV] (ignored) "+ioEx, ioEx) ;
//                else
//                    log.debug("IOException [SELECT/CSV-TSV] (ignored) "+ioEx, ioEx) ;
//            }
            // This catches things like NIO exceptions.
            catch (Exception ex) { log.debug("Exception [SELECT/CSV-TSV] "+ex, ex) ; } 
            return ;
        }
        
        SPARQL_ServletBase.errorBadRequest("Can't determine output serialization: "+serializationType) ;
    }
    
    
    private static boolean isEOFexception(IOException ioEx)
    {
        if ( ioEx.getClass().getName().equals("org.mortbay.jetty.EofException eofEx") )
            return true ;
        if ( ioEx instanceof java.io.EOFException )
            return true ;
        return false ;
    }

    private static void output(String contentType, String charset, OutputContent proc, 
                               HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
            setHttpResponse(httpRequest, httpResponse, contentType, charset) ; 
            httpResponse.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = httpResponse.getOutputStream() ;
            proc.output(out) ;
            out.flush() ;
            httpResponse.flushBuffer();
        } catch (IOException ex) { SPARQL_ServletBase.errorOccurred(ex) ; }
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

    private static void jsonOutput(String contentType, OutputContent proc,
                                   HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
            String callback = paramCallback(httpRequest) ;
            String outputField = paramOutput(httpRequest) ;
            ServletOutputStream out = httpResponse.getOutputStream() ;

            if ( callback != null )
            {
                out.print(callback) ;
                out.println("(") ;
            }

            output(contentType, WebContent.charsetUTF8, proc, httpRequest, httpResponse) ;

            if ( callback != null )
            {
                out.print(")") ;
                out.println() ;
            }
            out.flush() ;
            httpResponse.flushBuffer();

        } catch (IOException ex) { SPARQL_ServletBase.errorOccurred(ex) ; }
    }
    
    private static void textOutput(String contentType, OutputContent proc, 
                                   HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
            ServletOutputStream out = httpResponse.getOutputStream() ;
            output(contentType, WebContent.charsetUTF8, proc, httpRequest, httpResponse) ;
            out.flush() ;
            httpResponse.flushBuffer();
        } catch (IOException ex) { SPARQL_ServletBase.errorOccurred(ex) ; }
    }

    private static String paramForceAccept(HttpServletRequest request)
    {
        String x = fetchParam(request, HttpNames.paramForceAccept) ;
        return expandShortName(x) ; 
    }
    
    private static String paramStylesheet(HttpServletRequest request)
    { return fetchParam(request, HttpNames.paramStyleSheet) ; }
    
    private static String paramOutput(HttpServletRequest request)
    {
        // Two names.
        String x = fetchParam(request, HttpNames.paramOutput1) ;
        if ( x == null )
            x = fetchParam(request, HttpNames.paramOutput2) ;
        return expandShortName(x) ; 
    }
    
    private static String paramAcceptField(HttpServletRequest request)
    {
        String acceptField = WebLib.getAccept(request) ;
        String acceptParam = fetchParam(request, HttpNames.paramAccept) ;
        
        if ( acceptParam != null )
            acceptField = acceptParam ;
        if ( acceptField == null )
            return null ;
        return expandShortName(acceptField) ; 
    }

    // Short names for "output="
    // TODO Map !
    public static final String contentOutputJSON          = "json" ;
    public static final String contentOutputXML           = "xml" ;
    public static final String contentOutputSPARQL        = "sparql" ;
    public static final String contentOutputText          = "text" ;
    public static final String contentOutputCSV           = "csv" ;
    public static final String contentOutputTSV           = "tsv" ;
    
    private static String expandShortName(String str)
    {
        if ( str == null )
            return null ;
        // Some short names.
        if ( str.equalsIgnoreCase(contentOutputJSON) ) 
            return WebContent.contentTypeResultsJSON ;
        
        if ( str.equalsIgnoreCase(contentOutputSPARQL) )
            return WebContent.contentTypeResultsXML ;
        
        if ( str.equalsIgnoreCase(contentOutputXML) )
            return WebContent.contentTypeResultsXML ;
        
        if ( str.equalsIgnoreCase(contentOutputText) )
            return WebContent.contentTypeTextPlain ;
        
        if ( str.equalsIgnoreCase(contentOutputCSV) )
            return WebContent.contentTypeTextCSV ;
        
        if ( str.equalsIgnoreCase(contentOutputTSV) )
            return WebContent.contentTypeTextTSV ;
        
        return str ;
    }
    
    private static String paramCallback(HttpServletRequest request) { return fetchParam(request, HttpNames.paramCallback) ; }
    
    private static String fetchParam(HttpServletRequest request, String parameterName)
    {
        String value = request.getParameter(parameterName) ;
        if ( value != null )
        {
            value = value.trim() ;
            if ( value.length() == 0 )
                value = null ;
        }
        return value ;
    }

}
