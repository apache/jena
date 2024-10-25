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

/** IssueGroup is not quite URIScheme:
 * <ul>
 * <li>It groups http and https as one category
 * <li>It allows setting different groups to be strict/non-strict independently
 * </ul>
 */
public enum IssueGroup {
    SYNTAX,         // Parse errors in the basic RFC3986 grammar
    GENERAL,        // General IRI e.g. scheme name is not lowercase
    // Supported schemes as groups
    HTTP,
    URN,            // General URNs: Not the UUID or OID namespaces.
    UUID,           // urn:uuid: and uuid:
    DID,
    OID,
    FILE;

    private IssueGroup() {}

    public static IssueGroup get(String name) {
        if ( name == null )
            return null;
        if ( name.endsWith(":") )
            name = name.substring(0, name.length()-1);
        for ( IssueGroup iGroup : IssueGroup.values() ) {
            if ( iGroup.name().equalsIgnoreCase(name) )
                return iGroup;
        }
        return null;
    }
}