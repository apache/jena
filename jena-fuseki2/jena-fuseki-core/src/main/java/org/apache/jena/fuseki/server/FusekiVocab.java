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
import org.apache.jena.iri.IRI;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.system.IRIResolver;

public class FusekiVocab
{
    public static String NS = "http://jena.apache.org/fuseki#";
    private static Model model = ModelFactory.createDefaultModel();

    public static final Resource tServer            = resource("Server");

    public static final Resource fusekiService      = resource("Service");

    public static final Property pServices          = property("services");
    public static final Property pServiceName       = property("name");
    
    public static final Property pAllowedUsers      = property("allowedUsers");
    public static final Property pPasswordFile      = property("passwd");
    public static final Property pRealm             = property("realm");
    public static final Property pAuth              = property("auth");
    
    // Server endpoints.
    public static final Property pServerPing        = property("pingEP");
    public static final Property pServerStats       = property("statsEP");

    public static final Property pServiceQueryEP                = property("serviceQuery");
    public static final Property pServiceUpdateEP               = property("serviceUpdate");
    public static final Property pServiceUploadEP               = property("serviceUpload");
    public static final Property pServiceReadWriteGraphStoreEP  = property("serviceReadWriteGraphStore");
    public static final Property pServiceReadGraphStoreEP       = property("serviceReadGraphStore");
    public static final Property pServiceReadWriteQuadsEP       = property("serviceReadWriteQuads");
    public static final Property pServiceReadQuadsEP            = property("serviceReadQuads");

    public static final Property pAllowTimeoutOverride          = property("allowTimeoutOverride");
    public static final Property pMaximumTimeoutOverride        = property("maximumTimeoutOverride");
    
    // Internal
    
    private static final String stateNameActive     = DataServiceStatus.ACTIVE.name;
    private static final String stateNameOffline    = DataServiceStatus.OFFLINE.name;
    private static final String stateNameClosing    = DataServiceStatus.CLOSING.name;
    private static final String stateNameClosed     = DataServiceStatus.CLOSED.name;
    
    public static final Resource stateActive        = resource(stateNameActive);
    public static final Resource stateOffline       = resource(stateNameOffline);
    public static final Resource stateClosing       = resource(stateNameClosing);
    public static final Resource stateClosed        = resource(stateNameClosed);
    
//    public static final Property pStatus            = property("status");

    private static Resource resource(String localname) { return model.createResource(iri(localname)); }
    private static Property property(String localname) { return model.createProperty(iri(localname)); }
        
    private static String iri(String localname)
    {
        String uri = NS+localname;
        IRI iri = IRIResolver.parseIRI(uri);
        if ( iri.hasViolation(true) )
            throw new FusekiException("Bad IRI: "+iri);
        if ( ! iri.isAbsolute() )
            throw new FusekiException("Bad IRI: "+iri);
        
        return uri;
    }
}

