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

package org.apache.jena.arq.junit5.manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.extension.*;

public class ManifestRunner implements Extension, TestInstanceFactory, BeforeAllCallback, AfterAllCallback {

    private List<Manifest> manifests;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // Store store = context.getStore(ns);
        try {
            Class<?> c = context.getTestClass().orElse(null);
            // c.getAnnotation(Prefix.class).value();
            String[] manifestFiles = c.getAnnotation(Manifests.class).value();
            System.out.println("manifests: " + Arrays.asList(manifestFiles));

            manifests = new ArrayList<Manifest>();
            for ( String manifestFile : manifestFiles ) {
                Manifest manifest = Manifest.parse(manifestFile);
                manifests.add(manifest);
            }

        } catch (Exception ex) {
            System.err.println("Bad things: " + ex.getMessage());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {}

    //@TestFactory
    Stream<DynamicNode> recursiveTests() {
        System.out.println("recursiveTests: ");
       return Stream.of();
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext,
                                     ExtensionContext extensionContext) throws TestInstantiationException {
        return recursiveTests();
    }
}
