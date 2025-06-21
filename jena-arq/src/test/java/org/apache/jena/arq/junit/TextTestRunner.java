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

package org.apache.jena.arq.junit;

import java.util.function.Function;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import org.apache.jena.arq.junit.manifest.Manifest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.runners.RunnerOneManifest;
import org.apache.jena.arq.junit.runners.SetupManifests;
import org.apache.jena.atlas.junit.TextListenerLong;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.junit.EarlReport;

public class TextTestRunner {

    public static void runOne(String manifestFile, Function<ManifestEntry, Runnable> testMaker) {
        runOne(null, manifestFile, testMaker);
    }

    public static void runOne(EarlReport report, String manifestFile, Function<ManifestEntry, Runnable> testMaker) {
        Manifest manifest = Manifest.parse(manifestFile);
        RunnerOneManifest top = SetupManifests.build(report, manifest, testMaker, null);
        int countManifests = top.getManifestCount();

        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
        // Better: silent warning error handler.
        // Get rid of CheckerLiterals.WarnOnBadLiterals?
        // CheckerLiterals.WarnOnBadLiterals = false ;

        // Count includes the manifest itself.
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new TextListenerLong(System.out, countManifests));
        //junit.addListener(new TextListenerDots(System.out));

        Result result = junitCore.run(top);

        System.out.println("Tests run: "+(result.getRunCount()-countManifests));
        System.out.println("Failures:  "+result.getFailureCount());
        System.out.println("Manifests: "+top.getManifestCount());
    }
}

