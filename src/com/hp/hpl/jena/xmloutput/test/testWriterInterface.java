/*
    (c)      Copyright 2001, 2002 Hewlett-Packard Development Company, LP
    All rights reserved.
    [See end of file]
    $Id: testWriterInterface.java,v 1.11 2004-03-05 15:52:36 chris-dollin Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import com.hp.hpl.jena.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.*;
import com.hp.hpl.jena.xmloutput.impl.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.shared.*;

import java.io.*;

/**
 *
 * @author  bwm, jjc
 * @version $Revision: 1.11 $
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
/*
 *  (c)   Copyright 2001,2002 Hewlett-Packard Development Company, LP
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id: testWriterInterface.java,v 1.11 2004-03-05 15:52:36 chris-dollin Exp $
 */
