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

package org.apache.jena.delta.server.http;

import java.io.*;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.server.PatchLogServer;
import org.apache.jena.delta.server.PatchLogServer.LogEntry;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.system.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for exposing a patch log server over HTTP.
 */
public class DeltaServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(DeltaServlet.class);
    
    private final PatchLogServer server;
    
    /**
     * Create a new DeltaServlet.
     * @param server The patch log server to expose
     */
    public DeltaServlet(PatchLogServer server) {
        this.server = server;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Root path - redirect to the list endpoint
                resp.sendRedirect("$/list");
                return;
            }
            
            if (pathInfo.equals("/$/list")) {
                // List all patch logs
                handleListPatchLogs(req, resp);
                return;
            }
            
            if (pathInfo.startsWith("/$/info/")) {
                // Get info about a patch log
                String name = pathInfo.substring(7);
                handleGetPatchLogInfo(name, req, resp);
                return;
            }
            
            if (pathInfo.startsWith("/$/fetch/")) {
                // Get patches from a patch log
                String name = pathInfo.substring(8);
                handleFetchPatches(name, req, resp);
                return;
            }
            
            if (pathInfo.startsWith("/$/patch/")) {
                // Get a specific patch
                String rest = pathInfo.substring(8);
                int slashIdx = rest.indexOf('/');
                if (slashIdx == -1) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path: " + pathInfo);
                    return;
                }
                String name = rest.substring(0, slashIdx);
                String idStr = rest.substring(slashIdx + 1);
                handleGetPatch(name, idStr, req, resp);
                return;
            }
            
            // Unknown path
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not found: " + pathInfo);
        } catch (Exception e) {
            LOG.error("Error handling request: {}", pathInfo, e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }
            
            if (pathInfo.startsWith("/$/create/")) {
                // Create a new patch log
                String name = pathInfo.substring(9);
                handleCreatePatchLog(name, req, resp);
                return;
            }
            
            if (pathInfo.startsWith("/$/append/")) {
                // Append a patch to a patch log
                String name = pathInfo.substring(9);
                handleAppendPatch(name, req, resp);
                return;
            }
            
            // Unknown path
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not found: " + pathInfo);
        } catch (Exception e) {
            LOG.error("Error handling request: {}", pathInfo, e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    /**
     * Handle a request to list all patch logs.
     */
    private void handleListPatchLogs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<LogEntry> logs = server.listPatchLogs();
        
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        builder.key("datasets").startArray();
        for (LogEntry log : logs) {
            builder.startObject();
            builder.key("name").value(log.getName());
            builder.key("version").value(log.getHead().toString());
            builder.finishObject();
        }
        builder.finishArray();
        builder.finishObject();
        
        sendJson(resp, builder.build());
    }
    
    /**
     * Handle a request to get info about a patch log.
     */
    private void handleGetPatchLogInfo(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LogEntry log = server.getPatchLogInfo(name);
        
        if (log == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Patch log not found: " + name);
            return;
        }
        
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        builder.key("name").value(log.getName());
        builder.key("version").value(log.getHead().toString());
        builder.finishObject();
        
        sendJson(resp, builder.build());
    }
    
    /**
     * Handle a request to create a new patch log.
     */
    private void handleCreatePatchLog(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Id id = server.createPatchLog(name);
        
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        builder.key("name").value(name);
        builder.key("version").value(id.toString());
        builder.finishObject();
        
        sendJson(resp, builder.build());
    }
    
    /**
     * Handle a request to append a patch to a patch log.
     */
    private void handleAppendPatch(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the expected version
        String versionParam = req.getParameter("version");
        Id expected = (versionParam != null) ? Id.fromString(versionParam) : null;
        
        // Read the patch
        RDFPatch patch;
        try (InputStream in = req.getInputStream()) {
            patch = RDFPatchOps.read(in);
        }
        
        // Append the patch
        Id id = server.append(name, patch, expected);
        
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        builder.key("name").value(name);
        builder.key("version").value(id.toString());
        builder.finishObject();
        
        sendJson(resp, builder.build());
    }
    
    /**
     * Handle a request to fetch patches from a patch log.
     */
    private void handleFetchPatches(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the version to start from
        String versionParam = req.getParameter("version");
        Id start = (versionParam != null) ? Id.fromString(versionParam) : null;
        
        // Get the patches
        Iterable<RDFPatch> patches;
        try {
            patches = server.getPatches(name, start);
        } catch (DeltaException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }
        
        // Set the content type
        resp.setContentType("application/rdf-patch");
        
        // Write the patches
        try (OutputStream out = resp.getOutputStream()) {
            for (RDFPatch patch : patches) {
                RDFPatchOps.write(out, patch);
            }
        }
    }
    
    /**
     * Handle a request to get a specific patch.
     */
    private void handleGetPatch(String name, String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Id id = Id.fromString(idStr);
        
        // Get the patch
        RDFPatch patch;
        try {
            patch = server.getPatch(name, id);
        } catch (DeltaException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }
        
        if (patch == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Patch not found: " + idStr);
            return;
        }
        
        // Set the content type
        resp.setContentType("application/rdf-patch");
        
        // Write the patch
        try (OutputStream out = resp.getOutputStream()) {
            RDFPatchOps.write(out, patch);
        }
    }
    
    /**
     * Send a JSON response.
     */
    private void sendJson(HttpServletResponse resp, JsonObject json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = resp.getWriter()) {
            JSON.write(writer, json);
        }
    }
    
    /**
     * Send an error response.
     */
    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        builder.key("error").value(status);
        builder.key("message").value(message);
        builder.finishObject();
        
        try (PrintWriter writer = resp.getWriter()) {
            JSON.write(writer, builder.build());
        }
    }
}