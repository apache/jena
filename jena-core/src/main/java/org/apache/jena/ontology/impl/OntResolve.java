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

package org.apache.jena.ontology.impl;

import org.apache.jena.irix.IRIs;

public class OntResolve {
    private static boolean systemBaseIsFile = IRIs.getSystemBase().hasScheme("file");

    /**
     * Sort out URL for imports and FileManager usage. We canonicalize the URI so
     * that it does not matter if "file:relativePath" is used. Matches to record
     * already imported ontologies match up.
     */
    public static String resolve(String uri) {
        if ( uri == null )
            return null;
        if ( !systemBaseIsFile )
            return uri;
        if ( uri.startsWith("file:") )
            return IRIs.resolve(uri);
        return uri;
    }
}
