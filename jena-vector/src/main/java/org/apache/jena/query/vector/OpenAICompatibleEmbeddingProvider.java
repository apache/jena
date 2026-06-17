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

import static org.apache.jena.riot.web.HttpNames.hAccept;
import static org.apache.jena.riot.web.HttpNames.hAuthorization;
import static org.apache.jena.riot.web.HttpNames.hContentType;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.WebContent;

public class OpenAICompatibleEmbeddingProvider implements EmbeddingProvider {
    private final String endpoint;
    private final String model;
    private final String apiKeyEnv;
    private final int batchSize;
    private final HttpClient httpClient;

    public OpenAICompatibleEmbeddingProvider(String endpoint, String model, String apiKeyEnv, int batchSize) {
        this(endpoint, model, apiKeyEnv, batchSize, null);
    }

    public OpenAICompatibleEmbeddingProvider(String endpoint, String model, String apiKeyEnv, int batchSize, HttpClient httpClient) {
        this.endpoint = normalizeEndpoint(Objects.requireNonNull(endpoint, "endpoint"));
        this.model = Objects.requireNonNull(model, "model");
        this.apiKeyEnv = apiKeyEnv;
        this.batchSize = batchSize > 0 ? batchSize : 1;
        this.httpClient = httpClient;
    }

    @Override
    public List<float[]> embed(List<String> inputs) {
        if (inputs.isEmpty())
            return List.of();
        List<float[]> output = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i += batchSize) {
            int end = Math.min(inputs.size(), i + batchSize);
            output.addAll(embedBatch(inputs.subList(i, end)));
        }
        return output;
    }

    private List<float[]> embedBatch(List<String> inputs) {
        JsonObject requestJson = JsonBuilder.buildObject(b -> {
            b.pair("model", model);
            b.key("input").startArray();
            inputs.forEach(b::value);
            b.finishArray();
            b.pair("encoding_format", "float");
        });

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint + "/embeddings"))
                .POST(BodyPublishers.ofString(JSON.toStringFlat(requestJson)))
                .header(hContentType, WebContent.contentTypeJSON)
                .header(hAccept, WebContent.contentTypeJSON);

        String apiKey = apiKey();
        if (apiKey != null && !apiKey.isBlank())
            builder.header(hAuthorization, "Bearer " + apiKey);

        HttpResponse<InputStream> response = HttpLib.execute(client(), builder.build());
        JsonObject json = JSON.parse(response.body());
        return parseEmbeddings(json, inputs.size());
    }

    private HttpClient client() {
        return httpClient != null ? httpClient : HttpEnv.getHttpClient(endpoint);
    }

    private String apiKey() {
        if (apiKeyEnv == null || apiKeyEnv.isBlank())
            return null;
        return System.getenv(apiKeyEnv);
    }

    private static String normalizeEndpoint(String endpoint) {
        String x = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        if (!x.endsWith("/v1"))
            x = x + "/v1";
        return x;
    }

    private static List<float[]> parseEmbeddings(JsonObject json, int expectedSize) {
        JsonArray data = json.get("data").getAsArray();
        List<JsonObject> entries = new ArrayList<>();
        for (JsonValue value : data)
            entries.add(value.getAsObject());
        entries.sort(Comparator.comparingInt(OpenAICompatibleEmbeddingProvider::indexOf));

        if (entries.size() != expectedSize)
            throw new VectorException("Embedding endpoint returned " + entries.size() + " vectors for " + expectedSize + " inputs");

        List<float[]> results = new ArrayList<>(entries.size());
        for (JsonObject entry : entries)
            results.add(parseVector(entry.get("embedding").getAsArray()));
        return results;
    }

    private static int indexOf(JsonObject object) {
        JsonValue index = object.get("index");
        return index == null ? 0 : index.getAsNumber().value().intValue();
    }

    private static float[] parseVector(JsonArray array) {
        float[] vector = new float[array.size()];
        for (int i = 0; i < array.size(); i++)
            vector[i] = array.get(i).getAsNumber().value().floatValue();
        return vector;
    }
}
