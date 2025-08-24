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

package org.apache.jena.shacl.tests.jena_shacl;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.shacl.testing.ShaclTest;
import org.apache.jena.shacl.testing.ShaclTestItem;
import org.apache.jena.shacl.testing.ShaclTests;

@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")
public class TestJenaShacl {

    private static Stream<Arguments> provideArgs() {
        String manifestFile = "src/test/files/local/manifest.ttl";
        List<String> omitManifests = List.of();
        List<Pair<String, ShaclTestItem>> z = ShaclTests.manifestNamed(manifestFile, omitManifests);
        List<Arguments> x = z.stream().map(p->Arguments.of(p.getLeft(), p.getRight())).toList();
        return x.stream();
    }

    private ShaclTestItem test;

    public TestJenaShacl(String name,  ShaclTestItem test) {
        this.test = test;
    }

    @Test
    public void test() { ShaclTest.shaclTest(test); }
}
