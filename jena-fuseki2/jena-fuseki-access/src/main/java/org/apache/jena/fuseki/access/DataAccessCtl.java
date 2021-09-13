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

import java.util.Collection;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFilteredView;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;

/** A library of operations related to data access security for Fuseki */
public class DataAccessCtl {
    static { JenaSystem.init(); }

//    /**
//     * Flag for whether this is data access controlled or not - boolean false or undef for "not
//     * controlled". This is an alternative to {@link DatasetGraphAccessControl}.
//     * @see #isAccessControlled(DatasetGraph)
//     */
//    public static final Symbol   symControlledAccess        = Symbol.create(VocabSecurity.getURI() + "controlled");

    /**
     * Symbol for the {@link AuthorizationService}.
     * This is an alternative to {@link DatasetGraphAccessControl}.
     * @see #isAccessControlled(DatasetGraph)
     */
    public static final Symbol   symAuthorizationService    = Symbol.create(VocabSecurity.getURI() + "authService");

    /** Get the user from the servlet context via {@link HttpServletRequest#getRemoteUser} */
    public static final Function<HttpAction, String> requestUserServlet = (action)->action.getUser();

    /**
     * Get the user from {@code ?user} query string parameter. Use carefully; for situations where the user name has
     * been authenticated already and is being passed on securely. Also for testing.
     */
    public static final Function<HttpAction, String> paramUserServlet = (action)->action.getRequestParameter("user");

    /**
     * Add data access control information on a {@link DatasetGraph}. This modifies the
     * {@link DatasetGraph}'s {@link Context}.
     */
    private static void addAuthorizatonService(DatasetGraph dsg, AuthorizationService authService) {
        //dsg.getContext().set(symControlledAccess, true);
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
            throw new IllegalArgumentException("DatasetGraph is already wrapped on a DatasetGraphAccessControl with a different AuthorizationService");
        }

        DatasetGraphAccessControl dsg1 = new DatasetGraphAccessControl(dsgBase, reg);
        return dsg1;
    }

    /**
     * Return whether a {@code DatasetGraph} has access control, either because it is wrapped in
     * {@link DatasetGraphAccessControl} or because it has the context settings.
     */
    public static boolean isAccessControlled(DatasetGraph dsg) {
        if ( dsg instanceof DatasetGraphAccessControl )
            return true;
//        if ( dsg.getContext().isDefined(DataAccessCtl.symControlledAccess) )
//            return true;
        if ( dsg.getContext().isDefined(DataAccessCtl.symAuthorizationService) )
            return true;
        return false;
    }

    /**
     * Return a read-only {@link DatasetGraphFilteredView} that fulfils the {@link SecurityContext}.
     * See also {@link SecurityContext#filterTDB} which is more efficient.
     * This code creates a general solution.
     */
    public static DatasetGraph filteredDataset(DatasetGraph dsg, SecurityContext sCxt) {
        if ( sCxt instanceof SecurityContextAllowAll )
            return dsg;
        if ( sCxt instanceof SecurityContextAllowNone ) {
            return new DatasetGraphZero();
        }
        // Nothing special done for SecurityContextAllowNamedGraphs currently.
        // Unfortunately that means find all named graphs.
//        if ( sCxt instanceof SecurityContextAllowNamedGraphs ) {
//        }

        Collection<Node> names = sCxt.visibleGraphs();
        if ( names == null )
            // TODO does not scale.
            names = Iter.toList(dsg.listGraphNodes());

        return new DatasetGraphFilteredView(dsg, sCxt.predicateQuad(), sCxt.visibleGraphs());
    }
}
