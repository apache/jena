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

package org.apache.jena.sparql.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

/**
 * Tests for {@link JsonIterator}.
 */
public class TestJsonIterator {

    @Test
    public void testIterator() {
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
        }
        Query query = QueryFactory.create("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }", Syntax.syntaxARQ);
        try ( QueryExecution qexec = QueryExecutionFactory.create(query, model) ) {
            JsonIterator execJsonItems = (JsonIterator) qexec.execJsonItems();
            assertTrue(execJsonItems.hasNext());
            assertNotNull(execJsonItems.next());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testInitialStates() {
        JsonIterator iterator = new JsonIterator(null, Collections.emptyList());
        assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveNotImplemented() {
        JsonIterator iterator = new JsonIterator(null, Collections.emptyList());
        iterator.remove();
    }
}
