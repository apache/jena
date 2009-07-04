/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.xmloutput;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.FileManager;

public class TestWriterFeatures extends ModelTestBase
{
    
    public TestWriterFeatures(String name)
    {
        super(name) ;
    }
    
    private static String testFileBase = "file:testing/abbreviated" ;

    private void checkReadWriteRead(String filename, String writerName,
                                    String propertyName, String propertyValue)
    {
            Model model = createMemModel();
            FileManager.get().readModel(model, filename );
            
            StringWriter sw = new StringWriter();
            RDFWriter w =  model.getWriter(writerName) ;
            if ( propertyName != null )
                w.setProperty(propertyName, propertyValue) ;
            w.write(model, sw, null) ;
            
            try { sw.close(); } catch (IOException ex) {}

            Model model2 = createMemModel();
            model2.read( new StringReader( sw.toString() ), filename );
            assertTrue( model.isIsomorphicWith( model2 ) );
    }
    
    private void checkReadWriteRead(String filename, String propertyName, String propertyValue)
    {
        checkReadWriteRead(filename, "RDF/XML", propertyName, propertyValue) ;
        checkReadWriteRead(filename, "RDF/XML-ABBREV", propertyName, propertyValue) ;
    }

    // test the tests !
    public void testEntity_0()
    { checkReadWriteRead(testFileBase+"/entities_1.ttl", "showXmlDeclaration", "true") ; }

    public void testEntity_1()
    { checkReadWriteRead(testFileBase+"/entities_1.ttl", "showDoctypeDeclaration", "true") ; }

    public void testEntity_2()
    { checkReadWriteRead(testFileBase+"/entities_2.ttl", "showDoctypeDeclaration", "true") ; }
    
    public void testEntity_3()
    { checkReadWriteRead(testFileBase+"/entities_3.ttl", "showDoctypeDeclaration", "true") ; }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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
 */