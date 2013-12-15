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

import java.io.IOException ;
import java.io.InputStream ;
import java.util.zip.GZIPInputStream ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

public class Upload {
    public static void incomingData(HttpAction action, StreamRDF dest, boolean isGraph) {
        ContentType ct = FusekiLib.getContentType(action) ;
         
        if ( WebContent.contentTypeMultiFormData.equalsIgnoreCase(ct.getContentType()) ) {
            fileUploadWorker(action, dest, isGraph) ;
            return ;
        }
        // Single graph (or quads) in body.
        
        String base = ActionLib.wholeRequestURL(action.request) ; // XXX Actually wrong?!
        Lang lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
        if ( lang == null ) {
            ServletOps.errorBadRequest("Unknown content type for triples: " + ct) ;
            return ;
        }
        InputStream input = null ;
        try { input = action.request.getInputStream() ; } 
        catch (IOException ex) { IO.exception(ex) ; }
    
        int len = action.request.getContentLength() ;
        if ( action.verbose ) {
            if ( len >= 0 )
                action.log.info(format("[%d]   Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s", action.id, len,
                                ct.getContentType(), ct.getCharset(), lang.getName())) ;
            else
                action.log.info(format("[%d]   Body: Content-Type=%s, Charset=%s => %s", action.id, ct.getContentType(),
                                ct.getCharset(), lang.getName())) ;
        }
    
        ActionSPARQL.parse(action, dest, input, lang, base) ;
    }
    
    /**  Process an HTTP upload of RDF files (triples or quads)
     *   Stream straight into a graph or dataset -- unlike SPARQL_Upload the destination
     *   is known at the start of the multipart file body
     */
    
    public static void fileUploadWorker(HttpAction action, StreamRDF dest, boolean isGraph) {
        String base = ActionLib.wholeRequestURL(action.request) ;
        String item = (isGraph)?"triple":"quad" ;
        ServletFileUpload upload = new ServletFileUpload();
        long count = -1 ;
        
        //log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName)) ;
        
        try {
            FileItemIterator iter = upload.getItemIterator(action.request);
            while (iter.hasNext()) {
                FileItemStream fileStream = iter.next();
                if (fileStream.isFormField())
                    ServletOps.errorBadRequest("Only files accept in multipart file upload") ;
                //Ignore the field name.
                //String fieldName = fileStream.getFieldName();
    
                InputStream stream = fileStream.openStream();
                // Process the input stream
                String contentTypeHeader = fileStream.getContentType() ;
                ContentType ct = ContentType.create(contentTypeHeader) ;
                Lang lang = null ;
                if ( ! WebContent.contentTypeTextPlain.equals(ct.getContentType()) )
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
                
                StreamRDFCounting countingDest =  StreamRDFLib.count(dest) ;
                try {
                    ActionSPARQL.parse(action, countingDest, stream, lang, base);
                    long c = countingDest.count() ;
                    
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %d %s%s", 
                                           action.id, printfilename,  ct.getContentType(), ct.getCharset(), lang.getName(),
                                           c, item, (c==1)?"":"s")) ;
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
    }
}

