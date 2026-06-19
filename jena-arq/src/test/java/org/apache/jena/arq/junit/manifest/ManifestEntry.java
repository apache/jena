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

package org.apache.jena.arq.junit.manifest;

import static org.apache.jena.system.G.isBlank;
import static org.apache.jena.system.G.isLiteral;
import static org.apache.jena.system.G.isURI;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.system.G;
import org.apache.jena.system.RDFDataException;

public class ManifestEntry {
    private final Manifest manifest;
    private final Node entry;
    private final String name;
    private final Node testType;
    private final String specVersion;
    private final Node action;
    private final Node result;

    static Set<Object> onceNoType = new HashSet<>();
    static Set<Object> onceWithVersion = new HashSet<>();

    public ManifestEntry(Manifest manifest, Node entry, String name, Node testType, String specVersion, Node action, Node result) {
        super();

        this.manifest = manifest;
        this.entry = requireResource(entry, "entry");
        this.name = name;

        // Legacy - some local tests are written short-hand with no  - assumes a SPARQL eval test.
        // See SPARQLTests.
        //this.testType = requireResource(testType, "testType");
        this.testType = testType;
        this.specVersion = specVersion;
        this.action = requireResource(action, "action");
        this.result = optionalResource(result, "result");
    }

    private static void runOnce(Set<Object> once, Object key, Runnable msg) {
        if ( ! once.contains(key) ) {
            once.add(key);
            msg.run();
        }
    }

    // --> G
    // Check for URI or blank node.
    private static Node requireResource(Node n, String role) {
        if ( n == null )
            throw new RDFDataException(role+ ": null");
        if ( isURI(n) )
            return n;
        if ( isBlank(n) )
            return n;
        throw new RDFDataException(role+": Not a resource: "+NodeFmtLib.displayStr(n));
    }

    private static Node optionalResource(Node n, String role) {
        if ( n == null )
            return n;
        return requireResource(n, role);
    }

    // Check for URI or blank node.
    private static String requireString(Node n, String role) {
        if ( n == null )
            throw new RDFDataException(role+ ": null");
        if ( ! isLiteral(n) )
            throw new RDFDataException(role+": Not a string: "+NodeFmtLib.displayStr(n));
        return G.asString(n);   // xsd:string.
        //return n.getLiteralLexicalForm();
    }

    private static String optionalString(Node n, String role) {
        if ( n == null )
            return null;
        return requireString(n, role);
    }

    /**
     * Return a ManifestEntry with different type/action/result.
     * This is used to replace rdf-tests-cg where test behaviour is expected to be different.
     */
    public static ManifestEntry alter(ManifestEntry entry, Node testType, String specVersion,  Node action, Node result) {
        return new ManifestEntry(entry.getManifest(),
                                 entry.getEntry(),
                                 entry.getName(),
                                 testType,
                                 specVersion,
                                 action,
                                 result);
    }

    /**
     * Return a ManifestEntry with different name.
     */
    public static ManifestEntry alter(ManifestEntry entry, String altName) {
        return new ManifestEntry(entry.getManifest(),
                                 entry.getEntry(),
                                 altName,
                                 entry.getTestType(),
                                 entry.getSpecVersion(),
                                 entry.getAction(),
                                 entry.getResult());
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

    public String getSpecVersion() {
        return specVersion;
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

