/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfxml.xmloutput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelTestLib;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.rdfxml.xmloutput.impl.BaseXMLWriter;
import org.apache.jena.rdfxml.xmloutput.impl.SimpleLogger;

public abstract class BaseTestXMLOutput
{
    protected abstract String getLang();

    public BaseTestXMLOutput() { }

    static SimpleLogger realLogger;

    static boolean sawErrors;

    static SimpleLogger falseLogger = new SimpleLogger() {
        @Override
        public void warn(String s) {
            sawErrors = true;
        }

        @Override
        public void warn(String s, Exception e) {
            sawErrors = true;
        }
    };

    static void blockLogger() {
        realLogger = BaseXMLWriter.setLogger(falseLogger);
        sawErrors = false;
    }

    static boolean unblockLogger() {
        BaseXMLWriter.setLogger(realLogger);
        return sawErrors;
    }

    static protected class Change {
        public void modify(RDFWriterI w) {}

        public void modify(Model m) {}

        public void modify(Model m, RDFWriterI w) {
            modify(m);
            modify(w);
        }

        public static Change none() {
            return new Change();
        }

        public static Change setProperty(final String property, final String value) {
            return new Change() {
                @Override
                public void modify(RDFWriterI writer) {
                    writer.setProperty(property, value);
                }
            };
        }

        public static Change setProperty(final String property, final boolean value) {
            return new Change() {
                @Override
                public void modify(RDFWriterI writer) {
                    writer.setProperty(property, value);
                }
            };
        }

        public static Change setPrefix(final String prefix, final String URI) {
            return new Change() {
                @Override
                public void modify(Model m) {
                    m.setNsPrefix(prefix, URI);
                }
            };
        }

        public static Change blockRules(String ruleName) {
            return setProperty("blockrules", ruleName);
        }

        public Change andSetPrefix(String prefix, String URI) {
            return and(Change.setPrefix(prefix, URI));
        }

        private Change and(final Change change) {
            return new Change() {
                @Override
                public void modify(Model m, RDFWriterI w) {
                    Change.this.modify(m, w);
                    change.modify(m, w);
                }
            };
        }
    }



    /*package*/ void checkA(String filename, String regex, Change code) throws IOException {
        checkY(filename, regex, null, code);
    }

    /*package*/ void checkY(String filename, String regexPresent, String regexAbsent, Change code) throws IOException {
        checkX(filename, null, regexPresent, regexAbsent, false, code);
    }

    /*package*/ void checkC(String filename, String encoding, String regexPresent, String regexAbsent, Change code) throws IOException {
        checkX(filename, encoding, regexPresent, regexAbsent, false, code);
    }

    /*package*/ void checkB(String filename, String regexAbsent, Change code, String base) throws IOException {
        // Ensure regexAbsent is present when no changes (Change.none())
        checkZ(filename, null, regexAbsent, null, false, Change.none(), base);
        checkZ(filename, null, null, regexAbsent, false, code, base);
    }

    /*package*/ void checkX(String filename, String encoding, String regexPresent, String regexAbsent,
                         boolean errs, Change code) throws IOException {
        checkZ(filename, encoding, regexPresent, regexAbsent, errs, code, "file:"+filename);
    }

    protected void checkZ(String filename, String encoding, String regexPresent, String regexAbsent,
                         boolean errorExpected, Change code, String base) throws IOException {
        blockLogger();
        boolean errorsFound;
        Model m = ModelTestLib.createMemModel();

        try(InputStream in = new FileInputStream(filename)) {
            m.read(in,base);
        }
        @SuppressWarnings("resource")
        Writer sw;
        ByteArrayOutputStream bos = null;
        if (encoding == null)
            sw = new StringWriter();
        else {
            bos = new ByteArrayOutputStream();
            sw = new OutputStreamWriter(bos, encoding);
        }
        Properties p = (Properties) System.getProperties().clone();
        @SuppressWarnings("deprecation")
        RDFWriterI writer = m.getWriter(getLang());
        code.modify( m, writer );
        writer.write( m, sw, base );
        sw.close();

        String contents;
        if (encoding == null)
            contents = sw.toString();
        else {
            contents = bos.toString(encoding);
        }
        try {
            Model m2 = ModelTestLib.createMemModel();
            m2.read(new StringReader(contents), base);
            assertTrue(m.isIsomorphicWith(m2), "Data got changed.");
            if (regexPresent != null) {
                boolean b = Pattern.compile(regexPresent,Pattern.DOTALL).matcher(contents).find();
                if ( !b ) {
                    System.err.println("File: "+filename);
                    System.err.println("Should find /" + regexPresent + "/ in \n"+contents);
                }
                assertTrue(b, "Should find /" + regexPresent + "/ in |"+contents+"|");
            }
            if (regexAbsent != null) {
                boolean b = !Pattern.compile(regexAbsent,Pattern.DOTALL).matcher(contents).find();
                if ( !b ) {
                    System.err.println("File: "+filename);
                    System.err.println("Should not find /" + regexPresent + "/ in \n"+contents);
                }
                assertTrue(b, "Should not find /" + regexAbsent + "/ in |"+contents+"|");
            }
            contents = null;
        } finally {
            errorsFound = unblockLogger();
            System.setProperties(p);
            if (contents != null) {
                System.err.println("===================");
                System.err.println("Offending content - " + toString());
                System.err.println("===================");
                System.err.println(contents);
                System.err.println("===================");
            }
        }
        assertEquals(errorExpected, errorsFound, "Errors (not) detected.");

    }

    }
