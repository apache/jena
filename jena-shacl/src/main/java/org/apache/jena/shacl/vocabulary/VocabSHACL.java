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

package org.apache.jena.shacl.vocabulary;

import org.apache.jena.iri.IRI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shacl.ShaclException;

/**
 * SHACL related vocabulary, not the {@link SHACL} namespace.
 */
public class VocabSHACL {

    private static String NS = "http://jena.apache.org/shacl#";
    private static Model model = ModelFactory.createDefaultModel();

    public static Resource opValidate = resource("validate");

    private static Resource resource(String localname) { return model.createResource(iri(localname)); }
    private static Property property(String localname) { return model.createProperty(iri(localname)); }

    private static String iri(String localname) {
        String uri = NS + localname;
        IRI iri = IRIResolver.parseIRI(uri);
        if ( iri.hasViolation(true) )
            throw new ShaclException("Bad IRI: "+iri);
        if ( ! iri.isAbsolute() )
            throw new ShaclException("Bad IRI: "+iri);
        return uri;
    }
}
