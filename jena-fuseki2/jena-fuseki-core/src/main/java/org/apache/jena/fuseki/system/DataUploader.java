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

import static java.lang.String.format;
import static org.apache.jena.riot.WebContent.ctMultipartFormData;
import static org.apache.jena.riot.WebContent.ctMultipartMixed;
import static org.apache.jena.riot.WebContent.ctTextPlain;
import static org.apache.jena.riot.WebContent.matchContentType;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

/**
 * Functions to load data to a StreamRDF.
 * <p>
 * This is used for {@link GSP_RW Graph Store Protocol PUT/POST},
 * including the extension to dataset (quads) loads
 * and also {@link UploadRDF} the direct upload service.
 * @see UploadRDF
 */
public class DataUploader {

    /**
     * Parse the body contents to the {@link StreamRDF}.
     * <p>
     * This function is used by {@link GSP_RW} and {@link UploadRDF}.
     * <p>
     * This function assumes it is inside a transaction.
     * <p>
     * Supports Content-Type, with data in the body, as well as
     * {@code multipart/mixed} (<a href="https://tools.ietf.org/html/rfc204">RFC 2046</a>) and
     * {@code multipart/form-data} (<a href="https://tools.ietf.org/html/rfc1867">RFC 1867</a>)
     * with a file part.
     * @throws RiotParseException
     */
    public static UploadDetails incomingData(HttpAction action, StreamRDF dest) {
        ContentType ct = ActionLib.getContentType(action);

        if ( ct == null ) {
            ServletOps.errorBadRequest("No content type");
            return null;
        }

        if ( matchContentType(ctMultipartFormData, ct) || matchContentType(ctMultipartMixed, ct) ) {
            // multipart
            return fileUploadMultipart(action, dest);
        }

        // Single graph or quads in body.
        String base = ActionLib.wholeRequestURL(action.getRequest());
        Lang lang = RDFLanguages.contentTypeToLang(ct.getContentTypeStr());
        if ( lang == null ) {
            ServletOps.errorBadRequest("Unknown content type for triples: " + ct);
            return null;
        }
        long len = action.getRequestContentLengthLong();

        StreamRDFCounting countingDest = StreamRDFLib.count(dest);
        try {
            ActionLib.parse(action, countingDest, lang, base);
            UploadDetails details = new UploadDetails(countingDest.count(), countingDest.countTriples(),countingDest.countQuads());
            action.log.info(format("[%d] Body: Content-Length=%d, Content-Type=%s, Charset=%s => %s : %s",
                                   action.id, len, ct.getContentTypeStr(), ct.getCharset(), lang.getName(),
                                   details.detailsStr()));
            return details;
        } catch (RiotParseException ex) {
            action.log.info(format("[%d] Attempt to load: Content-Length=%d, Content-Type=%s, Charset=%s => %s",
                                   action.id, len, ct.getContentTypeStr(), ct.getCharset(), ex.getMessage()));
            // Exhaust input.
            ActionLib.consumeBody(action);
            throw ex;
        }
    }

    /**
     * Process an HTTP upload of RDF files (triples or quads) with content type
     * "multipart/form-data" or "multipart/mixed".
     * <p>
     * Form data (content-disposition: form-data; name="...") is rejected.
     * <p>
     * Data is streamed straight into the destination graph or dataset.
     * <p>
     * This function assumes it is inside a transaction.
     */
    private static UploadDetails fileUploadMultipart(HttpAction action, StreamRDF dest) {
        String base = ActionLib.wholeRequestURL(action.getRequest());
        ServletFileUpload upload = new ServletFileUpload();
        StreamRDFCounting countingDest =  StreamRDFLib.count(dest);

        try {
            FileItemIterator iter = upload.getItemIterator(action.getRequest());
            while (iter.hasNext()) {
                FileItemStream fileStream = iter.next();
                if (fileStream.isFormField()) {
                    // Form field - this code only supports multipart file upload.
                    String fieldName = fileStream.getFieldName();
                    InputStream stream = fileStream.openStream();
                    String value = Streams.asString(stream, "UTF-8");
                    // This code is currently used to put multiple files into a single destination.
                    // Additional field/values do not make sense.
                    ServletOps.errorBadRequest(format("Only files accepted in multipart file upload (got %s=%s)", fieldName, value));
                    // errorBadRequest does not return.
                    return null;
                }

                InputStream input = fileStream.openStream();
                // Content-Type:
                String contentTypeHeader = fileStream.getContentType();
                ContentType ct = ContentType.create(contentTypeHeader);

                Lang lang = null;
                if ( ! matchContentType(ctTextPlain, ct) )
                    lang = RDFLanguages.contentTypeToLang(ct.getContentTypeStr());

                if ( lang == null ) {
                    // Not a recognized Content-Type. Look at file extension.
                    String name = fileStream.getName();
                    if ( name == null || name.equals("") )
                        ServletOps.errorBadRequest("No name for content - can't determine RDF syntax");
                    lang = RDFLanguages.pathnameToLang(name);
                    if (name.endsWith(".gz"))
                        input = new GZIPInputStream(input);
                }
                if ( lang == null )
                    // Desperate.
                    lang = RDFLanguages.RDFXML;

                String printfilename = fileStream.getName();
                if ( printfilename == null  || printfilename.equals("") )
                    printfilename = "<none>";

                // count just this step
                StreamRDFCounting countingDest2 =  StreamRDFLib.count(countingDest);
                try {
                    ActionLib.parse(action, countingDest2, input, lang, base);
                    UploadDetails details1 = new UploadDetails(countingDest2.count(), countingDest2.countTriples(),countingDest2.countQuads());
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %s",
                                           action.id, printfilename, ct.getContentTypeStr(), ct.getCharset(), lang.getName(),
                                           details1.detailsStr()));
                } catch (RiotParseException ex) {
                    action.log.info(format("[%d] Filename: %s, Content-Type=%s, Charset=%s => %s : %s",
                                           action.id, printfilename, ct.getContentTypeStr(), ct.getCharset(), lang.getName(),
                                           ex.getMessage()));
                    ActionLib.consumeBody(action);
                    throw ex;
                }
            }
        }
        catch (ActionErrorException ex) { throw ex; }
        catch (Exception ex)            { ServletOps.errorOccurred(ex.getMessage()); }
        // Overall results.
        UploadDetails details = new UploadDetails(countingDest.count(), countingDest.countTriples(),countingDest.countQuads());
        return details;
    }
}

