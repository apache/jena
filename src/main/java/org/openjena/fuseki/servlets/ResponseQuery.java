/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import java.io.IOException ;
import java.io.OutputStream ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.io.IO ;
import org.openjena.fuseki.DEF ;
import org.openjena.fuseki.FusekiException ;
import org.openjena.fuseki.FusekiLib ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.conneg.AcceptList ;
import org.openjena.fuseki.conneg.ConNeg ;
import org.openjena.fuseki.conneg.MediaType ;
import org.openjena.fuseki.conneg.TypedInputStream ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.xmloutput.RDFXMLWriterI ;

/** This is the content negotiation for each kind of SPARQL query result */ 
public class ResponseQuery
{
    private static Logger log = LoggerFactory.getLogger(ResponseQuery.class) ;
    
    interface OutputContent { void output(ServletOutputStream out) ; }

    static AcceptList prefContentTypeResultSet     = DEF.rsOffer ; 
    static AcceptList prefContentTypeRDF           = DEF.rdfOffer ;

//    static final String paramStyleSheet     = "stylesheet" ;
//    static final String paramAccept         = "accept" ;
//    static final String paramOutput1        = "output" ;        // See Yahoo! developer: http://developer.yahoo.net/common/json.html 
//    static final String paramOutput2        = "format" ;        // Alternative name 
//    static final String paramCallback       = "callback" ;
//    static final String paramForceAccept    = "force-accept" ;  // Force the accept header at the last moment 
//    static final String headerAccept        = "Accept" ;
    
    public static void doResponseModel(Model model, HttpServletRequest request, HttpServletResponse response)
    {
        String mimeType = null ;        // Header request type 
        
        // TODO Use MediaType throughout.
        MediaType i = ConNeg.chooseContentType(request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;
        
        String writerMimeType = mimeType ;
        
        if ( mimeType == null )
            // LOG ME
            SPARQL_ServletBase.error(HttpSC.NOT_ACCEPTABLE_406, "") ;
        
        TypedInputStream ts = new TypedInputStream(null, mimeType, WebContent.charsetUTF8) ;
        Lang lang = FusekiLib.langFromContentType(ts.getMediaType()) ; 
        RDFWriter rdfw = FusekiLib.chooseWriter(lang) ;
             
        if ( rdfw instanceof RDFXMLWriterI )
        {
            rdfw.setProperty("showXmlDeclaration", "true") ;
//            if ( rdfw instanceof Abbreviated )
//                // Workaround for the j.cook.up bug.
//                rdfw.setProperty("blockRules", "propertyAttr") ;
        }
        
        // TODO Allow a mode of write to buffer (memory, disk), write buffer later.
        // Time/space tradeoff.
        try {
            OutputStream out = new NullOutputStream() ;
            rdfw.write(model, out, null) ;
            IO.flush(out) ;
        } catch (JenaException ex)
        {
            SPARQL_ServletBase.errorOccurred(ex) ;
        }
        
        // Managed to write it locally
        
        System.err.println("SET HEADERS") ;
        // Set headers
        
        try {
            rdfw.write(model, response.getOutputStream(), null) ;
            response.getOutputStream().flush() ;
        }
        catch (IOException ex) { SPARQL_ServletBase.errorOccurred(ex) ; }
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
        
        String mimeType = null ; 
        MediaType i = ConNeg.chooseContentType(request, DEF.rsOffer, DEF.acceptRSXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;
        
        // TODO Stylesheet.
        
        // Does &output= override?
        // Requested output type by the web form or &output= in the request.
        // Overrides content negotiation. 
        String outputField = paramOutput(request) ;    // Expands short names
        
        String serializationType = mimeType ;           // Chosoe the serializer based on this.
        String contentType = mimeType ;                 // Set the HTTP respose header to this.
             
        String x = paramForceAccept(request) ;
        
        // Force text.
        
//        if ( outputField != null ) 
//        {
//            serializationType = 
//            if ( outputField.equals("json") || outputField.equals(Joseki.contentTypeResultsJSON) )
//            {
//                serializationType = Joseki.contentTypeResultsJSON ;
//                contentType = Joseki.contentTypeResultsJSON ;
//            }
//            if ( outputField.equals("xml") || outputField.equals(Joseki.contentTypeResultsXML) )
//            {
//                serializationType = Joseki.contentTypeResultsXML ;
//                contentType = Joseki.contentTypeResultsXML ;
//            }
//            if ( outputField.equals("text") || outputField.equals(Joseki.contentTypeTextPlain) )
//            {
//                serializationType = Joseki.contentTypeTextPlain ;
//                contentType = Joseki.contentTypeTextPlain ;
//            }
//            
//            if ( outputField.equals("csv") || outputField.equals(Joseki.contentTypeTextCSV) )
//            {
//                serializationType = Joseki.contentTypeTextCSV ;
//                contentType = Joseki.contentTypeTextCSV ;
//            }
//
//            if ( outputField.equals("tsv") || outputField.equals(Joseki.contentTypeTextTSV) )
//            {
//                serializationType = Joseki.contentTypeTextTSV ;
//                contentType = Joseki.contentTypeTextTSV ;
//            }
//
//        }

        // ---- Step 4: Style sheet - change to application/xml.
        final String stylesheetURL = paramStylesheet(request) ;
        if ( stylesheetURL != null && serializationType.equals(WebContent.contentTypeResultsXML))
            contentType = WebContent.contentTypeXML ;
        
        
        // ---- Step 5: text/plain?
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
                    public void output(ServletOutputStream out)
                    {
                        if ( resultSet != null )
                            ResultSetFormatter.outputAsXML(out, resultSet, stylesheetURL) ;
                        if ( booleanResult != null )
                            ResultSetFormatter.outputAsXML(out, booleanResult.booleanValue(), stylesheetURL) ;
                    }
                }, request, response) ;
            }
//            catch (IOException ioEx)
//            {
//                if ( isEOFexception(ioEx) )
//                    log.warn("IOException[(SELECT/XML)] (ignored) "+ioEx, ioEx) ;
//                else
//                    log.debug("IOException[(SELECT/XML)] (ignored) "+ioEx, ioEx) ;
//            }
            // This catches things like NIO exceptions.
            catch (Exception ex) { log.debug("Exception [SELECT/XML]"+ex, ex) ; } 
            return ;
        }

        // ---- Form: JSON
        if ( serializationType.equals(WebContent.contentTypeResultsJSON) )
        {
            try {
                jsonOutput(contentType, new OutputContent(){
                    public void output(ServletOutputStream out)
                    {
                        if ( resultSet != null )
                            ResultSetFormatter.outputAsJSON(out, resultSet) ;
                        if (  booleanResult != null )
                            ResultSetFormatter.outputAsJSON(out, booleanResult.booleanValue()) ;
                    }
                }, request, response) ;
            }
//            catch (IOException ioEx)
//            {
//                if ( isEOFexception(ioEx) )
//
//                    log.warn("IOException[SELECT/JSON] (ignored) "+ioEx, ioEx) ;
//                else
//                    log.debug("IOException [SELECT/JSON] (ignored) "+ioEx, ioEx) ;
//            }
            // This catches things like NIO exceptions.
            catch (Exception ex) { log.debug("Exception [SELECT/JSON] "+ex, ex) ; } 
            return ;
        }

        // ---- Form: text
        if ( serializationType.equals(WebContent.contentTypeTextPlain) )
        {
            try {
                textOutput(contentType, new OutputContent(){
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
           if ( charset != null )
               contentType = contentType+"; charset="+charset ;
           log.trace("Content-Type for response: "+contentType) ;
           httpResponse.setContentType(contentType) ;
       }
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
        String acceptField = request.getHeader(HttpNames.hAccept) ;
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
    public static final String contentOutputCSV           = "text" ;
    
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
        if ( str.equalsIgnoreCase(contentOutputCSV) )
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */