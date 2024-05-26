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
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFWriterI ;
import org.apache.jena.riot.*;
import org.junit.Assert ;
import org.junit.Test ;

/** Tests of the RDF/XML writers used via RIOT */
public class TestRDFXML_WriterProperties {

    static Model model = ModelFactory.createDefaultModel() ;
    static {
        RDFDataMgr.read(model,
                        new StringReader("<http://example/s> <http://example/p> <http://example/o> .\n"+
                                         "<http://example/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example/T> .\n"
                                        ), null, Lang.NT) ;
    }

    @Test public void propertiesAbbrev1() {
        // Write without setting properties
        String x0 = RDFWriter.source(model).format(RDFFormat.RDFXML_ABBREV).asString();

        // Write with setting properties, old style.
        @SuppressWarnings("deprecation")
        RDFWriterI rdfWriter = model.getWriter("RDF/XML-ABBREV");
        rdfWriter.setProperty("showXmlDeclaration", "true");
        rdfWriter.setProperty("showDoctypeDeclaration", "true");
        StringWriter w = new StringWriter() ;
        rdfWriter.write(model, w, null);
        String x1 = w.toString() ;

        // Did it have an effect?
        Assert.assertNotEquals(x0, x1) ;
    }


    @Test public void propertiesAbbrev2() {
        String name = "RDF/XML-ABBREV" ;
        // Write without setting properties
        String x0 = RDFWriter.source(model)
                             .format(RDFFormat.RDFXML_ABBREV)
                             .asString();

        // Write with setting properties, new style (set a mapping in the context)
        Map<String, Object> properties = new HashMap<>() ;
        properties.put("showXmlDeclaration", "true");
        properties.put("showDoctypeDeclaration", "true");
        String x1 = RDFWriter.source(model)
                             .format(RDFFormat.RDFXML_ABBREV)
                             .set(SysRIOT.sysRdfWriterProperties, properties)
                             .asString();
        // Did it have an effect?
        Assert.assertNotEquals(x0, x1) ;

        // Write with setting properties, old style.
        @SuppressWarnings("deprecation")
        RDFWriterI rdfWriter = model.getWriter(name);
        rdfWriter.setProperty("showXmlDeclaration", "true");
        rdfWriter.setProperty("showDoctypeDeclaration", "true");
        StringWriter w = new StringWriter() ;
        rdfWriter.write(model, w, null);
        String x2 = w.toString() ;

        // Did it have the same effect?
        Assert.assertEquals(x1, x2) ;
    }


}
