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

/**
 * Setting for reporting issues
 */
public enum Severity {
    IGNORE(000),         // Ignore the issue
    WARNING(100),        // Scheme-specific issue; valid RDF3986 syntax OK
    ERROR(200),          // Scheme-specific issue; valid RDF3986 syntax OK
    INVALID(300)         // Treat as "can't continue" e.g. RFC3986 parse error.
    ;

    private final int level;

    Severity(int level) {
        this.level = level;
    }

    /**
     * Return the severity level.
     */
    public int level() {
        return level;
    }
}