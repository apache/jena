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

public abstract class AbstractManifestTest implements Runnable {

    protected final ManifestEntry manifestEntry;

    protected AbstractManifestTest(ManifestEntry manifestEntry) {
        this.manifestEntry = manifestEntry;
    }

    @Override
    public final void run() {
        startTest();
        try {
            runTest();
            success();
        } catch (Throwable th) {
            failure();
            throw th;
        }
    }

    private String uri() {
        if ( manifestEntry.getURI() != null )
            return manifestEntry.getURI();
        return "http://test/testURI";
    }

    protected void startTest() {}
    protected abstract void runTest();
    protected void success() { EarlReporter.success(uri()); }
    protected void failure() { EarlReporter.failure(uri()); }
    protected void ignored() { EarlReporter.ignored(uri()); }
}
