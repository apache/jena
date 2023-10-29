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
 * This is the test suite that compares result with jena-iri.
 * See also {@link TestIRIxSyntax} for other IRIx parsing operations.
 * See also {@link TestIRIxOps} for IRIx operations.
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

    // This is legal with path and no authority.
    //@Test public void parse_http_02a()   { badSpecific("http:/file/name.txt"); }

    @Test public void parse_http_03()   { badSpecific("http://user@host/file/name.txt"); }

    @Test public void parse_http_04()   { good("nothttp://user@host/file/name.txt"); }

    @Test public void parse_http_05()   { good("nothttp://user@/file/name.txt"); }

    @Test public void parse_http_06() { badSpecific("http://user@host:8081/abc/def?qs=ghi#jkl"); }

    @Test public void parse_file_01() { good("file:///file/name.txt"); }

    // This is legal by RFC 8089, but not by earlier versions of the "file:" scheme.
    @Test public void parse_file_02() { badSpecific("file://host/file/name.txt"); }

    // This is legal by RFC 8089 (jena-iri, based on the original RFC 1738, fails this with missing authority).
    @Test public void parse_file_03() { goodNoIRICheck("file:/file/name.txt"); }

    // -- FTP

    @Test public void parse_ftp_01() { good("ftp://user@host:3333/abc/def?qs=ghi#jkl"); }

    @Test public void parse_ftp_02() { good("ftp://[::1]/abc/def?qs=ghi#jkl"); }

    @Test public void parse_urn_01() { good("urn:nid:nss"); }

    @Test public void parse_urn_02() { good("urn:x-local:abc/def"); }

    // @formatter:off
    // namestring    = assigned-name
    //                 [ rq-components ]
    //                 [ "#" f-component ]
    // rq-components = [ "?+" r-component ]
    //                 [ "?=" q-component ]
    // @formatter:on

    @Test public void parse_urn_03()        { good("urn:x-local:abc/def?+more"); }

    @Test public void parse_urn_04()        { good("urn:x-local:abc/def?=123"); }

    @Test public void parse_urn_05()        { good("urn:x-local:abc/def?+resolve?=123#frag"); }

    @Test public void parse_urn_06()        { good("urn:abc0:def#frag"); }
    //  urn:2char:1char
    // urn:NID:NSS where NID is at least 2 alphas, and at most 32 long

    /**
     * Allow UCSCHARs in the NSS, and the RFC 8141 components.
     */
    // XXX Not ASCII in the NSS part, or components.
    private static boolean I_URN = true;
    private static void parse_internation_urn(String string) {
        if ( I_URN )
            good(string);
        else
            badSpecific(string);
    }

    @Test public void parse_urn_bad_01() { badSpecific("urn:"); }

    @Test public void parse_urn_bad_02() { badSpecific("urn:x:abc"); }

    @Test public void parse_urn_bad_03() { badSpecific("urn:abc:"); }

    // 33 chars
    @Test public void parse_urn_bad_04() { badSpecific("urn:abcdefghij-123456789-123456789-yz:a"); }

    // Bad by URN specific rule for the query components.
    @Test public void parse_urn_bad_05() { badSpecific("urn:local:abc/def?query=foo"); }

    // URNs are defined in RFC 8141 referring to RFC 3986 (URI - ASCII)
    @Test public void parse_intn_urn_01()    { parse_internation_urn("urn:NID:αβγ"); }
    @Test public void parse_intn_urn_02()    { parse_internation_urn("urn:nid:nss#αβγ"); }
    @Test public void parse_intn_urn_03()    { parse_internation_urn("urn:nid:nss?=αβγ"); }
    @Test public void parse_intn_urn_04()    { parse_internation_urn("urn:nid:nss?+αβγ"); }

    private static String testUUID = "aa045fc2-a781-11eb-9041-afa3877612ee";

    // RFC 8141 allows query and fragment in urn: (limited character set).
    // It even permits retrospectively applying to older schemes,
    // However, the r- (?+"), p- ("?=") or f- (#) component does not play a part in URN equivalence.

    // Allow r-component, q-component and f-component
    private static final boolean UUID_8141 = true;
    private static void parse_uuid_8141(String string) {
        if ( UUID_8141 )
            good(string);
        else
            badSpecific(string);
    }

    // -- uuid:

    @Test public void parse_uuid_01() { good("uuid:"+testUUID); }

    @Test public void parse_uuid_02() { good("uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }

    @Test public void parse_uuid_bad_01() { badSpecific("uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo"); }

    // Too short
    @Test public void parse_uuid_bad_02() { badSpecific("uuid:06e775ac-2c38-11b2"); }

    // Too long
    @Test public void parse_uuid_bad_03() { badSpecific("uuid:06e775ac-2c38-11b2-9999"); }

    // Bad character
    @Test public void parse_uuid_bad_04() { badSpecific("uuid:06e775ac-ZZZZ-11b2-801c-8086f2cc00c9"); }

    // For the ad-hoc "uuid:" do not allow r/q/f components.

    @Test public void parse_uuid_bad_10() { badSpecific("uuid:"+testUUID+ "?+chars"); }

    @Test public void parse_uuid_bad_11() { badSpecific("uuid:"+testUUID+ "?=chars"); }

    @Test public void parse_uuid_bad_12() { badSpecific("uuid:"+testUUID+"#frag"); }


    // -- urn:uuid:

    // RFC 8141 allows query and fragment in urn: (limited character set).
    // It even permits retrospectively applying to older schemes,
    // However, the r- (?+"), p- ("?=") or f- (#) component does not play a part in URN equivalence.

    @Test public void parse_urn_uuid_01() { good("urn:uuid:"+testUUID); }

    @Test public void parse_urn_uuid_02() { good("urn:uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }

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
    @Test public void parse_urn_uuid_bad_01() { badSpecific("urn:uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo"); }

    // Bad length
    @Test public void parse_urn_uuid_bad_02() { badSpecific("urn:uuid:06e775ac"); }

    // Bad character
    @Test public void parse_urn_uuid_bad_03() { badSpecific("urn:uuid:06e775ac-ZZZZ-11b2-801c-8086f2cc00c9"); }

    // Always bad. At least one char.
    @Test public void parse_urn_uuid_bad_04() { badSpecific("urn:uuid:" + testUUID + "?="); }

    // Always bad. At least one char.
    @Test public void parse_urn_uuid_bad_05() { badSpecific("urn:uuid:" + testUUID + "?+"); }

    @Test public void parse_urn_uuid_bad_06() { badSpecific("urn:uuid:" + testUUID + "?"); }

    @Test public void parse_urn_uuid_bad_07() { badSpecific("urn:uuid:" + testUUID + "?abc"); }

    // XXX Not ASCII in the NSS part
    @Test public void parse_urn_uuid_bad_12() { badSpecific("urn:uuid:" + testUUID + "#αβγ"); }
    @Test public void parse_urn_uuid_bad_13() { badSpecific("urn:uuid:" + testUUID + "?=αβγ"); }
    @Test public void parse_urn_uuid_bad_14() { badSpecific("urn:uuid:" + testUUID + "?+αβγ"); }

    private static void good(String string) {
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

    // Where jena-iri odes not get the right answer.
    private static void goodNoIRICheck(String string) {
        IRIx iri = IRIx.create(string);
        java.net.URI javaURI = java.net.URI.create(string);
    }

    // Expect an IRIParseException
    private static void bad(String string) {
        try {
            IRIs.checkEx(string);
            IRIs.reference(string);
            //RFC3986.check(string);
            fail("Did not fail: "+string);
        } catch (IRIException ex) {}
    }

    private static void badSpecific(String string) {
        bad(string);
    }
}
