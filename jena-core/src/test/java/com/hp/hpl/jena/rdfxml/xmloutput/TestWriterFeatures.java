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

package com.hp.hpl.jena.rdfxml.xmloutput;

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
            
            String contents = null ;
            
            try ( StringWriter sw = new StringWriter() ) {
                RDFWriter w =  model.getWriter(writerName) ;
                if ( propertyName != null )
                    w.setProperty(propertyName, propertyValue) ;
                w.write(model, sw, null) ;
                contents = sw.toString() ;
            } catch (IOException ex) { /* ignore : StringWriter */ }

            try ( StringReader sr = new StringReader( contents ) ) {
                Model model2 = createMemModel();
                model2.read( new StringReader( contents ), filename );
                assertTrue( model.isIsomorphicWith( model2 ) );
            }
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
