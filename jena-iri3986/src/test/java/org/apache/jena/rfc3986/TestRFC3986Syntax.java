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

import static org.apache.jena.rfc3986.LibTestURI.badSyntax;
import static org.apache.jena.rfc3986.LibTestURI.goodSyntax;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * General parsing of URIs, not scheme specific rules.
 * @see TestURISchemes
 * @see TestIRI3986
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestRFC3986Syntax {

    // Detailed testing IPv4 parsing is in {@link TestParseIPv4Address}
    // Detailed testing IPv6 parsing is in {@link TestParseIPv6Address}

    // == Components
    @Test public void parse_01() { good("http://host",       "http", "host", null, "", null, null); }
    @Test public void parse_02() { good("http://host/",      "http", "host", null, "/", null, null); }
    @Test public void parse_03() { good("http://host:",      "http", "host", "", "", null, null); }
    @Test public void parse_04() { good("http://host:/",     "http", "host", "", "/", null, null); }
    @Test public void parse_05() { good("http://host:10/",   "http", "host", "10", "/", null, null); }
    @Test public void parse_06() { good("http://host:80/",   "http", "host", "80", "/", null, null); }
    @Test public void parse_07() { good("https://host:443/", "https", "host", "443", "/", null, null); }
    @Test public void parse_08() { good("https://host/path", "https", "host", null, "/path", null,null); }

    @Test public void parse_09() { good("https://host/path?queryString",       "https", "host", null, "/path", "queryString", null); }
    @Test public void parse_10() { good("https://host/path?queryString#frag",  "https", "host", null, "/path", "queryString", "frag"); }
    @Test public void parse_11() { good("https://host/path?queryString#frag",  "https", "host", null, "/path", "queryString", "frag"); }
    @Test public void parse_12() { good("https://host/?a=b&c=d",               "https", "host", null, "/", "a=b&c=d", null); }
    @Test public void parse_13() { good("https://host:8081/abc/def?qs=ghi#jkl",  "https", "host", "8081", "/abc/def", "qs=ghi","jkl" ); }

    @Test public void parse_14() { good("https:///abc/", "https", "", null, "/abc/", null, null); }
    @Test public void parse_15() { goodAll("https://user@host/abc/", "https", "user@host", "user", "host", null, "/abc/", null, null); }
    @Test public void parse_16() { goodAll("https://pw:user@host:1234/abc/", "https", "pw:user@host:1234", "pw:user", "host", "1234", "/abc/", null, null); }
    // Query string includes later "?"
    @Test public void parse_17() { good("https://host/path?qs?qs?qs", "https", "host", null, "/path", "qs?qs?qs", null); }

    // The host can be zero characters
    @Test public void parse_partial_01() { good("https://?qs", "https", "", null, "", "qs", null); }
    @Test public void parse_partial_02() { good("https:/?qs", "https", null, null, "/", "qs", null); }
    @Test public void parse_partial_03() { good("https:?qs", "https", null, null, "", "qs", null); }
    @Test public void parse_partial_04() { good("http:/abc/def", "http", null, null, "/abc/def", null, null); }
    @Test public void parse_partial_05() { good("http:/abc", "http", null, null, "/abc", null, null); }
    @Test public void parse_partial_06() { good("http:abc", "http", null, null, "abc", null, null); }

    @Test public void parse_partial_07() { good("//host/", null, "host", null, "/", null, null); }
    @Test public void parse_partial_08() { good("//host", null, "host", null, "", null, null); }
    @Test public void parse_partial_09() { good("/path", null, null, null, "/path", null, null); }
    @Test public void parse_partial_10() { good("?qs=value", null, null, null, "", "qs=value", null); }
    @Test public void parse_partial_11() { good("#frag", null, null, null, "", null, "frag"); }
    @Test public void parse_partial_12() { good("#", null, null, null, "", null, ""); }
    @Test public void parse_partial_13() { good("", null, null, null, "", null, null); }

    // == Internationalization
    @Test public void parse_i1() { good("http://αβγδ/ŸŽ?a=Ž#Ÿ", "http", "αβγδ", null, "/ŸŽ", "a=Ž", "Ÿ"); }
    @Test public void parse_i2() { badSyntax("αβγδ://host/"); }

    // == Relative
    @Test public void parse_rel_1() { good("/abcde", null, null, null, "/abcde", null, null); }
    @Test public void parse_rel_2() { good(".", null, null, null, ".", null, null); }
    @Test public void parse_rel_3() { good("..", null, null, null, "..", null, null); }

    // == % encoding
    // %XX in host added at RFC 3986.
    @Test public void parse_enc_1() { goodSyntax("http://ab%AAdef/xyzβ/abc"); }
    @Test public void parse_enc_2() { goodSyntax("/ab%ffdef"); }

    // Bad %-encoded
    @Test public void bad_encode_1() { badSyntax("http://example/xyz%"); }
    @Test public void bad_encode_2() { badSyntax("http://example/xyz%A"); }
    @Test public void bad_encode_3() { badSyntax("http://example/xyz%A?"); }
    @Test public void bad_encode_4() { badSyntax("http://example/xyz%ZZ?"); }
    @Test public void bad_encode_5() { badSyntax("http://example/xyz%AZ?"); }

    // == IP v4 and v6
    @Test public void parse_addr_v6_1() { good("http://[::1]/abc", "http", "[::1]", null, "/abc", null, null); }
    @Test public void parse_addr_v6_2() { good("http://[1::1]/abc", "http", "[1::1]", null, "/abc", null, null); }

    @Test public void parse_addr_v4_1() { good("http://10.11.12.13/abc", "http", "10.11.12.13",null, "/abc", null, null); }
    //@Test public void parse_addr_v4_2() { bad("http://10.11.12.1300/abc"); }
    @Test public void parse_addr_dnsname() { good("http://example.org/abc", "http", "example.org", null, "/abc", null, null); }

    // IP-vFuture inside [ ]
    //   IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
    @Test public void parse_addr_vFuture() { good("http://[vF.abc]/", "http", "[vF.abc]", null, "/", null, null); }
    // Bad IPv6
    @Test public void bad_ipv6_1() { badSyntax("http://[::80/xyz"); }
    @Test public void bad_ipv6_2() { badSyntax("http://host]/xyz"); }
    @Test public void bad_ipv6_3() { badSyntax("http://[]/xyz"); }

    // == bad and weird
    @Test public void parse_bad_01() { badSyntax("https://host/ /path"); }
    @Test public void parse_bad_02() { badSyntax("http://abcdef:80:/xyz"); }

    @Test public void parse_weird_01() { good("a:b/c", "a", null, null, "b/c", null, null); }
    @Test public void parse_weird_02() { good("c:/directory", "c", null, null, "/directory", null, null); }
    @Test public void parse_weird_03() { good("http://?user/def", "http", "", null, "", "user/def", null); }

    // XLink not valid unencoded in a fragment.
    @Test public void bad_frag_1() { badSyntax("http://eg.com/test.txt#xpointer(/unit[5])"); }
    @Test public void bad_frag_2() { badSyntax("http:///def#frag#frag"); }

    // == Bad scheme
    @Test public void bad_uri_scheme_1() { badSyntax(":segment"); }
    @Test public void bad_uri_scheme_3() { badSyntax("1://host/xyz"); }
    @Test public void bad_uri_scheme_4() { badSyntax("a~b://host/xyz"); }
    @Test public void bad_uri_scheme_5() { badSyntax("aβ://host/xyz"); }
    @Test public void bad_uri_scheme_6() { badSyntax("_:xyz"); }
    @Test public void bad_uri_scheme_7() { badSyntax("a_b:xyz"); }

    // ------------------------

    // == Authority
    @Test public void bad_authority_1() { badSyntax("ftp://abc@def@host/abc"); }
    // Multiple colon in authority
    @Test public void bad_authority_2() { badSyntax("http://abc:def:80/abc"); }

    // Schemes - light checks, RDF 3986 syntax only. Full checks in TestURISchemes

    // == http:, https: above

    // == file:
    @Test public void parse_file_01() { good("file:///dir/file.txt",   "file", "", null,     "/dir/file.txt", null, null); }
    // Often fixed up by higher level code.
    @Test public void parse_file_02() { good("file://host/file.txt",   "file", "host", null, "/file.txt", null, null); }
    @Test public void parse_file_03() { good("file:/dir/file.txt",     "file", null ,null,   "/dir/file.txt", null, null); }
    @Test public void parse_file_04() { good("file:file.txt",          "file", null, null,   "file.txt", null, null); }
    @Test public void parse_file_05() { good("file:",                  "file", null, null,   "", null, null); }
    @Test public void parse_file_06() { good("file:.",                 "file", null, null,   ".", null, null); }

    // == did: (RFC3986 parsing)
    @Test public void parse_did_01() { good("did:method:specific", "did", null, null, "method:specific", null, null); }
    @Test public void parse_did_02() { good("did:method:1:2:3:4",  "did", null, null, "method:1:2:3:4",  null, null); }
    @Test public void parse_did_03() { good("did:method:1:2::4",   "did", null, null, "method:1:2::4",   null, null); }
    @Test public void parse_did_05() { good("did:method:1",        "did", null, null, "method:1",        null, null); }

    // == example:
    @Test public void parse_example_01() { good("example:string#frag", "example", null, null, "string", null, "frag"); }

    // == urn:
    @Test public void parse_urn_01()  { good("urn:x-local:abc/def", "urn", null, null, "x-local:abc/def", null, null); }
    @Test public void parse_urn_02()  { goodSyntax("urn:abc0:def"); }

    // With queryString not components.

    //NB, as a query string, the leading ? is not included even though the rq-components rule in RFC 8141 included it in URN syntax.
    @Test public void parse_urn_component_01() { good("urn:ns:abc/def?+more",          "urn", null, null, "ns:abc/def", "+more", null); }
    @Test public void parse_urn_component_02() { good("urn:ns:abc/def?=123",           "urn", null, null, "ns:abc/def", "=123", null); }
    @Test public void parse_urn_component_03() { good("urn:ns:abc/def?=rComp?+qComp",  "urn", null, null, "ns:abc/def", "=rComp?+qComp", null); }
    @Test public void parse_urn_component_04() { good("urn:ns:abc/def?=r?+q#frag",     "urn", null, null, "ns:abc/def", "=r?+q", "frag"); }
    @Test public void parse_urn_component_05() { good("urn:ns:abc/def#frag",           "urn", null, null, "ns:abc/def", null, "frag"); }
    // Allow Unicode in the NSS and components.
    // (Strictly, URNs are ASCII)
    @Test public void parse_urn_unicode_01()   { good("urn:ns:αβγ",       "urn", null, null, "ns:αβγ", null, null); }
    @Test public void parse_urn_unicode_02()   { good("urn:ns:x?=αβγ",    "urn", null, null, "ns:x", "=αβγ", null); }
    @Test public void parse_urn_unicode_03()   { good("urn:ns:x?+α?=βγ",  "urn", null, null, "ns:x", "+α?=βγ", null); }
    @Test public void parse_urn_unicode_04()   { good("urn:ns:x?+α?=β#γ", "urn", null, null, "ns:x", "+α?=β", "γ"); }

    // == urn:uuid: , uuid:
    private static final String testUUID = "326f63ea-7447-11ee-b715-0be26fda5b37";

    @Test public void parse_uuid_01()   { goodSyntax("uuid:"+testUUID); }
    @Test public void parse_uuid_02()   { goodSyntax("uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }
    @Test public void parse_uuid_03()   { goodSyntax("UUID:"+testUUID); }

    @Test public void parse_urn_uuid_01()   { goodSyntax("urn:uuid:"+testUUID); }
    @Test public void parse_urn_uuid_02()   { goodSyntax("urn:uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }
    @Test public void parse_urn_uuid_03()   { goodSyntax("URN:UUID:"+testUUID); }
    @Test public void parse_urn_uuid_04()   { goodSyntax("URN:UUID:"+testUUID+"?=abc"); }
    @Test public void parse_urn_uuid_05()   { goodSyntax("URN:UUID:"+testUUID+"?=αβγ"); }
    @Test public void parse_urn_uuid_06()   { goodSyntax("URN:UUID:"+testUUID+"+=α?=β#γ"); }

    // == urn:oid:
    // Good by URI syntax, bad by oid: scheme.
    @Test public void parse_urn_oid_01()   { good("urn:oid:1.22222", "urn", null, null, "oid:1.22222", null, null); }
    @Test public void parse_urn_oid_02()   { good("urn:oid:1.2.3.01", "urn", null, null, "oid:1.2.3.01", null, null); }

    // == oid:
    @Test public void parse_oid_01()   { good("oid:1.2", "oid", null, null, "1.2", null, null); }
    @Test public void parse_oid_02()   { good("oid:1.2.3.01", "oid", null, null, "1.2.3.01", null, null); }

    // == urn:example:
    @Test public void parse_urn_example_01()   { good("urn:example:abc", "urn", null, null, "example:abc", null, null); }

    //==  ftp:
    @Test public void parse_ftp_01()    { goodSyntax("ftp://user@host:3333/abc/def?qs=ghi#jkl"); }
    @Test public void parse_ftp_02()    { goodSyntax("ftp://[::1]/abc/def?qs=ghi#jkl"); }

    // ----

    // Check legal RFC 3986/7 syntax and check the corresponding parts.
    // All parts except authority and user
    private void good(String iristr,
                      String scheme, String host, String port,
                      String path, String query, String fragment) {
        IRI3986 iri = RFC3986.create(iristr);
        assertEquals(iristr,   iri.str(),      "string -- ");
        assertEquals(scheme,   iri.scheme(),   "scheme -- ");
        assertEquals(host,     iri.host(),     "host -- ");
        assertEquals(port,     iri.port(),     "port -- ");
        assertEquals(path,     iri.path(),     "path --");
        assertEquals(query,    iri.query(),    "query -- ");
        assertEquals(fragment, iri.fragment(), "fragment -- " );

        IRI3986 iri2 = RFC3986.createByRegex(iristr);
        assertEquals(iri, iri2);
    }

    // All parts.
    private void goodAll(String iristr,
                         String scheme, String authority, String userInfo, String host, String port,
                         String path, String query, String fragment) {
        IRI3986 iri = RFC3986.create(iristr);
        assertEquals(iristr,   iri.str(),         "string -- ");
        assertEquals(scheme,   iri.scheme(),      "scheme -- ");
        assertEquals(authority,  iri.authority(), "authority --");
        assertEquals(userInfo,   iri.userInfo(),  "userInfo --");
        assertEquals(host,     iri.host(),        "host -- ");
        assertEquals(port,     iri.port(),        "port -- ");
        assertEquals(path,     iri.path(),        "path --");
        assertEquals(query,    iri.query(),       "query -- ");
        assertEquals(fragment, iri.fragment(),    "fragment -- " );

        IRI3986 iri2 = RFC3986.createByRegex(iristr);
        assertEquals(iri, iri2);
    }
}
