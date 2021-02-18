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

package org.apache.jena.fuseki.jetty;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/** Library of functions to help with setting Jetty up with HTTPS.
 * This code is not supposed to be fully general.
 * It sets up "http" to redirect to "https".
 */
public class JettyHttps {

    /*
    * Useful documentation:
    *   http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html
    *   https://medium.com/vividcode/enable-https-support-with-self-signed-certificate-for-embedded-jetty-9-d3a86f83e9d9
    *
    * Generate a self-signed certificate
    *   keytool -keystore mykey.jks -alias mykey -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -genkey -validity 3650
    *
    * Access with curl
    *     curl -v -k https://localhost:8443/
    *     curl -v -k -L http://localhost:8080/
    */

    /**
     * Create a HTTPS Jetty server for the {@link ServletContextHandler}
     * <p>
     * If httpPort is -1, don't add http otherwise make http redirect to https.
     */
    public static Server jettyServerHttps(ServletContextHandler handler, String keystore, String certPassword, int httpPort, int httpsPort) {
        // Server handling http and https.
        Server jettyServer = server(keystore, certPassword, httpPort, httpsPort);
        if ( httpPort > 0 ) {
            // Redirect http to https.
            // Order matters. Check https and bounce if http as first choice.
            SecuredRedirectHandler srh = new SecuredRedirectHandler();
            JettyLib.addHandler(jettyServer, srh);
        }
        JettyLib.addHandler(jettyServer, handler);
        return jettyServer;
    }

    /** Build the server - http and https connectors.
     * If httpPort is -1, don't add http.
     */
    private static Server server(String keystore, String certPassword, int httpPort, int httpsPort) {
        Server server = new Server();
        if ( httpPort > 0 ) {
            ServerConnector plainConnector = httpConnector(server, httpPort, httpsPort);
            server.addConnector(plainConnector);
        }
        ServerConnector httpsConnector = httpsConnector(server, httpsPort, keystore, certPassword);
        server.addConnector(httpsConnector);
        return server;
    }

    /** Add HTTP to a {@link Server}, setting the secure redirection port. */
    private static ServerConnector httpConnector(Server server, int httpPort, int httpsPort) {
        HttpConfiguration http_config = httpConfiguration();
        http_config.setSendServerVersion(false);
        if ( httpPort >  0 ) {
            http_config.setSecureScheme(HttpScheme.HTTPS.asString());
            http_config.setSecurePort(httpsPort);
        }
        ServerConnector plainConnector = new ServerConnector(server, new HttpConnectionFactory(http_config));
        plainConnector.setPort(httpPort);
        return plainConnector;
    }

    /** Add HTTPS to a {@link Server}. */
    private static ServerConnector httpsConnector(Server server, int httpsPort, String keystore, String certPassword) {
        SslContextFactory.Server sslContextFactoryServer = new SslContextFactory.Server();
        sslContextFactoryServer.setKeyStorePath(keystore);
        sslContextFactoryServer.setKeyStorePassword(certPassword);

        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);

        HttpConfiguration https_config = httpConfiguration();
        https_config.setSecureScheme(HttpScheme.HTTPS.asString());
        https_config.setSecurePort(httpsPort);
        https_config.addCustomizer(src);

        // HTTPS Connector
        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactoryServer, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(httpsPort);
        return sslConnector;
    }

    /** HTTP configuration with setting for Fuseki workload. No "secure" settings. */
    private static HttpConfiguration httpConfiguration() {
        HttpConfiguration http_config = new HttpConfiguration();
        // Some people do try very large operations ... really, should use POST.
        http_config.setRequestHeaderSize(512 * 1024);
        http_config.setOutputBufferSize(1024 * 1024);
//      http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(false);
        return http_config;
    }
}
