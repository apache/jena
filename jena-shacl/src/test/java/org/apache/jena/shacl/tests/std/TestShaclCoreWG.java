/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.shacl.tests.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.shacl.testing.ShaclTest;
import org.apache.jena.shacl.testing.ShaclTestItem;
import org.apache.jena.shacl.testing.ShaclTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestShaclCoreWG {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {
        String manifest = "testing/std/core/manifest.ttl";
        List<String> omitManifests = new ArrayList<>();
        return ShaclTests.junitParameters(manifest, omitManifests);
    }

    private ShaclTestItem test;

    public TestShaclCoreWG(String name,  ShaclTestItem test) {
        this.test = test;
    }

    @Test
    public void test() { ShaclTest.shaclTest(test, false); }
}
