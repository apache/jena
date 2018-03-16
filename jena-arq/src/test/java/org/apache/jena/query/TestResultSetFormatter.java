/**
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
package org.apache.jena.query;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.engine.JsonIterator;
import org.junit.Test;

/**
 * Tests for the {@link ResultSetFormatter}.
 */
public class TestResultSetFormatter {

    @Test
    public void testIterator() throws IOException {
        Model model = ModelFactory.createDefaultModel();
        {
            Resource r = model.createResource(AnonId.create("first"));
            Property p = model.getProperty("");
            RDFNode node = ResourceFactory.createTypedLiteral("123", XSDDatatype.XSDdecimal);
            model.add(r, p, node);
            r = model.createResource(AnonId.create("second"));
            p = model.getProperty("");
            node = ResourceFactory.createTypedLiteral("abc", XSDDatatype.XSDstring);
            model.add(r, p, node);
            r = model.createResource(AnonId.create("third"));
            p = model.getProperty("");
            node = ResourceFactory.createLangLiteral("def", "en");
            model.add(r, p, node);
            r = model.createResource(AnonId.create("fourth"));
            p = model.getProperty("");
            node = ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean);
            model.add(r, p, node);
        }
        Query query = QueryFactory.create("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }", Syntax.syntaxARQ);
        try ( QueryExecution qexec = QueryExecutionFactory.create(query, model) ) {
            JsonIterator execJsonItems = (JsonIterator) qexec.execJsonItems();
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ResultSetFormatter.outputAsJSON(baos, execJsonItems);
                String output = baos.toString(Charset.forName("UTF-8"));
                assertTrue(output.contains("\"_:first\""));
                assertTrue(output.contains("\"_:second\""));
                assertTrue(output.contains("\"_:third\""));
                assertTrue(output.contains("\"_:fourth\""));
                assertTrue(output.contains("\"true\""));
                assertTrue(output.contains("\"123\""));
                assertTrue(output.contains("\"abc\""));
            }
        }
    }

}
