/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.arq.junit.sparql;

import org.apache.jena.shared.JenaException;

public enum SparqlSpecVersion {
    SPARQL_10("sparql-1.0"),
    SPARQL_11("sparql-1.1"),
    SPARQL_12("sparql-1.2"),
    ARQ("ARQ")
    ;

    private final String specVersion;

    SparqlSpecVersion(String string) {
        this.specVersion = string;
    }

    static SparqlSpecVersion get(String label) {
        if ( label == null )
            return null;
        return switch(label) {
            case "sparql-1.0" -> SPARQL_10;
            case "sparql-1.1" -> SPARQL_11;
            case "sparql-1.2" -> SPARQL_12;
            case "ARQ", "arq" -> ARQ;
            default -> { throw new JenaException("Unrecognized version label: "+label); }
        };
    }
}
