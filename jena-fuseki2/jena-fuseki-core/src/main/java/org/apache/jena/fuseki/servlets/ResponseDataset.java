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

import static org.apache.jena.riot.WebContent.* ;

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
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.web.HttpSC;

public class ResponseDataset
{
    // Short names for "output="
    private static final String contentOutputJSONLD        = "json-ld" ;
    private static final String contentOutputJSONRDF       = "json-rdf" ;
    private static final String contentOutputJSON          = "json" ;
    private static final String contentOutputXML           = "xml" ;
    private static final String contentOutputText          = "text" ;
    private static final String contentOutputTTL           = "ttl" ;
    private static final String contentOutputNT            = "nt" ;
    private static final String contentOutputTriG          = "trig" ;
    private static final String contentOutputNQuads        = "n-quads" ;

    public static Map<String,String> shortNamesModel = new HashMap<>() ;
    static {
        // Some short names.  keys are lowercase.
        ResponseOps.put(shortNamesModel, contentOutputJSONLD,   contentTypeJSONLD) ;
        ResponseOps.put(shortNamesModel, contentOutputJSONRDF,  contentTypeRDFJSON) ;
        ResponseOps.put(shortNamesModel, contentOutputJSON,     contentTypeJSONLD) ;
        ResponseOps.put(shortNamesModel, contentOutputXML,      contentTypeRDFXML) ;
        ResponseOps.put(shortNamesModel, contentOutputText,     contentTypeTurtle) ;
        ResponseOps.put(shortNamesModel, contentOutputTTL,      contentTypeTurtle) ;
        ResponseOps.put(shortNamesModel, contentOutputNT,       contentTypeNTriples) ;
        ResponseOps.put(shortNamesModel, contentOutputNQuads,  WebContent.contentTypeNQuads) ;
        ResponseOps.put(shortNamesModel, contentOutputTriG,     WebContent.contentTypeTriG) ;
    }

    public static void doResponseModel(HttpAction action, Model model) {
        Dataset ds = DatasetFactory.create(model) ;
        ResponseDataset.doResponseDataset(action, ds);
    }

    public static void doResponseDataset(HttpAction action, Dataset dataset) {
        HttpServletRequest request = action.request ;
        HttpServletResponse response = action.response ;

        String mimeType = null ;        // Header request type

        MediaType i = ConNeg.chooseContentType(request, DEF.constructOffer, DEF.acceptTurtle) ;
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

        try {
            ResponseOps.setHttpResponse(action, contentType, charset) ;
            response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = response.getOutputStream() ;
            try {
                if ( RDFLanguages.isQuads(lang) )
                    RDFDataMgr.write(out, dataset, lang) ;
                else
                    RDFDataMgr.write(out, dataset.getDefaultModel(), lang) ;
                out.flush() ;
            } catch (JenaException ex) { 
                // Some RDF/XML data is unwritable. All we can do is pretend it's a bad
                // request (inappropriate content type).
                if ( lang.equals(Lang.RDFXML) )
                    ServletOps.errorBadRequest("Failed to write output in RDF/XML: "+ex.getMessage()) ;
                else
                    ServletOps.errorOccurred("Failed to write output: "+ex.getMessage(), ex) ;
            }
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex) {
            action.log.info("Exception while writing the response model: "+ex.getMessage(), ex) ;
            ServletOps.errorOccurred("Exception while writing the response model: "+ex.getMessage(), ex) ;
        }
    }
}

