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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/** Immutable choice of severity settings for {@link Issue Issues}. */
public class SeverityMap {
    private final String severityMapName ;
    private final Map<Issue, Severity> map;

    public static SeverityMap create(String name, Map<Issue, Severity> map) {
        checkSeverityMappingComplete(name, map);
        return new SeverityMap(name, map);
    }

    private SeverityMap(String name, Map<Issue, Severity> map) {
        this.map = Map.copyOf(map);
        this.severityMapName = name;
    }

    /** Return a mutable copy of the severity settings. */
    public Map<Issue, Severity> asMap() {
        return new ConcurrentHashMap<>(map);
    }

    public Severity get(Issue issue) {
        return map.get(issue);
    }

    public Severity getOrDefault(Issue issue, Severity dftSeverity) {
        return map.getOrDefault(issue, dftSeverity);
    }

    public void forEach(BiConsumer<Issue, Severity> action) {
        map.forEach(action);
    }

    @Override
    public String toString() {
        return "SeverityMap: "+severityMapName;
    }

    /** Utility to help build a {@link Map} for creating a {@link SeverityMap}. */
    public static void setSeverity(Map<Issue, Severity> map, Issue issue, Severity severity) {
        Objects.requireNonNull(issue);
        Objects.requireNonNull(severity);
        map.put(issue, severity);
    }

    /** Utility to verify that a severity map is complete. */
    /*package*/ static void checkSeverityMappingComplete(String name, Map<Issue, Severity> levels) {
        Set<Issue> keys = levels.keySet();
        for ( Issue issue : Issue.values()) {
            if ( ! keys.contains(issue) ) {
                System.err.printf("Severity map %s : Missing entry for issue %s\n", name, issue);
            }
        }
        if ( levels.get(Issue.ParseError) != Severity.INVALID ) {
            System.err.printf("Severity map %s : %s is not severity INVALID", name, Issue.ParseError);
        }
    }
}
