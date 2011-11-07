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

package com.hp.hpl.jena.xmloutput;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import com.hp.hpl.jena.Jena;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.NoWriterForLangException;
import com.hp.hpl.jena.xmloutput.impl.Abbreviated;
import com.hp.hpl.jena.xmloutput.impl.Basic;

/**
 *
 * @author  bwm, jjc
 * @version $Revision: 1.1 $
 */
public class testWriterInterface extends ModelTestBase {
    private String lang;
    /**
     * Constructor requires that all tests be named
     *
     * @param name The name of this test
     */
    public testWriterInterface(String name, String lang) {
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
        m.write( sos );
        assertTrue( sos.toString().indexOf( newline_XMLNS ) > -1 );
    }
    
    public void testInterface() {
        Model m1 = createMemModel();
        assertTrue( "Default writer should be Basic.",  m1.getWriter() instanceof Basic );
        assertTrue( "RDF/XML writer should be Basic.", m1.getWriter() instanceof Basic );
        assertTrue(
            "RDF/XML-ABBREV writer should be Abbreviated.",
            m1.getWriter("RDF/XML-ABBREV") instanceof Abbreviated);
        assertTrue(
            "N-TRIPLE writer should be NTripleWriter.",
            m1.getWriter("N-TRIPLE") instanceof NTripleWriter);
    }

    public void testNoWriter() {
        Model m1 = createMemModel();
        try {
            m1.setWriterClassName("foobar", "");
            m1.getWriter("foobar");
            fail("Missing Writer undetected.");
        } catch (NoWriterForLangException jx) {
            // that's what we expected
        }
    }

    public void testAnotherWriter() {
        Model m1 = createMemModel();
        m1.setWriterClassName("foobar", Jena.PATH + ".xmloutput.impl.Basic");
        assertTrue(
            "Failed to access set writer",
            (m1.getWriter("foobar") instanceof Basic));
    }

    public void testWriting() {
        // Changed to use "in-memory files" (ByteArrayOutputStream)
        // Used to use temporary file. 
        //System.err.println(lang);
        OutputStream output = null;
        Model m1 = createMemModel();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            output = out ;
            m1.write(output, lang);
            out.reset() ;
            output.close() ;
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (output != null)
                try {
                    output.close();
                } catch (Exception e) { }
        }
    }

}
