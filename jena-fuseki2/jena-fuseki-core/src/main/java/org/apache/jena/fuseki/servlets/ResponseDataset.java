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

import static org.apache.jena.riot.WebContent.charsetUTF8;
import static org.apache.jena.riot.WebContent.contentTypeJSONLD;
import static org.apache.jena.riot.WebContent.contentTypeNTriples;
import static org.apache.jena.riot.WebContent.contentTypeRDFJSON;
import static org.apache.jena.riot.WebContent.contentTypeRDFXML;
import static org.apache.jena.riot.WebContent.contentTypeTurtle;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.conneg.ConNeg;
import org.apache.jena.fuseki.conneg.WebLib;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;

public class ResponseDataset
{
    // Short names for "output="
    private static final String contentOutputTriG          = "trig" ;
    private static final String contentOutputNQuads        = "n-quads" ;


    public static Map<String,String> shortNamesModel = new HashMap<String, String>() ;
    static {

        // Some short names.  keys are lowercase.
        
        ResponseOps.put(shortNamesModel, contentOutputNQuads,  WebContent.contentTypeNQuads) ;
        ResponseOps.put(shortNamesModel, contentOutputTriG,     WebContent.contentTypeTriG) ;
    }

    public static void doResponseDataset(HttpAction action, Dataset dataset) 
    {
        HttpServletRequest request = action.request ;
        HttpServletResponse response = action.response ;
        
        String mimeType = null ;        // Header request type 

        // TODO Use MediaType throughout.
        MediaType i = ConNeg.chooseContentType(request, DEF.quadsOffer, DEF.acceptNQuads) ;
        if ( i != null )
            mimeType = i.getContentType() ;

        String outputField = ResponseOps.paramOutput(request, shortNamesModel) ;
        if ( outputField != null )
            mimeType = outputField ;

        String writerMimeType = mimeType ;

        if ( mimeType == null )
        {
            Fuseki.actionLog.warn("Can't find MIME type for response") ;
            String x = WebLib.getAccept(request) ;
            String msg ;
            if ( x == null )
                msg = "No Accept: header" ;
            else
                msg = "Accept: "+x+" : Not understood" ;
            ServletOps.error(HttpSC.NOT_ACCEPTABLE_406, msg) ;
        }

        String contentType = mimeType ;
        String charset =     charsetUTF8 ;

        String forceAccept = ResponseOps.paramForceAccept(request) ;
        if ( forceAccept != null )
        {
            contentType = forceAccept ;
            charset = charsetUTF8 ;
        }

        Lang lang = RDFLanguages.contentTypeToLang(contentType) ;
        if ( lang == null )
            ServletOps.errorBadRequest("Can't determine output content type: "+contentType) ;
        
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
            ResponseResultSet.setHttpResponse(action, contentType, charset) ; 
            response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = response.getOutputStream() ;
            RDFDataMgr.write(out, dataset, lang) ;
            out.flush() ;
        }
        catch (Exception ex) { 
            action.log.info("Exception while writing the response model: "+ex.getMessage(), ex) ;
            ServletOps.errorOccurred("Exception while writing the response model: "+ex.getMessage(), ex) ;
        }
    }
}

