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
import static org.apache.jena.rfc3986.LibTestURI.good;
import static org.apache.jena.rfc3986.LibTestURI.schemeViolation;

import org.junit.jupiter.api.Test;

/** Test IRIs for scheme-specific validation rules. */
public class TestURISchemes {

    // == General
    // "jena:" is any unregistered scheme.
    @Test public void general_host_01() { good("jena://host/abc"); }
    @Test public void general_host_02() { schemeViolation("jena://HOST/abc", null, Issue.iri_host_not_lowercase); }
    @Test public void general_userinfo_01() { schemeViolation("jena://user@host/abc", null, Issue.iri_user_info_present); }
    @Test public void general_userinfo_02() { schemeViolation("jena://user:password@host/file/name.txt", null, Issue.iri_user_info_present, Issue.iri_password); }

    @Test public void general_percent_01() { good("ab%FFdef"); }
    @Test public void general_percent_02() { schemeViolation("ab%aFdef", null, Issue.iri_percent_not_uppercase); }
    @Test public void general_percent_03() { schemeViolation("ab%Afdef", null, Issue.iri_percent_not_uppercase); }
    @Test public void general_percent_04() { good("http://host/ab%FFdef"); }
    @Test public void general_percent_05() { schemeViolation("http://host%AA/ab%FFdef", null, Issue.iri_host_not_lowercase); }
    @Test public void general_percent_06() { schemeViolation("http://host%aa/ab%aa123", null, Issue.iri_percent_not_uppercase); }

    @Test public void general_dot_segments_01() { schemeViolation("http://example/abc/../def/", null, Issue.iri_bad_dot_segments); }
    @Test public void general_dot_segments_02() { schemeViolation("jena://example/abc/../def/", null, Issue.iri_bad_dot_segments); }
    @Test public void general_dot_segments_03() { schemeViolation("http://example/.", null, Issue.iri_bad_dot_segments); }
    @Test public void general_dot_segments_04() { schemeViolation("http://example/./", null, Issue.iri_bad_dot_segments); }

    @Test public void general_dot_segments_05() { schemeViolation("http:/..", null, Issue.iri_bad_dot_segments, Issue.http_no_host); }
    @Test public void general_dot_segments_06() { schemeViolation("http:/.", null, Issue.iri_bad_dot_segments, Issue.http_no_host); }
    @Test public void general_dot_segments_07() { good("./abcd"); }
    @Test public void general_dot_segments_08() { good("../abcd"); }
    @Test public void general_dot_segments_09() { good("../../abcd"); }
    @Test public void general_dot_segments_10() { good("./../abcd"); }
    @Test public void general_dot_segments_11() { schemeViolation("../../abcd/..", null, Issue.iri_bad_dot_segments); }
    @Test public void general_dot_segments_12() { schemeViolation("../../abcd/.", null, Issue.iri_bad_dot_segments); }
    @Test public void general_dot_segments_13() { good("http://host/pa.th/"); }
    @Test public void general_dot_segments_14() { good("http://host/.path/"); }
    @Test public void general_dot_segments_15() { good("http://host/path./"); }

    // == http:, https:
    @Test public void scheme_http_empty_host_1() { schemeViolation("http:///abc",  URIScheme.HTTP, Issue.http_empty_host); }
    @Test public void scheme_http_empty_host_2() { schemeViolation("https:///abc", URIScheme.HTTPS, Issue.http_empty_host); }
    // Including good, bad IPv4 addresses
    //@Test public void scheme_addr_v4_1() { schemeViolation("http://10.11.12.1300/abc", URIScheme.GENERAL, Issue.iri_bad_ipv4_address); }
    // RFC 3986 has the syntax of IPv6 addresses in the grammar.
    @Test public void scheme_addr_v6_1() { badSyntax("http://[1234:1.2.3.4]/abc"); }

    // == file:
    // Expects an absolute file path.
    @Test public void scheme_file_00()   { good("file:///path"); }
    @Test public void scheme_file_01()   { schemeViolation("file:", URIScheme.FILE, Issue.file_relative_path); }
    @Test public void scheme_file_02()   { schemeViolation("file:/", URIScheme.FILE, Issue.file_bad_form); }
    // Empty host, no path.
    @Test public void scheme_file_03()   { schemeViolation("file://", URIScheme.FILE, Issue.file_bad_form); }
    @Test public void scheme_file_10()   { schemeViolation("file:file/name.txt", URIScheme.FILE, Issue.file_relative_path); }
    @Test public void scheme_file_11()   { schemeViolation("file:/file/name.txt", URIScheme.FILE, Issue.file_bad_form); }
    // Host name.
    @Test public void scheme_file_12()   { schemeViolation("file://file/name.txt", URIScheme.FILE, Issue.file_bad_form); }

    // == did:
    // More in TestparseDID

    @Test public void scheme_did_01() { good("did:method:specific"); }
    @Test public void scheme_did_02() { schemeViolation("did::", URIScheme.DID, Issue.did_bad_syntax); }

    //== example: No violations
    //== urn:example: No violations

    // == urn:
    // Not a know namespace

    @Test public void scheme_urn_01() { good("urn:ns:nss"); }
    @Test public void scheme_urn_02() { good("urn:123:nss"); }
    @Test public void scheme_urn_03() { good("urn:1-2:nss"); }
    @Test public void scheme_urn_04() { schemeViolation("urn:-12:nss", URIScheme.URN, Issue.urn_bad_nid); }
    @Test public void scheme_urn_05() { schemeViolation("urn:and-:nss", URIScheme.URN, Issue.urn_bad_nid); }

    @Test public void scheme_urn_06() { schemeViolation("urn:", URIScheme.URN, Issue.urn_bad_nid); }
    @Test public void scheme_urn_07() { schemeViolation("urn::", URIScheme.URN, Issue.urn_bad_nid); }
    @Test public void scheme_urn_08() { schemeViolation("urn::abc", URIScheme.URN, Issue.urn_bad_nid); }

    @Test public void scheme_urn_09() { schemeViolation("urn:x:abc", URIScheme.URN, Issue.urn_bad_nid); }
    @Test public void scheme_urn_10() { schemeViolation("urn:abc:", URIScheme.URN, Issue.urn_bad_nss); }

    // OK by URN syntax, forbidden by RFC 8141 section 5.1
    @Test public void scheme_urn_11() { schemeViolation("urn:X-local:nss", URIScheme.URN, Issue.urn_x_namespace); }
    @Test public void scheme_urn_12() { schemeViolation("urn:x-local:nss", URIScheme.URN, Issue.urn_x_namespace); }
    // OK by URN syntax, forbidden by RFC 8141 section 5.1 Informal namespace.
    @Test public void scheme_urn_13() { schemeViolation("urn:urn-abc:nss", URIScheme.URN, Issue.urn_bad_nid); }
    @Test public void scheme_urn_14() { good("urn:urn-7:nss"); }
    @Test public void scheme_urn_15() { good("urn:nid:a"); }

    // 32 char NID
    @Test public void scheme_urn_16() { good("urn:12345678901234567890123456789012:a"); }
    // 33 char NID
    @Test public void scheme_urn_17() { schemeViolation("urn:abcdefghij-123456789-123456789-yz:a", URIScheme.URN, Issue.urn_bad_nid); }
    // Bad by URN specific rule for the query components.
    @Test public void scheme_urn_18() { schemeViolation("urn:local:abc/def?query=foo", URIScheme.URN, Issue.urn_bad_components); }
    // Two f-components = two fragments
    @Test public void scheme_urn_19() { badSyntax("urn:local:abc/def#f1#f2"); }
    @Test public void scheme_urn_20() { schemeViolation("urn:αβγ:abc", URIScheme.URN, Issue.urn_bad_nid); }

    // == urn:uuid:

    @Test public void scheme_urn_uuid_good_01() { good("urn:uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9"); }
    @Test public void scheme_urn_uuid_good_02() { good("urn:uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?+r?=q#frag"); }

    @Test public void scheme_urn_uuid_01() { schemeViolation("urn:uuid:", URIScheme.URN_UUID, Issue.uuid_bad_pattern); }
    @Test public void scheme_urn_uuid_02() { schemeViolation("urn:uuid:0000", URIScheme.URN_UUID, Issue.uuid_bad_pattern); }
    @Test public void scheme_urn_uuid_03() { schemeViolation("urn:UUID:06e775ac-2c38-11b2-801c-8086f2cc00c9", null, Issue.uuid_not_lowercase); }
    @Test public void scheme_urn_uuid_04() { schemeViolation("urn:UUID:06e775ac-2c38-11b2-801c-8086f2cc00c9?query=foo", null, Issue.urn_bad_components, Issue.uuid_not_lowercase); }
    @Test public void scheme_urn_uuid_05() { schemeViolation("urn:uuid:06e775ac-2c38-11b2-ZZZZ-8086f2cc00c9", URIScheme.URN_UUID, Issue.uuid_bad_pattern); }
    @Test public void scheme_urn_uuid_06() { schemeViolation("urn:uuid:06e775ac-2c38-11b2-ZZZZ-8086f2cc00c9?query=foo", null, Issue.uuid_bad_pattern, Issue.urn_bad_components); }

    // == uuid:

    @Test public void scheme_uuid_01() { schemeViolation("uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9", URIScheme.UUID, Issue.uuid_scheme_not_registered); }
    @Test public void scheme_uuid_02() { schemeViolation("UUID:06e775ac-2c38-11b2-801c-8086f2cc00c9", null, Issue.iri_scheme_name_is_not_lowercase,  Issue.uuid_scheme_not_registered); }
    @Test public void scheme_uuid_03() { schemeViolation("uuid:", URIScheme.UUID, Issue.uuid_scheme_not_registered, Issue.uuid_bad_pattern); }
    @Test public void scheme_uuid_04() { schemeViolation("uuid:0000-1111", URIScheme.UUID, Issue.uuid_scheme_not_registered, Issue.uuid_bad_pattern); }
    @Test public void scheme_uuid_05() { schemeViolation("UUID:0000-1111", null, Issue.iri_scheme_name_is_not_lowercase, Issue.uuid_scheme_not_registered, Issue.uuid_bad_pattern); }
    // No URN components in UUID
    @Test public void scheme_uuid_06() { schemeViolation("uuid:06e775ac-2c38-11b2-801c-8086f2cc00c9?+r", URIScheme.UUID, Issue.uuid_scheme_not_registered, Issue.uuid_has_query); }
    @Test public void scheme_uuid_07() { schemeViolation("uuid:06e775AC-2c38-11b2-801c-8086f2cc00c9?+r", URIScheme.UUID, Issue.uuid_scheme_not_registered, Issue.uuid_has_query, Issue.uuid_not_lowercase); }

    // == urn:oid:
    // More in TestParseOID
    @Test public void scheme_urn_oid_1() { good("urn:oid:2.3.4"); }
    @Test public void scheme_urn_oid_2() { schemeViolation("urn:oid:Z", URIScheme.URN_OID, Issue.oid_bad_syntax); }

    // == oid:
    @Test public void scheme_oid_1() { schemeViolation("oid:2.3.4", URIScheme.OID, Issue.oid_scheme_not_registered); }
    @Test public void scheme_oid_2() { schemeViolation("oid:Z", URIScheme.OID, Issue.oid_bad_syntax, Issue.oid_scheme_not_registered); }

    // == ftp: (unsupported scheme but can have IRI violations)
    @Test public void parse_ftp_01()    { schemeViolation("ftp://user@host:3333/abc/def?qs=ghi#jkl", null, Issue.iri_user_info_present); }
    @Test public void parse_ftp_02()    { good("ftp://[::1]/abc/def?qs=ghi#jkl"); }
}
