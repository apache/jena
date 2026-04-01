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

package org.apache.jena.irix;

import static org.apache.jena.atlas.lib.Lib.uppercase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

/**
 * Test of parsing and schema violations.
 * <p>
 *
 * See also {@link TestIRIxSyntaxRFC3986} for RDF 3986 syntax only parsing.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class TestIRIxJenaSystem extends AbstractTestIRIx_3986 {
    // Up until jena 5.6.0, this was the test suite that compares results with jena-iri.
    public TestIRIxJenaSystem(String name, IRIProvider provider) {
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
    @Test public void parse_06() { good("/ab%ffdef"); }

    @Test public void parse_07() { good("http://host/abcdef?qs=foo#frag"); }

    @Test public void parse_08() { good(""); }

    @Test public void parse_09() { good("."); }

    @Test public void parse_10() { good(".."); }

    @Test public void parse_11() { good("//host:8081/abc/def?qs=ghi#jkl"); }

    // Legal, if weird, scheme name.
    @Test public void parse_12() { good("a+.-9://h/"); }

    // No path.

    @Test public void parse_13() { good("http://host"); }

    @Test public void parse_14() { good("http://host#frag"); }

    @Test public void parse_15() { good("http://host?query"); }

    // : in first segment in path.
    @Test public void parse_16() { good("http://host/a:b/"); }

    @Test public void parse_17() { good("/a:b/"); }

    @Test public void parse_18() { good("/z/a:b"); }

    // This character is from a report on users@jena.
    @Test public void parse_nfc() { good("http://host/ή"); }

    // ---- bad

    // Leading ':'
    @Test public void bad_scheme_1() { badSyntax(":segment"); }

    // Bad scheme
    @Test public void bad_scheme_2() { badSyntax("://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_3() { badSyntax("1://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_4() { badSyntax("a~b://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_5() { badSyntax("aβ://host/xyz"); }

    // Bad scheme
    @Test public void bad_scheme_6() { badSyntax("_:xyz"); }

    // Bad scheme
    @Test public void bad_scheme_7() { badSyntax("a_b:xyz"); }

    // Space!
    @Test public void bad_chars_1() { badSyntax("http://abcdef:80/xyz /abc"); }

    // colons
    @Test public void bad_host_1() { badSyntax("http://abcdef:80:/xyz"); }

    // Bad IPv6
    @Test public void bad_ipv6_1() { badSyntax("http://[::80/xyz"); }

    // Bad IPv6
    @Test public void bad_ipv6_2() { badSyntax("http://host]/xyz"); }

    // Bad IPv6
    @Test public void bad_ipv6_3() { badSyntax("http://[]/xyz"); }

    // Multiple @
    @Test public void bad_authority_1() { badSyntax("ftp://abc@def@host/abc"); }

    // Multiple colon in authority
    @Test public void bad_authority_2() { badSyntax("http://abc:def:80/abc"); }

    // Bad %-encoding.
    @Test public void bad_percent_1() { badSyntax("http://example/abc%ZZdef"); }

    @Test public void bad_percent_2() { badSyntax("http://abc%ZZdef/"); }

    // Bad %-encoded
    @Test public void bad_percent_3() { badSyntax("http://example/xyz%"); }

    // Bad %-encoded
    @Test public void bad_percent_4() { badSyntax("http://example/xyz%A"); }

    // Bad %-encoded
    @Test public void bad_percent_5() { badSyntax("http://example/xyz%A?"); }

    // [] not allowed.
    @Test public void bad_frag_1() { badSyntax("http://eg.com/test.txt#xpointer(/unit[5])"); }

    @Test public void equality_01() {
        String s = "https://jena.apache.org/";
        IRIx iri1 = test_create(s);
        IRIx iri2 = test_create(s);
        assertEquals(iri1, iri2);
        assertEquals(iri1.hashCode(), iri2.hashCode());
    }

    // HTTP scheme specific rules.
    @Test public void parse_http_01()   { badBySchemeOnCreate("http:///file/name.txt"); }

    // HTTP scheme specific rules.
    @Test public void parse_http_02()   { badBySchemeOnCreate("HTTP:///file/name.txt"); }

    // This is legal with path and no authority.
    //@Test public void parse_http_02a()   { badSpecific("http:/file/name.txt"); }

    @Test public void parse_http_03()   { badByScheme("http://user@host/file/name.txt", 0, 1); }

    @Test public void parse_http_04()   { good("nothttp://user@host/file/name.txt"); }

    @Test public void parse_http_05()   { good("nothttp://user@/file/name.txt"); }

    @Test public void parse_http_06() { badByScheme("http://user@host:8081/abc/def?qs=ghi#jkl", 0, 1); }

    @Test public void parse_file_01() { good("file:///file/name.txt"); }

    // This is legal by RFC 8089, but not by earlier versions of the "file:" scheme.
    @Test public void parse_file_02() { badByScheme("file://host/file/name.txt", 0, 1); }

    // This is legal by RFC 8089 (jena-iri, based on the original RFC 1738, fails this with missing authority).
    @Test public void parse_file_03() { good("file:/file/name.txt"); }

    // -- FTP

    @Test public void parse_ftp_01() { good("ftp://user@host:3333/abc/def?qs=ghi#jkl"); }

    @Test public void parse_ftp_02() { good("ftp://[::1]/abc/def?qs=ghi#jkl"); }

    @Test public void parse_urn_01() { good("urn:nid:nss"); }

    @Test public void parse_urn_02() { good("urn:ns:abc/def"); }

    // @formatter:off
    // namestring    = assigned-name
    //                 [ rq-components ]
    //                 [ "#" f-component ]
    // rq-components = [ "?+" r-component ]
    //                 [ "?=" q-component ]
    // @formatter:on

    @Test public void parse_urn_03()        { good("urn:ns:abc/def?+more"); }

    @Test public void parse_urn_04()        { good("urn:ns:abc/def?=123"); }

    @Test public void parse_urn_05()        { good("urn:ns:abc/def?+resolve?=123#frag"); }

    @Test public void parse_urn_06()        { good("urn:abc0:def#frag"); }
    //  urn:2char:1char
    // urn:NID:NSS where NID is at least 2 alphas, and at most 32 long

    @Test public void parse_urn_bad_01() { badBySchemeOnCreate("urn:"); }

    @Test public void parse_urn_bad_02() { badBySchemeOnCreate("urn:x:abc"); }

    @Test public void parse_urn_bad_03() { badBySchemeOnCreate("urn:abc:"); }

    // 33 chars
    @Test public void parse_urn_bad_04() { badBySchemeOnCreate("urn:abcdefghij-123456789-123456789-yz:a"); }

    // Bad by URN specific rule for the query components.
    @Test public void parse_urn_bad_05() { badBySchemeOnCreate("urn:local:abc/def?query=foo"); }

    // URNs are defined in RFC 8141 referring to RFC 3986 (URI - ASCII)
    /**
     * Allow UCSCHARs in the NSS, and the RFC 8141 components.
     */
    private static boolean I_URN = true;
    private void parse_international_urn(String string) {
        if ( I_URN )
            good(string);
        else
            badBySchemeOnCreate(string);
    }

    @Test public void parse_intn_urn_01()    { parse_international_urn("urn:NID:αβγ"); }
    @Test public void parse_intn_urn_02()    { parse_international_urn("urn:nid:nss#αβγ"); }
    @Test public void parse_intn_urn_03()    { parse_international_urn("urn:nid:nss?=αβγ"); }
    @Test public void parse_intn_urn_04()    { parse_international_urn("urn:nid:nss?+αβγ"); }

    private static String testUUID = "aa045fc2-a781-11eb-9041-afa3877612ee";

    // RFC 8141 allows query and fragment in urn: (limited character set).
    // It even permits retrospectively applying to older schemes,
    // However, the r- (?+"), p- ("?=") or f- (#) component does not play a part in URN equivalence.

    // Allow r-component, q-component and f-component
    private static final boolean UUID_8141 = true;
    private void parse_uuid_8141(String string) {
        if ( UUID_8141 )
            good(string);
        else
            badBySchemeOnCreate(string);
    }

    // -- uuid:

    @Test public void parse_uuid_01() { good("uuid:"+testUUID); }

    @Test public void parse_uuid_02() { good("uuid:"+(uppercase(testUUID))); }

    @Test public void parse_uuid_bad_01() { badByScheme("uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo", 1, 0); }

    // Too short
    @Test public void parse_uuid_bad_02() { badByScheme("uuid:06e775ac-2c38-11b2", 1, 0); }

    // Too long
    @Test public void parse_uuid_bad_03() { badByScheme("uuid:06e775ac-2c38-11b2-9999", 1, 0); }

    // Bad character
    @Test public void parse_uuid_bad_04() { badByScheme("uuid:06e775ac-ZZZZ-11b2-801c-8086f2cc00c9", 1, 0); }

    // For the ad-hoc "uuid:" do not allow r/q/f components.

    @Test public void parse_uuid_bad_10() { badByScheme("uuid:"+testUUID+ "?+chars", 1, 0); }

    @Test public void parse_uuid_bad_11() { badByScheme("uuid:"+testUUID+ "?=chars", 1, 0); }

    @Test public void parse_uuid_bad_12() { badByScheme("uuid:"+testUUID+"#frag", 1, 0); }


    // -- urn:uuid:

    // RFC 8141 allows query and fragment in urn: (limited character set).
    // It even permits retrospectively applying to older schemes,
    // However, the r- (?+"), p- ("?=") or f- (#) component does not play a part in URN equivalence.

    @Test public void parse_urn_uuid_01() { good("urn:uuid:"+testUUID); }

    @Test public void parse_urn_uuid_02() { good("urn:uuid:"+uppercase(testUUID)); }

    @Test public void parse_urn_uuid_03() { parse_uuid_8141("urn:uuid:"+testUUID+"#frag"); }

    // Zero char fragment is legal.
    @Test public void parse_urn_uuid_04() { parse_uuid_8141("urn:uuid:" + testUUID + "#"); }

    // RFC 8141 allows query string must be ?=<one+ char> or ?+<one+ char>
    @Test public void parse_urn_uuid_21() { parse_uuid_8141("urn:uuid:" + testUUID + "?=chars"); }

    // RFC 8141 allows "query string" where it must must be ?=<one+ char> or ?+<one+ char>
    @Test public void parse_urn_uuid_22() { parse_uuid_8141("urn:uuid:" + testUUID + "?=ab/?cd"); }

    @Test public void parse_urn_uuid_23() { parse_uuid_8141("urn:uuid:" + testUUID + "?+chars"); }

    @Test public void parse_urn_uuid_24() { parse_uuid_8141("urn:uuid:" + testUUID + "?+ab/?cd"); }

    @Test public void parse_urn_uuid_25() { parse_uuid_8141("urn:uuid:" + testUUID + "?+chars?=chars#frag"); }

    @Test public void parse_urn_uuid_26() { parse_uuid_8141("urn:uuid:" + testUUID + "?+chars?=chars#frag"); }

    // Strange cases.
    // The r- and q- components can have '?', '+' and '=' in them
    // so the first occurrence captures everything up to the
    // fragment or end of string.

    @Test public void parse_urn_uuid_27() { parse_uuid_8141("urn:uuid:" + testUUID + "?+chars?"); }

    @Test public void parse_urn_uuid_28() { parse_uuid_8141("urn:uuid:" + testUUID + "?+chars??=next"); }

    // Single q-component
    @Test public void parse_urn_uuid_29() { parse_uuid_8141("urn:uuid:" + testUUID + "?=chars?a=b"); }

    // Single q-component!
    @Test public void parse_urn_uuid_30() { parse_uuid_8141("urn:uuid:" + testUUID + "?=aaa?+bbb"); }

    // Single r-component
    @Test public void parse_urn_uuid_31() { parse_uuid_8141("urn:uuid:" + testUUID + "?+aaa?+bbb"); }

    @Test public void parse_urn_uuid_32() { parse_uuid_8141("urn:uuid:" + testUUID + "?=Q?+R"); }

    // Always bad.
    // Query string, not a component.
    @Test public void parse_urn_uuid_bad_01() { badBySchemeOnCreate("urn:uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo"); }

    // XXX ???? Hard error?
    // Bad length
    @Test public void parse_urn_uuid_bad_02() { badByScheme("urn:uuid:06e775ac", 1, 0); }

    // XXX ???? Hard error?
    // Bad character
    @Test public void parse_urn_uuid_bad_03() { badByScheme("urn:uuid:06e775ac-ZZZZ-11b2-801c-8086f2cc00c9", 1, 0); }

    // Always bad. At least one char.
    @Test public void parse_urn_uuid_bad_04() { badBySchemeOnCreate("urn:uuid:" + testUUID + "?="); }

    // Always bad. At least one char.
    @Test public void parse_urn_uuid_bad_05() { badBySchemeOnCreate("urn:uuid:" + testUUID + "?+"); }

    @Test public void parse_urn_uuid_bad_06() { badBySchemeOnCreate("urn:uuid:" + testUUID + "?"); }

    @Test public void parse_urn_uuid_bad_07() { badBySchemeOnCreate("urn:uuid:" + testUUID + "?abc"); }

    // Not ASCII in the NSS part
    // XXX Check legality; align to "uuid:"
    @Test public void parse_urn_uuid_bad_12() { good("urn:uuid:" + testUUID + "#αβγ"); }
    @Test public void parse_urn_uuid_bad_13() { good("urn:uuid:" + testUUID + "?=αβγ"); }
    @Test public void parse_urn_uuid_bad_14() { good("urn:uuid:" + testUUID + "?+αβγ"); }

    private void good(String string) {
        IRIx iri = test_create(string);
        assertNotNull(iri);
        // Check that the JDK can at least parse the string.
        java.net.URI javaURI = java.net.URI.create(string);
        assertNotNull(javaURI);
    }

    // Expect an IRIParseException
    private void badSyntax(String string) {
        try {
            IRIx iri = test_create(string);
            fail("Did not fail: "+string);
        } catch (IRIException ex) {}
    }

    private void badBySchemeOnCreate(String string) {
        try {
            test_create(string);
            fail("<"+string+">: Expected an exception when created");
        } catch (IRIException ex) {}
    }

    // See also org.apache.jena.rfc3986.TestURISchemes that tests
    // which errors and warning are expected.

    private void badByScheme(String string, int expectedNumErrors, int expectedNumWarnings) {
        IRIx iri = test_create(string);
        // This is after the "error on create" step in IRIProvider3986.
        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger warnings = new AtomicInteger(0);
        iri.handleViolations((isError, str) -> {
            if ( isError )
                errors.incrementAndGet();
            else
                warnings.incrementAndGet();
        });
        //System.out.printf("(e=%d, w=%d) %s\n", errors.get(), warnings.get(),  string);
        assertEquals("errors", expectedNumErrors, errors.get());
        assertEquals("warnings", expectedNumWarnings, warnings.get());
    }
}
