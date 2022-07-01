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

package org.apache.jena.riot.writer;

import java.io.StringReader ;
import java.io.StringWriter ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFWriterI ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.Assert ;
import org.junit.Test ;

/** Tests of the RDF/XML writers used via RIOT */ 
public class TestWriteRDFXML {
    
    static Model model = ModelFactory.createDefaultModel() ;
    static {
        RDFDataMgr.read(model, new StringReader("<http://example/s> <http://example/p> <http://example/o> ."), null, Lang.NT) ;
    }
    
    @Test public void propertiesAbbrev() {
        String name = "RDF/XML-ABBREV" ;
        // Write without setting properties
        StringWriter w = new StringWriter() ;
        model.getWriter(name).write(model, w, null); 
        String x0 = w.toString() ;

        // Write with setting properties
        RDFWriterI rdfWriter = model.getWriter(name);
        rdfWriter.setProperty("showXmlDeclaration", "true");
        rdfWriter.setProperty("showDoctypeDeclaration", "true");
        StringWriter w2 = new StringWriter() ;
        rdfWriter.write(model, w2, null);
        String x2 = w2.toString() ;
        
        // Did it have an effect?
        Assert.assertNotEquals(x0, x2) ;
    }
    
}
