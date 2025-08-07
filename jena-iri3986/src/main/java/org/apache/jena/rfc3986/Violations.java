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

import java.util.HashMap;
import java.util.Map;

public class Violations {

    /** System default settings for violation levels */
    public static final SeverityMap globalDftLevels = systemSettings();

    /** Strict, all errors, settings. */
    public static final SeverityMap strictLevels = allErrorSettings();

    /** Current system settings for violation levels */
    private static SeverityMap levels = globalDftLevels;
    public static SeverityMap severities() {
        return levels;
    }

    /** Get the current severity level for an issue using the system settings. */
    public static Severity getSeverity(Issue issue) {
        return levels.getOrDefault(issue, Severity.INVALID);
    }

    /** Get the current severity level for an issue using the global settings. */
    public static Severity getSeverity(SeverityMap severityMap, Issue issue) {
        return severityMap.getOrDefault(issue, Severity.INVALID);
    }

    /** Set the global severity map for an issue using the system settings. */
    public static void setSystemSeverityMap(SeverityMap newSeverityMap) {
        levels = newSeverityMap;
    }

    public static void reset() {
        setSystemSeverityMap(globalDftLevels);
    }

    /** System default settings */
    private static SeverityMap systemSettings() {
        Map<Issue, Severity> severityMap = new HashMap<>();
        SeverityMap.setSeverity(severityMap, Issue.ParseError,                        Severity.INVALID);

        // General
        SeverityMap.setSeverity(severityMap, Issue.iri_percent_not_uppercase,         Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.iri_host_not_lowercase,            Severity.WARNING);
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
        SeverityMap.setSeverity(severityMap, Issue.http_empty_port,                   Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.http_port_not_advised,             Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.http_omit_well_known_port,         Severity.ERROR);

        // urn:uuid and uuid
        SeverityMap.setSeverity(severityMap, Issue.uuid_bad_pattern,                  Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_has_query,                    Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_has_fragment,                 Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_not_lowercase,                Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.uuid_scheme_not_registered,        Severity.WARNING);

        // urn (not UUID)
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_pattern,                   Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_nid,                       Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_nss,                       Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_components,                Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_non_ascii_character,           Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.urn_x_namespace,                   Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.urn_bad_informal_namespace,        Severity.WARNING);

        // file
        SeverityMap.setSeverity(severityMap, Issue.file_bad_form,                     Severity.WARNING);
        SeverityMap.setSeverity(severityMap, Issue.file_relative_path,                Severity.WARNING);

        // did
        SeverityMap.setSeverity(severityMap, Issue.did_bad_syntax,                    Severity.ERROR);

        // OID
        SeverityMap.setSeverity(severityMap, Issue.oid_bad_syntax,                    Severity.ERROR);
        SeverityMap.setSeverity(severityMap, Issue.oid_scheme_not_registered,         Severity.WARNING);

        return SeverityMap.create("IRI3986 SystemSettings", severityMap);
    }

    /** "all errors" settings */
    private static SeverityMap allErrorSettings() {
        Map<Issue, Severity> severityMap = new HashMap<>();
        for ( Issue issue : Issue.values() ) {
            SeverityMap.setSeverity(severityMap, issue, Severity.ERROR);
        }
        // Always 'INVALID'
        SeverityMap.setSeverity(severityMap, Issue.ParseError,                        Severity.INVALID);
        return SeverityMap.create("All errors",severityMap);
    }
}
