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

package org.apache.jena.rdfpatch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.junit.Test;

public class TestPatchIO_Text extends AbstractTestPatchIO {

    private static void noWarning(Runnable action) {
    }

    @Override
    protected void write(OutputStream out, RDFPatch path) {
        RDFPatchOps.write(out, path);
    }

    @Override
    protected RDFPatch read(InputStream in) {
        return RDFPatchOps.read(in, ErrorHandlerFactory.errorHandlerExceptionOnError());
    }

    private static String DIR = "testing/files/";

    @Test
    public void readPatch_1() {
        RDFPatchOps.read(DIR+"syntax-1.rdfp");
    }

    private static ErrorHandler ehExceptions = ErrorHandlerTestLib.asExceptions();

    private static RDFPatch parseSyntax(String string) {
        byte[] bytes = Bytes.string2bytes(string);
        InputStream input = new ByteArrayInputStream(bytes);
        RDFPatch patch = RDFPatchOps.read(input, ehExceptions);
        return patch;
    }

    @Test(expected=ErrorHandlerTestLib.ExWarning.class)
    public void read_warning_01() {
        String str = "A <http://example/s1> <http://example/p1> 'abc\uFFFDdef' <http://example/g1> .";
        parseSyntax(str);
    }

    @Test public void read_no_warning_01() {
        // Explicit escape - accepted.
        String str = "A <http://example/s1> <http://example/p1> 'abc\\uFFFDdef' <http://example/g1> .";
        parseSyntax(str);
    }
}
