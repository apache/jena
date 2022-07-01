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
import static org.apache.jena.fuseki.server.CounterName.UpdateExecErrors;
import static org.apache.jena.fuseki.servlets.ActionExecLib.incCounter;
import static org.apache.jena.fuseki.servlets.SPARQLProtocol.countParamOccurences;
import static org.apache.jena.fuseki.servlets.SPARQLProtocol.messageForException;
import static org.apache.jena.fuseki.servlets.SPARQLProtocol.messageForParseException;
import static org.apache.jena.riot.WebContent.*;
import static org.apache.jena.riot.web.HttpNames.paramRequest;
import static org.apache.jena.riot.web.HttpNames.paramUpdate;
import static org.apache.jena.riot.web.HttpNames.paramUsingGraphURI;
import static org.apache.jena.riot.web.HttpNames.paramUsingNamedGraphURI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.modify.UsingList;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.web.HttpSC;

public class SPARQL_Update extends ActionService
{
    // Base URI used to isolate parsing from the current directory of the server.
    private static final String UpdateParseBase = Fuseki.BaseParserSPARQL;
    private static final IRIxResolver resolver = IRIxResolver.create()
                                                            .base(UpdateParseBase)
                                                            .resolve(true)
                                                            .allowRelative(false)
                                                            .build();

    public SPARQL_Update() { super(); }

    @Override
    public void execOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "POST,PATCH,OPTIONS");
        ServletOps.success(action);
    }

    @Override
    public void execGet(HttpAction action) {
        ServletOps.errorMethodNotAllowed(HttpNames.METHOD_GET, "GET not support for SPARQL Update. Use POST or PATCH");
    }

    @Override
    public void execPost(HttpAction action) {
        executeLifecycle(action);
    }

    @Override
    public void execPatch(HttpAction action) {
        executeLifecycle(action);
    }

    @Override
    public void execute(HttpAction action) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ct = ctSPARQLUpdate;

        if ( matchContentType(ctSPARQLUpdate, ct) ) {
            executeBody(action);
            return;
        }
        if ( isHtmlForm(ct) ) {
            executeForm(action);
            return;
        }
        ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: " + action.getRequestContentType());
    }

    protected static List<String> paramsForm = Arrays.asList(paramRequest, paramUpdate,
                                                             paramUsingGraphURI, paramUsingNamedGraphURI);
    protected static List<String> paramsPOST = Arrays.asList(paramUsingGraphURI, paramUsingNamedGraphURI);

    @Override
    public void validate(HttpAction action) {
        //HttpServletRequest request = action.getRequest();

        if ( HttpNames.METHOD_OPTIONS.equals(action.getRequestMethod()) )
            return;

        if ( ! HttpNames.METHOD_POST.equalsIgnoreCase(action.getRequestMethod()) )
            ServletOps.errorMethodNotAllowed("SPARQL Update : use POST");

        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ct = ctSPARQLUpdate;

        if ( matchContentType(ctSPARQLUpdate, ct) ) {
            String charset = action.getRequestCharacterEncoding();
            if ( charset != null && !charset.equalsIgnoreCase(charsetUTF8) )
                ServletOps.errorBadRequest("Bad charset: " + charset);
            validate(action, paramsPOST);
            return;
        }

        if ( isHtmlForm(ct) ) {
            int x = countParamOccurences(action.getRequest(), paramUpdate) + countParamOccurences(action.getRequest(), paramRequest);
            if ( x == 0 )
                ServletOps.errorBadRequest("SPARQL Update: No 'update=' parameter");
            if ( x != 1 )
                ServletOps.errorBadRequest("SPARQL Update: Multiple 'update=' parameters");

            String requestStr = action.getRequestParameter(paramUpdate);
            if ( requestStr == null )
                requestStr = action.getRequestParameter(paramRequest);
            if ( requestStr == null )
                ServletOps.errorBadRequest("SPARQL Update: No update= in HTML form");
            validate(action, paramsForm);
            return;
        }

        ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Must be "+contentTypeSPARQLUpdate+" or "+contentTypeHTMLForm+" (got "+ct.getContentTypeStr()+")");
    }

    protected void validate(HttpAction action, Collection<String> params) {
        if ( params != null ) {
            Enumeration<String> en = action.getRequestParameterNames();
            for (; en.hasMoreElements(); ) {
                String name = en.nextElement();
                if ( !params.contains(name) )
                    ServletOps.warning(action, "SPARQL Update: Unrecognized request parameter (ignored): "+name);
            }
        }
    }

    private void executeBody(HttpAction action) {
        InputStream input = null;
        try { input = action.getRequestInputStream(); }
        catch (IOException ex) { ServletOps.errorOccurred(ex); }

        if ( action.verbose ) {
            // Verbose mode only .... capture request for logging (does not scale).
            byte[] bytes = IO.readWholeFile(input);
            input = new ByteArrayInputStream(bytes);
            try {
                String requestStr = Bytes.bytes2string(bytes);
                action.log.info(format("[%d] Update = %s", action.id, ServletOps.formatForLog(requestStr)));
            } catch (Exception ex) {
                action.log.info(format("[%d] Update = <failed to decode>", action.id));
            }
        } else {
            // Some kind of log message to show its an update.
            action.log.info(format("[%d] Update", action.id));
        }
        execute(action, input);
        ServletOps.successNoContent(action);
    }

    private void executeForm(HttpAction action) {
        String requestStr = action.getRequestParameter(paramUpdate);
        if ( requestStr == null )
            requestStr = action.getRequestParameter(paramRequest);

        if ( action.verbose )
            action.log.info(format("[%d] Form update = \n%s", action.id, requestStr));
        // A little ugly because we are taking a copy of the string, but hopefully shouldn't be too big if we are in this code-path
        // If we didn't want this additional copy, we could make the parser take a Reader in addition to an InputStream
        byte[] b = StrUtils.asUTF8bytes(requestStr);
        ByteArrayInputStream input = new ByteArrayInputStream(b);
        requestStr = null;  // free it early at least
        execute(action, input);
        ServletOps.successPage(action,"Update succeeded");
    }

    protected void execute(HttpAction action, InputStream input) {
        UsingList usingList = processProtocol(action.getRequest());

        // If the dsg is transactional, then we can parse and execute the update in a streaming fashion.
        // If it isn't, we need to read the entire update request before performing any updates, because
        // we have to attempt to make the request atomic in the face of malformed updates.
        UpdateRequest req = null;
        if (!action.isTransactional()) {
            try {
                req = UpdateFactory.read(usingList, input, UpdateParseBase, Syntax.syntaxARQ);
            }
            catch (UpdateException ex) { ServletOps.errorBadRequest(ex.getMessage()); return; }
            catch (QueryParseException ex) { ServletOps.errorBadRequest(messageForException(ex)); return; }
        }

        action.beginWrite();
        try {
            if (req == null )
                UpdateAction.parseExecute(usingList, action.getActiveDSG(), input, UpdateParseBase, Syntax.syntaxARQ);
            else
                UpdateAction.execute(req, action.getActiveDSG());
            action.commit();
        } catch (UpdateException ex) {
            ActionLib.consumeBody(action);
            abortSilent(action);
            incCounter(action.getEndpoint().getCounters(), UpdateExecErrors);
            ServletOps.errorOccurred(ex.getMessage());
        } catch (QueryParseException ex) {
            ActionLib.consumeBody(action);
            abortSilent(action);
            String msg = messageForParseException(ex);
            action.log.warn(format("[%d] Parse error: %s", action.id, msg));
            ServletOps.errorBadRequest(messageForException(ex));
        } catch (QueryBuildException|QueryExceptionHTTP ex) {
            ActionLib.consumeBody(action);
            abortSilent(action);
            // Counter inc'ed further out.
            String msg = messageForException(ex);
            action.log.warn(format("[%d] Bad request: %s", action.id, msg));
            ServletOps.errorBadRequest(messageForException(ex));
        } catch (OperationDeniedException ex) {
            ActionLib.consumeBody(action);
            abortSilent(action);
            throw ex;
        } catch (Throwable ex) {
            ActionLib.consumeBody(action);
            if ( ! ( ex instanceof ActionErrorException ) ) {
                abortSilent(action);
                ServletOps.errorOccurred(ex.getMessage(), ex);
            }
        } finally { action.end(); }
    }

    /* [It is an error to supply the using-graph-uri or using-named-graph-uri parameters
     * when using this protocol to convey a SPARQL 1.1 Update request that contains an
     * operation that uses the USING, USING NAMED, or WITH clause.]
     *
     * We will simply capture any using parameters here and pass them to the parser, which will be
     * responsible for throwing an UpdateException if the query violates the above requirement,
     * and will also be responsible for adding the using parameters to update queries that can
     * accept them.
     */
    private UsingList processProtocol(HttpServletRequest request) {
        UsingList toReturn = new UsingList();

        String[] usingArgs = request.getParameterValues(paramUsingGraphURI);
        String[] usingNamedArgs = request.getParameterValues(paramUsingNamedGraphURI);
        if ( usingArgs == null && usingNamedArgs == null )
            return toReturn;
        if ( usingArgs == null )
            usingArgs = new String[0];
        if ( usingNamedArgs == null )
            usingNamedArgs = new String[0];
        // Impossible.
//        if ( usingArgs.length == 0 && usingNamedArgs.length == 0 )
//            return;

        for ( String nodeUri : usingArgs ) {
            toReturn.addUsing(createNode(nodeUri));
        }
        for ( String nodeUri : usingNamedArgs ) {
            toReturn.addUsingNamed(createNode(nodeUri));
        }

        return toReturn;
    }

    private static void abortSilent(HttpAction action) {
        action.abortSilent();
    }

    private static Node createNode(String x) {
        try {
            IRIx iri = resolver.resolve(x);
            return NodeFactory.createURI(iri.str());
        } catch (Exception ex) {
            ServletOps.errorBadRequest("SPARQL Update: bad IRI: "+x);
            return null;
        }

    }
}
