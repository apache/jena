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

package org.apache.jena.rdfpatch.filelog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.rdfpatch.filelog.rotate.ManagedOutput;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestRotate
{
    static { LogCtl.setJavaLogging(); }

    @Parameters(name="Policy={0}")
    public static Collection<Object[]> data()
    {
        List<Object[]> params = new ArrayList<>();
        for ( FilePolicy p : FilePolicy.values() ) {
            Object[] x = {p, false};
            params.add(x);
        }
        return params;
    }

    private final FilePolicy policy;
    private final boolean includesBaseName;

    private static Path DIR = Paths.get("target/filelog");

    @BeforeClass
    public static void beforeClass() {
        FileOps.ensureDir(DIR.toString());
        FileOps.clearAll(DIR.toString());
    }

    /** "touch" - without metadata */
    private static void touch(String filename) {
        Path p = DIR.resolve(filename);
        if ( ! Files.exists(p) ) {
            try ( OutputStream out = new FileOutputStream(p.toString()) ) {}
            catch (IOException ex) {
                IO.exception(ex);
            }
        }
    }

    // Test for file existance : pass the full filename.
    private static void assertExists(String filename) {
        Path p = Paths.get(filename);
        boolean b = Files.exists(p);
        assertTrue("File does not exist: "+p, b);
    }

    private static void assertNotExists(String filename) {
        Path p = Paths.get(filename);
        boolean b = Files.exists(p);
        assertFalse("File exists: "+p, b);
    }

    private static void assertExists(Path dir, String filename) {
        Path p = dir.resolve(filename);
        boolean b = Files.exists(p);
        assertTrue("File does not exist: "+p, b);
    }

    public TestRotate(FilePolicy policy, Boolean includesBaseName) {
        this.policy = policy;
        this.includesBaseName = includesBaseName;
    }

    @Test
    public void t1_firstFile() throws IOException {
        String FN = "fileA-"+policy.name();
        ManagedOutput mout = OutputMgr.create(DIR, FN, policy);
        try ( OutputStream out = mout.output() ) {
            PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8.name());
            ps.print("abcdef");
        }
        assertNotNull(mout.latestFilename());
        assertExists(mout.latestFilename().toString());
    }

    @Test
    public void t2_rotateFile() throws IOException {
        String FN = "fileB-"+policy.name();
        ManagedOutput mout = OutputMgr.create(DIR, FN, policy);
        try ( OutputStream out = mout.output() ) { }
        assertNotNull(mout.latestFilename());
        mout.rotate();
        try ( OutputStream out = mout.output() ) { }
        assertNotNull(mout.latestFilename());
        String x2 = mout.latestFilename().toString();
        assertExists(x2);
    }
}
