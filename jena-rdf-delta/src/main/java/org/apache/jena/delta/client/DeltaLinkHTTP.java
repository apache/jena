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

package org.apache.jena.delta.client;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.system.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of DeltaLink that communicates with a patch log server over HTTP.
 */
public class DeltaLinkHTTP implements DeltaLink {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaLinkHTTP.class);
    
    private final String serverURL;
    private final HttpClient httpClient;
    private final List<PatchLogListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Create a new DeltaLinkHTTP.
     * @param serverURL The URL of the patch log server
     */
    public DeltaLinkHTTP(String serverURL) {
        this.serverURL = serverURL.endsWith("/") ? serverURL : serverURL + "/";
        this.httpClient = HttpClient.newBuilder().build();
    }
    
    @Override
    public String getServerURL() {
        return serverURL;
    }
    
    @Override
    public List<String> listDatasets() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + "$/list"))
                .GET()
                .header("Accept", "application/json")
                .build();
                
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new DeltaException("Failed to list datasets: " + response.statusCode());
            }
            
            JsonObject obj = JSON.parse(response.body());
            JsonArray datasets = obj.get("datasets").getAsArray();
            
            List<String> result = new ArrayList<>();
            for (JsonValue v : datasets) {
                result.add(v.getAsObject().get("name").getAsString().value());
            }
            
            return result;
        } catch (IOException | InterruptedException e) {
            throw new DeltaException("Error listing datasets", e);
        }
    }
    
    @Override
    public PatchLogInfo getPatchLogInfo(String dsName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + "$/info/" + dsName))
                .GET()
                .header("Accept", "application/json")
                .build();
                
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                return null;
            }
            
            if (response.statusCode() != 200) {
                throw new DeltaException("Failed to get patch log info: " + response.statusCode());
            }
            
            JsonObject obj = JSON.parse(response.body());
            
            String name = obj.get("name").getAsString().value();
            String current = obj.get("version").getAsString().value();
            
            return new PatchLogInfo(name, Id.fromString(current));
        } catch (IOException | InterruptedException e) {
            throw new DeltaException("Error getting patch log info", e);
        }
    }
    
    @Override
    public Id createDataset(String dsName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + "$/create/" + dsName))
                .POST(BodyPublishers.noBody())
                .build();
                
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new DeltaException("Failed to create dataset: " + response.statusCode());
            }
            
            JsonObject obj = JSON.parse(response.body());
            String version = obj.get("version").getAsString().value();
            
            return Id.fromString(version);
        } catch (IOException | InterruptedException e) {
            throw new DeltaException("Error creating dataset", e);
        }
    }
    
    @Override
    public Id append(String dsName, RDFPatch patch, Id expected) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            RDFPatchOps.write(baos, patch);
            
            String url = serverURL + "$/append/" + dsName;
            if (expected != null) {
                url += "?version=" + expected.toString();
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(BodyPublishers.ofByteArray(baos.toByteArray()))
                .header("Content-Type", "application/rdf-patch")
                .build();
                
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new DeltaException("Failed to append patch: " + response.statusCode());
            }
            
            JsonObject obj = JSON.parse(response.body());
            String version = obj.get("version").getAsString().value();
            
            return Id.fromString(version);
        } catch (IOException | InterruptedException e) {
            throw new DeltaException("Error appending patch", e);
        }
    }
    
    @Override
    public List<RDFPatch> fetch(String dsName, Id version) {
        try {
            String url = serverURL + "$/fetch/" + dsName;
            if (version != null) {
                url += "?version=" + version.toString();
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/rdf-patch")
                .build();
                
            HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
            
            if (response.statusCode() != 200) {
                throw new DeltaException("Failed to fetch patches: " + response.statusCode());
            }
            
            List<RDFPatch> patches = new ArrayList<>();
            
            try (InputStream is = response.body()) {
                // The response is a sequence of patches
                while (true) {
                    RDFPatch patch = RDFPatchOps.read(is);
                    if (patch == null)
                        break;
                    patches.add(patch);
                }
            }
            
            return patches;
        } catch (IOException | InterruptedException e) {
            throw new DeltaException("Error fetching patches", e);
        }
    }
    
    @Override
    public RDFPatch fetch(String dsName, Id version, boolean stopOnError) {
        try {
            String url = serverURL + "$/patch/" + dsName + "/" + version.toString();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/rdf-patch")
                .build();
                
            HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
            
            if (response.statusCode() == 404) {
                if (stopOnError)
                    throw new DeltaException("Patch not found: " + version);
                return null;
            }
            
            if (response.statusCode() != 200) {
                throw new DeltaException("Failed to fetch patch: " + response.statusCode());
            }
            
            try (InputStream is = response.body()) {
                return RDFPatchOps.read(is);
            }
        } catch (IOException | InterruptedException e) {
            throw new DeltaException("Error fetching patch", e);
        }
    }
    
    @Override
    public void register(PatchLogListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unregister(PatchLogListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void close() {
        // Nothing to do
    }
}