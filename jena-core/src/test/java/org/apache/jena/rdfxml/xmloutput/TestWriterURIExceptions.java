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

package org.apache.jena.rdfxml.xmloutput;

import java.io.StringWriter;

import junit.framework.TestCase;
import org.apache.jena.irix.IRIException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
     Added as a place to put the test(s) which ensure that thrown URI exceptions
     carry the bad URI with them.
*/
public class TestWriterURIExceptions extends TestCase
    {
    public TestWriterURIExceptions( String name )
        { super( name ); }

        public void testBadURIExceptionContainsBadURIInMessage() {
            String badURI = "http://host/path[]";   // [] are illegal in IRIs in this position.
            Model m = ModelFactory.createDefaultModel();
            m.add(m.createResource(badURI), m.createProperty("eg:BC"), m.createResource("eg:CD"));
            try {
                m.write(new StringWriter(), "RDF/XML");
                fail("should detect bad URI " + badURI);
            } catch (IRIException e) {
                assertTrue("message must contain failing URI", e.getMessage().indexOf(badURI) > 0);
            }
        }
    }