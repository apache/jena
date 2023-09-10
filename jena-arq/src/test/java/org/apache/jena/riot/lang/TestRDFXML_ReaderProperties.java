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

package org.apache.jena.riot.lang;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.lang.rdfxml.RRX;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.junit.Test;

/**
 * Tests for settign reder properties - specific to ARP0 and ARP1.
 */
public class TestRDFXML_ReaderProperties {
    @Test public void rdfxmlreaderProperties_arp0() {
        execTest(RRX.RDFXML_ARP0);
    }

    @Test public void rdfxmlreaderProperties_arp1() {
        execTest(RRX.RDFXML_ARP0);
    }

    private void execTest(Lang parser) {
        // Inline illustrative data.
        String data = StrUtils.strjoinNL
                ("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                ,"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
               ,"         xmlns:ex=\"http://examples.org/\""
               ,"         xml:base=\"http://example/\""
               ,"         >"
                // This rdf:ID starts with a digit which normally causes a warning.
                ,"  <ex:Type rdf:ID='012345'></ex:Type>"
                ,"</rdf:RDF>"
                );


        // Properties to be set.
        // This is a map propertyName->value
        Map<String, Object> properties = new HashMap<>();
        // See class ARPErrorNumbers for the possible ARP properties.
        properties.put("WARN_BAD_NAME", "EM_IGNORE");

        Model model = ModelFactory.createDefaultModel();
        // Build and run a parser
        RDFParserBuilder builder = RDFParser.fromString(data)
            .lang(parser)
            // Put a properties object into the Context.
            .set(SysRIOT.sysRdfReaderProperties, properties)
            // Exception on warning or error.
            .errorHandler(ErrorHandlerFactory.errorHandlerExceptions());
        builder.parse(model);
        assertEquals(1, model.size());
    }
}
