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

import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OntDataPropertyTest {

    @Test
    public void testDataPropertyDomainsAndRanges() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntClass.Named c = m.createOntClass("C");
        OntDataRange.Named d = m.getDatatype(XSD.xstring);
        OntDataProperty p = m.createDataProperty("D");
        Assertions.assertNotNull(p.addRangeStatement(m.getRDFSLiteral()));
        Assertions.assertNotNull(p.addDomainStatement(m.getOWLNothing()));
        Assertions.assertSame(p, p.addDomain(m.getOWLThing()).addRange(d).addDomain(c));
        Assertions.assertEquals(2, p.ranges().count());
        Assertions.assertEquals(3, p.domains().count());

        Assertions.assertSame(p, p.removeDomain(m.getOWLThing()).removeRange(d));
        Assertions.assertEquals(1, p.ranges().count());
        Assertions.assertEquals(2, p.domains().count());

        p.removeRange(null).removeDomain(null);
        Assertions.assertEquals(2, m.size());
    }

    @Test
    public void testDataSuperProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty p1 = m.createDataProperty("D");
        OntDataProperty p2 = m.createDataProperty("P");
        Assertions.assertNotNull(p1.addSubPropertyOfStatement(m.getOWLBottomDataProperty()));
        Assertions.assertSame(p1, p1.addSuperProperty(m.getOWLTopDataProperty())
                .addSuperProperty(p2));
        Assertions.assertEquals(3, p1.superProperties().count());

        Assertions.assertSame(p1, p1.removeSuperProperty(m.getOWLThing()).removeSuperProperty(m.getOWLTopDataProperty()));
        Assertions.assertEquals(2, p1.superProperties().count());
        p1.removeSuperProperty(null);
        Assertions.assertEquals(0, p1.superProperties().count());
    }

    @Test
    public void testDataSubProperties() {
        OntModel m = OntModelFactory.createModel();

        OntDataProperty p1 = m.createDataProperty("p1");
        OntDataProperty p2 = m.createDataProperty("p2");
        Assertions.assertSame(p1, p1.addSubProperty(p2));
        Assertions.assertEquals(List.of(p2), p1.subProperties().toList());
        Assertions.assertEquals(List.of(), p1.superProperties().toList());
        m.statements(p2, RDFS.subPropertyOf, p1).toList().get(0).addAnnotation(m.getRDFSComment(), "xxx");
        Assertions.assertEquals(8, m.size());

        Assertions.assertSame(p1, p1.removeSubProperty(p2));
        Assertions.assertEquals(List.of(), p1.subProperties().toList());
        Assertions.assertEquals(List.of(), p1.superProperties().toList());

        Assertions.assertEquals(2, m.size());
    }

    @Test
    public void testDataPropertyAdditionalDeclarations() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty p = m.createDataProperty("P");
        Assertions.assertNotNull(p.addFunctionalDeclaration());
        Assertions.assertTrue(p.isFunctional());
        Assertions.assertSame(p, p.setFunctional(false));
        Assertions.assertFalse(p.isFunctional());
    }

    @Test
    public void testDataPropertyEquivalentProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty a = m.createDataProperty("A");
        OntDataProperty b = m.createDataProperty("B");
        OntDataProperty c = m.createDataProperty("C");
        Assertions.assertNotNull(a.addEquivalentPropertyStatement(b));
        Assertions.assertSame(a, a.addEquivalentProperty(c).addEquivalentProperty(m.getOWLBottomDataProperty()));
        Assertions.assertEquals(3, a.equivalentProperties().count());
        Assertions.assertSame(a, a.removeEquivalentProperty(b).removeEquivalentProperty(m.getRDFSComment()));
        Assertions.assertEquals(2, a.equivalentProperties().count());
        Assertions.assertSame(a, a.removeEquivalentProperty(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testDataPropertyDisjointProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty a = m.createDataProperty("A");
        OntDataProperty b = m.createDataProperty("B");
        OntDataProperty c = m.createDataProperty("C");
        Assertions.assertNotNull(a.addPropertyDisjointWithStatement(b));
        Assertions.assertSame(a, a.addDisjointProperty(c).addDisjointProperty(m.getOWLBottomDataProperty()));
        Assertions.assertEquals(3, a.disjointProperties().count());
        Assertions.assertSame(a, a.removeDisjointProperty(b).removeDisjointProperty(m.getRDFSComment()));
        Assertions.assertEquals(2, a.disjointProperties().count());
        Assertions.assertSame(a, a.removeDisjointProperty(null));
        Assertions.assertEquals(3, m.size());
    }

    @Test
    public void testListDisjoints() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntDataProperty d1 = m.createDataProperty("d1");
        OntDataProperty d2 = m.createDataProperty("d2");
        OntDataProperty d3 = m.createDataProperty("d3");
        OntDataProperty d4 = m.createDataProperty("d4");
        m.createDisjointDataProperties(d1, d2);
        m.createDisjointDataProperties(d1, d3);

        Assertions.assertEquals(0, d4.disjoints().count());
        Assertions.assertEquals(2, d1.disjoints().count());
        Assertions.assertEquals(1, d2.disjoints().count());
        Assertions.assertEquals(1, d3.disjoints().count());
    }
}
