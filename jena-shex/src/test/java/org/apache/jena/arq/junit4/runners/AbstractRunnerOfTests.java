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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import org.apache.jena.arq.junit4.manifest.*;

/**
 * Common super class for {@code @Runner(....)}.
 * <p>
 * Creates a runner for a manifest that has children for each included manifest and
 * each test defined in the manifest. It follow included manifests Annotations
 * supported:
 * <ul>
 * <li>{@code @Label("Some name")}</li>
 * <li>{@code @Manifests({"manifest1","manifest2",...})}</li>
 * </ul>
 * This class sorts out the annotations, including providing before/after class, then
 * creates a hierarchy of tests to run.
 *
 * @see SetupManifests
 */
public abstract class AbstractRunnerOfTests extends ParentRunner<Runner> {
    private Description  description;
    private List<Runner> children = new ArrayList<>();

    // Need unique names.

    public AbstractRunnerOfTests(Class<? > klass, Function<ManifestEntry, Runnable> maker) throws InitializationError {
        super(klass);
        String label = SetupManifests.getLabel(klass);
        if ( label == null )
            label = klass.getName();

        // Get the annotation arguments.
        String prefix = SetupManifests.getPrefix(klass);
        description = Description.createSuiteDescription(label);
        prepare(klass, maker,prefix);
    }

    private void prepare(Class<? > klass, Function<ManifestEntry, Runnable> maker, String prefix) throws InitializationError {
        List<String> manifests = SetupManifests.getManifests(klass);
        if ( manifests.isEmpty() )
            //System.err.println("No manifests: "+label);
            throw new InitializationError("No manifests");
        prepare(manifests, klass.getSimpleName(), maker, prefix);
    }

    private void prepare(List<String> manifests, String traceName, Function<ManifestEntry, Runnable> maker, String prefix) throws InitializationError {
        // For each manifest
        for ( String manifestFile : manifests ) {
            //System.out.println("** "+klass.getSimpleName()+" -- "+manifestFile);
            if ( SetupManifests.PrintManifests ) {
                if ( traceName != null )
                    SetupManifests.out.println("** "+traceName+" -- "+manifestFile);
                else
                    SetupManifests.out.println("** Manifest: "+manifestFile);
                SetupManifests.out.incIndent();
            }
            Manifest manifest = Manifest.parse(manifestFile);
            if ( SetupManifests.PrintManifests ) {
                // Record manifests.
                Manifest.walk(manifest, m->SetupManifests.out.println(m.getFileName()+" :: "+m.getName()), e->{});
            }
            Runner runner = SetupManifests.build(null, manifest, maker, prefix);
            description.addChild(runner.getDescription());
            children.add(runner);
            if ( SetupManifests.PrintManifests )
                SetupManifests.out.decIndent();
        }
        if ( SetupManifests.PrintManifests )
            SetupManifests.out.flush();
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    protected List<Runner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
