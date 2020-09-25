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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdOptions;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class TestJsonLDReader {

    @Test
    public final void simpleReadTest() throws IOException {
        String jsonld = someSchemaDorOrgJsonld();
        Dataset ds = jsonld2dataset(jsonld, null);
        assertJohnDoeIsOK(ds.getDefaultModel());
    }

    /**
     * Test using the jena Context mechanism to pass the jsonld "@context"
     */
    @Test
    public final void overrideAtContextTest() throws JsonGenerationException, IOException {
        // some jsonld using schema.org's URI as "@context"
        String jsonld = someSchemaDorOrgJsonld();

        // pass the jsonldContext to the read using a jena Context
        JsonLDReadContext jenaCtx = new JsonLDReadContext();
        jenaCtx.setJsonLDContext(schemaOrgResolvedContext());

        // read the jsonld, replacing its "@context"
        Dataset ds = jsonld2dataset(jsonld, jenaCtx);

        // check ds is correct
        assertJohnDoeIsOK(ds.getDefaultModel());
    }

    @Test
    public final void overrideJsonLdOptions() throws JsonGenerationException, IOException {
        // some jsonld using a (fake) http://pseudo.schema.org's URI as "@context"
        String jsonld = "{\"@id\":\"_:b0\",\"@type\":\"Person\",\"name\":\"John Doe\",\"@context\":\"http://pseudo.schema.org\"}";

        JsonLdOptions options = new JsonLdOptions();
        DocumentLoader dl = new DocumentLoader();
        dl.addInjectedDoc("http://pseudo.schema.org", String.format("{ \"@context\": %s }", schemaOrgResolvedContext()));
        options.setDocumentLoader(dl);

        // pass the jsonldContext and JsonLdOptions to the read using a jena Context
        JsonLDReadContext jenaCtx = new JsonLDReadContext();
        jenaCtx.setOptions(options);

        // read the jsonld, replacing its "@context"
        Dataset ds = jsonld2dataset(jsonld, jenaCtx);

        // check ds is correct
        assertJohnDoeIsOK(ds.getDefaultModel());
    }

    //
    //
    //

    /**
     * Reading some jsonld String, using a Context
     * @return a new Dataset
     * @throws IOException
     */
    private Dataset jsonld2dataset(String jsonld, Context jenaCtx) throws IOException {
        Dataset ds = DatasetFactory.create();

        try (InputStream in = new ByteArrayInputStream(jsonld.getBytes(StandardCharsets.UTF_8))) {
            RDFParser.create()
                    .source(in)
                    .errorHandler(ErrorHandlerFactory.errorHandlerNoLogging)
                    .lang(Lang.JSONLD)
                    .context(jenaCtx)
                    .parse(ds.asDatasetGraph());
        }

        return ds;
    }

    /**
     * Example data
     */
    private String someSchemaDorOrgJsonld() {
        return String.format("{\"@id\": \"_:b0\", \"@type\": \"Person\", \"name\": \"John Doe\", %s }", schemaOrgContext());
    }

    private String schemaOrgContext() {
        return "\"@context\": \"http://schema.org/\"";
    }

    // a subset of schema.org that can be used as @context for jsonld
    private String schemaOrgResolvedContext() {
        return "{\"name\":{\"@id\":\"http://schema.org/name\"},\"Person\": {\"@id\": \"http://schema.org/Person\"}}";

    }

    /**
     * Checking that the data loaded from someSchemaDorOrgJsonld into a model, is OK
     */
    private void assertJohnDoeIsOK(Model m) {
        assertTrue(m.contains(null, RDF.type, m.createResource("http://schema.org/Person")));
        assertTrue(m.contains(null, m.createProperty("http://schema.org/name"), "John Doe"));
    }


}
