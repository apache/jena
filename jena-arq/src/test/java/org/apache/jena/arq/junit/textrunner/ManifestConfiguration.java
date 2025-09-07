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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Configuration for one run of TextTestRunner.
 */
public class ManifestConfiguration implements Iterable<ManifestConfiguration.Entry> {
    public record Entry(String manifestFile, String prefix) {}

    private List<Entry> manifests = new ArrayList<>();

    static ManifestConfiguration singleton = new ManifestConfiguration();

    public static ManifestConfiguration get() {
            return singleton;
    }

    public void add(String manifestFile) {
        add(manifestFile, null);
    }

    public void add(String manifestFile, String prefix) {
        manifests.add(new Entry(manifestFile, prefix));
    }

    @Override
    public Iterator<Entry> iterator() {
        return manifests.iterator();
    }
}
