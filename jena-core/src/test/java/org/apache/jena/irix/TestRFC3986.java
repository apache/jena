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

package org.apache.jena.irix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.jena.iri.IRI;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

/**
 * Test of parsing and schema violations.
 * See also plain parse tests in {@link TestParseIRIx}
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TestRFC3986 extends AbstractTestIRIx {
    public TestRFC3986(String name, IRIProvider provider) {
        super(name, provider);
    }

    // Assumes full authority parsing and not scheme-specific checks.

    @Test public void parse_00() { good("http://host"); }

    @Test public void parse_01() { good("http://host:8081/abc/def?qs=ghi#jkl"); }

    @Test public void parse_02() { good("http://[::1]:8080/abc/def?qs=ghi#jkl"); }

    // %XX in host added at RFC 3986.
    @Test public void parse_03() { good("http://ab%AAdef/xyzβ/abc"); }

    @Test public void parse_04() { good("/abcdef"); }

    @Test public void parse_05() { good("/ab%FFdef"); }

    // Uppercase preferred
    @Test public void parse_06() { goodNoIRICheck("/ab%ffdef"); }

    @Test public void parse_07() { good("http://host/abcdef?qs=foo#frag"); }

    @Test public void parse_08() { good(""); }

    @Test public void parse_09() { good("."); }

    @Test public void parse_10() { good(".."); }

    @Test public void parse_11() { good("//host:8081/abc/def?qs=ghi#jkl"); }

    // Legal, if weird, scheme name.
    @Test public void parse_12() { goodNoIRICheck("a+.-9://h/"); }

    // No path.

    @Test public void parse_13() { good("http://host"); }

    @Test public void parse_14() { good("http://host#frag"); }

    @Test public void parse_15() { good("http://host?query"); }

    // : in first segment in path.
    @Test public void parse_16() { good("http://host/a:b/"); }

    @Test public void parse_17() { good("/a:b/"); }

    @Test public void parse_18() { good("/z/a:b"); }

    @Test public void equality_01() {
        String s = "https://jena.apache.org/";
        IRIx iri1 = IRIx.create(s);
        IRIx iri2 = IRIx.create(s);
        assertEquals(iri1, iri2);
        assertEquals(iri1.hashCode(), iri2.hashCode());
    }

    // HTTP scheme specific rules.
    @Test public void parse_http_01()   { badSpecific("http:///file/name.txt"); }

    // HTTP scheme specific rules.
    @Test public void parse_http_02()   { badSpecific("HTTP:///file/name.txt"); }

    // This is treated as legal with path and no authority.
    //@Test public void parse_http_02a()   { badSpecific("http:/file/name.txt"); }

    @Test public void parse_http_03()   { badSpecific("http://user@host/file/name.txt"); }

    @Test public void parse_http_04()   { good("nothttp://user@host/file/name.txt"); }

    @Test public void parse_http_05()   { good("nothttp://user@/file/name.txt"); }

    @Test public void parse_file_01() { good("file:///file/name.txt"); }

    // We reject "file://host/" forms.
    @Test public void parse_file_02() { badSpecific("file://host/file/name.txt"); }

    // This is legal by RFC 8089 (jena-iri, based on the original RFC 1738, fails this with missing authority).
    @Test public void parse_file_03() { goodNoIRICheck("file:/file/name.txt"); }

    @Test public void parse_urn_01() { good("urn:x-local:abc/def"); }

    // rq-components = [ "?+" r-component ]
    //                 [ "?=" q-component ]

    @Test public void parse_urn_02()        { good("urn:x-local:abc/def?+more"); }

    @Test public void parse_urn_03()        { good("urn:x-local:abc/def?=123"); }

    @Test public void parse_urn_04()        { good("urn:x-local:abc/def?+resolve?=123#frag"); }

    @Test public void parse_urn_05()        { good("urn:abc0:def"); }

    private static String testUUID = "aa045fc2-a781-11eb-9041-afa3877612ee";

    @Test public void parse_uuid_01() { good("uuid:"+testUUID); }

    @Test public void parse_uuid_02() { good("uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }

    @Test public void parse_uuid_03() { good("urn:uuid:"+testUUID); }

    @Test public void parse_uuid_04() { good("urn:uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }

    // -- FTP

    @Test public void parse_ftp_01() { good("ftp://user@host:3333/abc/def?qs=ghi#jkl"); }

    @Test public void parse_ftp_02() { good("ftp://[::1]/abc/def?qs=ghi#jkl"); }

    // ---- bad

    // Leading ':'
    @Test public void bad_scheme_1() { bad(":segment"); }

    // Bad scheme
    @Test public void bad_scheme_2() { bad("://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_3() { bad("1://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_4() { bad("a~b://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_5() { bad("aβ://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_6() { bad("_:xyz"); }

    // Bad scheme
    @Test public void bad_scheme_7() { bad("a_b:xyz"); }

    // Space!
    @Test public void bad_chars_1() { bad("http://abcdef:80/xyz /abc"); }

    // colons
    @Test public void bad_host_1() { bad("http://abcdef:80:/xyz"); }

    // Bad IPv6
    @Test public void bad_ipv6_1() { bad("http://[::80/xyz"); }

    // Bad IPv6
    @Test public void bad_ipv6_2() { bad("http://host]/xyz"); }

    // Bad IPv6
    @Test public void bad_ipv6_3() { bad("http://[]/xyz"); }

    // Multiple @
    @Test public void bad_authority_1() { bad("ftp://abc@def@host/abc"); }

    // Multiple colon in authority
    @Test public void bad_authority_2() { bad("http://abc:def:80/abc"); }

    // Bad %-encoding.
    @Test public void bad_percent_1() { bad("http://example/abc%ZZdef"); }

    @Test public void bad_percent_2() { bad("http://abc%ZZdef/"); }

    // Bad %-encoded
    @Test public void bad_percent_3() { bad("http://example/xyz%"); }

    // Bad %-encoded
    @Test public void bad_percent_4() { bad("http://example/xyz%A"); }

    // Bad %-encoded
    @Test public void bad_percent_5() { bad("http://example/xyz%A?"); }

    // [] not allowed.
    @Test public void bad_frag_1() { bad("http://eg.com/test.txt#xpointer(/unit[5])"); }

    // ---- bad by scheme.
    @Test public void parse_http_bad_01() { badSpecific("http://user@host:8081/abc/def?qs=ghi#jkl"); }

    //  urn:2char:1char
    // urn:NID:NSS where NID is at least 2 alphas, and at most 32 long
    @Test public void parse_urn_bad_01() { badSpecific("urn:"); }
    @Test public void parse_urn_bad_02() { badSpecific("urn:x:abc"); }

    @Test public void parse_urn_bad_03() { badSpecific("urn:abc:"); }
    // 33 chars
    @Test public void parse_urn_bad_04() { badSpecific("urn:abcdefghij-123456789-123456789-yz:a"); }

    // Bad by URN specific rule for the query components.
    @Test public void parse_urn_bad_05()    { badSpecific("urn:local:abc/def?query=foo"); }

    @Test public void parse_urn_uuid_bad_01() {
        badSpecific("urn:uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo");
    }

    @Test public void parse_urn_uuid_bad_02() {
        badSpecific("urn:uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9#frag");
    }

    @Test public void parse_urn_uuid_bad_03() {
        // Bad length
        badSpecific("urn:uuid:06e775ac");
    }

    @Test public void parse_urn_uuid_bad_04() {
        // Bad character
        badSpecific("urn:uuid:06e775ac-ZZZZ-11b2-801c-8086f2cc00c9");
    }

    @Test public void parse_uuid_bad_01() {
        badSpecific("uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo");
    }

    @Test public void parse_uuid_bad_02() {
        badSpecific("uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9#frag");
    }

    @Test public void parse_uuid_bad_03() {
        badSpecific("uuid:06e775ac-2c38-11b2");
    }

    @Test public void parse_uuid_bad_04() {
        badSpecific("urn:uuid:06e775ac-ZZZZ-11b2-801c-8086f2cc00c9");
    }

    // No char fragment is legal.
    @Test public void parse_uuid_bad_05() {
        badSpecific("urn:uuid:" + testUUID + "#");
    }

    // RFC 8141 allows query string must be ?=<one+ char> or ?+<one+ char>
    @Test public void parse_uuid_bad_06() {
        badSpecific("urn:uuid:" + testUUID + "?=chars");
    }

    @Test public void parse_uuid_bad_07() {
        badSpecific("urn:uuid:" + testUUID + "?+chars");
    }

    @Test public void parse_uuid_bad_08() {
        badSpecific("urn:uuid:" + testUUID + "?=");
    }

    @Test public void parse_uuid_bad_09() {
        badSpecific("urn:uuid:" + testUUID + "?+");
    }

    // RFC 8141 allows query and fragment in urn: (limited character set).
    // RFC 4122 (uuid namespace definition) does not.
    @Test
    public void parse_uuid_bad_8141_01() {
        badSpecific("urn:uuid:" + testUUID + "#frag");
    }

    // No char fragment is legal.
    @Test
    public void parse_uuid_bad_8141_02() {
        badSpecific("urn:uuid:" + testUUID + "#");
    }

    // RFC 8141 allows query string must be ?=<one+ char> or ?+<one+ char>
    @Test
    public void parse_uuid_bad_8141_03() {
        badSpecific("urn:uuid:" + testUUID + "?=chars");
    }

    @Test
    public void parse_uuid_bad_8141_04() {
        badSpecific("urn:uuid:" + testUUID + "?+chars");
    }

    private void good(String string) {
        IRIx iri = IRIx.create(string);
        assertNotNull(iri);
        if ( true ) {
            // Run against checking mode.
            IRI iri1 = SetupJenaIRI.iriCheckerFactory().create(string);
            if ( iri1.hasViolation(true) ) {
                iri1.violations(true).forEachRemaining(v-> System.err.println("IRI = "+string + " :: "+v.getLongMessage()));
                fail("Violations "+string);
            }
        }
        // Check that the JDK can at least parse the string.
        java.net.URI javaURI = java.net.URI.create(string);
        assertNotNull(javaURI);
    }

    private void goodNoIRICheck(String string) {
        IRIx iri = IRIx.create(string);
        java.net.URI javaURI = java.net.URI.create(string);
    }

    // Expect an IRIParseException
    private void bad(String string) {
        try {
            IRIs.checkEx(string);
            IRIs.reference(string);
            //RFC3986.check(string);
            fail("Did not fail: "+string);
        } catch (IRIException ex) {}
    }

    private void badSpecific(String string) {
        bad(string);
    }
}
