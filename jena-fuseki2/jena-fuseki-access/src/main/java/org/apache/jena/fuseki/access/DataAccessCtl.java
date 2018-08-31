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

package org.apache.jena.fuseki.access;

import java.util.function.Function;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServiceDispatchRegistry;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFilteredView;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;
import org.eclipse.jetty.security.SecurityHandler;

/** A library of operations related to data access security for Fuseki */  
public class DataAccessCtl {
    static { JenaSystem.init(); }
    
    /**
     * Flag for whether this is data access controlled or not - boolean false or undef for "not
     * controlled". This is an alternative to {@link DatasetGraphAccessControl}.
     */
    public static final Symbol   symControlledAccess        = Symbol.create(VocabSecurity.getURI() + "controlled");
    
    /**
     * Symbol for the {@link AuthorizationService}. Must be present if
     * {@link #symControlledAccess} indicates data access control.
     * This is an alternative to {@link DatasetGraphAccessControl}.
     */
    public static final Symbol   symAuthorizationService    = Symbol.create(VocabSecurity.getURI() + "authService");

    /** Get the user from the servlet context via {@link HttpServletRequest#getRemoteUser} */ 
    public static final Function<HttpAction, String> requestUserServlet = (action)->action.request.getRemoteUser();

    /**
     * Get the user from {@code ?user} query string parameter. Use carefully; for situations where the user name has
     * been authenticated already and is being passed on securely. Also for testing.
     */
    public static final Function<HttpAction, String> paramUserServlet = (action)->action.request.getParameter("user");

    /**
     * Add data access control information on a {@link DatasetGraph}. This modifies the
     * {@link DatasetGraph}'s {@link Context}.
     */
    private static void addAuthorizatonService(DatasetGraph dsg, AuthorizationService authService) {
        dsg.getContext().set(symControlledAccess, true);
        dsg.getContext().set(symAuthorizationService, authService);
    }

    /**
     * Return a {@link DatasetGraph} with added data access control. 
     * Use of the original {@code DatasetGraph} is not controlled.
     */
    public static Dataset controlledDataset(Dataset dsBase, AuthorizationService reg) {
        DatasetGraph dsg = controlledDataset(dsBase.asDatasetGraph(), reg);
        return DatasetFactory.wrap(dsg);
    }
    
    /**
     * Return a {@link DatasetGraph} with added data access control. Use of the original
     * {@code DatasetGraph} is not controlled.
     */
    public static DatasetGraph controlledDataset(DatasetGraph dsgBase, AuthorizationService reg) {
        if ( dsgBase instanceof DatasetGraphAccessControl ) {
            DatasetGraphAccessControl dsgx = (DatasetGraphAccessControl)dsgBase;
            if ( reg == dsgx.getAuthService() )
                return dsgx;
            throw new IllegalArgumentException("DatasetGraph is alerady wrapped on a DatasetGraphAccessControl with a different AuthorizationService");
        }
        
        DatasetGraphAccessControl dsg1 = new DatasetGraphAccessControl(dsgBase, reg);
        return dsg1;
    } 

    /**
     * Return a {@code FusekiServer.Builder} setup for data access control
     * but with no Jetty security handler.
     */
    public static FusekiServer.Builder fusekiBuilder(Function<HttpAction, String> determineUser) {
        return fusekiBuilder(null, determineUser);
    }

    /**
     * Return a {@code FusekiServer.Builder} setup for data access control and with a
     * Jetty security handler.
     */
    public static FusekiServer.Builder fusekiBuilder(SecurityHandler securityHandler, Function<HttpAction, String> determineUser) {
        FusekiServer.Builder builder = FusekiServer.create();
        if ( securityHandler != null )
            builder.securityHandler(securityHandler);
        // Replace the standard operations with the SecurityRegistry processing ones. 
        builder.registerOperation(Operation.Query, WebContent.contentTypeSPARQLQuery, new Filtered_SPARQL_QueryDataset(determineUser));
        builder.registerOperation(Operation.GSP_R, new Filtered_SPARQL_GSP_R(determineUser));
        builder.registerOperation(Operation.Quads_R, new Filtered_REST_Quads_R(determineUser));
        return builder;
    }

    /**
     * Modify in-place an existing {@link FusekiServer} so that the read-operations for
     * query/GSP/Quads go to the data-filtering versions of the {@link ActionService ActionServices}.
     * (It is better to create the server via {@link #DataAccessCtl.builder} first rather than modify afterwards.) 
     */
    public static void modifyForAccessCtl(FusekiServer server, Function<HttpAction, String> determineUser) {
        /* 
         * Reconfigure standard Jena Fuseki, replacing the default implementation of "query"
         * with a filtering one.  This for this server only. 
         */
        // The mapping operation to handler is in the ServiceDispatchRegistry and is per
        // server (per servlet context). "registerOrReplace" would be a better name,
        ActionService queryServletAccessFilter = new Filtered_SPARQL_QueryDataset(determineUser);
        ServletContext cxt = server.getServletContext();
        ServiceDispatchRegistry.get(cxt).register(Operation.Query, WebContent.contentTypeSPARQLQuery, queryServletAccessFilter);
        ServiceDispatchRegistry.get(cxt).register(Operation.GSP_R, null, new Filtered_SPARQL_GSP_R(determineUser));
        ServiceDispatchRegistry.get(cxt).register(Operation.Quads_R, null, new Filtered_REST_Quads_R(determineUser));
    }
    
    /**
     * Return whether a {@code DatasetGraph} has access control, either because it is wrapped in
     * {@link DatasetGraphAccessControl} or because it has the context settings.
     */
    public static boolean isAccessControlled(DatasetGraph dsg) {
        if ( dsg instanceof DatasetGraphAccessControl )
            return true;
        if ( dsg.getContext().isDefined(DataAccessCtl.symControlledAccess) )
            return true;
        if ( dsg.getContext().isDefined(DataAccessCtl.symAuthorizationService) )
            return true;
        return false;
    }

    /**
     * Return a read-only {@link DatasetGraphFilteredView} that fulfils the {@link SecurityContext}.
     */
    public static DatasetGraphFilteredView filteredDataset(DatasetGraph dsg, SecurityContext sCxt) {
        return new DatasetGraphFilteredView(dsg, sCxt.predicateQuad(), sCxt.visibleGraphs());
    }
}
