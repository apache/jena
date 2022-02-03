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

import static java.lang.String.format;
import static org.apache.jena.fuseki.server.CounterName.UploadExecErrors;
import static org.apache.jena.fuseki.servlets.ActionExecLib.incCounter;
import static org.apache.jena.riot.WebContent.matchContentType;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.fuseki.system.UploadDetailsWithName;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.web.HttpSC;

/**
 * Upload data into a graph within a dataset. This is {@code fuseki:serviceUpload}.
 * <p>
 * It is better to use GSP or quads POST, or the {@link Upload} service with the body being the content.
 * <p>
 * This class works with general HTML form file upload where the name is somewhere in the form and that may be
 * after the data.
 * <p>
 * Consider this service useful for small files and use GSP or quads POST for large ones.
 * <p>
 * <i>Legacy</i>.
 */
public class HTML_FileUpload extends ActionService
{
    public HTML_FileUpload() {
        super();
    }

    @Override
    public void execPost(HttpAction action) {
        executeLifecycle(action);
    }

    @Override
    public void execOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "POST,PATCH,OPTIONS");
        ServletOps.success(action);
    }

    @Override
    public void validate(HttpAction action)
    {}

    @Override
    public void execute(HttpAction action) {
        // Only allows one file in the upload.
        boolean isMultipart = matchContentType(WebContent.ctMultipartFormData,
                                               ActionLib.getContentType(action));

        if ( ! isMultipart )
            ServletOps.error(HttpSC.BAD_REQUEST_400 , "Not a file upload");

        long count = upload(action, Fuseki.BaseUpload);
        ServletOps.success(action);
        try {
            action.setResponseContentType("text/html");
            action.setResponseStatus(HttpSC.OK_200);
            PrintWriter out = action.getResponseWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Success</h1>");
            out.println("<p>");
            out.println("Triples = "+count + "\n");
            out.println("<p>");
            out.println("</p>");
            out.println("<button onclick=\"timeFunction()\">Back to Fuseki</button>");
            out.println("</p>");
            out.println("<script type=\"text/javascript\">");
            out.println("function timeFunction(){");
            out.println("window.location.href = \"/fuseki.html\";}");
            out.println("</script>");
            out.println("</body>");
            out.println("</html>");
            out.flush();
        }
        catch (Exception ex) {
            incCounter(action.getEndpoint().getCounters(), UploadExecErrors);
            ServletOps.errorOccurred(ex);
        }
    }

    // Also used by SPARQL_REST
    private static long upload(HttpAction action, String base) {
        if ( action.isTransactional() )
            return uploadTxn(action, base);
        else
            return uploadNonTxn(action, base);
    }

    /**
     * Non-transaction - buffer to a temporary graph so that parse errors
     * are caught before inserting any data.
     */
    private static long uploadNonTxn(HttpAction action, String base) {
        UploadDetailsWithName upload = multipartUploadWorker(action, base);
        String graphName = upload.graphName;
        DatasetGraph dataTmp = upload.data;
        long count = upload.count;

        if ( graphName == null )
            action.log.info(format("[%d] Upload: %d Quads(s)",action.id, count));
        else
            action.log.info(format("[%d] Upload: Graph: %s, %d triple(s)", action.id, graphName,  count));

        Node gn = null;
        if ( graphName != null ) {
            gn = graphName.equals(HttpNames.graphTargetDefault)
                ? Quad.defaultGraphNodeGenerated
                : NodeFactory.createURI(graphName);
        }

        action.beginWrite();
        try {
            if ( gn != null )
                FusekiNetLib.addDataInto(dataTmp.getDefaultGraph(), action.getActiveDSG(), gn);
            else
                FusekiNetLib.addDataInto(dataTmp, action.getActiveDSG());
            action.commit();
            return count;
        } catch (OperationDeniedException ex) {
            action.abortSilent();
            throw ex;
        } catch (RuntimeException ex) {
            // If anything went wrong, try to backout.
            action.abortSilent();
            ServletOps.errorOccurred(ex.getMessage());
            return -1;
        }
        finally { action.end(); }
    }

    /**
     * Transactional - we'd like better handle the data and go straight to the destination, with an abort on parse error.
     * For an HTML file upload that's not so simple in the general case.
     * Use Graph Store protocol for bulk uploads.
     */
    private static long uploadTxn(HttpAction action, String base) {
        return uploadNonTxn(action, base);
    }

    /**
     * Process an HTTP file upload of RDF using the name field for the graph name destination.
     */
    private static UploadDetailsWithName multipartUploadWorker(HttpAction action, String base) {
        DatasetGraph dsgTmp = DatasetGraphFactory.create();
        ServletFileUpload upload = new ServletFileUpload();
        String graphName = null;
        boolean isQuads = false;
        long count = -1;

        String name = null;
        ContentType ct = null;
        Lang lang = null;

        try {
            FileItemIterator iter = upload.getItemIterator(action.getRequest());
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String fieldName = item.getFieldName();
                InputStream input = item.openStream();
                if ( item.isFormField() ) {
                    // Graph name.
                    String value = Streams.asString(input, "UTF-8");
                    if ( fieldName.equals(HttpNames.paramGraph) ) {
                        graphName = value;
                        if ( graphName != null && !graphName.equals("") && !graphName.equals(HttpNames.graphTargetDefault) ) {
                            // -- Check IRI with additional checks.
                            try {
                                IRIx iri = IRIx.create(value);
                                if ( ! iri.isReference() )
                                    ServletOps.errorBadRequest("IRI not suitable: " + graphName);
                            } catch (IRIException ex) {
                                ServletOps.errorBadRequest("Bad IRI: " + graphName);
                            }
                            // End check IRI
                        }
                    } else if ( fieldName.equals(HttpNames.paramDefaultGraphURI) )
                        graphName = null;
                    else
                        // Add file type?
                        action.log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName));
                } else {
                    // Process the input stream
                    name = item.getName();
                    if ( name == null || name.equals("") || name.equals("UNSET FILE NAME") )
                        ServletOps.errorBadRequest("No name for content - can't determine RDF syntax");

                    String contentTypeHeader = item.getContentType();
                    ct = ContentType.create(contentTypeHeader);

                    lang = RDFLanguages.contentTypeToLang(ct.getContentTypeStr());
                    if ( lang == null ) {
                        lang = RDFLanguages.pathnameToLang(name);

                        // JENA-600 filenameToLang() strips off certain
                        // extensions such as .gz and
                        // we need to ensure that if there was a .gz extension
                        // present we wrap the stream accordingly
                        if ( name.endsWith(".gz") )
                            input = new GZIPInputStream(input);
                    }

                    if ( lang == null )
                        // Desperate.
                        lang = RDFLanguages.RDFXML;

                    isQuads = RDFLanguages.isQuads(lang);

                    action.log.info(format("[%d] Upload: Filename: %s, Content-Type=%s, Charset=%s => %s", action.id, name,
                                           ct.getContentTypeStr(), ct.getCharset(), lang.getName()));

                    StreamRDF x = StreamRDFLib.dataset(dsgTmp);
                    StreamRDFCounting dest = StreamRDFLib.count(x);
                    try {
                        ActionLib.parse(action, dest, input, lang, base);
                    } catch (RiotParseException ex) {
                        ActionLib.consumeBody(action);
                        ServletOps.errorParseError(ex);
                    }
                    count = dest.count();
                }
            }

            if ( graphName == null || graphName.equals("") )
                graphName = HttpNames.graphTargetDefault;
            if ( isQuads )
                graphName = null;
            return new UploadDetailsWithName(graphName, dsgTmp, count);
        }
        catch (ActionErrorException ex) { throw ex; }
        catch (Exception ex)            { ServletOps.errorOccurred(ex); return null; }
    }
}
