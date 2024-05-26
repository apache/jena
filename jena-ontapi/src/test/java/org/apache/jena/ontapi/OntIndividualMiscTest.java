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

import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.vocabulary.OWL2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * To test {@link OntIndividual}.
 */
public class OntIndividualMiscTest {

    @Test
    public void testPositiveAssertions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntDataProperty d = m.createDataProperty("D");
        OntObjectProperty p = m.createObjectProperty("P");
        OntAnnotationProperty a = m.getRDFSComment();

        Assertions.assertSame(i1, i1.addAssertion(d, m.createLiteral("1"))
                .addAssertion(d, m.createLiteral("2"))
                .addAssertion(p.asNamed(), i2)
                .addAssertion(a, m.createLiteral("3")));
        Assertions.assertEquals(4, i1.positiveAssertions().count());
        Assertions.assertEquals(2, i1.positiveAssertions(d).count());
        Assertions.assertEquals(8, m.size());

        Assertions.assertSame(i1, i1.removeAssertion(d, null).removeAssertion(p.asNamed(), i2));
        Assertions.assertEquals(1, i1.positiveAssertions().count());
        Assertions.assertSame(i1, i1.removeAssertion(null, null));
        Assertions.assertEquals(0, i1.positiveAssertions().count());
        Assertions.assertEquals(4, m.size());
    }

    @Test
    public void testDifferentIndividuals() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual a = m.createIndividual("A");
        OntIndividual b = m.createOntClass("C1").createIndividual("B");
        OntIndividual c = m.createOntClass("C2").createIndividual();
        OntIndividual d = m.createIndividual("D");

        Assertions.assertNotNull(a.addDifferentFromStatement(b));
        Assertions.assertSame(a, a.addDifferentIndividual(c).addDifferentIndividual(d).removeDifferentIndividual(b));
        Assertions.assertEquals(2, a.differentIndividuals().count());
        Assertions.assertSame(a, a.removeDifferentIndividual(null));
        Assertions.assertEquals(7, m.size());
    }

    @Test
    public void testSameIndividuals() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual a = m.createIndividual("A");
        OntIndividual b = m.createOntClass("C1").createIndividual("B");
        OntIndividual c = m.createOntClass("C2").createIndividual();
        OntIndividual d = m.createIndividual("D");

        Assertions.assertNotNull(a.addSameAsStatement(b));
        Assertions.assertSame(a, a.addSameIndividual(c).addSameIndividual(d).removeSameIndividual(b));
        Assertions.assertEquals(2, a.sameIndividuals().count());
        Assertions.assertSame(a, a.removeSameIndividual(null));
        Assertions.assertEquals(7, m.size());
    }

    @Test
    public void testNegativeAssertions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntIndividual i3 = m.createIndividual("I3");
        OntDataProperty d = m.createDataProperty("D");
        OntObjectProperty p = m.createObjectProperty("P");

        Assertions.assertSame(i1, i1.addNegativeAssertion(d, m.createLiteral("1")));
        Assertions.assertEquals(1, i1.negativeAssertions().count());
        Assertions.assertEquals(0, i1.positiveAssertions().count());
        Assertions.assertSame(i1, i1.addNegativeAssertion(d, m.createLiteral("2"))
                .addNegativeAssertion(p, i2).addNegativeAssertion(p, i3));
        Assertions.assertEquals(4, i1.negativeAssertions().count());
        Assertions.assertEquals(0, i1.positiveAssertions().count());

        Assertions.assertEquals(2, m.statements(null, OWL2.targetIndividual, null).count());
        Assertions.assertEquals(2, m.statements(null, OWL2.targetValue, null).count());
        Assertions.assertEquals(21, m.size());

        Assertions.assertSame(i1, i1.removeNegativeAssertion(d, null).removeNegativeAssertion(p.asNamed(), i3));
        Assertions.assertEquals(1, i1.negativeAssertions().count());
        Assertions.assertEquals(1, m.statements(null, OWL2.targetIndividual, null).count());
        Assertions.assertEquals(0, m.statements(null, OWL2.targetValue, null).count());

        Assertions.assertSame(i1, i1.removeNegativeAssertion(null, null));
        Assertions.assertEquals(5, m.size());
    }

    @Test
    public void testRemoveIndividual() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntIndividual i3 = m.createIndividual("I3");
        OntDataProperty d = m.createDataProperty("D");
        OntObjectProperty p = m.createObjectProperty("P");

        i1.addNegativeAssertion(p, i2)
                .addAssertion(p.asNamed(), i3)
                .addNegativeAssertion(d, m.createLiteral("1"))
                .addAssertion(d, m.createLiteral("2")).addComment("The individual to test");
        Assertions.assertEquals(16, m.size());

        Assertions.assertEquals(4, m.removeOntObject(i1).size());
    }

    @Test
    public void testClassAssertions() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntClass c1 = m.createOntClass("C1");
        OntClass c2 = m.createOntClass("C2");
        OntIndividual i2 = c2.createIndividual();
        long size = 4;

        Assertions.assertSame(i1, i1.detachClass(c1).detachClass(m.getOWLThing()));
        Assertions.assertEquals(size, m.size());

        Assertions.assertNotNull(i1.addClassAssertion(c2));
        Assertions.assertSame(i1, i1.attachClass(c2).attachClass(c1).attachClass(m.getOWLThing()));
        Assertions.assertEquals(3, i1.classes().count());
        Assertions.assertEquals(size + 3, m.size());

        Assertions.assertSame(i1, i1.detachClass(c2));
        Assertions.assertEquals(size + 2, m.size());
        Assertions.assertEquals(2, i1.classes().count());
        Assertions.assertSame(i1, i1.detachClass(null));
        Assertions.assertEquals(size, m.size());
        Assertions.assertEquals(0, i1.classes().count());

        Assertions.assertSame(i2, i2.attachClass(m.getOWLThing()).attachClass(c1).attachClass(c2));
        Assertions.assertEquals(size + 2, m.size());
        Assertions.assertEquals(3, i2.classes().count());

        // not possible to delete all class assertions:
        try {
            i2.detachClass(null);
            Assertions.fail("Possible to delete all class expressions");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertEquals(size + 2, m.size());
        Assertions.assertSame(i2, i2.detachClass(c2).detachClass(m.getOWLThing()));
        Assertions.assertEquals(1, i2.classes().count());
        Assertions.assertEquals(size, m.size());

        // not possible to delete the last class assertions:
        try {
            i2.detachClass(c1);
            Assertions.fail("Possible to delete the last class expressions");
        } catch (OntJenaException j) {
            // expected
        }
        Assertions.assertEquals(size, m.size());
    }

    @Test
    public void testListDisjoints() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntIndividual i1 = m.createIndividual("I1");
        OntIndividual i2 = m.createIndividual("I2");
        OntIndividual i3 = m.createIndividual("I3");
        OntIndividual i4 = m.createIndividual("I4");
        m.createDifferentIndividuals(i1, i2);
        m.createDifferentIndividuals(i1, i3);

        Assertions.assertEquals(0, i4.disjoints().count());
        Assertions.assertEquals(2, i1.disjoints().count());
        Assertions.assertEquals(1, i2.disjoints().count());
        Assertions.assertEquals(1, i3.disjoints().count());
    }
}
