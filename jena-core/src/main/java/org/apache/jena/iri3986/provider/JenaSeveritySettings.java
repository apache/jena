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

package org.apache.jena.iri3986.provider;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rfc3986.Issue;
import org.apache.jena.rfc3986.Severity;
import org.apache.jena.rfc3986.SeverityMap;

public class JenaSeveritySettings {

    private static SeverityMap jenaSystemSettings = buildJenaSystemSettings();

    /** System default settings */
    public static SeverityMap jenaSystemSettings() {
        return jenaSystemSettings;
    }

    private static SeverityMap buildJenaSystemSettings() {
        Map<Issue, Severity> severityMap = new HashMap<>();
        SeverityMap.setSeverity(severityMap, Issue.ParseError,                        Severity.INVALID);

        // General
        SeverityMap.setSeverity(severityMap, Issue.iri_percent_not_uppercase,         Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.iri_host_not_lowercase,            Severity.IGNORE);
        SeverityMap.setSeverity(severityMap, Issue.iri_user_info_present,             Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.iri_password,                      Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.iri_bad_ipv4_address,              Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.iri_bad_ipv6_address,              Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.iri_bad_dot_segments,              Severity.WARNING);

        // Scheme
        SeverityMap.setSeverity(severityMap, Issue.iri_scheme_name_is_not_lowercase,  Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.iri_scheme_expected,               Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.iri_scheme_unexpected,             Severity.ERROR);

        // http/https
        SeverityMap.setSeverity(severityMap, Issue.http_no_host,                      Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.http_empty_host,                   Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.http_empty_port,                   Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.http_port_not_advised,             Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.http_omit_well_known_port,         Severity.WARNING);

        // urn:uuid and uuid
        SeverityMap.setSeverity(severityMap, Issue.uuid_bad_pattern,                  Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_has_query,                    Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_has_fragment,                 Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_not_lowercase,                Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.uuid_scheme_not_registered,        Severity.IGNORE);

        // urn (not UUID)
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_pattern,                   Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_nid,                       Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_nss,                       Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_components,                Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_non_ascii_character,           Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_x_namespace,                   Severity.IGNORE);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_informal_namespace,        Severity.WARNING);

        // file
        SeverityMap.setSeverity(severityMap, Issue.file_bad_form,                     Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.file_relative_path,                Severity.WARNING);

        // DID
        SeverityMap.setSeverity(severityMap, Issue.did_bad_syntax,                    Severity.ERROR);

        // OID
        SeverityMap.setSeverity(severityMap, Issue.oid_bad_syntax,                    Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.oid_scheme_not_registered,         Severity.WARNING);

        // Performs a completeness check.
        return SeverityMap.create("Jena settings", severityMap);
    }
}
