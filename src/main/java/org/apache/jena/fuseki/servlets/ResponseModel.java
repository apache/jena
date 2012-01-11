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

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.conneg.MediaType ;
import org.apache.jena.fuseki.conneg.TypedInputStream ;
import org.apache.jena.fuseki.conneg.WebLib ;
import org.apache.jena.fuseki.http.HttpSC ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.xmloutput.RDFXMLWriterI ;

public class ResponseModel
{
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
                ResponseResultSet.setHttpResponse(request, response, ts.getMediaType(), ts.getCharset()) ; 
                response.setStatus(HttpSC.OK_200) ;
                rdfw.write(model, response.getOutputStream(), null) ;
                response.getOutputStream().flush() ;
            }
            catch (Exception ex) { SPARQL_ServletBase.errorOccurred(ex) ; }
        }

}

