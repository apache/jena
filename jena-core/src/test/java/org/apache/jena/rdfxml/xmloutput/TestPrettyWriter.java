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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelTestLib;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPrettyWriter {

    /**
     * @param filename Read this file, write it out, read it in.
     * @param regex    Written file must match this.
     */
    private void check( String filename, String regex ) throws IOException {
        check(filename, regex, true);
    }

    private void checkNoMatch(String filename, String regex ) throws IOException {
        check(filename, regex, false);

    }
    private void check( String filename, String regex, boolean expected ) throws IOException {
        String contents = null;
        try {

            Model m = ModelTestLib.createMemModel();
            m.read( filename );
            try ( StringWriter sw = new StringWriter() ) {
                m.write( sw, "RDF/XML-ABBREV", filename );
                contents = sw.toString();
            }
            Model m2 = ModelTestLib.createMemModel();
            m2.read( new StringReader( contents ), filename );
            assertTrue( m.isIsomorphicWith( m2 ) );

            boolean found = Pattern.compile( regex,Pattern.DOTALL ).matcher( contents ).find();
            if ( expected != found ) {
                System.err.println("File: "+filename);
                System.err.println("Looking for /" + regex + "/ in "+contents);
                System.err.println("File: "+filename);
                m2.write(System.err, "RDF/XML");
            }

            assertTrue(expected==found, "Looking for /" + regex + "/ ");
            contents = null;
        } finally {
            if (contents != null) {
                System.err.println("Incorrect contents:");
                System.err.println(contents);
            }
        }
    }

    @Test
    public void testConsistency() throws IOException {
        checkNoMatch("file:testing/abbreviated/consistency.rdf"
                    ,"rdf:resource");
    }

    @Test
    public void testRDFCollection() throws IOException {
        check("file:testing/abbreviated/collection.rdf"
             ,"rdf:parseType=[\"']Collection[\"']");
    }

    @Test
    public void testOWLPrefix()  throws IOException {
        check("file:testing/abbreviated/collection.rdf"
             ,"xmlns:owl=[\"']http://www.w3.org/2002/07/owl#[\"']");
    }

    @Test
    public void testLi() throws IOException {
        check("file:testing/abbreviated/container.rdf"
             ,"<rdf:li.*<rdf:li.*<rdf:li.*<rdf:li");
    }
}
