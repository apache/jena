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

import java.io.ByteArrayOutputStream ;
import java.io.StringWriter ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.impl.NTripleWriter ;
import org.apache.jena.rdf.model.test.ModelTestBase ;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Abbrev ;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Basic ;

public class TestWriterInterface extends ModelTestBase {
    private String lang;
    /**
     * Constructor requires that all tests be named
     *
     * @param name The name of this test
     */
    public TestWriterInterface(String name, String lang) {
        super(name);
        this.lang = lang;
        //if ( lang!=null)
        //setName(name+"("+lang+")");
        //this.
    }

    /**
         Introduced to cope with bug 832682: double spacing on windows platforms.
         Make sure the xmlns prefixes are introduced by the correct line separator.
         (Java doesn't appear to understand that the notion of "line separator" should
         be portable ... come back C, all is forgiven. Well, not *all* ...)
    */
    public void testLineSeparator() {
        String newline = System.getProperty( "line.separator" );
        String newline_XMLNS = newline + "    xmlns";
        Model m = modelWithStatements( "http://eh/spoo thingies something" );
        m.setNsPrefix( "eh", "http://eh/" );
        StringWriter sos = new StringWriter();
        m.write( sos , "RDF/XML");
        assertTrue( sos.toString().contains( newline_XMLNS ) );
    }

    @SuppressWarnings("deprecation")
    public void testInterface() {
        Model m1 = createMemModel();
        // Not true when RIOT is used!
        assertTrue( "Default writer should be Basic.",  m1.getWriter(null) instanceof RDFXML_Basic );
        assertTrue( "RDF/XML writer should be Basic.", m1.getWriter("RDF/XML") instanceof RDFXML_Basic );
        assertTrue("RDF/XML-ABBREV writer should be Abbreviated.",
                   m1.getWriter("RDF/XML-ABBREV") instanceof RDFXML_Abbrev);
        assertTrue("N-TRIPLE writer should be NTripleWriter.",
                   m1.getWriter("N-TRIPLE") instanceof NTripleWriter);
    }

    public void testWriting() {
        Model m1 = createMemModel();
        try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
            m1.write(out, lang);
            out.reset() ;
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
