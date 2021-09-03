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

package org.apache.jena.vocabulary;

import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.ResourceFactory ;

public class ACL {
    
/**
*    Basic Access Control ontology.
*    https://www.w3.org/wiki/WebAccessControl
*/

    public static final String uri = "http://www.w3.org/ns/auth/acl#";

    /** returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI()
        { return uri; }

    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static Resource APPEND = Init.Append();
    public static Resource WRITE = Init.Write();
    public static Resource CONTROL = Init.Control();
    public static Resource ACCESS = Init.Access();
    public static Resource AUTHORIZATION = Init.Authorization();
    public static Resource AUTHENTICATEDAGENT = Init.AuthenticatedAgent();
    public static Resource ORIGIN = Init.Origin();
    public static Resource READ = Init.Read();
    public static final Property accessControl = Init.accessControl();
    public static final Property accessTo = Init.accessTo();
    public static final Property delegates = Init.delegates();
    public static final Property mode = Init.mode();
    public static final Property agentClass = Init.agentClass();
    public static final Property origin = Init.origin();
    public static final Property _default = Init._default();
    public static final Property agent = Init.agent();
    public static final Property agentGroup = Init.agentGroup();
    public static final Property accessToClass = Init.accessToClass();
    public static final Property defaultForNew = Init.defaultForNew();
    public static final Property owner = Init.owner();

    public static class Init {
        public static Resource Append() { return resource( "Append" ); }
        public static Resource Write() { return resource( "Write" ); }
        public static Resource Control() { return resource( "Control" ); }
        public static Resource Access() { return resource( "Access" ); }
        public static Resource Authorization() { return resource( "Authorization" ); }
        public static Resource AuthenticatedAgent() { return resource( "AuthenticatedAgent" ); }
        public static Resource Origin() { return resource( "Origin" ); }
        public static Resource Read() { return resource( "Read" ); }
        public static Property accessControl() { return property( "accessControl" ); }
        public static Property accessTo() { return property( "accessTo" ); }
        public static Property delegates() { return property( "delegates" ); }
        public static Property mode() { return property( "mode" ); }
        public static Property agentClass() { return property( "agentClass" ); }
        public static Property origin() { return property( "origin" ); }
        public static Property _default() { return property( "default" ); }
        public static Property agent() { return property( "agent" ); }
        public static Property agentGroup() { return property( "agentGroup" ); }
        public static Property accessToClass() { return property( "accessToClass" ); }
        public static Property defaultForNew() { return property( "defaultForNew" ); }
        public static Property owner() { return property( "owner" ); }
    }

    public static final class Nodes {
        public static final Node Append = Init.Append().asNode();
        public static final Node Write = Init.Write().asNode();
        public static final Node Control = Init.Control().asNode();
        public static final Node Access = Init.Access().asNode();
        public static final Node Authorization = Init.Authorization().asNode();
        public static final Node AuthenticatedAgent = Init.AuthenticatedAgent().asNode();
        public static final Node Origin = Init.Origin().asNode();
        public static final Node Read = Init.Read().asNode();
        public static final Node accessControl = Init.accessControl().asNode();
        public static final Node accessTo = Init.accessTo().asNode();
        public static final Node delegates = Init.delegates().asNode();
        public static final Node mode = Init.mode().asNode();
        public static final Node agentClass = Init.agentClass().asNode();
        public static final Node origin = Init.origin().asNode();
        public static final Node _default = Init._default().asNode();
        public static final Node agent = Init.agent().asNode();
        public static final Node agentGroup = Init.agentGroup().asNode();
        public static final Node accessToClass = Init.accessToClass().asNode();
        public static final Node defaultForNew = Init.defaultForNew().asNode();
        public static final Node owner = Init.owner().asNode();
    }
}