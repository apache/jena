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

package org.apache.jena.atlas.net;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetAddress;
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
}
