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

package org.apache.jena.shex.runner;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.parser.ShexParseException;
import org.apache.jena.shex.parser.ShExC;
import org.apache.jena.shex.sys.SysShex;
import org.junit.runners.model.InitializationError;

public class RunnerShexBadSyntax extends AbstractRunnerFiles {

    public RunnerShexBadSyntax(Class<? > klass) throws InitializationError {
        super(klass, RunnerShexBadSyntax::makeShexSyntaxTest, includes(), excludes());
    }

    private static Set<String> includes() {
        Set<String> includes = new HashSet<>();
        return includes;
    }

    private static Set<String> excludes() {
        Set<String> excludes = new HashSet<>();

        if ( ! SysShex.STRICT ) {
            // 2 length constraints of the same kind.
            excludes.add("1iriLength2.shex");
            excludes.add("1literalLength2.shex");
            // Unknown datatype, followed by numeric facet
            excludes.add("1unknowndatatypeMaxInclusive.shex");
        }

        return excludes;
    }

    public static Runnable makeShexSyntaxTest(String filename) {
        return ()->fileBadSyntax(filename);
    }

    public static ShexSchema fileBadSyntax(String filename) {
        String str = IO.readWholeFileAsUTF8(filename);
        InputStream input = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        try {
            ShexSchema shapes = ShExC.parse(input, filename, null);
            // Should not get here.
            System.out.print("-- ");
            System.out.println(FileOps.basename(filename));
            System.out.println(str);
            fail("Parsed negative syntax test");
            return shapes;
        } catch (ShexParseException ex) {
            return null;
        }
    }
}
