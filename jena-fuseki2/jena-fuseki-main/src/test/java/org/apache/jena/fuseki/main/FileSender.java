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

package org.apache.jena.fuseki.main;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;

/**
 * Simple multipart form data HTTP PUT/POST sender, primarily for.
 * This class does not stream the content.
 */
public class FileSender {
    // This class is in main/src to enable sharing without needing to depend on a test artifact.

    private static record Entry(String fileName, String content, String contentType) {}
    private List<Entry> items = new ArrayList<>();
    private final String url;

    public FileSender(String url ) { this.url = url; }

    public void add(String filename, String content, String type) {
        Entry e = new Entry(filename, content, type);
        items.add(e);
    }

    /** Return response code */
    public int send(String method) {
        String WNL = "\r\n";   // Web newline
        String boundary = UUID.randomUUID().toString();

        // This is for testing so build a body.
        StringBuilder strBuidler = new StringBuilder();
        for ( Entry e : items ) {
            strBuidler.append("--" + boundary+WNL);
            strBuidler.append("Content-Disposition: form-data; name=\"FILE\"; filename=\""+e.fileName+"\""+WNL);
            strBuidler.append("Content-Type: "+e.contentType+";charset=UTF-8"+WNL);
            strBuidler.append(WNL);
            strBuidler.append(e.content);
            strBuidler.append(WNL);
        }
        strBuidler.append("--" + boundary + "--"+WNL);

        URI uri = HttpLib.toRequestURI(url);
        String body = strBuidler.toString();
        String ctHeaderValue = WebContent.contentTypeMultipartFormData+"; boundary="+boundary;

        HttpRequest request = HttpRequest
                .newBuilder(uri)
                .setHeader(HttpNames.hContentType, ctHeaderValue)
                .method(method, BodyPublishers.ofString(body))
                .build();
        HttpResponse<InputStream> response = HttpLib.executeJDK(HttpClient.newHttpClient(), request, BodyHandlers.ofInputStream());
        HttpLib.handleResponseNoBody(response);
        return response.statusCode();
    }
}
