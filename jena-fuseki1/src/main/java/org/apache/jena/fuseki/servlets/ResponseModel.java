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

import static org.apache.jena.fuseki.servlets.ServletBase.error ;
import static org.apache.jena.fuseki.servlets.ServletBase.errorBadRequest ;
import static org.apache.jena.fuseki.servlets.ServletBase.errorOccurred ;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.conneg.WebLib ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.rdf.model.Model ;

public class ResponseModel
{
    private static Logger slog = ServletBase.log ;

    // Short names for "output="
    private static final String contentOutputJSONLD        = "json-ld" ;
    private static final String contentOutputJSONRDF       = "json-rdf" ;
    private static final String contentOutputJSON          = "json" ;
    private static final String contentOutputXML           = "xml" ;
    private static final String contentOutputText          = "text" ;
    private static final String contentOutputTTL           = "ttl" ;
    private static final String contentOutputNT            = "nt" ;

    public static Map<String,String> shortNamesModel = new HashMap<String, String>() ;
    static {

        // Some short names.  keys are lowercase.
        ResponseOps.put(shortNamesModel, contentOutputJSONLD, WebContent.contentTypeJSONLD) ;
        ResponseOps.put(shortNamesModel, contentOutputJSONRDF, WebContent.contentTypeRDFJSON) ;
        ResponseOps.put(shortNamesModel, contentOutputJSON, WebContent.contentTypeJSONLD) ;
        ResponseOps.put(shortNamesModel, contentOutputXML,  WebContent.contentTypeRDFXML) ;
        ResponseOps.put(shortNamesModel, contentOutputText, WebContent.contentTypeTurtle) ;
        ResponseOps.put(shortNamesModel, contentOutputTTL,  WebContent.contentTypeTurtle) ;
        ResponseOps.put(shortNamesModel, contentOutputNT,   WebContent.contentTypeNTriples) ;
    }

    public static void doResponseModel(HttpAction action, Model model) 
    {
        HttpServletRequest request = action.request ;
        HttpServletResponse response = action.response ;
        
        String mimeType = null ;        // Header request type 

        // TODO Use MediaType throughout.
        MediaType i = ConNeg.chooseContentType(request, DEF.rdfOffer, DEF.acceptRDFXML) ;
        if ( i != null )
            mimeType = i.getContentType() ;

        String outputField = ResponseOps.paramOutput(request, shortNamesModel) ;
        if ( outputField != null )
            mimeType = outputField ;

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
            error(HttpSC.NOT_ACCEPTABLE_406, msg) ;
        }

        String contentType = mimeType ;
        String charset =     WebContent.charsetUTF8 ;

        String forceAccept = ResponseOps.paramForceAccept(request) ;
        if ( forceAccept != null )
        {
            contentType = forceAccept ;
            charset = WebContent.charsetUTF8 ;
        }

        Lang lang = RDFLanguages.contentTypeToLang(contentType) ;
        if ( lang == null )
            errorBadRequest("Can't determine output content type: "+contentType) ;
        
//        if ( rdfw instanceof RDFXMLWriterI )
//            rdfw.setProperty("showXmlDeclaration", "true") ;

    //        // Write locally to check it's possible.
    //        // Time/space tradeoff.
    //        try {
    //            OutputStream out = new NullOutputStream() ;
    //            RDFDataMgr.write(out, model, lang) ;
    //            IO.flush(out) ;
    //        } catch (JenaException ex)
    //        {
    //            SPARQL_ServletBase.errorOccurred(ex) ;
    //        }

        try {
            ResponseResultSet.setHttpResponse(request, response, contentType, charset) ; 
            response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = response.getOutputStream() ;
            RDFDataMgr.write(out, model, lang) ;
            out.flush() ;
        }
        catch (Exception ex) { 
            slog.info("Exception while writing the response model: "+ex.getMessage(), ex) ;
            errorOccurred("Exception while writing the response model: "+ex.getMessage(), ex) ;
        }
    }
}

