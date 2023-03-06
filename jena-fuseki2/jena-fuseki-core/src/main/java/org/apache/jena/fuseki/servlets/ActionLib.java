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

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.CharacterCodingException;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.system.ConNeg;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.web.HttpSC;

/** Operations related to servlets */

public class ActionLib {

    /** Calculate the operation, given action and data access point */
    public static String mapRequestToEndpointName(HttpAction action, DataAccessPoint dataAccessPoint) {
        String uri = action.getActionURI();
        return mapRequestToEndpointName(uri, dataAccessPoint);
    }

    /** Calculate the operation, given request URI and data access point */
    public static String mapRequestToEndpointName(String uri, DataAccessPoint dataAccessPoint) {
        if ( dataAccessPoint == null )
            return "";
        String name = dataAccessPoint.getName();
        if ( name.length() >= uri.length() )
            return "";
        if ( name.equals("/") )
            // Case "/" and uri "/service"
            return uri.substring(1);
        return uri.substring(name.length()+1);   // Skip the separating "/"
    }

    /**
     * Implementation of mapRequestToDataset(String) that looks for the longest match
     * in the registry. This includes use in direct naming GSP.
     */
    public static String unused_mapRequestToDatasetLongest(String uri, DataAccessPointRegistry registry) {
        if ( uri == null )
            return null;

        // This covers local, using the URI as a direct name for
        // a graph, not just using the indirect ?graph= or ?default
        // forms.

        String ds = null;
        for ( String ds2 : registry.keys() ) {
            if ( ! uri.startsWith(ds2) )
                continue;

            if ( ds == null ) {
                ds = ds2;
                continue;
            }
            if ( ds.length() < ds2.length() ) {
                ds = ds2;
                continue;
            }
        }
        return ds;
    }

    /** Calculate the full URL including query string
     * for the HTTP request. This may be quite long.
     * @param request HttpServletRequest
     * @return String The full URL, including query string.
     */
    public static String wholeRequestURL(HttpServletRequest request) {
        StringBuffer sb = request.getRequestURL();
        String queryString = request.getQueryString();
        if ( queryString != null ) {
            sb.append("?");
            sb.append(queryString);
        }
        return sb.toString();
    }

    /*
     * The context path can be:
     * "" for the root context
     * "/APP" for named contexts
     * so:
     * "/dataset/server" becomes "/dataset/server"
     * "/APP/dataset/server" becomes "/dataset/server"
     */
    public static String removeContextPath(HttpAction action) {
        return actionURI(action.getRequest());
    }

    /**
     * @return the URI without context path of the webapp and without query string.
     */
    public static String actionURI(HttpServletRequest request) {
//      Log.info(this, "URI                     = '"+request.getRequestURI());
//      Log.info(this, "Context Path            = '"+request.getContextPath()+"'");
//      Log.info(this, "Servlet path            = '"+request.getServletPath()+"'");
//      ServletContext cxt = this.getServletContext();
//      Log.info(this, "ServletContext path     = '"+cxt.getContextPath()+"'");

        String uri = request.getRequestURI();
        ServletContext servletCxt = request.getServletContext();
        if ( servletCxt == null )
            return request.getRequestURI();

        String contextPath = servletCxt.getContextPath();
        if ( contextPath == null )
            return uri;
        if ( contextPath.isEmpty())
            return uri;

        String x = uri;
        if ( uri.startsWith(contextPath) )
            x = uri.substring(contextPath.length());
        return x;
    }

    /** Negotiate the content-type and set the response headers */
    public static MediaType contentNegotation(HttpAction action, AcceptList myPrefs, MediaType defaultMediaType) {
        MediaType mt = ConNeg.chooseContentType(action.getRequest(), myPrefs, defaultMediaType);
        if ( mt == null )
            return null;
        if ( mt.getContentTypeStr() != null )
            action.setResponseContentType(mt.getContentTypeStr());
        if ( mt.getCharset() != null )
            action.setResponseCharacterEncoding(mt.getCharset());
        return mt;
    }

    /** Negotiate the content-type for an RDF triples syntax and set the response headers */
    public static MediaType contentNegotationRDF(HttpAction action) {
        return contentNegotation(action, DEF.rdfOffer, DEF.acceptRDFXML);
    }

    /** Negotiate the content-type for an RDF quads syntax and set the response headers */
    public static MediaType contentNegotationQuads(HttpAction action) {
        return contentNegotation(action, DEF.quadsOffer, DEF.acceptNQuads);
    }

    /** Split a string on "," and remove leading and trailing whitespace on each element */
    public static String[] splitOnComma(String string) {
        String split[] = string.split(",");
        for ( int i = 0 ; i < split.length ; i++ ) {
            split[i] = split[i].trim();
        }
        return split;
    }

    public static boolean splitContains(String[] elts, String str) {
        for ( int i = 0 ; i < elts.length ; i++ ) {
            if ( Lib.equals(elts[i],  str) )
                return true;
        }
        return false;
    }

    /**
     * Parse RDF content from the body of the request of the action, ends the
     * request, and sends a 400 if there is a parse error.
     *
     * @throws ActionErrorException
     */
    public static void parseOrError(HttpAction action, StreamRDF dest, Lang lang, String base) {
        try {
            parse(action, dest, lang, base);
        } catch (RiotParseException ex) {
            ActionLib.consumeBody(action);
            ServletOps.errorParseError(ex);
        }
    }

    /**
     * Parse RDF content. This wraps up the parse step reading from an action.
     * It includes handling compression if the {@code Content-Encoding} header is present
     * @throws RiotParseException
     */
    public static void parse(HttpAction action, StreamRDF dest, Lang lang, String base) {
        try {
            InputStream input = action.getRequestInputStream();
            parse(action, dest, input, lang, base);
        } catch (IOException ex) { IO.exception(ex); }
    }

    /**
     * Parse RDF content. This wraps up the parse step reading from an input stream.
     * @throws RiotParseException
     */
    public static void parse(HttpAction action, StreamRDF dest, InputStream input, Lang lang, String base) {
        try {
            if ( ! RDFParserRegistry.isRegistered(lang) )
                ServletOps.errorBadRequest("No parser for language '"+lang.getName()+"'");
            ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(action.log);
            RDFParser.create()
                .errorHandler(errorHandler)
                .source(input)
                .lang(lang)
                .base(base)
                .parse(dest);
        } catch (RuntimeIOException ex) {
            if ( ex.getCause() instanceof CharacterCodingException )
                throw new RiotException("Character Coding Error: "+ex.getMessage());
            throw ex;
        }
    }

    /**
     * Reset the request input stream for an {@link HttpAction} if necessary.
     * If there is a {@code Content-Length} header, throw away input to exhaust this request.
     * If there is a no {@code Content-Length} header, no need to do anything - the connection is not reusable.
     */
    public static void consumeBody(HttpAction action) {
        // If there isn't a Content-Length, we can't recover.
        try {
            if ( action.getRequestContentLengthLong() > 0 ) {
                InputStream input = action.getRequestInputStreamRaw();
                IO.skipToEnd(input);
            }
        } catch (IOException ex) { IO.exception(ex); }
    }

    /*
     * Parse RDF content using content negotiation.
     */
    public static Graph readFromRequest(HttpAction action, Lang defaultLang) {
        ContentType ct = ActionLib.getContentType(action);
        Lang lang;

        if ( ct == null || ct.getContentTypeStr().isEmpty() ) {
            // "Content-type:" header - absent or no value. Guess!
            lang = defaultLang;
        } else if ( ct.equals(WebContent.ctHTMLForm)) {
            ServletOps.errorBadRequest("HTML Form data sent to SHACL validation server");
            return null;
        } else {
            lang = RDFLanguages.contentTypeToLang(ct.getContentTypeStr());
            if ( lang == null ) {
                lang = defaultLang;
//            ServletOps.errorBadRequest("Unknown content type for triples: " + ct);
//            return null;
            }
        }
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        ActionLib.parseOrError(action, dest, lang, null);
        return graph;
    }

    /** Output a dataset to the HTTP response. */
    public static void datasetResponse(HttpAction action, DatasetGraph dsg, Lang lang) {
        Objects.requireNonNull(lang);
        RDFFormat format = getNetworkFormatForLang(lang);
        datasetResponse(action, dsg, format, null);
    }

    /** Output a dataset to the HTTP response. */
    public static void datasetResponse(HttpAction action, DatasetGraph dsg, RDFFormat format, String contentType) {
        Objects.requireNonNull(dsg);
        Objects.requireNonNull(format);
        writeResponse(action, (out, fmt) -> RDFDataMgr.write(out, dsg, fmt), format, contentType);
    }

    /** Output a graph to the HTTP response. Bad, unwritable RDF/XML causes a "406 Not Acceptable". */
    public static void graphResponse(HttpAction action, Graph graph, Lang lang) {
        Objects.requireNonNull(lang);
        RDFFormat format = getNetworkFormatForLang(lang);
        graphResponse(action, graph, format, null);
    }

    /** Output a graph to the HTTP response. Bad, unwritable RDF/XML causes a "406 Not Acceptable". */
    public static void graphResponse(HttpAction action, Graph graph, RDFFormat format, String contentType) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(format);
        writeResponse(action, (out, fmt) -> RDFDataMgr.write(out, graph, fmt), format, contentType);
    }

    /**
     * Return the preferred {@link RDFFormat} for a given {@link Lang}.
    *
     */
    public static RDFFormat getNetworkFormatForLang(Lang lang) {
        Objects.requireNonNull(lang);
        if ( lang == Lang.RDFXML )
            return RDFFormat.RDFXML_PLAIN;
        // Could prefer streaming over non-streaming but historically, output has been "pretty" for e.g. turtle
//        RDFFormat fmt = StreamRDFWriter.defaultSerialization(lang);
//        if ( fmt != null )
//            return fmt;
        return RDFWriterRegistry.defaultSerialization(lang);
    }

    /**
     * Output a graph to the HTTP response (does not set the status code) using the given Content-Type string.
     * One of {@code lang} and {@code fmt} maybe null and will be calculated.
     * {@code actualContentType} maybe null in which case the standard content type for the syntax is used.
     */
    private static void writeResponse(HttpAction action, BiConsumer<OutputStream, RDFFormat> writeAction,RDFFormat fmt, String actualContentType) {
        String ct = actualContentType;
        Lang lang = fmt.getLang();
        if ( ct == null )
            ct = lang.getContentType().toHeaderString();

        try {
            OutputStream out = action.getResponseOutputStream();
            // Do not use try-finally here. Error handling headers are written later.
            // RDF/XML can go wrong during writing so we buffered to know it will succeed when we produce the output bytes.
            // (Other formats are much less prone to this.)
            boolean materializeFirst = ( lang == Lang.RDFXML );
            if ( materializeFirst ) {
                byte[] bytes;
                try ( ByteArrayOutputStream bout = new ByteArrayOutputStream(100*1024) ) {
                    writeAction.accept(bout, fmt);
                    bytes = bout.toByteArray();
                } catch (JenaException ex) {
                    // Problems formatting.
                    action.log.warn(format("[%d] Failed to produce %s: %s", action.id, lang.getLabel(), ex.getMessage()));
                    ServletOps.error(HttpSC.NOT_ACCEPTABLE_406);
                    return;
                }
                // Succeeded in formatting the RDF
                // Can not write the length here if compressed.
                action.setResponseContentLength(bytes.length);
                action.setResponseContentType(ct);
                action.setResponseStatus(HttpSC.OK_200);
                out.write(bytes);
            } else {
                // Try to write directly (streaming if possible).
                action.setResponseContentType(ct);
                writeAction.accept(out, fmt);
            }
            out.flush();
        } catch (IOException ex) { IO.exception(ex); }
    }

    /** Get one or zero strings from an HTTP header */
    public static String getOneHeader(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if ( values == null )
            return null;
        if ( values.length == 0 )
            return null;
        if ( values.length > 1 )
            ServletOps.errorBadRequest("Multiple occurrences of '"+name+"'");
        return values[0];
    }

    /**
     * Get the content type of an action.
     * @param  action
     * @return ContentType
     */
    public static ContentType getContentType(HttpAction action) {
        return FusekiNetLib.getContentType(action.getRequest());
    }

    public static void setCommonHeadersForOptions(HttpAction action) {
        setCommonHeadersForOptions(action.getResponse());
    }

    private static void setCommonHeadersForOptions(HttpServletResponse httpResponse) {
        if ( Fuseki.CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowHeaders, "X-Requested-With, Content-Type, Authorization");
        setCommonHeaders(httpResponse);
    }

    public static void setCommonHeaders(HttpAction action) {
        setCommonHeaders(action.getResponse());
    }

    private static void setCommonHeaders(HttpServletResponse httpResponse) {
        if ( Fuseki.CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowOrigin, "*");
        if ( Fuseki.outputFusekiServerHeader )
            httpResponse.setHeader(HttpNames.hServer, Fuseki.serverHttpName);
    }

    /**
     * Extract the name after the container name (servlet name).
     * @param action an HTTP action
     * @return item name as "/name" or {@code null}
     */
    private /*unused*/ static String extractItemName(HttpAction action) {
//          action.log.info("context path  = "+action.getRequestContextPath());
//          action.log.info("pathinfo      = "+action.getRequestPathInfo());
//          action.log.info("servlet path  = "+action.getRequestServletPath());
        // if /name
        //    request.getServletPath() otherwise it's null
        // if /*
        //    request.getPathInfo(); otherwise it's null.

        // PathInfo is after the servlet name.
        String x1 = action.getRequestServletPath();
        String x2 = action.getRequestPathInfo();

        String pathInfo = action.getRequestPathInfo();
        if ( pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") )
            // Includes calling as a container.
            return null;
        String name = pathInfo;
        // pathInfo starts with a "/"
        int idx = pathInfo.lastIndexOf('/');
        if ( idx > 0 )
            name = name.substring(idx);
        // Returns "/name"
        return name;
    }

    // Packing of OPTIONS.

    public static void doOptionsGet(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,OPTIONS");
    }

    public static void doOptionsGetHead(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS");
    }

    public static void doOptionsGetPost(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,POST,OPTIONS");
    }

    public static void doOptionsGetPostHead(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,POST,HEAD,OPTIONS");
    }

    public static void doOptionsGetPostDelete(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,POST,DELETE,OPTIONS");
    }

    public static void doOptionsGetPostDeleteHead(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,POST,DELETE,OPTIONS");
    }

    public static void doOptionsPost(HttpAction action) {
        setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "POST,OPTIONS");
    }
}
