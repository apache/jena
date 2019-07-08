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

import java.io.InputStream;
import java.nio.charset.CharacterCodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.system.ConNeg;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.web.HttpNames;

/** Operations related to servlets */

public class ActionLib {
    /**
     * Get the datasets from an {@link HttpAction}
     * that assumes the form /dataset/service.
     * @param action the request
     * @return the dataset
     */
    public static String mapRequestToDataset(HttpAction action) {
         String uri = action.getActionURI();
         return mapActionRequestToDataset(uri);
     }

    /** Map request to uri in the registry.
     *  A possible implementation for mapRequestToDataset(String)
     *  that assumes the form /dataset/service
     *  Returning null means no mapping found.
     *  The URI must be the action URI (no contact path)
     */

    public static String mapActionRequestToDataset(String uri) {
        // Chop off trailing part - the service selector
        // e.g. /dataset/sparql => /dataset
        int i = uri.lastIndexOf('/');
        if ( i == -1 )
            return null;
        if ( i == 0 ) {
            // started with '/' - leave.
            return uri;
        }

        return uri.substring(0, i);
    }

    /** Calculate the operation, given action and data access point */
    public static String mapRequestToOperation(HttpAction action, DataAccessPoint dsRef) {
        if ( dsRef == null )
            return "";
        String uri = action.getActionURI();
        String name = dsRef.getName();
        if ( name.length() >= uri.length() )
            return "";
        return uri.substring(name.length()+1);   // Skip the separating "/"

    }

    /**
     * Implementation of mapRequestToDataset(String) that looks for the longest match
     * in the registry. This includes use in direct naming GSP.
     */
    public static String mapRequestToDatasetLongest$(String uri, DataAccessPointRegistry registry) {
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

    /** Calculate the fill URL including query string
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

        return actionURI(action.request);
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

        String contextPath = request.getServletContext().getContextPath();
        String uri = request.getRequestURI();
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
        MediaType mt = ConNeg.chooseContentType(action.request, myPrefs, defaultMediaType);
        if ( mt == null )
            return null;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType());
        if ( mt.getCharset() != null )
            action.response.setCharacterEncoding(mt.getCharset());
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

    /**
     * Parse RDF content
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
        catch (RiotException ex) { ServletOps.errorBadRequest("Parse error: "+ex.getMessage()); }
    }

    /** Get the content type of an action or return the default.
     * @param  action
     * @return ContentType
     */
    public static ContentType getContentType(HttpAction action) {
        return FusekiNetLib.getContentType(action.request);
    }
    
    public static void setCommonHeadersForOptions(HttpServletResponse httpResponse) {
        if ( Fuseki.CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowHeaders, "X-Requested-With, Content-Type, Authorization");
        setCommonHeaders(httpResponse);
    }

    public static void setCommonHeaders(HttpServletResponse httpResponse) {
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
//          action.log.info("context path  = "+action.request.getContextPath());
//          action.log.info("pathinfo      = "+action.request.getPathInfo());
//          action.log.info("servlet path  = "+action.request.getServletPath());
        // if /name
        //    request.getServletPath() otherwise it's null
        // if /*
        //    request.getPathInfo(); otherwise it's null.

        // PathInfo is after the servlet name.
        String x1 = action.request.getServletPath();
        String x2 = action.request.getPathInfo();

        String pathInfo = action.request.getPathInfo();
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
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,OPTIONS");
    }

    public static void doOptionsGetHead(HttpAction action) {
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS");
    }

    public static void doOptionsGetPost(HttpAction action) {
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,POST,OPTIONS");
    }

    public static void doOptionsGetPostHead(HttpAction action) {
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,POST,HEAD,OPTIONS");
    }

    public static void doOptionsGetPostDelete(HttpAction action) {
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,POST,DELETE,OPTIONS");
    }

    public static void doOptionsGetPostDeleteHead(HttpAction action) {
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,POST,DELETE,OPTIONS");
    }

    public static void doOptionsPost(HttpAction action) {
        ServletBase.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "POST,OPTIONS");
    }
}

