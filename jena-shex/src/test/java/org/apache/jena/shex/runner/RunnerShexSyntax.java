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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.parser.ShExC;
import org.junit.runners.model.InitializationError;

public class RunnerShexSyntax extends AbstractRunnerFiles {

    public RunnerShexSyntax(Class<? > klass) throws InitializationError {
        super(klass, RunnerShexSyntax::makeShexSyntaxTest, includes(), excludes());
    }

    private static Set<String> includes() {
        Set<String> includes = new HashSet<>();
        return includes;
    }

    private static Set<String> excludes() {
        Set<String> excludes = new HashSet<>();

        // Contains \ud800 (ill-formed surrogate pair)
        excludes.add("1refbnode_with_spanning_PN_CHARS_BASE1.shex");
        // Contains \u0d00 (ill-formed surrogate pair)
        excludes.add("_all.shex");

        return excludes;
    }

    public static Runnable makeShexSyntaxTest(String filename) {
        return ()->shapesFromFileSyntax(filename);
    }

    public static ShexSchema shapesFromFileSyntax(String filename) {
        String str = IO.readWholeFileAsUTF8(filename);
        InputStream input = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        try {
            ShexSchema shapes = ShExC.parse(input, filename, null);
            return shapes;
        } catch (RuntimeException ex) {
            System.out.print("-- ");
            System.out.println(FileOps.basename(filename));
            if ( ex.getMessage() != null )
                System.out.println(ex.getMessage());
            else
                System.out.println(ex.getClass().getSimpleName());
            System.out.println(str);
            throw ex;
        }
    }
}
