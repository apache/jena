/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.vector.assembler.VectorAssembler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestVectorEndToEnd {
    private HttpServer server;
    private Dataset dataset;

    @BeforeEach
    public void before() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/embeddings", this::handleEmbeddings);
        server.start();

        dataset = createDataset("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
    }

    @AfterEach
    public void after() {
        if (dataset != null)
            dataset.close();
        if (server != null)
            server.stop(0);
    }

    @Test
    public void vectorQueryRanksByMockEmbeddings() {
        dataset.begin(ReadWrite.WRITE);
        dataset.getDefaultModel().read(new StringReader(StrUtils.strjoinNL(
                "@prefix ex: <http://example/> .",
                "ex:item1 ex:label \"cat animal\" .",
                "ex:item2 ex:label \"car vehicle\" ."
        )), "", "TURTLE");
        dataset.commit();
        dataset.end();

        String query = StrUtils.strjoinNL(
                "PREFIX ex: <http://example/>",
                "PREFIX vector: <http://jena.apache.org/vector#>",
                "SELECT ?s ?score {",
                "  (?s ?score) vector:query (\"kitten\" 2) .",
                "}"
        );

        dataset.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet rs = qExec.execSelect();
            List<String> rows = ResultSetFormatter.toList(rs).stream()
                    .map(qs -> qs.getResource("s").getURI())
                    .toList();
            assertEquals(List.of("http://example/item1", "http://example/item2"), rows);
        } finally {
            dataset.end();
        }
    }

    @Test
    public void vectorDeleteRemovesIndexedDocument() {
        dataset.begin(ReadWrite.WRITE);
        dataset.getDefaultModel().read(new StringReader(StrUtils.strjoinNL(
                "@prefix ex: <http://example/> .",
                "ex:item1 ex:label \"cat animal\" ."
        )), "", "TURTLE");
        dataset.commit();
        dataset.end();

        dataset.begin(ReadWrite.WRITE);
        dataset.getDefaultModel().removeAll(
                dataset.getDefaultModel().createResource("http://example/item1"),
                dataset.getDefaultModel().createProperty("http://example/label"),
                null);
        dataset.commit();
        dataset.end();

        String query = StrUtils.strjoinNL(
                "PREFIX vector: <http://jena.apache.org/vector#>",
                "SELECT ?s { (?s) vector:query (\"kitten\" 10) }"
        );
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qExec = QueryExecutionFactory.create(query, dataset)) {
            assertTrue(ResultSetFormatter.toList(qExec.execSelect()).isEmpty());
        } finally {
            dataset.end();
        }
    }

    private Dataset createDataset(String endpoint) {
        String spec = StrUtils.strjoinNL(
                "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX vector: <http://jena.apache.org/vector#>",
                "PREFIX ex: <http://example/>",
                "",
                "[] ja:loadClass \"org.apache.jena.query.vector.VectorQuery\" .",
                "vector:VectorDataset rdfs:subClassOf ja:RDFDataset .",
                "vector:VectorIndexLucene rdfs:subClassOf vector:VectorIndex .",
                "",
                "ex:dataset a vector:VectorDataset ;",
                "    vector:dataset ex:baseDataset ;",
                "    vector:index ex:index .",
                "",
                "ex:baseDataset a ja:RDFDataset ;",
                "    ja:defaultGraph ex:graph .",
                "",
                "ex:graph a ja:MemoryModel .",
                "",
                "ex:index a vector:VectorIndexLucene ;",
                "    vector:directory \"mem\" ;",
                "    vector:dimension 3 ;",
                "    vector:similarity vector:cosine ;",
                "    vector:textPredicate ex:label ;",
                "    vector:embeddingProvider ex:embeddings .",
                "",
                "ex:embeddings a vector:OpenAICompatibleEmbeddings ;",
                "    vector:endpoint \"" + endpoint + "\" ;",
                "    vector:model \"mock-embedding\" ."
        );
        Reader reader = new StringReader(spec);
        Model model = ModelFactory.createDefaultModel();
        model.read(reader, "", "TURTLE");
        VectorAssembler.init();
        Resource root = model.getResource("http://example/dataset");
        return (Dataset)Assembler.general().open(root);
    }

    private void handleEmbeddings(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        JsonObject request = JSON.parse(exchange.getRequestBody());
        List<String> inputs = inputs(request.get("input"));
        JsonObject response = JsonBuilder.buildObject(b -> {
            b.pair("object", "list");
            b.key("data").startArray();
            for (int i = 0; i < inputs.size(); i++) {
                b.startObject();
                b.pair("object", "embedding");
                b.pair("index", i);
                b.key("embedding").startArray();
                for (float value : vectorFor(inputs.get(i)))
                    b.value(value);
                b.finishArray();
                b.finishObject();
            }
            b.finishArray();
        });
        byte[] bytes = JSON.toStringFlat(response).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static List<String> inputs(JsonValue input) {
        if (input.isString())
            return List.of(input.getAsString().value());
        JsonArray array = input.getAsArray();
        List<String> inputs = new ArrayList<>();
        for (JsonValue value : array)
            inputs.add(value.getAsString().value());
        return inputs;
    }

    private static float[] vectorFor(String text) {
        String t = text.toLowerCase();
        if (t.contains("cat") || t.contains("kitten"))
            return new float[] { 1, 0, 0 };
        if (t.contains("car") || t.contains("vehicle"))
            return new float[] { 0, 1, 0 };
        return new float[] { 0, 0, 1 };
    }
}
