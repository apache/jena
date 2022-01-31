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

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Target of GSP operations.<br/>
 * Extensions: "?graph=union" and "?graph=default"
 *
 */
public class GraphTarget {

    public final static GraphTarget determineTarget(DatasetGraph dsg, HttpAction action) {
        return determineTarget(dsg, action, false);
    }

    /** With GSP direct naming. */
    public final static GraphTarget determineTargetGSP(DatasetGraph dsg, HttpAction action) {
        return determineTarget(dsg, action, Fuseki.GSP_DIRECT_NAMING);
    }

    private final static GraphTarget determineTarget(DatasetGraph dsg, HttpAction action, boolean allowDirectNaming) {
        // Inside a transaction.
        if ( dsg == null )
            ServletOps.errorOccurred("Internal error : No action graph (not in a transaction?)");
//        if ( ! dsg.isInTransaction() )
//            ServletOps.errorOccurred("Internal error : No transaction");

        boolean dftGraph = GSPLib.getOneOnly(action.getRequest(), HttpNames.paramGraphDefault) != null;
        String uri = GSPLib.getOneOnly(action.getRequest(), HttpNames.paramGraph);

        if ( !dftGraph && uri == null ) {
            // No params - direct naming?
            if ( ! allowDirectNaming )
                ServletOps.errorBadRequest("Neither default graph nor named graph specified");

            // Direct naming.
            String directName = action.getRequestRequestURL().toString();
            if ( action.getRequestRequestURI().equals(action.getDatasetName()) )
                // No name (should have been a quads operations).
                ServletOps.errorBadRequest("Neither default graph nor named graph specified and no direct name");
            Node gn = NodeFactory.createURI(directName);
            return createNamed(dsg, gn);
        }

        if ( dftGraph )
            return createDefault(dsg);
        // Named graph as default
        if ( uri.equals(HttpNames.graphTargetDefault) )
            // But "named" default
            return createDefault(dsg);
        // Named graph - union
        if ( uri.equals(HttpNames.graphTargetUnion) )
            return createUnion(dsg);
        // This logs bad URIs.
        String absUri = resolve0(uri, action);
        return createNamed(dsg, absUri);
    }

    // Resolving a relative URI in ?graph= is a bit murky.
    //   Whether to use the base of the dataset or the dataset+service endpoint name.
    //   How to find the dataset URL when service can be on the dataset directly or a named endpoint.
    //   And will it match in the dataset named graphs anyway?

    /** Check URI, require it to be absolute. */
    private static String resolve0(String uri, HttpAction action) {
        try {
            IRIx iri = IRIx.create(uri);
            if ( ! iri.isReference() )
                action.log.warn(format("[%d] URI is relative: <%s>", action.id, uri));
            return uri;
        } catch (IRIException ex) {
            action.log.warn(format("[%d] Bad URI <%s> : %s", action.id, uri, ex.getMessage()));
            return uri;
        }
    }

    /** Resolve URI. Calculate a base URI from the dataset URI and resolve uri against that.*/
    private static String xresolve(String uriStr, HttpAction action) {
        // Strictly, a bit naughty on the URI resolution, but more sensible.
        // Make the base the URI of the dataset.
        // Strictly, the base includes service and query string but that is unhelpful.
        // wholeRequestURL(request);
        IRIx uri;
        try {
            uri = IRIx.create(uriStr);
            if ( uri.isReference() )
                return uri.str();
        } catch (IRIException ex) {
            FmtLog.warn(Fuseki.actionLog, "Bad URI: '"+uriStr);
            ServletOps.errorBadRequest("Bad IRI: "+uriStr+" : " + ex.getMessage());
            return null;
        }

        String baseStr = action.getRequestRequestURL().toString();
        Endpoint ep = action.getEndpoint();
        if ( ! ep.isUnnamed() && baseStr.endsWith(ep.getName()) ) {
            // Remove endpoint name
            baseStr = baseStr.substring(0, baseStr.length()-ep.getName().length());
        }
        // Make sure it ends in "/", treating the dataset as a container.
        if ( !baseStr.endsWith("/") )
            baseStr = baseStr + "/";
        try {
            IRIx base = IRIx.create(baseStr);
            IRIx uri2 = base.resolve(uri);
            if ( ! uri2.isReference() ) {
                FmtLog.warn(Fuseki.actionLog, "Bad URI (after resolving): '"+uriStr);
                ServletOps.errorBadRequest("Bad IRI: " + uriStr);
            }
            return uri2.str();
        } catch (IRIException ex) {
            ServletOps.errorBadRequest("Bad IRI: "+uriStr+" : " + ex.getMessage());
            return null;
        }
    }

    final private boolean      isDefault;
    final private boolean      isUnion;
    final private DatasetGraph dsg;
    final private Node         graphName;

    static GraphTarget createNamed(DatasetGraph dsg, String graphName) {
        return createNamed(dsg, NodeFactory.createURI(graphName));
    }

    static GraphTarget createNamed(DatasetGraph dsg, Node graphName) {
        return new GraphTarget(false, false, dsg, graphName);
    }

    static GraphTarget createDefault(DatasetGraph dsg) {
        return new GraphTarget(true, false, dsg, null);
    }

    static GraphTarget createUnion(DatasetGraph dsg) {
        return new GraphTarget(false, true, dsg, null);
    }

    /**
     * Create a new GraphTarget which is like the original but aimed at a different
     * DatasetGraph
     */
    static GraphTarget retarget(GraphTarget target, DatasetGraph dsg) {
        GraphTarget target2 = new GraphTarget(target, dsg);
        return target2;
    }

    private GraphTarget(boolean isDefault, boolean isUnion, DatasetGraph dsg, Node graphName) {
        if ( ! isUnion && !isDefault && graphName == null )
            throw new IllegalArgumentException("Inconsistent: not default, union nor graph name");
        else if ( isDefault && graphName != null )
            throw new IllegalArgumentException("Inconsistent: default and a graph name");
        else if ( isUnion && graphName != null )
            throw new IllegalArgumentException("Inconsistent: union and a graph name");
        else if ( isDefault && isUnion )
            throw new IllegalArgumentException("Inconsistent: default and union graph");

        this.isDefault = isDefault;
        this.isUnion = isUnion;
        this.dsg = dsg;
        this.graphName = graphName;
    }

    private GraphTarget(GraphTarget other, DatasetGraph dsg) {
        this.dsg = dsg; // other.dsg; // Retarget
        this.isDefault = other.isDefault;
        this.isUnion = other.isUnion;
        this.graphName = other.graphName;
    }

    public DatasetGraph dataset() { return dsg; }
    public boolean isDefault()    { return isDefault; }
    public boolean isUnion()      { return isUnion; }
    public Node graphName()       { return graphName; }

    /**
     * Get a graph for the action -  this is not a test for graph existence.
     * May return null or an empty graph.
     */
    public Graph graph() {
        if ( isDefault )
            return dsg.getDefaultGraph();
        if ( isUnion )
            return dsg.getUnionGraph();
        return dsg.getGraph(graphName);
    }

    public boolean exists() {
        if ( isDefault || isUnion )
            return true;
        Graph g = graph();
        return g != null && ! g.isEmpty();
    }

    public String label() {
        if ( isDefault )
            return "default";
        if ( isUnion )
            return "union";
        return NodeFmtLib.strTTL(graphName);
    }

    @Override
    public String toString() {
        return "target:"+label();
    }
}
