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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

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
            List<Handler> handlerList = new ArrayList<>();

            if (currentHandler instanceof Handler.Container) {
                Handler.Container container = (Handler.Container)currentHandler;
                handlerList.addAll(container.getHandlers());
            }
            handlerList.add(handler);
            Handler.Container container = new Handler.Sequence(handlerList);
            server.setHandler(container);
        }
    }

    /** Add the RDF MIME Type mappings */
    public static void setMimeTypes(ServletContextHandler context) {
        MimeTypes.Mutable mimeTypes = context.getMimeTypes(); //new MimeTypes.Mutable();

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

    /** Create a resource for a filename */
    public static Resource newResource(String filename) {
        return ResourceFactory.root().newResource(filename);
    }
}