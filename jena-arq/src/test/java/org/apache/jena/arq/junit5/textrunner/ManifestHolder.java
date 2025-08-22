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

package org.apache.jena.arq.junit5.textrunner;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

import org.apache.jena.arq.junit5.Scripts;
import org.apache.jena.shared.JenaException;

// This class DOES NOT run on its own. It is used by TextTestRunner.

class ManifestHolder {

    static class INIT implements LauncherDiscoveryListener {

        @Override public void launcherDiscoveryStarted(LauncherDiscoveryRequest request) {
            String fn = request.getConfigurationParameters().get(MANIFEST).get();
            if ( fn == null ) {
                System.err.println("Manifest not set");
                throw new JenaException("Manifest not set");
            }
            manifest = fn;
        }
    }

    public static String MANIFEST = "org.apache.jena.manifest";
    public static String namePrefix = null;
    public static String manifest = null;

    public ManifestHolder() {}

    @TestFactory
    @DisplayName("TextTestRunner")
    public Stream<DynamicNode> testFactory() {
        //String fn = System.getProperty(MANIFEST);

        String fn = manifest;

        if ( fn == null ) {
            System.err.println("Manifest not set");
            throw new JenaException("Manifest not set");
        }

//            DynamicNode dn = DynamicTest.dynamicTest("Hello!", ()->System.err.println("ManifestHolder: Executable"));
//            return Stream.of(dn);

        try {
            var x = Scripts.manifestTestFactory(fn, namePrefix);
            return x;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}