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

package org.apache.jena.arq.junit4.runners;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

import org.apache.jena.arq.junit4.EarlReport;
import org.apache.jena.arq.junit4.manifest.Manifest;
import org.apache.jena.arq.junit4.manifest.ManifestEntry;
import org.apache.jena.arq.junit4.manifest.Manifests;
import org.apache.jena.arq.junit4.manifest.Prefix;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.VocabTestQuery;

public class SetupManifests {

    // Print all manifests, top level and included.
    /*package*/ static boolean PrintManifests = false;
    /*package*/ static IndentedWriter out = IndentedWriter.stdout;

    private static class ManifestStructure {
        int manifestCount = 0 ;
    }

    /**
     * Do one level of tests. test are {@link Runnable Runnables} that each succeed or fail with an exception.
     */
    public static RunnerOneManifest build(EarlReport report, Manifest manifest, Function<ManifestEntry, Runnable> maker, String prefix) {
        ManifestStructure mStruct = new ManifestStructure();
        RunnerOneManifest top = buildForManifest(mStruct, report, manifest, maker, prefix);
        top.setManifestCount(mStruct.manifestCount);
        return top;
    }

    /**
     * Do one level of tests, recurse into sub-levels.
     * A test is a {@link Runnable} that succeeds or fails with an exception.
     */
    private static RunnerOneManifest buildForManifest(ManifestStructure mStruct, EarlReport report, Manifest manifest, Function<ManifestEntry, Runnable> maker, String prefix) {
        Description description = Description.createSuiteDescription(manifest.getName());
        if ( PrintManifests )
            out.println(manifest.getFileName()+" :: "+manifest.getName());

        // Count - adjust later reporting
        mStruct.manifestCount++;

        RunnerOneManifest thisLevel = new RunnerOneManifest(manifest, description);

        Iterator<String> sub = manifest.includedManifests();
        while(sub.hasNext() ) {
            if ( PrintManifests )
                out.incIndent();

            String mf = sub.next();
            Manifest manifestSub = Manifest.parse(mf);
            Runner runner = buildForManifest(mStruct, report, manifestSub, maker, prefix);
            thisLevel.add(runner);
            if ( PrintManifests )
                out.decIndent();
        }

        // Check entries do have test targets.

        manifest.entries().forEach(entry->{
            if ( entry.getAction() == null )
                throw new RuntimeException("Missing: action ["+entry.getEntry()+"]");
            if ( entry.getName() == null )
                throw new RuntimeException("Missing: label ["+entry.getEntry()+"]");
        });

        prepareTests(report, thisLevel, manifest, maker, prefix);
        return thisLevel;
    }

    public static void prepareTests(EarlReport report, RunnerOneManifest level, Manifest manifest, Function<ManifestEntry, Runnable> maker, String prefix) {
        manifest.entries().forEach(entry->{
            String label = prepareTestLabel(entry, prefix);
            Runnable runnable = maker.apply(entry);
            if ( runnable != null ) {
                Runner r = new RunnerOneTest(label, runnable, entry.getURI(), report);
                level.add(r);
            }
        });
    }

    private static String prepareTestLabel(ManifestEntry entry, String prefix) {
        String label = fixupName(entry.getName());
        if ( prefix != null )
            label = prefix+label;

        // action URI or action -> qt:query
        String str = null;

        if ( entry.getAction() != null ) {
            if ( entry.getAction().isURIResource() )
                str = entry.getAction().getURI();
            else if ( entry.getAction().isAnon() ) {
                Statement stmt = entry.getAction().getProperty(VocabTestQuery.query);
                if ( stmt != null && stmt.getObject().isURIResource() )
                    str = stmt.getObject().asResource().getURI();
            }
        }

        if ( str != null ) {
            int x = str.lastIndexOf('/');
            if ( x > 0 && x < str.length() ) {
                String fn = str.substring(x+1) ;
                label = label+" ("+fn+")";
            }
        }
        return label;
    }

    private static String fixupName(String string) {
            // Eclipse used to parse test names and () were special.
    //        string = string.replace('(', '[');
    //        string = string.replace(')', ']');
            return string;
        }

    /*package*/ static String getLabel(Class<? > klass) {
        Label annotation = klass.getAnnotation(Label.class);
        return ( annotation == null ) ? null : annotation.value();
    }

    /*package*/ static String getPrefix(Class<? > klass) {
        Prefix annotation = klass.getAnnotation(Prefix.class);
        return ( annotation == null ) ? null : annotation.value();
    }

    /*package*/ static List<String> getManifests(Class<? > klass) throws InitializationError {
        Manifests annotation = klass.getAnnotation(Manifests.class);
        if ( annotation == null ) {
            throw new InitializationError(String.format("class '%s' must have a @Manifests annotation", klass.getName()));
        }
        return List.of(annotation.value());
    }
}
