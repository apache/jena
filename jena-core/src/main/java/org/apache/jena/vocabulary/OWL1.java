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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary definitions from file:vocabularies/owl.owl - OWL ver 1.1.
 */
public class OWL1 {
    /**
     * The namespace of the vocabulary as a string.
     */
    public static final String NS = "http://www.w3.org/2002/07/owl#";

    /**
     * The namespace of the vocabulary as a string
     */
    public static String getURI() {
        return NS;
    }

    // These will use ResourceFactory which creates Resource etc without a specific model.
    // This is safer for complex initialization paths.
    protected static Resource resource(String uri) {
        return ResourceFactory.createResource(NS + uri);
    }

    protected static Property property(String uri) {
        return ResourceFactory.createProperty(NS, uri);
    }

    /**
     * The namespace of the vocabulary as a resource
     */
    public static final Resource NAMESPACE = ResourceFactory.createResource(NS);

    /**
     * A resource that denotes the OWL-full sublanguage of OWL
     */
    public static final Resource FULL_LANG = ResourceFactory.createResource(getURI());

    /**
     * A resource, not officially sanctioned by WebOnt, that denotes the OWL-DL sublanguage of OWL
     */
    public static final Resource DL_LANG = ResourceFactory.createResource("http://www.w3.org/TR/owl-features/#term_OWLDL");

    /**
     * A resource, not officially sanctioned by WebOnt, that denotes the OWL-Lite sublanguage of OWL
     */
    public static final Resource LITE_LANG = ResourceFactory.createResource("http://www.w3.org/TR/owl-features/#term_OWLLite");

    // Vocabulary properties
    ///////////////////////////

    public static final Property maxCardinality = property("maxCardinality");

    public static final Property versionInfo = property("versionInfo");

    public static final Property equivalentClass = property("equivalentClass");

    public static final Property distinctMembers = property("distinctMembers");

    public static final Property oneOf = property("oneOf");

    public static final Property sameAs = property("sameAs");

    public static final Property incompatibleWith = property("incompatibleWith");

    public static final Property minCardinality = property("minCardinality");

    public static final Property complementOf = property("complementOf");

    public static final Property onProperty = property("onProperty");

    public static final Property equivalentProperty = property("equivalentProperty");

    public static final Property inverseOf = property("inverseOf");

    public static final Property backwardCompatibleWith = property("backwardCompatibleWith");

    public static final Property differentFrom = property("differentFrom");

    public static final Property priorVersion = property("priorVersion");

    public static final Property imports = property("imports");

    public static final Property allValuesFrom = property("allValuesFrom");

    public static final Property unionOf = property("unionOf");

    public static final Property hasValue = property("hasValue");

    public static final Property someValuesFrom = property("someValuesFrom");

    public static final Property disjointWith = property("disjointWith");

    public static final Property cardinality = property("cardinality");

    public static final Property intersectionOf = property("intersectionOf");

    // Vocabulary classes
    ///////////////////////////

    public static final Resource Thing = resource("Thing");

    public static final Resource DataRange = resource("DataRange");

    public static final Resource Ontology = resource("Ontology");

    public static final Resource DeprecatedClass = resource("DeprecatedClass");

    public static final Resource AllDifferent = resource("AllDifferent");

    public static final Resource DatatypeProperty = resource("DatatypeProperty");

    public static final Resource SymmetricProperty = resource("SymmetricProperty");

    public static final Resource TransitiveProperty = resource("TransitiveProperty");

    public static final Resource DeprecatedProperty = resource("DeprecatedProperty");

    public static final Resource AnnotationProperty = resource("AnnotationProperty");

    public static final Resource Restriction = resource("Restriction");

    public static final Resource Class = resource("Class");

    public static final Resource OntologyProperty = resource("OntologyProperty");

    public static final Resource ObjectProperty = resource("ObjectProperty");

    public static final Resource FunctionalProperty = resource("FunctionalProperty");

    public static final Resource InverseFunctionalProperty = resource("InverseFunctionalProperty");

    public static final Resource Nothing = resource("Nothing");

    // Vocabulary individuals
    ///////////////////////////


}
