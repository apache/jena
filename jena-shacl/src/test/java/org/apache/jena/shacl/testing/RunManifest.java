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

package org.apache.jena.shacl.testing;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.lib.ShLib;

public class RunManifest {

    public static void runTest(String manifest) {
        runTest(manifest, true);
    }

    public static void runTest(String manifest, boolean verbose) {
        if ( verbose ) {
            String fn = manifest;
            if ( manifest.startsWith("file://" ) )
                fn = manifest.substring("file://".length());
            String x = IO.readWholeFileAsUTF8(fn);
            System.out.print(x);
            if ( ! x.endsWith("\n") )
                System.out.println();
            System.out.println("<><><><><>");
        }

        List<String> omitManifests = new ArrayList<>();
        // Miss out. e.g.
        //omitManifests.add(IRILib.filenameToIRI("src/test/resources/std/core/validation-reports/manifest.ttl"));

        List<ShaclTestItem> testCases = ShaclTests.manifest(manifest, omitManifests);

        testCases.forEach(stc->{
            if ( false ) {
                Shapes shapes = Shapes.parse(stc.getShapesGraph().getModel());
                ShLib.printShapes(shapes);
                System.out.println("<><><><><>");
            }
            try {
                ShaclTest.shaclTest(stc, verbose);
                System.out.println("OK: "+manifest);
            } catch (AssertionError ex) {
                System.out.flush();
                System.err.println(ex.getClass().getSimpleName()+": "+ex.getMessage());
            } catch (Throwable ex) {
                System.out.flush();
                System.err.println(ex.getClass().getSimpleName()+": "+ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
