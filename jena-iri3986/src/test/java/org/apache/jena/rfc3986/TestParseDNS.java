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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Test the class ParseDNS */
public class TestParseDNS {

    @Test public void parseDNS_01() { goodDNS("a.b.c.d"); }
    @Test public void parseDNS_02() { goodDNS("ab.cd.e"); }
    @Test public void parseDNS_03() { goodDNS("ab.cd.e."); }
    @Test public void parseDNS_04() { goodDNS("192.168.0.1"); }
    //@Test public void parseDNS_05() { goodDNS(""); }      // Unclear if RFC 1034 actually meant ""
    //@Test public void parseDNS_06() { goodDNS(" "); }     // RFC 1034 does allow " " - it is the root address.
    // \u00E9 - e-acute
    @Test public void parseDNS_07() { goodDNS("\u00E9"); }
    @Test public void parseDNS_08() { goodDNS("%E9"); }

    // <label> ::= <let-dig> (<let-dig-hyp>)* <let-dig>
    @Test public void parseDNS_09() { goodDNS("abc-def"); }
    @Test public void parseDNS_10() { goodDNS("0abc-def0"); }

    // [DNS] More tests
    // %encoding.
    // Internationalization.
    // \u00E9 - e-acute

    @Test public void parseDNS_bad_01() { badDNS("a.b..c.d"); }
    @Test public void parseDNS_bad_02() { badDNS("."); }            // Must be at least one label.
    @Test public void parseDNS_bad_03() { badDNS(".abcd"); }
    //@Test public void parseDNS_bad_04() { badDNS("300.168.0.1"); }  // IPv4-like
    @Test public void parseDNS_bad_05() { badDNS("a.b.c:d"); }
    @Test public void parseDNS_bad_06() { badDNS("a.b.c:80"); }
    @Test public void parseDNS_bad_07() { badDNS("abc-"); }
    @Test public void parseDNS_bad_08() { badDNS("%7Z"); }
    @Test public void parseDNS_bad_09() { badDNS("%Z7"); }

    private static void goodDNS(String string) {
        ParseDNS.parse(string);
    }

    private static void badDNS(String string) {
        assertThrows(IRIParseException.class, ()->ParseDNS.parse(string));
    }
}
