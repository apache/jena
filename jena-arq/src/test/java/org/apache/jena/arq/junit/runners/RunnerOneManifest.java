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

package org.apache.jena.arq.junit.runners;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.arq.junit.manifest.Manifest;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/** Runner for a manifest - children are added by "RunnerOfTests" */
public class RunnerOneManifest extends Runner {
    private Description description;
    private List<Runner> tests = new ArrayList<>();
    private Manifest manifest;

    public RunnerOneManifest(Manifest manifest, Description description) {
        this.manifest = manifest;
        this.description = description;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        //System.out.println("Manifest: "+getDescription().getDisplayName());
        notifier.fireTestStarted(description);
        tests.forEach(r -> r.run(notifier));
        notifier.fireTestFinished(description);
    }

    public void add(Runner runner) {
        description.addChild(runner.getDescription());
        tests.add(runner);
    }
}
