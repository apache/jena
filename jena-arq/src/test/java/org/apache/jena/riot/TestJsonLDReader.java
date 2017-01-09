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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.lang.JsonLDReader;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.utils.JsonUtils;

public class TestJsonLDReader {

    @Test public final void test() {
        try {
            String jsonld = "{\"@id\":\"_:b0\",\"@type\":\"http://schema.org/Person\",\"name\":\"John Doe\",\"@context\":\"http://schema.org/\"}";
            StringReader reader = new StringReader(jsonld);
            Model m = ModelFactory.createDefaultModel();
            m.read(reader, null, "JSON-LD");
            m.write(System.out, "TURTLE");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
   
    /** test using the jena Context mechanism to pass the jsonld "@context" */
    @Test public final void replaceAtContextTest() throws JsonGenerationException, IOException {
         // some jsonld using schema.org's URI as "@context"
        String jsonld = "{\"@id\":\"_:b0\",\"@type\":\"Person\",\"name\":\"John Doe\",\"@context\":\"http://schema.org/\"}";
        
        // a subset of schema.org that can be used as @context for jsonld
        String jsonldContext = "{\"name\":{\"@id\":\"http://schema.org/name\"},\"Person\": {\"@id\": \"http://schema.org/Person\"}}";
        
        // pass the jsonldContext to the read using a jena Context
        Context jenaCtx = new Context();
        Object jsonldContextAsMap = JsonUtils.fromInputStream(new ByteArrayInputStream(jsonldContext.getBytes(StandardCharsets.UTF_8)));
        jenaCtx.set(JsonLDReader.JSONLD_CONTEXT, jsonldContextAsMap);
        
        // read the jsonld, replacing its "@context"
        InputStream in = new ByteArrayInputStream(jsonld.getBytes(StandardCharsets.UTF_8));
        Dataset ds = DatasetFactory.create();
        ReaderRIOT reader = RDFDataMgr.createReader(Lang.JSONLD);
        reader.read(in, null, null, StreamRDFLib.dataset(ds.asDatasetGraph()), jenaCtx);
        
        Model m = ds.getDefaultModel();
        m.write(System.out, "TTL");
        
        assertTrue(m.contains(null, RDF.type, m.createResource("http://schema.org/Person")));
        assertTrue(m.contains(null, m.createProperty("http://schema.org/name"), "John Doe"));
     }
 }
