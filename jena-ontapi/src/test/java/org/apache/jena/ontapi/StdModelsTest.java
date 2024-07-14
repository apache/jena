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

package org.apache.jena.ontapi;

import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class StdModelsTest {
    static final String NS = "http://example.com/test#";

    static Model createStdModelClassesABCDEFGThing() {
        Model m = ModelFactory.createDefaultModel();
        m.createResource(NS + "A", OWL2.Class);
        m.createResource(NS + "B", OWL2.Class);
        m.createResource(NS + "C", OWL2.Class);
        m.createResource(NS + "D", OWL2.Class);
        m.createResource(NS + "E", OWL2.Class);
        m.createResource(NS + "F", OWL2.Class);
        m.createResource(NS + "G", OWL2.Class);
        m.createResource(OWL2.Thing.getURI(), OWL2.Class);
        return m;
    }

    @Test
    public void testShortestPath0() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");

        List<Statement> actual = StdModels.findShortestPath(m, A, B, s -> true);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void testShortestPath1() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Property p = m.createProperty(NS + "p");
        A.addProperty(p, B);

        List<Statement> actual = StdModels.findShortestPath(m, A, B, s -> true);
        Assertions.assertEquals(List.of(p), actual.stream().map(Statement::getPredicate).toList());
    }

    @Test
    public void testShortestPath2() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Resource C = m.getResource(NS + "C");
        Property p = m.createProperty(NS + "p");
        A.addProperty(p, B);
        B.addProperty(p, C);

        List<Statement> actual = StdModels.findShortestPath(m, A, C, s -> true);
        Assertions.assertEquals(List.of(p, p), actual.stream().map(Statement::getPredicate).toList());
    }

    @Test
    public void testShortestPath3() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Resource C = m.getResource(NS + "C");
        Resource D = m.getResource(NS + "D");
        Resource E = m.getResource(NS + "E");
        Resource F = m.getResource(NS + "F");
        Property p = m.createProperty(NS + "p");
        // a - b - c
        A.addProperty(p, B);
        B.addProperty(p, C);

        // a - d - e - f
        A.addProperty(p, D);
        D.addProperty(p, E);
        E.addProperty(p, F);

        List<Statement> actual1 = StdModels.findShortestPath(m, A, C, s -> true);
        Assertions.assertEquals(List.of(p, p), actual1.stream().map(Statement::getPredicate).toList());

        List<Statement> actual2 = StdModels.findShortestPath(m, A, F, s -> true);
        Assertions.assertEquals(List.of(p, p, p), actual2.stream().map(Statement::getPredicate).toList());

        List<Statement> actual3 = StdModels.findShortestPath(m, A, C, s -> p.equals(s.getPredicate()));
        Assertions.assertEquals(List.of(p, p), actual3.stream().map(Statement::getPredicate).toList());

        List<Statement> actual4 = StdModels.findShortestPath(m, A, F, s -> p.equals(s.getPredicate()));
        Assertions.assertEquals(List.of(p, p, p), actual4.stream().map(Statement::getPredicate).toList());
    }

    @Test
    public void testShortestPath4() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Resource C = m.getResource(NS + "C");
        Resource D = m.getResource(NS + "D");
        Resource E = m.getResource(NS + "E");
        Resource F = m.getResource(NS + "F");
        Property p = m.createProperty(NS + "p");
        Property q = m.createProperty(NS + "q");

        // a - b - c by q
        A.addProperty(q, B);
        B.addProperty(q, C);

        // a - d - e - f by p
        A.addProperty(p, D);
        D.addProperty(p, E);
        E.addProperty(p, F);

        List<Statement> actual1 = StdModels.findShortestPath(m, A, C, s -> p.equals(s.getPredicate()));
        Assertions.assertTrue(actual1.isEmpty());
        List<Statement> actual2 = StdModels.findShortestPath(m, A, F, s -> p.equals(s.getPredicate()));
        Assertions.assertEquals(List.of(p, p, p), actual2.stream().map(Statement::getPredicate).toList());
    }

    @Test
    public void testShortestPath5() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Property p = m.createProperty(NS + "p");
        A.addProperty(p, A);

        List<Statement> actual = StdModels.findShortestPath(m, A, A, s -> true);
        Assertions.assertEquals(List.of(p), actual.stream().map(Statement::getPredicate).toList());
    }

    @Test
    public void testShortestPath6() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Resource C = m.getResource(NS + "C");
        Property p = m.createProperty(NS + "p");
        Property q = m.createProperty(NS + "q");
        // a - b - a by q
        // tests loop detection
        A.addProperty(q, B);
        B.addProperty(q, A);

        List<Statement> actual = StdModels.findShortestPath(m, A, C, s -> Set.of(p, q).contains(s.getPredicate()));
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    public void testShortestPath7() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Resource D = m.getResource(NS + "D");
        Resource E = m.getResource(NS + "E");
        Resource F = m.getResource(NS + "F");
        Property p = m.createProperty(NS + "p");
        Property q = m.createProperty(NS + "q");

        // a - d - e - f by p and q
        A.addProperty(p, D);
        D.addProperty(q, E);
        D.addProperty(q, B);
        E.addProperty(p, F);

        List<Statement> actual = StdModels.findShortestPath(m, A, F, s -> Set.of(p, q).contains(s.getPredicate()));
        Assertions.assertEquals(List.of(p, q, p), actual.stream().map(Statement::getPredicate).toList());
    }

    @Test
    public void testShortestPath8() {
        Model m = createStdModelClassesABCDEFGThing();
        Resource A = m.getResource(NS + "A");
        Resource B = m.getResource(NS + "B");
        Resource D = m.getResource(NS + "D");
        Resource E = m.getResource(NS + "E");
        Resource F = m.getResource(NS + "F");
        Property p = m.createProperty(NS + "p");
        Property q = m.createProperty(NS + "q");

        // a - d - e - f by p and q
        A.addProperty(p, D);
        D.addProperty(q, E);
        D.addProperty(q, "bluff");
        D.addProperty(q, B);
        E.addProperty(p, F);
        F.addProperty(q, "arnie");

        List<Statement> actual = StdModels.findShortestPath(m, A, ResourceFactory.createPlainLiteral("arnie"),
                s -> Set.of(p, q).contains(s.getPredicate()));
        Assertions.assertEquals(List.of(p, q, p, q), actual.stream().map(Statement::getPredicate).toList());
    }
}
