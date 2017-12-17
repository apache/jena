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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;
import static org.apache.jena.riot.WebContent.ctMultipartFormData ;
import static org.apache.jena.riot.WebContent.ctTextPlain ;
import static org.apache.jena.riot.WebContent.matchContentType ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.zip.GZIPInputStream ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.commons.fileupload.util.Streams ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotParseException ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

public class Upload {
    public static UploadDetails incomingData(HttpAction action, StreamRDF dest) {
        ContentType ct = FusekiLib.getContentType(action) ;
        
        if ( ct == null ) {
            ServletOps.errorBadRequest("No content type") ;
            return null ;
        }
         
        if ( matchContentType(ctMultipartFormData, ct) ) {
            return fileUploadWorker(action, dest) ;
        }
        // Single graph (or quads) in body.
        
        String base = ActionLib.wholeRequestURL(action.request) ;
        Lang lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
        if ( lang == null ) {
            ServletOps.errorBadRequest("Unknown content type for triples: " + ct) ;
            return null ;
        }
        InputStream input = null ;
        try { input = action.request.getInputStream() ; } 
        catch (IOException ex) { IO.exception(ex) ; }
    
        long len = action.request.getContentLengthLong();

        StreamRDFCounting countingDest = StreamRDFLib.count(dest) ;
        try {
            ActionLib.parse(action, countingDest, input, lang, base) ;
            UploadDetails details = new UploadDetails(countingDest.count(), countingDest.countTriples(),countingDest.countQuads()) ;
            action.log.info(format("[%d] Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s : %s", 
                                   action.id, len, ct.getContentType(), ct.getCharset(), lang.getName(),
                                   details.detailsStr())) ;
            return details ;
        } catch (RiotParseException ex) {
            action.log.info(format("[%d] Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s : %s",
                                   action.id, len, ct.getContentType(), ct.getCharset(), lang.getName(),
                                   ex.getMessage())) ;
            throw ex ;
        }
    }
    
    /**  Process an HTTP upload of RDF files (triples or quads)
     *   Stream straight into a graph or dataset -- unlike SPARQL_Upload the destination
     *   is known at the start of the multipart file body
     */
    
    public static UploadDetails fileUploadWorker(HttpAction action, StreamRDF dest) {
        String base = ActionLib.wholeRequestURL(action.request) ;
        ServletFileUpload upload = new ServletFileUpload();
        //log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName)) ;
        
        // Overall counting.
        StreamRDFCounting countingDest =  StreamRDFLib.count(dest) ;
        
        try {
            FileItemIterator iter = upload.getItemIterator(action.request);
            while (iter.hasNext()) {
                FileItemStream fileStream = iter.next();
                if (fileStream.isFormField()) {
                    // Ignore?
                    String fieldName = fileStream.getFieldName() ;
                    InputStream stream = fileStream.openStream();
                    String value = Streams.asString(stream, "UTF-8") ;
                    ServletOps.errorBadRequest(format("Only files accepted in multipart file upload (got %s=%s)",fieldName, value)) ;
                }
                //Ignore the field name.
                //String fieldName = fileStream.getFieldName();
    
                InputStream stream = fileStream.openStream();
                // Process the input stream
                String contentTypeHeader = fileStream.getContentType() ;
                ContentType ct = ContentType.create(contentTypeHeader) ;
                Lang lang = null ;
                if ( ! matchContentType(ctTextPlain, ct) )
                    lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
    
                if ( lang == null ) {
                    String name = fileStream.getName() ; 
                    if ( name == null || name.equals("") ) 
                        ServletOps.errorBadRequest("No name for content - can't determine RDF syntax") ;
                    lang = RDFLanguages.filenameToLang(name) ;
                    if (name.endsWith(".gz"))
                        stream = new GZIPInputStream(stream);
                }
                if ( lang == null )
                    // Desperate.
                    lang = RDFLanguages.RDFXML ;
    
                String printfilename = fileStream.getName() ; 
                if ( printfilename == null  || printfilename.equals("") )
                    printfilename = "<none>" ; 
    
                // Before
                // action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s", 
                //                        action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName())) ;
                
                // count just this step
                StreamRDFCounting countingDest2 =  StreamRDFLib.count(countingDest) ;
                try {
                    ActionLib.parse(action, countingDest2, stream, lang, base);
                    UploadDetails details1 = new UploadDetails(countingDest2.count(), countingDest2.countTriples(),countingDest2.countQuads()) ;
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %s", 
                                           action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName(),
                                           details1.detailsStr())) ;
                } catch (RiotParseException ex) {
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %s",
                                           action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName(),
                                           ex.getMessage())) ;
                    throw ex ;
                }
            }
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)            { ServletOps.errorOccurred(ex.getMessage()) ; }
        // Overall results.
        UploadDetails details = new UploadDetails(countingDest.count(), countingDest.countTriples(),countingDest.countQuads()) ;
        return details ;
    }
}

