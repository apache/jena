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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.jena.atlas.io.IO;
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
            // Multipart
            return multipartCommonsFileUpload(action, dest);
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

    // HttpServletRequests.getParts.
    // This operation combines the multi-part parsing and the handling the files.
    // Out code wishes to stream straigh to the destination graph or dataset.
    // Each application server (Tomcat and Jetty) has special configuration.
    //
    // As of Jetty12, the config object is in the servlet holder and the code
    // has special handling for request.getAttribute("org.eclipse.jetty.multipartConfig");
    //
    // Fuseki is not a servlet!
    // Use Apache Commons FileUpload as the mulipart parser.

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
    private static UploadDetails multipartCommonsFileUpload(HttpAction action, StreamRDF dest) {
        String base = ActionLib.wholeRequestURL(action.getRequest());
        StreamRDFCounting countingDest =  StreamRDFLib.count(dest);

        // We only use the request body parsing capability of a ServletFileUpload.
        JakartaServletFileUpload<?,?> upload = new JakartaServletFileUpload<>();
        try {
            FileItemInputIterator iter = upload.getItemIterator(action.getRequest());
            while (iter.hasNext()) {
                FileItemInput part = iter.next();

                if (part.isFormField()) {
                    // Form field - this code only supports multipart file upload.
                    String fieldName = part.getFieldName();
                    InputStream stream = part.getInputStream();
                    String value = IO.readWholeFileAsUTF8(stream);
                    // This code is currently used to put multiple files into a single destination.
                    // Additional field/values do not make sense.
                    ServletOps.errorBadRequest(format("Only files accepted in multipart file upload (got %s=%s)", fieldName, value));
                    // errorBadRequest does not return.
                    return null;
                }

                InputStream input = part.getInputStream();
                String contentTypeHeader = part.getContentType();
                ContentType ct = ContentType.create(contentTypeHeader);
                String fieldName = part.getFieldName();         // Corresponds to ServletAPI Part.getName
                String submittedFileName = part.getName();      // Corresponds to ServletAPI Part.getSubmittedFileName

                handlePart(action, input, base, ct, submittedFileName, countingDest);
            }
        }
        catch (ActionErrorException ex) { throw ex; }
        catch (Exception ex)            { ServletOps.errorOccurred(ex.getMessage()); }
        // Overall results.
        UploadDetails details = new UploadDetails(countingDest.count(), countingDest.countTriples(),countingDest.countQuads());
        return details;
    }

    /**
     * Process one item in a multiple part file upload.
     * This does not depend on the choice of multi-part parser.
     */
    private static void handlePart(HttpAction action, InputStream input,
                                   String base, ContentType ct, String submittedFileName,
                                   StreamRDF dest) throws IOException {
        Lang lang = null;
        if ( ! matchContentType(ctTextPlain, ct) )
            lang = RDFLanguages.contentTypeToLang(ct.getContentTypeStr());

        if ( lang == null ) {
            // Not a recognized Content-Type. Look at file extension.
            if ( submittedFileName == null || submittedFileName.equals("") )
                ServletOps.errorBadRequest("No name for content - can't determine RDF syntax");
            lang = RDFLanguages.pathnameToLang(submittedFileName);
            if (submittedFileName.endsWith(".gz"))
                input = new GZIPInputStream(input);
        }
        if ( lang == null )
            // Desperate.
            lang = RDFLanguages.RDFXML;

        String printfilename = submittedFileName;
        if ( printfilename == null || printfilename.equals("") )
            printfilename = "<none>";

        // count just this step
        StreamRDFCounting countingDest2 =  StreamRDFLib.count(dest);
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

//    // ----
//    // ---- This code is kept temporarily as we switch back to using Apache Commons FileUpload.
//
//    //     Version that works using HttpServletrequets.getParts.
//    //     That means in Tomcat and Jetty, special configuration has to be done.
//    //
//    //     Tomcat: In the WAR file: META-INF/context.xml
//    //     <Context allowCasualMultipartParsing="true">
//    //     </Context>
//    //
//    //     At Jetty12, this no longer works.
//    //
//
// Apache Commons FileUploader is a portable solution
//
//    // @MultipartConfig but for ServletFilters
//    // * @MultipartConfig only works on servlets.
//    // * Jetty: attribute setting.
//    // * Tomcat: META-INF/context.xml setting
//
//    // Jetty requires a setting of this annotation object as a request attribute.
//    private static final MultipartConfigElement multipartConfigElement;
//    private static final String jettyMultipartAttributeName;
//
//    static {
//        String x;
//        MultipartConfigElement y;
//        // May not be on the classpath
//        try {
//            Class.forName("org.eclipse.jetty.server.handler.ContextRequest");
//            //Portable across ee versions
//            x = org.eclipse.jetty.ee10.servlet.ServletContextRequest.MULTIPART_CONFIG_ELEMENT;
//            //x = "org.eclipse.jetty.multipartConfig";
//            y = new MultipartConfigElement("");
//        }
//        catch (ClassNotFoundException th) { x = null; y = null; }
//        jettyMultipartAttributeName = x;
//        multipartConfigElement  = y;
//    }
//
//    /**
//     * Process an HTTP upload of RDF files (triples or quads) with content type
//     * "multipart/form-data" or "multipart/mixed".
//     * <p>
//     * Data is streamed straight into the destination graph or dataset.
//     * <p>
//     * This function assumes it is inside a transaction.
//     */
//    private static UploadDetails multipartJakartaParts(HttpAction action, StreamRDF dest) {
//        HttpServletRequest request = action.getRequest();
//        String base = ActionLib.wholeRequestURL(request);
//        StreamRDFCounting countingDest =  StreamRDFLib.count(dest);
//
//        // Jetty
//        if ( jettyMultipartAttributeName != null && request.getAttribute(jettyMultipartAttributeName) == null ) {
//            request.setAttribute(jettyMultipartAttributeName, multipartConfigElement);
//        }
//
//        // But the this goes to the Jetty Servlet holder, not the request attributes.
//        // Maybe there is a way to inject the object at build time.
//
//        {
//            // This call is made internally to Jetty.
//            // This redirects to the servletHolder specifically for jettyMultipartAttributeName
//            // ServletContextRequest line 273: of getAttribute:
//            // case ServletContextRequest.MULTIPART_CONFIG_ELEMENT -> _matchedResource.getResource().getServletHolder().getMultipartConfigElement();
//
//            //servletHolder.getRegistration().setMultipartConfig(multipartConfigElement);
//            // But then it fails because there no file directory to use (which we don't care about).
//
//            Object x = request.getAttribute(jettyMultipartAttributeName);
//        }
//
//        try {
//            for ( Part part : request.getParts() ) {
//                InputStream input = part.getInputStream();
//                boolean isFile = part.getHeader("Content-disposition") != null ;
//                if ( ! isFile ) {
//                    // Form field - this code only supports multipart file upload.
//                    String fieldName = part.getName();
//                    InputStream stream = part.getInputStream();
//                    String value = IO.readWholeFileAsUTF8(stream);
//                    // This code is currently used to put multiple files into a single destination.
//                    // Additional field/values do not make sense.
//                    ServletOps.errorBadRequest(format("Only files accepted in multipart file upload (got %s=%s)", fieldName, value));
//                    // errorBadRequest does not return.
//                    return null;
//                }
//
//                String contentTypeHeader = part.getContentType();
//                ContentType ct = ContentType.create(contentTypeHeader);
//                String partName = part.getName(); // AKA fieldName
//                String submittedFileName = part.getSubmittedFileName();
//
//                handlePart(action, input, base, ct, submittedFileName, countingDest);
//            }
//        }
//        catch (ActionErrorException ex) { throw ex; }
//        catch (Exception ex)            {
//            ex.printStackTrace();
//            ServletOps.errorOccurred(ex.getMessage());
//        }
//        // Overall results.
//        UploadDetails details = new UploadDetails(countingDest.count(), countingDest.countTriples(),countingDest.countQuads());
//        return details;
//    }

