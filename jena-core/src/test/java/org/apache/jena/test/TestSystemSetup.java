/**
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

package org.apache.jena.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase ;
import junit.framework.TestSuite ;
import org.apache.commons.lang3.SystemUtils;
import org.apache.jena.JenaRuntime ;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;

public class TestSystemSetup extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(TestSystemSetup.class, "System setup") ;
    }
    
    public void testRDF11() {
        // This should be "false" in Jena2. 
        // This should be "true" in Jena3. 
        if ( ! JenaRuntime.isRDF11 )
            fail("RDF 1.0 mode enabled in Jena3 test run") ;
    }

    /** This test relies on forking a clean JVM */ 
    public void testInitFromRDFS() throws IOException, InterruptedException {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String java = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        if (SystemUtils.IS_OS_WINDOWS)
            java += ".exe";

        List<String> args = Arrays.asList(java, "-cp", classpath,
                "org.apache.jena.test.RDFSJenaInitTestApp");
        Process child = new ProcessBuilder().command(args)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();

        Assert.assertEquals(0, child.waitFor());
        Assert.assertEquals(RDFS.subClassOf.toString()+"\n",
                            FileUtils.readWholeFileAsUTF8(child.getInputStream()));
    }
}

