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

package org.apache.jena.fuseki.main.sys;

import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;

/** Helpers for working with Jetty.
 * <h3>SecurityHandler</h3>
 *  <pre>
 *     UserStore userStore = JettyLib.makeUserStore(...);
 *     SecurityHandler securityHandler = JettyLib.makeSecurityHandler(String pathSpec, String realm, UserStore userStore);
 *  </pre>
 */
public class JettyLib {

    /** Add or append a {@link Handler} to a Jetty {@link Server}. */
    public static void addHandler(Server server, Handler handler) {
        final Handler currentHandler = server.getHandler();
        if (currentHandler == null) {
            server.setHandler(handler);
        } else {
            if (currentHandler instanceof HandlerList) {
                ((HandlerList) currentHandler).addHandler(handler);
            } else {
                // Singleton handler. Convert to list.
                final HandlerList handlerList = new HandlerList();
                handlerList.addHandler(currentHandler);
                handlerList.addHandler(handler);
                server.setHandler(handlerList);
            }
        }
    }

    /** Add the RDF MIME Type mappings */
    public static void setMimeTypes(ServletContextHandler context) {
        MimeTypes mimeTypes = new MimeTypes();
        // RDF syntax
        mimeTypes.addMimeMapping("nt",      WebContent.contentTypeNTriples);
        mimeTypes.addMimeMapping("nq",      WebContent.contentTypeNQuads);
        mimeTypes.addMimeMapping("ttl",     WebContent.contentTypeTurtle+";charset=utf-8");
        mimeTypes.addMimeMapping("trig",    WebContent.contentTypeTriG+";charset=utf-8");
        mimeTypes.addMimeMapping("rdf",     WebContent.contentTypeRDFXML);
        mimeTypes.addMimeMapping("jsonld",  WebContent.contentTypeJSONLD);
        mimeTypes.addMimeMapping("rj",      WebContent.contentTypeRDFJSON);
        mimeTypes.addMimeMapping("rt",      WebContent.contentTypeRDFThrift);
        mimeTypes.addMimeMapping("trdf",    WebContent.contentTypeRDFThrift);

        // SPARQL syntax
        mimeTypes.addMimeMapping("rq",      WebContent.contentTypeSPARQLQuery);
        mimeTypes.addMimeMapping("ru",      WebContent.contentTypeSPARQLUpdate);

        // SPARQL Result set
        mimeTypes.addMimeMapping("rsj",     WebContent.contentTypeResultsJSON);
        mimeTypes.addMimeMapping("rsx",     WebContent.contentTypeResultsXML);
        mimeTypes.addMimeMapping("srt",     WebContent.contentTypeResultsThrift);
        mimeTypes.addMimeMapping("srt",     WebContent.contentTypeResultsProtobuf);

        // Other
        mimeTypes.addMimeMapping("txt",     WebContent.contentTypeTextPlain);
        mimeTypes.addMimeMapping("csv",     WebContent.contentTypeTextCSV);
        mimeTypes.addMimeMapping("tsv",     WebContent.contentTypeTextTSV);
        context.setMimeTypes(mimeTypes);
    }

    /** HTTP configuration with setting for Fuseki workload. No "secure" settings. */
    public static HttpConfiguration httpConfiguration() {
        HttpConfiguration http_config = new HttpConfiguration();
        // Some people do try very large operations ... really, should use POST.
        http_config.setRequestHeaderSize(512 * 1024);
        http_config.setOutputBufferSize(1024 * 1024);
//      http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(false);
        return http_config;
    }
}
