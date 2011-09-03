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

package org.openjena.fuseki.server;

import org.openjena.fuseki.FusekiException ;
import org.openjena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;

public class FusekiVocab
{
    public static String NS = "http://jena.apache.org/fuseki#" ;
    
    public static final Node pServiceName = property("serviceName") ; 
    
    
    private static Node property(String localname)
    {
        String uri = NS+localname ;
        IRI iri = IRIResolver.parseIRI(uri) ;
        if ( iri.hasViolation(true) )
            throw new FusekiException("Bad IRI: "+iri) ;
        if ( ! iri.isAbsolute() )
            throw new FusekiException("Bad IRI: "+iri) ;
        
        return Node.createURI(uri) ;
    }
}

