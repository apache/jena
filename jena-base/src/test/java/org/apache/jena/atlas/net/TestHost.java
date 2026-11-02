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

package org.apache.jena.atlas.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.RuntimeIOException;

public class TestHost {
    @Test public void host_localNonLoopback() {
        // Assumes the machine has some kind of IP networking.
        try {
            InetAddress addr = Host.getLocalHostLANAddress();
            assertNotNull(addr);
            assertFalse(addr.isLoopbackAddress());
        } catch ( RuntimeIOException ex) {
            Throwable x = ex.getCause();
            if ( x instanceof UnknownHostException )
                return;
            throw ex;
        }
    }

    @Test public void host_localNonLoopbackAddress() {
        // Assumes the machine has some kind of IP networking.
        try {
            // InetAddress.toString is "host/address"
            String addr = Host.getHostAddressForIRI();
            assertNotNull(addr);
            assertFalse(addr.contains("/"));
        } catch ( RuntimeIOException ex) {
            Throwable x = ex.getCause();
            if ( x instanceof UnknownHostException )
                return;
            throw ex;
        }
    }

    @Test public void isLocalhost_01() { isLocalhost("http://localhost:3030/api", true); }
    @Test public void isLocalhost_02() { isLocalhost("http://localhost/api", true); }
    @Test public void isLocalhost_03() { isLocalhost("http://here.localhost/api", true); }
    @Test public void isLocalhost_04() { isLocalhost("http://127.0.0.1/index.html", true); }
    @Test public void isLocalhost_05() { isLocalhost("http://127.0.0.5/", true); }
    @Test public void isLocalhost_06() { isLocalhost("http://0.0.0.0/", true); }
    @Test public void isLocalhost_07() { isLocalhost("http://[::1]:3000/", true); }
    @Test public void isLocalhost_08() { isLocalhost("http://[::1]/", true); }
    // IPv6 carrying an IPv4 address
    @Test public void isLocalhost_09() { isLocalhost("http://[::ffff:127.0.0.1]/", true); }

    @Test public void isLocalhost_20() { isLocalhost("https://example.com", false); }
    @Test public void isLocalhost_21() { isLocalhost("https://example:3030/", false); }
    @Test public void isLocalhost_22() { isLocalhost("https://unknown/", false); }

    private static void isLocalhost(String urlString, boolean expected) {
        String host = getHost(urlString);
        boolean b = Host.isLocalhostLoopback(host);
        assertEquals(expected, b, ()->urlString);
    }

    private static String getHost(String url)  {
        // IRI3986 parser is not available here.
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
