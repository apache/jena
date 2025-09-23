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

package org.apache.jena.arq.junit.manifest;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

public class ManifestEntry {
    private final Manifest manifest;
    private final Node entry;
    private final String name;
    private final Node testType;
    private final Node action;
    private final Node result;

    public ManifestEntry(Manifest manifest, Node entry, String name, Node testType, Node action, Node result) {
        super();
        this.manifest = manifest;
        this.entry = entry;
        this.name = name;
        this.testType = testType;
        this.action = action;
        this.result = result;
    }

    /**
     * Return a ManifestEntry with different type/action/result.
     * This is used to replace rdf-tests-cg where test behaviour is expected to be different.
     */
    public static ManifestEntry alter(ManifestEntry entry, Node testType,  Node action, Node result) {
        return new ManifestEntry(entry.getManifest(),
                                 entry.getEntry(),
                                 entry.getName(),
                                 testType, action, result);
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Graph getGraph() {
        if ( manifest == null )
            return null;
        return manifest.getGraph();
    }

    public Node getEntry() {
        return entry;
    }

    public String getURI() {
        if ( entry.isURI() )
            return entry.getURI();
        return null;
    }

    public String getName() {
        return name;
    }

    public Node getTestType() {
        return testType;
    }

    public Node getAction() {
        return action;
    }

    public Node getResult() {
        return result;
    }

    @Override
    public String toString() {
        if ( false )
            // Multi-line
            return String.format("ManifestEntry: <%s>\n    \"%s\"\n    action=%s result=%s", getURI(), getName(), getAction(), getResult());
        // Shorter, single line
        return String.format("ManifestEntry: <%s>", getURI(), getName(), getAction(), getResult());
    }
}

