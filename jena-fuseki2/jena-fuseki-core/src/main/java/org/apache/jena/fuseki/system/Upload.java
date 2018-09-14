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

package org.apache.jena.fuseki.system;

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
import org.apache.jena.fuseki.servlets.ActionErrorException;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.iri.IRI;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotParseException ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class Upload {
    
    /** Parse the body contents to the {@link StreamRDF}. 
     *  This function is used by GSP.
     */ 
    public static UploadDetails incomingData(HttpAction action, StreamRDF dest) {
        ContentType ct = ActionLib.getContentType(action) ;
        
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
    
    /**  
     * Process an HTTP upload of RDF files (triples or quads)
     * Stream straight into the destination graph or dataset, ignoring any
     * headers in the form parts. This function is used by GSP.
     */
    
    public static UploadDetails fileUploadWorker(HttpAction action, StreamRDF dest) {
        String base = ActionLib.wholeRequestURL(action.request) ;
        ServletFileUpload upload = new ServletFileUpload();
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
                    // This code is currently used to put multiple files into a single destination.
                    // Additonal field/values do not make sense.
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
    
    // XXX Merge/replace with Upload.fileUploadWorker
    // This code sets the StreamRDF destination during processing and can handle multiple
    // files to multiple destinations for example, graphs within a dataset.
    // The only functional difference is the single destination (fileUploadWorker) and
    // switching on graph name (multipartUploadWorker).
    
    /** 
     * Process an HTTP file upload of RDF using the name field for the graph name destination.
     * This function is used by SPARQL_Upload for {@code fuseki:serviceUpload}.
     */
    public static UploadDetailsWithName multipartUploadWorker(HttpAction action, String base) {
        DatasetGraph dsgTmp = DatasetGraphFactory.create() ;
        ServletFileUpload upload = new ServletFileUpload() ;
        String graphName = null ;
        boolean isQuads = false ;
        long count = -1 ;

        String name = null ;
        ContentType ct = null ;
        Lang lang = null ;

        try {
            FileItemIterator iter = upload.getItemIterator(action.request) ;
            while (iter.hasNext()) {
                FileItemStream item = iter.next() ;
                String fieldName = item.getFieldName() ;
                InputStream stream = item.openStream() ;
                if ( item.isFormField() ) {
                    // Graph name.
                    String value = Streams.asString(stream, "UTF-8") ;
                    if ( fieldName.equals(HttpNames.paramGraph) ) {
                        graphName = value ;
                        if ( graphName != null && !graphName.equals("") && !graphName.equals(HttpNames.valueDefault) ) {
                            // -- Check IRI with additional checks.
                            IRI iri = IRIResolver.parseIRI(value) ;
                            if ( iri.hasViolation(false) )
                                ServletOps.errorBadRequest("Bad IRI: " + graphName) ;
                            if ( iri.getScheme() == null )
                                ServletOps.errorBadRequest("Bad IRI: no IRI scheme name: " + graphName) ;
                            if ( iri.getScheme().equalsIgnoreCase("http") || iri.getScheme().equalsIgnoreCase("https") ) {
                                // Redundant??
                                if ( iri.getRawHost() == null )
                                    ServletOps.errorBadRequest("Bad IRI: no host name: " + graphName) ;
                                if ( iri.getRawPath() == null || iri.getRawPath().length() == 0 )
                                    ServletOps.errorBadRequest("Bad IRI: no path: " + graphName) ;
                                if ( iri.getRawPath().charAt(0) != '/' )
                                    ServletOps.errorBadRequest("Bad IRI: Path does not start '/': " + graphName) ;
                            }
                            // End check IRI
                        }
                    } else if ( fieldName.equals(HttpNames.paramDefaultGraphURI) )
                        graphName = null ;
                    else
                        // Add file type?
                        action.log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName)) ;
                } else {
                    // Process the input stream
                    name = item.getName() ;
                    if ( name == null || name.equals("") || name.equals("UNSET FILE NAME") )
                        ServletOps.errorBadRequest("No name for content - can't determine RDF syntax") ;

                    String contentTypeHeader = item.getContentType() ;
                    ct = ContentType.create(contentTypeHeader) ;

                    lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
                    if ( lang == null ) {
                        lang = RDFLanguages.filenameToLang(name) ;

                        // JENA-600 filenameToLang() strips off certain
                        // extensions such as .gz and
                        // we need to ensure that if there was a .gz extension
                        // present we wrap the stream accordingly
                        if ( name.endsWith(".gz") )
                            stream = new GZIPInputStream(stream) ;
                    }

                    if ( lang == null )
                        // Desperate.
                        lang = RDFLanguages.RDFXML ;

                    isQuads = RDFLanguages.isQuads(lang) ;

                    action.log.info(format("[%d] Upload: Filename: %s, Content-Type=%s, Charset=%s => %s", action.id, name,
                                           ct.getContentType(), ct.getCharset(), lang.getName())) ;

                    StreamRDF x = StreamRDFLib.dataset(dsgTmp) ;
                    StreamRDFCounting dest = StreamRDFLib.count(x) ;
                    ActionLib.parse(action, dest, stream, lang, base) ;
                    count = dest.count() ;
                }
            }

            if ( graphName == null || graphName.equals("") )
                graphName = HttpNames.valueDefault ;
            if ( isQuads )
                graphName = null ;
            return new UploadDetailsWithName(graphName, dsgTmp, count) ;
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)            { ServletOps.errorOccurred(ex) ; return null ; }
    }
}

