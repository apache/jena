/**
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

package org.apache.jena.fuseki;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.atlas.io.IO;

public class FileSender {
    // HttpClient 4.3 has MultipartEntity but the 4.2->4.3 change is less
    // than trivial (and it seems some environment fix the HttpClient)
    // so here's some code for testing support.
//    MultipartEntity entity = new MultipartEntity();
//    entity.addPart(...)
//    HttpPost request = new HttpPost(url);
//    request.setEntity(entity);
//    HttpClient client = new DefaultHttpClient();
//    HttpResponse response = client.execute(request);

    class Entry {
        String fileName;
        String content;
        String contentType;
    }

    private List<Entry> items = new ArrayList<>();

    private String url;

    public FileSender(String url ) { this.url = url; }

    public void add(String filename, String content, String type) {
        Entry e = new Entry();
        e.fileName = filename;
        e.content = content;
        e.contentType = type;
        items.add(e);
    }

    public void send(String method) {
        try {
            String WNL = "\r\n";   // Web newline
            String boundary = UUID.randomUUID().toString();

            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try ( PrintStream ps = new PrintStream(connection.getOutputStream()); ) {
                for ( Entry e : items ) {
                    ps.print("--" + boundary+WNL);
                    ps.print("Content-Disposition: form-data; name=\"FILE\"; filename=\""+e.fileName+"\""+WNL);
                    ps.print("Content-Type: "+e.contentType+";charset=UTF-8"+WNL);
                    ps.print(WNL);
                    ps.print(e.content);
                    ps.print(WNL);
                }
                ps.print("--" + boundary + "--"+WNL);
            }
            connection.connect();
            int responseCode = connection.getResponseCode();
        } catch (IOException ex) { IO.exception(ex); }
    }
}

