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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shex.Shex;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.parser.ShexParseException;
import org.apache.jena.shex.writer.WriterShExC;
import org.junit.runners.model.InitializationError;

public class RunnerPrintShex extends org.apache.jena.shex.runner.AbstractRunnerFiles {

    public RunnerPrintShex(Class<? > klass) throws InitializationError {
        super(klass, RunnerPrintShex::makeShexPrintTest, includes(), excludes());
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

        // Don't work - incomplete printing?
        excludes.add("1literalPattern_with_all_controls.shex");
        excludes.add("1literalPattern_with_ascii_boundaries.shex");

        return excludes;
    }

    private static Runnable makeShexPrintTest(String filename) {
        return ()->testShexPrint(filename);
    }

    public static void testShexPrint(String FN) {
        ShexSchema schema = Shex.readSchema(FN);
        IndentedLineBuffer out = new IndentedLineBuffer();

        String label = FN;
        out.println("## Print");
        WriterShExC.print(out, schema);
        String s = out.toString();

        ShexSchema schema2;
        try {
            schema2 = Shex.schemaFromString(s);
        } catch (ShexParseException ex) {
            // Bad syntax in printed output.
            printFile(FN);
            System.out.println("-- --");
            System.out.println(s);
            System.out.println("** Syntax error ** "+FN);
            String msg = ex.getMessage();
            int i = msg.indexOf('\n');
            if ( i > 0 )
                msg = msg.substring(0,i);
            System.out.println("** "+msg);
            IndentedWriter out2 = IndentedWriter.stdout.clone();
            out2.setLineNumbers(true);
            WriterShExC.print(out2, schema);
            System.out.println("-- --");
            Shex.printSchema(schema);
            System.out.println("== ==");
            throw ex;
        }

        // bnode isomorphism.
        boolean equivalent = schema.sameAs(schema2);
        //if ( !equivalent ) {
        if ( !equivalent && !s.contains("_:") ) {
            printFile(FN);
            System.out.println("** Not same ** "+label);
            System.out.println(s);
            System.out.println("-- --");
            Shex.printSchema(System.out, schema);
            System.out.println("-- --");
            Shex.printSchema(System.out, schema2);
            System.out.println("== ==");
            fail("ShEx schames not equivalent");
        }
    }

    private static void printFile(String FN) {
        System.out.println("File: "+FN);
        System.out.print(IO.readWholeFileAsUTF8(FN));
    }

    private static void printString(String s) {
        System.out.print(s);
        if ( ! s.endsWith("\n") )
            System.out.println();
        System.out.println("-- --");
    }

}
