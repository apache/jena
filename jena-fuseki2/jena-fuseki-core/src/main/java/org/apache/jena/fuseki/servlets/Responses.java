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
import static org.apache.jena.atlas.lib.Lib.lowercase;
import static org.apache.jena.riot.WebContent.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.system.ConNeg;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.riot.rowset.rw.RowSetWriterXML;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.resultset.ResultsWriter;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the content negotiation for each kind of SPARQL query result */
public class Responses
{
    private static Logger xlog = LoggerFactory.getLogger(Responses.class);

    // Short names for result sets "output="
    private static final String contentOutputJSON          = "json";
    private static final String contentOutputXML           = "xml";
    private static final String contentOutputSPARQL        = "sparql";
    private static final String contentOutputText          = "text";
    private static final String contentOutputCSV           = "csv";
    private static final String contentOutputTSV           = "tsv";
    private static final String contentOutputThrift        = "thrift";

    // Short names for graph and datasets "output="
    private static final String contentOutputJSONLD        = "json-ld";
    private static final String contentOutputJSONRDF       = "json-rdf";
    private static final String contentOutputTTL           = "ttl";
    private static final String contentOutputTurtle        = "turtle";
    private static final String contentOutputNT            = "nt";
    private static final String contentOutputTriG          = "trig";
    private static final String contentOutputNQuads        = "n-quads";


    // Short names - result sets
    public static final Map<String,String> shortNamesResultSet = Map.ofEntries
            (entryLC(contentOutputJSON,    contentTypeResultsJSON),
             entryLC(contentOutputSPARQL,  contentTypeResultsXML),
             entryLC(contentOutputXML,     contentTypeResultsXML),
             entryLC(contentOutputText,    contentTypeTextPlain),
             entryLC(contentOutputCSV,     contentTypeTextCSV),
             entryLC(contentOutputTSV,     contentTypeTextTSV),
             entryLC(contentOutputThrift,  contentTypeResultsThrift)
            );

    // Short names - graphs etc
    public static final Map<String,String> shortNamesGraph = Map.ofEntries
            (entryLC(contentOutputJSONLD,  contentTypeJSONLD),
             entryLC(contentOutputJSONRDF, contentTypeRDFJSON),
             entryLC(contentOutputJSON,    contentTypeJSONLD),
             entryLC(contentOutputXML,     contentTypeRDFXML),
             entryLC(contentOutputText,    contentTypeTurtle),
             entryLC(contentOutputTTL,     contentTypeTurtle),
             entryLC(contentOutputTurtle,  contentTypeTurtle),
             entryLC(contentOutputNT,      contentTypeNTriples),
             entryLC(contentOutputNQuads,  contentTypeNQuads),
             entryLC(contentOutputTriG,    contentTypeTriG)
            );

    private static Map.Entry<String, String> entryLC(String key, String value) {
        return Map.entry(Lib.lowercase(key), value);
    }

    interface OutputContent { void output(OutputStream out) throws IOException; }

    // Set HTTP response and execute OutputContent inside try-catch.
    private static void output(HttpAction action, String contentType, String charset, OutputContent proc) {
        try {
            setHttpResponse(action, contentType, charset);
            ServletOps.success(action);
            OutputStream out = action.getResponseOutputStream();
            try {
                proc.output(out);
                out.flush();
            } catch (QueryCancelledException ex) {
                // Status code 200 may have already been sent.
                // We can try to set the HTTP response code anyway.
                // Breaking the results is the best we can do to indicate the timeout.
                // There isn't an ideal status code. Keep this aligned with the status used in ActionExecLib. execActionSub
                action.setResponseStatus(Fuseki.SC_QueryCancelled);
                action.log.info(format("[%d] Query Cancelled - results truncated (but 200 may have already been sent)", action.id));
                PrintStream ps = new PrintStream(out);
                ps.println();
                ps.println("##  Query cancelled due to timeout during execution   ##");
                ps.println("##  ****          Incomplete results           ****   ##");
                ps.flush();
                out.flush();
                // No point raising an exception - 200 was sent already.
                //errorOccurred(ex);
            }
        // Includes client gone.
        } catch (IOException ex) { ServletOps.errorOccurred(ex); }
        // Do not call httpResponse.flushBuffer() at this point. JSON callback closing details haven't been added.
        // Jetty closes the stream if it is a gzip stream.
    }

    public static void doResponseBoolean(HttpAction action, Boolean booleanResult) {
        ResponseResults.doResponseResultSet$(action, null, booleanResult, null, DEF.rsOfferBoolean);
    }

    public static void doResponseResultSet(HttpAction action, RowSet rowSet, Prologue qPrologue) {
        ResponseResults.doResponseResultSet$(action, rowSet, null, qPrologue, DEF.rsOfferTable);
    }

    static class ResponseResults {
        // One or the other argument must be null
        private static void doResponseResultSet$(HttpAction action,
                                                 RowSet rowSet, Boolean booleanResult,
                                                 Prologue qPrologue, AcceptList contentTypeOffer) {
            HttpServletRequest request = action.getRequest();
            long id = action.id;

            if ( rowSet == null && booleanResult == null ) {
                xlog.warn("doResponseResult: Both result set and boolean result are null");
                throw new FusekiException("Both result set and boolean result are null");
            }

            if ( rowSet != null && booleanResult != null ) {
                xlog.warn("doResponseResult: Both result set and boolean result are set");
                throw new FusekiException("Both result set and boolean result are set");
            }

            String mimeType = null;
            // -- Conneg
            MediaType i = ConNeg.chooseContentType(request, contentTypeOffer, DEF.acceptResultSetXML);
            if ( i != null )
                mimeType = i.getContentTypeStr();

            // -- Override content type from conneg.
            // Does &output= override?
            // Requested output type by the web form or &output= in the request.
            String outputField = paramOutput(request, shortNamesResultSet);    // Expands short names
            if ( outputField != null )
                mimeType = outputField;

            String serializationType = mimeType;           // Choose the serializer based on this.
            String contentType = mimeType;                 // Set the HTTP response header to this.

            // -- Stylesheet - change to application/xml.
            final String stylesheetURL = paramStylesheet(request);
            if ( stylesheetURL != null && Objects.equals(serializationType,contentTypeResultsXML) )
                contentType = contentTypeXML;

            // Force to text/plain?
            String forceAccept = paramForceAccept(request);
            if ( forceAccept != null )
                contentType = contentTypeTextPlain;

            // Some kind of general dispatch is neater but there are quite a few special cases.
            // text/plain is special because there is no ResultSetWriter for it (yet).
            // Text plain is special because of the formatting by prologue.
            // text/plain is not a registered result set language.
            //
            // JSON is special because of ?callback
            //
            // XML is special because of
            // (1) charset is a feature of XML, not the response
            // (2) ?stylesheet=
            //
            // Thrift is special because
            // (1) charset is meaningless
            // (2) there is no boolean result form.

            if ( Objects.equals(serializationType, contentTypeTextPlain) ) {
                textOutput(action, contentType, rowSet, qPrologue, booleanResult);
                return;
            }

            Lang lang = WebContent.contentTypeToLangResultSet(serializationType);
            if (lang == null )
                ServletOps.errorBadRequest("Not recognized for SPARQL results: "+serializationType);
            if ( ! ResultSetWriterRegistry.isRegistered(lang) )
                ServletOps.errorBadRequest("No results writer for "+serializationType);

            Context cxt = action.getContext().copy();
            String charset = charsetUTF8;
            String jsonCallback = null;

            switch(serializationType) {
                case contentTypeResultsXML-> {
                    // XML controls the character set
                    charset = null;
                    if ( stylesheetURL != null )
                        cxt.set(RowSetWriterXML.xmlStylesheet, stylesheetURL);
                }
                case contentTypeResultsJSON -> {
                    // JSON is always UTF-8.
                    // charset = null;
                    jsonCallback = paramCallback(action.getRequest());
                }
                case contentTypeResultsThrift -> {
                    if ( booleanResult != null )
                        ServletOps.errorBadRequest("Can't write a boolean result in thrift");
                    charset = null;
                }
                case contentTypeResultsProtobuf -> {
                    if ( booleanResult != null )
                        ServletOps.errorBadRequest("Can't write a boolean result in protobuf");
                    charset = null;
                }
            }

            // Finally, the general case
            generalOutput(action, lang, contentType, charset, cxt, jsonCallback, rowSet, booleanResult);
        }

        private static void textOutput(HttpAction action, String contentType, RowSet resultSet, Prologue qPrologue, Boolean booleanResult) {
            // Text is not streaming.
            OutputContent proc = out -> {
                if ( resultSet != null )
                    RowSetOps.out(out, resultSet, qPrologue);
                if (  booleanResult != null )
                    ResultSetFormatter.out(out, booleanResult.booleanValue());
            };

            output(action, contentType, charsetUTF8, proc);
        }

        /** Any format */
        private static void generalOutput(HttpAction action, Lang rsLang,
                                          String contentType, String charset,
                                          Context context, String callback,
                                          RowSet resultSet, Boolean booleanResult) {
            ResultsWriter rw = ResultsWriter.create()
                    .lang(rsLang)
                    .context(context)
                    .build();
            OutputContent proc = (out) -> {
                if ( callback != null ) {
                    String callbackFunction = callback;
                    callbackFunction = callbackFunction.replace("\r", "");
                    callbackFunction = callbackFunction.replace("\n", "");
                    out.write(StrUtils.asUTF8bytes(callbackFunction));
                    out.write('('); out.write('\n');
                }
                if ( resultSet != null )
                    rw.write(out, resultSet);
                if ( booleanResult != null )
                    rw.write(out, booleanResult.booleanValue());
                if ( callback != null ) {
                    out.write(')'); out.write('\n');
                }
            };
            output(action, contentType, charset, proc);
        }
    }

    public static void doResponseGraph(HttpAction action, Graph graph) {
        DatasetGraph ds = DatasetGraphFactory.wrap(graph);
        doResponseDataset(action, ds);
    }

    public static void doResponseDataset(HttpAction action, DatasetGraph dataset) {
        ResponseGraph.doResponseDataset$(action, dataset);
    }


    static class ResponseGraph {
        static void doResponseDataset$(HttpAction action, DatasetGraph dataset) {
            HttpServletRequest request = action.getRequest();

            String mimeType = null;        // Header request type

            MediaType i = ConNeg.chooseContentType(request, DEF.constructOffer, DEF.acceptTurtle);
            if ( i != null )
                mimeType = i.getContentTypeStr();

            String outputField = paramOutput(request, shortNamesGraph);
            if ( outputField != null )
                mimeType = outputField;

            String writerMimeType = mimeType;

            if ( mimeType == null ) {
                Fuseki.actionLog.warn("Can't find MIME type for response");
                String x = FusekiNetLib.getAccept(request);
                String msg;
                if ( x == null )
                    msg = "No Accept: header";
                else
                    msg = "Accept: " + x + " : Not understood";
                ServletOps.error(HttpSC.NOT_ACCEPTABLE_406, msg);
            }

            String contentType = mimeType;
            String charset = charsetUTF8;

            String forceAccept = paramForceAccept(request);
            if ( forceAccept != null ) {
                contentType = forceAccept;
                charset = charsetUTF8;
            }

            Lang lang = RDFLanguages.contentTypeToLang(contentType);
            if ( lang == null )
                ServletOps.errorBadRequest("Can't determine output content type: "+contentType);
            RDFFormat format = ActionLib.getNetworkFormatForLang(lang);

            try {
                ServletOps.success(action);
                ServletOutputStream out = action.getResponseOutputStream();
                try {
                    // Use the Content-Type from the content negotiation.
                    if ( RDFLanguages.isQuads(lang) )
                        ActionLib.datasetResponse(action, dataset, format, contentType);
                    else
                        ActionLib.graphResponse(action, dataset.getDefaultGraph(), format, contentType);
                    out.flush();
                } catch (JenaException ex) {
                    ServletOps.errorOccurred("Failed to write output: "+ex.getMessage(), ex);
                }
            }
            catch (ActionErrorException ex) { throw ex; }
            catch (Exception ex) {
                action.log.info("Exception while writing the response model: "+ex.getMessage(), ex);
                ServletOps.errorOccurred("Exception while writing the response model: "+ex.getMessage(), ex);
            }
        }
    }


    public static void doResponseJson(HttpAction action, Iterator<JsonObject> jsonItem) {
        ResponseJson.doResponseJson$(action, jsonItem);
    }

    static class ResponseJson {
        /**
         * Outputs a JSON query result
         *
         * @param action HTTP action
         * @param jsonItem a ResultSetJsonStream instance
         */
        static void doResponseJson$(HttpAction action, Iterator<JsonObject> jsonItem) {
            if ( jsonItem == null ) {
                xlog.warn("doResponseJson: Result set is null");
                throw new FusekiException("Result set is null");
            }

            jsonOutput(action, jsonItem);
        }

        private static void jsonOutput(HttpAction action, final Iterator<JsonObject> jsonItems) {
            OutputContent proc = out-> {
                    if ( jsonItems != null )
                        ResultSetFormatter.output(out, jsonItems);
                };

            try {
                String callback = paramCallback(action.getRequest());
                ServletOutputStream out = action.getResponseOutputStream();

                if ( callback != null ) {
                    callback = StringUtils.replaceChars(callback, "\r", "");
                    callback = StringUtils.replaceChars(callback, "\n", "");
                    out.write(StrUtils.asUTF8bytes(callback));
                    out.write('('); out.write('\n');
                }

                output(action, "application/json", WebContent.charsetUTF8, proc);

                if ( callback != null ) {
                    out.write(')'); out.write('\n');
                }
            } catch (IOException ex) {
                ServletOps.errorOccurred(ex);
            }
        }

        private static void output(HttpAction action, String contentType, String charset, OutputContent proc) {
            try {
                setHttpResponse(action, contentType, charset);
                action.setResponseStatus(HttpSC.OK_200);
                ServletOutputStream out = action.getResponseOutputStream();
                try {
                    proc.output(out);
                    out.flush();
                } catch (QueryCancelledException ex) {
                    // Bother. Status code 200 already sent.
                    xlog.info(format("[%d] Query Cancelled - results truncated (but 200 already sent)", action.id));
                    PrintStream ps = new PrintStream(out);
                    ps.println();
                    ps.println("##  Query cancelled due to timeout during execution   ##");
                    ps.println("##  ****          Incomplete results           ****   ##");
                    ps.flush();
                    out.flush();
                    // No point raising an exception - 200 was sent already.
                    // errorOccurred(ex);
                }
                // Includes client gone.
            } catch (IOException ex) {
                ServletOps.errorOccurred(ex);
            }
            // Do not call httpResponse.flushBuffer(); here - Jetty closes the stream if
            // it is a gzip stream
            // then the JSON callback closing details can't be added.
        }

        public static void setHttpResponse(HttpAction action, String contentType, String charset) {
            // ---- Set up HTTP Response
            // Stop caching (not that ?queryString URLs are cached anyway)
            ServletOps.setNoCache(action);
            // See: http://www.w3.org/International/O-HTTP-charset.html
            if ( contentType != null ) {
                if ( charset != null )
                    contentType = contentType + "; charset=" + charset;
                xlog.trace("Content-Type for response: " + contentType);
                action.setResponseContentType(contentType);
            }
        }
    }

    static String paramForceAccept(HttpServletRequest request) {
        String x = fetchParam(request, HttpNames.paramForceAccept);
        return x;
    }

    static String paramStylesheet(HttpServletRequest request) {
        return fetchParam(request, HttpNames.paramStyleSheet);
    }

    static String paramOutput(HttpServletRequest request, Map<String, String> map) {
        // Two names.
        String x = fetchParam(request, HttpNames.paramOutput1);
        if ( x == null )
            x = fetchParam(request, HttpNames.paramOutput2);
        if ( x == null )
            x = fetchParam(request, HttpNames.paramOutput3);
        return expandShortName(x, map);
    }

    private static String expandShortName(String str, Map<String, String> map) {
        if ( str == null )
            return null;
        // Force keys to lower case. See put() above.
        String key = lowercase(str);
        String str2 = map.get(key);
        if ( str2 == null )
            return str;
        return str2;
    }

    static String paramCallback(HttpServletRequest request) {
        return fetchParam(request, HttpNames.paramCallback);
    }

    private static String fetchParam(HttpServletRequest request, String parameterName) {
        String value = request.getParameter(parameterName);
        if ( value != null ) {
            value = value.trim();
            if ( value.length() == 0 )
                value = null;
        }
        return value;
    }

    /** Basic settings, including Content-Type, for a response. */
    static void setHttpResponse(HttpAction action, String contentType, String charset) {
        // ---- Set up HTTP Response
        // Stop caching (not that ?queryString URLs are cached anyway)
        if ( true )
            ServletOps.setNoCache(action);
        if ( contentType != null ) {
            if ( charset != null && !isXML(contentType) ) {
                // Doing it ourselves means it is logged for "verbose"
                contentType = contentType + "; charset=" + charset;
            }
            action.setResponseContentType(contentType);
            action.log.trace("Content-Type for response: " + contentType);
        }
    }

    private static boolean isXML(String contentType) {
        return contentType.equals(WebContent.contentTypeRDFXML) || contentType.equals(WebContent.contentTypeResultsXML)
               || contentType.equals(WebContent.contentTypeXML);
    }

}
