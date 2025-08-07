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

public enum Issue {
    ParseError,

    // General
    iri_percent_not_uppercase,
    iri_host_not_lowercase,
    iri_user_info_present,
    iri_password,
    iri_bad_ipv4_address,
    iri_bad_ipv6_address,
    iri_bad_dot_segments,   // Not at the start of a relative IRI.

    // Scheme
    iri_scheme_name_is_not_lowercase,
    iri_scheme_expected,
    iri_scheme_unexpected,

    // http/https
    http_no_host,
    http_empty_host,
    http_empty_port,
    http_port_not_advised,
    http_omit_well_known_port,

    // urn:uuid and uuid
    uuid_bad_pattern,
    uuid_has_query,
    uuid_has_fragment,
    uuid_not_lowercase,
    uuid_scheme_not_registered,

    // urn
    urn_non_ascii_character,
    urn_bad_pattern,
    urn_bad_nid,
    urn_bad_nss,
    urn_bad_components,
    urn_x_namespace,
    urn_bad_informal_namespace,

    // file
    file_bad_form,
    file_relative_path,

    // did
    did_bad_syntax,

    // urn:oid and oid:
    oid_bad_syntax,
    oid_scheme_not_registered,
}

