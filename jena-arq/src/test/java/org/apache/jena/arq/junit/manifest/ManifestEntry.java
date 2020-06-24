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

import org.apache.jena.rdf.model.Resource;

public class ManifestEntry {
    private final Manifest manifest;
    private final Resource entry;
    private final String name;
    private final Resource testType;
    private final Resource action;
    private final Resource result;

    public ManifestEntry(Manifest manifest, Resource entry, String name, Resource testType, Resource action, Resource result) {
        super();
        this.manifest = manifest;
        this.entry = entry;
        this.name = name;
        this.testType = testType;
        this.action = action;
        this.result = result;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Resource getEntry() {
        return entry;
    }

    public String getURI() {
        return entry.getURI();
    }

    public String getName() {
        return name;
    }

    public Resource getTestType() {
        return testType;
    }

    public Resource getAction() {
        return action;
    }

    public Resource getResult() {
        return result;
    }
}

