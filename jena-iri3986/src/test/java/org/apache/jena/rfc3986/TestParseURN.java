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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import org.apache.jena.rfc3986.ParseURN.URNParseException;

/** Test the class ParseURN */
public class TestParseURN {
    @Test public void parseURN_01() { goodURN1("urn:nid:nss",          "urn", "nid", "nss"); }

    @Test public void parseURN_02() { goodURN1("urn:nid:nss?+R?=Q#F",  "urn", "nid", "nss", "R", "Q", "F"); }
    @Test public void parseURN_03() { goodURN1("urn:nid:nss?=Q#F",     "urn", "nid", "nss", null , "Q", "F"); }
    @Test public void parseURN_04() { goodURN1("urn:nid:nss#F",        "urn", "nid", "nss", null , null, "F"); }

    @Test public void parseURN_05() { goodURN1("urn:nid:nss?+R#F",     "urn", "nid", "nss", "R" , null, "F"); }
    @Test public void parseURN_06() { goodURN1("urn:nid:nss?+R?=Q",    "urn", "nid", "nss", "R" , "Q",null); }
    @Test public void parseURN_07() { goodURN1("urn:nid:nss?=Q",       "urn", "nid", "nss", null, "Q", null); }

    @Test public void parseURN_08() { goodURN1("urn:nid:nss#",         "urn", "nid", "nss", null, null, ""); }

    @Test public void parseURN_09() { goodURN1("URN:OID:1.2.3",        "URN", "OID", "1.2.3"); }
    @Test public void parseURN_10() { goodURN1("urn:a-b:nss",          "urn", "a-b", "nss"); }

    // 32 character NID
    @Test public void parseURN_11() { goodURN1("urn:123456789-123456789-123456789-12:nss", "urn", "123456789-123456789-123456789-12", "nss"); }

    @Test public void parseURN_20() { goodURN1("urn:nid:nss?+R1?+R2",  "urn", "nid", "nss", "R1?+R2", null, null); }  // The r-component includes the ?+R2
    @Test public void parseURN_21() { goodURN1("urn:nid:nss?=Q?+R",    "urn", "nid", "nss", null, "Q?+R", null); }    // The q-component includes the ?+R
    @Test public void parseURN_22() { goodURN1("urn:nid:nss?=Q?+",     "urn", "nid", "nss", null, "Q?+", null); }     // The q-component includes the ?+
    @Test public void parseURN_23() { goodURN1("urn:nid:nss?=Q?=Q",    "urn", "nid", "nss", null, "Q?=Q", null); }    // The q-component includes the ?=R
    @Test public void parseURN_24() { goodURN1("urn:nid:nss?=Q?=",     "urn", "nid", "nss", null, "Q?=", null); }     // The q-component includes the ?=
    @Test public void parseURN_25() { goodURN1("urn:nid:nss?+R?Z",     "urn", "nid", "nss", "R?Z", null, null); }     // The r-component includes the ?Z
    @Test public void parseURN_26() { goodURN1("urn:nid:nss?=Q?n=v",   "urn", "nid", "nss", null, "Q?n=v", null); }   // The q-component includes the "?name=value"

    @Test public void parseURN_bad_01() { badURN("cat:ns:s"); }
    @Test public void parseURN_bad_02() { badURN("urn:ns"); }
    @Test public void parseURN_bad_03() { badURN("urn:ns:"); }
    @Test public void parseURN_bad_04() { badURN("urn:n:nss"); }

    @Test public void parseURN_bad_05() { badURN("urn:n:s"); }
    @Test public void parseURN_bad_06() { badURN("urn:-ns:123"); }
    @Test public void parseURN_bad_07() { badURN("urn:ns-:123"); }
    // 33 characters
    @Test public void parseURN_bad_08() { badURN("urn:123456789-123456789-123456789-123:nss"); }

    @Test public void parseURN_bad_10() { badURN("urn:"); }
    @Test public void parseURN_bad_11() { badURN("urn::"); }
    @Test public void parseURN_bad_12() { badURN("urn::abc"); }

    // Bad components.
    @Test public void parseURN_bad_20() { badURN1("urn:nid:nss?+#F"); }
    @Test public void parseURN_bad_21() { badURN1("urn:nid:nss?=#F"); }
    @Test public void parseURN_bad_22() { badURN1("urn:nid:nss?+R?="); }
    @Test public void parseURN_bad_23() { badURN1("urn:nid:nss?+?=Q"); }

    @Test public void parseURN_bad_24() { badURN1("urn:nid:nss?"); }
    @Test public void parseURN_bad_25() { badURN1("urn:nid:nss?junk"); }

    private void badURN(String string) {
        URN x = ParseURN.parseURN(string);
        assertNull(x, "Not null: "+Objects.toString(x));

        // Again, with handler.
        BiConsumer<Issue, String> handler = (issue, msg) -> { throw new URNParseException(string, msg); };
        assertThrowsExactly(URNParseException.class, ()->ParseURN.parseURN(string, handler));
    }

    private static void badURN1(String string) {
        // Expected to be good.
        IRI3986 iri = IRI3986.create(string);
        assertNotNull(iri);

        URN urn = ParseURN.parseURN(string);
        assertNull(urn, ()->"Not null: "+Objects.toString(urn)+ " <"+string+">");

        IssueCollector collector = new IssueCollector();
        // Expect it to pass (parse) validation and generate issues.
        ParseURN.validateURN(string, collector);
        assertFalse(collector.isEmpty(), ()->"Expected issues when validating <"+string+">");
    }

    private static void goodURN1(String string, String expectedScheme, String expectedNID, String expectedNSS ) {
        goodURN1(string, expectedScheme, expectedNID, expectedNSS, null, null, null);
    }

    private static void goodURN1(String string,
                                 String expectedScheme, String expectedNID, String expectedNSS,
                                 String expectedR, String expectedQ, String expectedF) {
        URNComponents expected = null;
        if ( expectedR != null || expectedQ != null || expectedF != null )
            expected = new URNComponents(expectedR, expectedQ, expectedF);

        // Should be OK as an IRI string.
        IRI3986 iri = IRI3986.create(string);
        // Parse from IRI
        URNComponents components = ParseURN.parseURNComponents(iri);
        checkURNComponents(expected, components);

        // Parse while parsing URN
        URN urn = ParseURN.parseURN(string);
        checkAssignedName(urn, expectedScheme, expectedNID, expectedNSS);
        checkURNComponents(expected, urn.components());

        IssueCollector collector = new IssueCollector();
        // Expect it to pass validation.
        ParseURN.validateURN(string, collector);
        assertTrue(collector.isEmpty(), ()->"Issues recorded <"+string+">");
    }

    private static void checkAssignedName(URN urn, String expectedScheme, String expectedNID, String expectedNSS) {
        assertEquals(expectedScheme, urn.scheme(), "scheme");
        assertEquals(expectedNID, urn.NID(), "NID");
        assertEquals(expectedNSS, urn.NSS(), "NSS");
    }

    private static void checkURNComponents(URNComponents expectedComponents, URNComponents actualComponents) {
        if ( expectedComponents == null && actualComponents == null )
            return;
        if ( expectedComponents == null && actualComponents != null ) {
            System.out.printf("Expected null URN components; got %s\n", actualComponents);
            return;
        }
        // expectedComponents not null
        if ( actualComponents == null ) {
            System.out.printf("Expected URN components: %s, got null\n", expectedComponents);
            return;
        }

        String expectedR = expectedComponents.rComponent();
        String expectedQ = expectedComponents.qComponent();
        String expectedF = expectedComponents.fComponent();

        if ( ! Objects.equals(expectedR, actualComponents.rComponent()) )
            System.out.printf("  Bad r-component: expected=%s, actual=%s\n", String.valueOf(expectedR), String.valueOf(actualComponents.rComponent()));
        if ( !Objects.equals(expectedQ, actualComponents.qComponent()) )
            System.out.printf("  Bad q-component: expected=%s, actual=%s\n", String.valueOf(expectedQ), String.valueOf(actualComponents.qComponent()));
        if ( !Objects.equals(expectedF, actualComponents.fComponent()) )
            System.out.printf("  Bad f-component: expected=%s, actual=%s\n", String.valueOf(expectedF), String.valueOf(actualComponents.fComponent()));
    }
}
