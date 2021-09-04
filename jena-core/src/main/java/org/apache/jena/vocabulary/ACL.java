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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;

public class ACL {
/**
    Basic Access Control ontology.
*/    private static final Model m = ModelFactory.createDefaultModel();
    public static final String NS = "http://www.w3.org/ns/auth/acl#";

    public static final Resource APPEND = m.createResource(NS+"Append");
    public static final Resource WRITE = m.createResource(NS+"Write");
    public static final Resource CONTROL = m.createResource(NS+"Control");
    public static final Resource ACCESS = m.createResource(NS+"Access");
    public static final Resource AUTHORIZATION = m.createResource(NS+"Authorization");
    public static final Resource AUTHENTICATEDAGENT = m.createResource(NS+"AuthenticatedAgent");
    public static final Resource ORIGIN = m.createResource(NS+"Origin");
    public static final Resource READ = m.createResource(NS+"Read");
    public static final Property accessControl = m.createProperty(NS+"accessControl");
    public static final Property accessTo = m.createProperty(NS+"accessTo");
    public static final Property delegates = m.createProperty(NS+"delegates");
    public static final Property mode = m.createProperty(NS+"mode");
    public static final Property agentClass = m.createProperty(NS+"agentClass");
    public static final Property origin = m.createProperty(NS+"origin");
    public static final Property _default = m.createProperty(NS+"_default");
    public static final Property agent = m.createProperty(NS+"agent");
    public static final Property agentGroup = m.createProperty(NS+"agentGroup");
    public static final Property accessToClass = m.createProperty(NS+"accessToClass");
    public static final Property defaultForNew = m.createProperty(NS+"defaultForNew");
    public static final Property owner = m.createProperty(NS+"owner");
}
