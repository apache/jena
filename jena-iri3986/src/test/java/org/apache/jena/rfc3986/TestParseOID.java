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

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.apache.jena.rfc3986.LibTestURI.test3986;

import org.junit.jupiter.api.Test;

import org.apache.jena.rfc3986.ParseOID.OIDParseException;

/** Test the class ParseOID */
public class TestParseOID {

    @Test public void oid_01() { goodOID("urn:oid:1"); }

    @Test public void oid_02() { goodOID("urn:oid:0");}

    @Test public void oid_03() { goodOID("urn:oid:153.8.0.981.0"); }

    @Test public void oid_04() { goodOID("urn:oid:1.2.840.113674.514.212.200"); }

    @Test public void oid_05() { badOID("urn:oid:"); }

    @Test public void oid_06() { badOID("oid:"); }

    @Test public void oid_07() { badOID("urn:oid:01"); }

    @Test public void oid_08() { badOID("urn:oid:1.2.3.01"); }

    @Test public void oid_09() { badOID("urn:oid:Z"); }

    @Test public void oid_10() { badOID("urn:oid:Z"); }

    @Test public void iri3986_oid_10() { test3986("urn:oid:2.3.4", 0); }
    @Test public void iri3986_oid_11() { test3986("urn:oid:Z", 1); }

    private void goodOID(String string) {
        ParseOID.parse(string);
    }

    private void badOID(String string) {
        assertThrowsExactly(OIDParseException.class,
                            ()->ParseOID.parse(string)
                );
    }
}
