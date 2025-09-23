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

package org.apache.jena.arq.junit.textrunner;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.manifest.ManifestProcessor;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.JenaException;

//This class DOES NOT run on its own.
// It is used by TextTestRunner and gets configuration from ManifestConfiguration
class ManifestHolder {

    public ManifestHolder() { }

    @TestFactory
    @DisplayName("TextTestRunner")
    public Stream<DynamicNode> testFactory() {
        Stream<DynamicNode> tests = null;
        for ( var entry : ManifestConfiguration.get() ) {
            Stream<DynamicNode> tests1 = oneManifest(entry.manifestFile(), entry.prefix());
            tests = StreamOps.concat(tests, tests1);
        }
        return tests;
    }

    private int totalManifestCount = 0 ;

    private Stream<DynamicNode> oneManifest(String fn, String prefix) {
        if ( fn == null ) {
            System.err.println("Manifest not set");
            throw new JenaException("Manifest not set");
        }
        try {
            int before = ManifestProcessor.getCounterManifests();
            Stream<DynamicNode> x = Scripts.manifestTestFactory(fn, prefix);
            int after = ManifestProcessor.getCounterManifests();
            totalManifestCount = after-before;
            return x;
        } catch (RiotNotFoundException ex) {
            System.err.println("Not found: "+fn);
            // Exceptions are swallowed by JUnit5.
            throw new RiotNotFoundException("Manifest "+fn);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}