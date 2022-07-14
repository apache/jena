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

package org.apache.jena.fuseki.access;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

/** Test parsing of assemblers with security aspects */
public class TestSecurityRegistry {
    static { JenaSystem.init(); }
    static final String DIR = "testing/SecurityRegistry/";

    @Test public void assemblerFile_1() {
        AuthorizationService authService = (AuthorizationService)AssemblerUtils.build(DIR+"assem-security-registry-1.ttl", VocabSecurity.tSecurityRegistry);
        assertNotNull(authService);
        SecurityRegistry sReg = (SecurityRegistry)authService;
        assertEquals(4, sReg.keys().size());
        assertEquals(3, sReg.get("user1").visibleGraphs().size());
    }

    @Test public void assemblerFile_2() {
        // WIP
        //   user1, all named graphs
        //   user2, all graphs
        //   user3, all named graphs +dft == all graphs
        //   any user, graph1
        AuthorizationService authService = (AuthorizationService)AssemblerUtils.build(DIR+"assem-security-registry-2.ttl", VocabSecurity.tSecurityRegistry);
        assertNotNull(authService);

        {
            SecurityContext sCxt = authService.get("user1");
            assertEquals(1, sCxt.visibleGraphs().size());
            Node x = sCxt.visibleGraphs().stream().findFirst().get();
            assertEquals(SecurityContext.allNamedGraphs, x);
        }

        {
            SecurityContext sCxt = authService.get("user2");
            assertEquals(1, sCxt.visibleGraphs().size());
            Node x = sCxt.visibleGraphs().stream().findFirst().get();
            assertEquals(SecurityContext.allGraphs, x);
        }

        {
            SecurityContext sCxt = authService.get("user3");
            assertEquals(1, sCxt.visibleGraphs().size());
            Node x = sCxt.visibleGraphs().stream().findFirst().get();
            assertEquals(SecurityContext.allGraphs, x);
        }

        {
            SecurityContext sCxt = authService.get("user4");
            assertTrue(sCxt instanceof SecurityContextDynamic);
            // In dynamic mode, the security context forbids everything
            assertFalse(sCxt.visableDefaultGraph());
            assertEquals(0, sCxt.visibleGraphs().size());
        }

        {
            SecurityContext sCxt = authService.get("user5");
            // Use has dynamic mode marker, but other graphs also specified => no dynamic mode
            assertFalse(sCxt instanceof SecurityContextDynamic);
            assertFalse(sCxt.visableDefaultGraph());
            assertEquals(2, sCxt.visibleGraphs().size());
            Collection<String> graphNames = sCxt.visibleGraphNames();
            assertTrue(graphNames.contains("http://host/graphname2"));
            assertTrue(graphNames.contains("urn:jena:accessGraphsDynamic"));
        }

        {
            SecurityContext sCxt = authService.get("*");
            assertEquals(1, sCxt.visibleGraphs().size());
            String x = sCxt.visibleGraphNames().stream().findFirst().get();
            assertEquals("http://host/graphname1", x);
        }
    }
}
