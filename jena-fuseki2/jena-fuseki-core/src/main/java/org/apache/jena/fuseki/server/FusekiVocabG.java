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

package org.apache.jena.fuseki.server;

import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;

/**
 * Fuseki Vocabulary - using {@link Node Nodes}.
 */
public class FusekiVocabG
{
    // Keep in-step with FusekiVocab (same constants, but as model resources and properties).
    private static final String NS = FusekiVocab.NS;
    public static String getURI() { return NS; }

    public static final Node tServer            = resource("Server");

    public static final Node fusekiService      = resource("Service");

    public static final Node pServices          = property("services");

    // Server endpoints.
    public static final Node pServerPing        = property("pingEP");
    public static final Node pServerStats       = property("statsEP");
    public static final Node pServerMetrics     = property("metricsEP");
    public static final Node pServerCompact     = property("compactEP");

    // Server features
    // Fuseki main - servlet context.
    public static final Node pServerContextPath = property("contextPath");

    // Endpoint description.
    public static final Node pServiceName               = property("name");
    public static final Node pEndpointName              = property("name");
    public static final Node pPasswordFile              = property("passwd");
    public static final Node pRealm                     = property("realm");
    public static final Node pAuth                      = property("auth");
    public static final Node pEndpoint                  = property("endpoint");
    public static final Node pOperation                 = property("operation");
    public static final Node pAllowedUsers              = property("allowedUsers");
    public static final Node pTimeout                   = property("timeout");
    public static final Node pImplementation            = property("implementation");
    public static final Node pQueryLimit                = property("queryLimit");
    public static final Node pUnionDefaultGraph         = property("unionDefaultGraph");
    public static final Node pAllowTimeoutOverride      = property("allowTimeoutOverride");
    public static final Node pMaximumTimeoutOverride    = property("maximumTimeoutOverride");
    public static final Node pDataset                   = property("dataset");

    // Endpoint description - old style.
    public static final Node pServiceQueryEP                = property("serviceQuery");
    public static final Node pServiceUpdateEP               = property("serviceUpdate");
    public static final Node pServiceUploadEP               = property("serviceUpload");
    public static final Node pServiceShaclEP                = property("serviceShacl");
    public static final Node pServiceReadWriteGraphStoreEP  = property("serviceReadWriteGraphStore");
    public static final Node pServiceReadGraphStoreEP       = property("serviceReadGraphStore");
    // No longer used.
//    public static final Node pServiceReadWriteQuadsEP       = property("serviceReadWriteQuads");
//    public static final Node pServiceReadQuadsEP            = property("serviceReadQuads");

    // Operation names : the standard operations.
    // "alt" names are the same but using "_" not "-".
    public static final Node opQuery       = resource("query");
    public static final Node opUpdate      = resource("update");
    public static final Node opUpload      = resource("upload");
    public static final Node opGSP_r       = resource("gsp-r");
    public static final Node opGSP_r_alt   = resource("gsp_r");
    public static final Node opGSP_rw      = resource("gsp-rw");
    public static final Node opGSP_rw_alt  = resource("gsp_rw");
    public static final Node opNoOp        = resource("no-op");
    public static final Node opNoOp_alt    = resource("no_op");
    public static final Node opShacl       = resource("shacl");
    public static final Node opPatch       = resource("patch");

    public static final Node opPREFIXES_R       = resource("prefixes-r");
    public static final Node opPREFIXES_RW      = resource("prefixes-rw");

    private static Node resource(String localname) { return NodeFactory.createURI(iri(localname)); }
    private static Node property(String localname) { return NodeFactory.createURI(iri(localname)); }

    private static String iri(String localname) {
        String uri = NS + localname;
        try {
            IRIx iri = IRIx.create(uri);
            if ( ! iri.isReference() )
                throw new FusekiException("Bad IRI (relative): "+uri);
            return uri;
        } catch (IRIException ex) {
            throw new FusekiException("Bad IRI: "+uri);
        }
    }
}
