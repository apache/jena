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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.rfc3986.Issue;

class Issues {

    private static Map<Issue, IssueGroup> issuesGroups = issuesGroups();
    private static Map<String, IssueGroup> schemeNameMap = new ConcurrentHashMap<>();

    public static IssueGroup getScheme(String schemeName) {
        schemeName = Lib.lowercase(schemeName);
        return schemeNameMap.computeIfAbsent(schemeName, IssueGroup::get);
    }

    public static IssueGroup groupForIssue(Issue issue) {
        return issuesGroups.get(issue);
    }

//    public static IssueGroup groupForScheme(String schemeName) {
//        // scheme to URIScheme
//        // URIScheme to IssueGroup
//        return issuesGroups.get(issue);
//    }

    private enum STRICT_MODE { STRICT, NON_STRICT }

    private static Map<IssueGroup, STRICT_MODE> strictnessMap = new ConcurrentHashMap<>();

    /*package*/ static boolean isStrict(Issue issue) {
        Objects.requireNonNull(issue);
        IssueGroup issueGroup = Issues.groupForIssue(issue);
        return isStrict(issueGroup);
    }

    /*package*/ static boolean isStrict(IssueGroup issueGroup) {
        Objects.requireNonNull(issueGroup);
        return strictnessMap.get(issueGroup) == STRICT_MODE.STRICT;
    }

    /*package*/ static void setStrictness(IssueGroup issueGroup, boolean strict) {
        setStrictness(issueGroup,
                      strict ? STRICT_MODE.STRICT : STRICT_MODE.NON_STRICT);
    }

    /*package*/ static void setStrictness(IssueGroup issueGroup, STRICT_MODE mode) {
        if ( mode == null )
            strictnessMap.remove(issueGroup);
        else
            strictnessMap.put(issueGroup, mode);
    }

    private static Map<Issue, IssueGroup> issuesGroups() {
        Map<Issue, IssueGroup> issueGroups = new ConcurrentHashMap<>();

        entry(issueGroups, Issue.ParseError, IssueGroup.SYNTAX);

        // General
        entry(issueGroups, Issue.iri_percent_not_uppercase,         IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_host_not_lowercase,            IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_user_info_present,             IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_password,                      IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_bad_ipv4_address,              IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_bad_ipv6_address,              IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_bad_dot_segments,              IssueGroup.GENERAL);

        // Scheme
        entry(issueGroups, Issue.iri_scheme_name_is_not_lowercase,  IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_scheme_expected,               IssueGroup.GENERAL);
        entry(issueGroups, Issue.iri_scheme_unexpected,             IssueGroup.GENERAL);

        // http/https
        entry(issueGroups, Issue.http_no_host,                      IssueGroup.HTTP);
        entry(issueGroups, Issue.http_empty_host,                   IssueGroup.HTTP);
        entry(issueGroups, Issue.http_empty_port,                   IssueGroup.HTTP);
        entry(issueGroups, Issue.http_port_not_advised,             IssueGroup.HTTP);
        entry(issueGroups, Issue.http_omit_well_known_port,         IssueGroup.HTTP);

        // urn:uuid and uuid
        entry(issueGroups, Issue.uuid_bad_pattern,                  IssueGroup.UUID);
        entry(issueGroups, Issue.uuid_has_query,                    IssueGroup.UUID);
        entry(issueGroups, Issue.uuid_has_fragment,                 IssueGroup.UUID);
        entry(issueGroups, Issue.uuid_not_lowercase,                IssueGroup.UUID);
        entry(issueGroups, Issue.uuid_scheme_not_registered,        IssueGroup.UUID);

        // urn (not UUID)
        entry(issueGroups, Issue.urn_bad_pattern,                   IssueGroup.URN);
        entry(issueGroups, Issue.urn_bad_nid,                       IssueGroup.URN);
        entry(issueGroups, Issue.urn_bad_nss,                       IssueGroup.URN);
        entry(issueGroups, Issue.urn_bad_components,                IssueGroup.URN);
        entry(issueGroups, Issue.urn_non_ascii_character,           IssueGroup.URN);
        entry(issueGroups, Issue.urn_bad_informal_namespace,        IssueGroup.URN);
        entry(issueGroups, Issue.urn_x_namespace,                   IssueGroup.URN);

        // file
        entry(issueGroups, Issue.file_bad_form,                     IssueGroup.FILE);
        entry(issueGroups, Issue.file_relative_path,                IssueGroup.FILE);

        // did
        entry(issueGroups, Issue.did_bad_syntax,                    IssueGroup.DID);
        // OID
        entry(issueGroups, Issue.oid_bad_syntax,                    IssueGroup.OID);
        entry(issueGroups, Issue.oid_scheme_not_registered,         IssueGroup.OID);

        checkComplete(issueGroups);

        return issueGroups;
    }

    private static void entry(Map<Issue, IssueGroup> issueGroups, Issue issue, IssueGroup issueGroup) {
        issueGroups.put(issue, issueGroup);
    }

    /** Check the issue grouping is complete. */
    private static void checkComplete(Map<Issue, IssueGroup> issuesGroups) {
        for ( Issue issue : Issue.values()) {
            if ( ! issuesGroups.containsKey(issue) ) {
                FmtLog.error(Issues.class, "IssueGroup : Missing entry for issue %s", issue);
            }
        }
        for ( IssueGroup group : IssueGroup.values()) {
            if ( ! issuesGroups.containsValue(group) )
                FmtLog.error(Issues.class, "IssueGroup : No entries for issue group %s", issuesGroups);
        }
    }
}

