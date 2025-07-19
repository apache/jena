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

package org.apache.jena.ontapi.assemblers;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class OA {
    public static final String NS = "https://jena.apache.org/ontapi/Assembler#";

    public static final Property graph = property("graph");
    public static final Property graphIRI = property("graphIRI");
    public static final Property graphLocation = property("graphLocation");
    public static final Property specificationName = property("specificationName");
    public static final Property personalityName = property("personalityName");
    public static final Property schema = property("schema");
    public static final Property reasonerURL = property("reasonerURL");
    public static final Property reasonerClass = property("reasonerClass");
    public static final Property reasonerFactory = property("reasonerFactory");
    public static final Property rules = property("rules");
    public static final Property rulesFrom = property("rulesFrom");
    public static final Property rule = property("rule");
    public static final Property ontModelSpec = property("ontModelSpec");
    public static final Property baseModel = property("baseModel");
    public static final Property importModels = property("importModels");
    public static final Property documentGraphRepository = property("documentGraphRepository");

    public static final Resource DocumentGraphRepository = resource("DocumentGraphRepository");
    public static final Resource OntSpecification = resource("OntSpecification");
    public static final Resource OntModel = resource("OntModel");
    public static final Resource ReasonerFactory = resource("ReasonerFactory");
    public static final Resource RuleSet = resource("RuleSet");

    private static Resource resource(String localName) {
        return ResourceFactory.createResource(NS + localName);
    }

    private static Property property(String localName) {
        return ResourceFactory.createProperty(NS + localName);
    }
}
