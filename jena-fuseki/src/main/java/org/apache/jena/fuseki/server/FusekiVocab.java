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

import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.system.IRIResolver ;

import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;

public class FusekiVocab
{
    public static String NS = "http://jena.apache.org/fuseki#" ;

    public static final Resource tServer = resource("Server") ;

    public static final Property pServices = property("services") ;
    public static final Property pServiceName = property("name") ;
    
    public static final Property pServiceQueryEP = property("serviceQuery") ;
    public static final Property pServiceUpdateEP = property("serviceUpdate") ;
    public static final Property pServiceUploadEP = property("serviceUpload") ;
    public static final Property pServiceReadWriteGraphStoreEP = property("serviceReadWriteGraphStore") ;
    public static final Property pServiceReadgraphStoreEP = property("serviceReadGraphStore") ;

    public static final Property pAllowTimeoutOverride = property("allowTimeoutOverride");
    public static final Property pMaximumTimeoutOverride = property("maximumTimeoutOverride");

    private static Resource resource(String localname) { return ResourceFactory.createResource(iri(localname)) ; }
    private static Property property(String localname) { return ResourceFactory.createProperty(iri(localname)) ; }
        
    private static String iri(String localname)
    {
        String uri = NS+localname ;
        IRI iri = IRIResolver.parseIRI(uri) ;
        if ( iri.hasViolation(true) )
            throw new FusekiException("Bad IRI: "+iri) ;
        if ( ! iri.isAbsolute() )
            throw new FusekiException("Bad IRI: "+iri) ;
        
        return uri ;
    }
}

