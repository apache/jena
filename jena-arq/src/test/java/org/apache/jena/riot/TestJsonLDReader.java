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

package org.apache.jena.riot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestJsonLDReader {

    // These tests fail under some java11 (but not java17)
    // for RIOT default JSON-LD 1.1 because Titanium contacts schema.org
    // with java.net.http/HTTP2 (default version setting)
    // which fails.

    @Test
    public final void simpleReadTest() throws IOException {
        String jsonld = someSchemaDotOrgJsonld();
        Dataset ds = jsonld2dataset(jsonld, null, Lang.JSONLD);
        assertJohnDoeIsOK(ds.getDefaultModel());
    }

    @Test
    public void testJsonLdBase() {
        // GH-1451
        //    {
        //        "@id": "./relative",
        //        "@type": "RelType",
        //        "http://example/p": { "@id" : "#frag" }
        //     }
        String jsonld = "{ '@id': './relative', '@type': 'RelType', 'http://example/p': { '@id' : '#frag' } }";
        jsonld = jsonld.replaceAll("'",  "\"");
        Graph g = RDFParser.fromString(jsonld, Lang.JSONLD).base("http://base/abc").toGraph();
        assertNotNull(g);
        Triple t = SSE.parseTriple("( <http://base/relative> <http://example/p> <http://base/abc#frag> )");
        assertTrue(g.contains(t));
    }

    /**
     * Reading some jsonld String, using a Context
     * @return a new Dataset
     * @throws IOException
     */
    private Dataset jsonld2dataset(String jsonld, Context jenaCtx, Lang lang) throws IOException {
        Dataset ds = DatasetFactory.create();
        RDFParser.create()
            .fromString(jsonld)
            .errorHandler(ErrorHandlerFactory.errorHandlerNoLogging)
            .lang(lang)
            // Only needed for JSON-LD 1.0 (jsonld-java) usage.
            // [JSONLD 1.0] Remove
            .context(jenaCtx)
            .parse(ds.asDatasetGraph());
        return ds;
    }

    /**
     * Example data
     */
    private String someSchemaDotOrgJsonld() {
        return String.format("{\"@id\": \"_:b0\", \"@type\": \"Person\", \"name\": \"John Doe\", %s }", schemaOrgContext());
    }

    private String schemaOrgContext() {
        return "\"@context\": \"https://schema.org/\"";
    }

    // a subset of schema.org that can be used as @context for jsonld
    private String schemaOrgResolvedContext() {
        return "{\"name\":{\"@id\":\"https://schema.org/name\"},\"Person\": {\"@id\": \"http://schema.org/Person\"}}";
    }

    private static Resource person1 = ResourceFactory.createResource("http://schema.org/Person");
    private static Resource person2 = ResourceFactory.createResource("https://schema.org/Person");
    private static Property name1 = ResourceFactory.createProperty("http://schema.org/name");
    private static Property name2 = ResourceFactory.createProperty("https://schema.org/name");

    /**
     * Checking that the data loaded from someSchemaDorOrgJsonld into a model, is OK
     */
    private void assertJohnDoeIsOK(Model m) {
        assertTrue(m.contains(null, RDF.type, person1) || m.contains(null, RDF.type, person2));
        assertTrue(m.contains(null, name1, "John Doe") || m.contains(null, name2, "John Doe"));
    }
}
