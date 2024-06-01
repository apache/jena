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
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OntAnnotationPropertyTest {

    @Test
    public void testAnnotationPropertyDomainsAndRanges() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntAnnotationProperty p = m.createAnnotationProperty("A");
        Assertions.assertNotNull(p.addRangeStatement(m.getRDFSComment()));
        Assertions.assertNotNull(p.addDomainStatement(m.getRDFSComment()));
        Assertions.assertSame(p, p.addDomain(m.getOWLThing()).addRange(m.getOWLNothing()).addDomain(m.getRDFSLabel()));
        Assertions.assertEquals(2, p.ranges().count());
        Assertions.assertEquals(3, p.domains().count());

        Assertions.assertSame(p, p.removeDomain(m.getOWLThing()).removeRange(m.getRDFSComment()));
        Assertions.assertEquals(1, p.ranges().count());
        Assertions.assertEquals(2, p.domains().count());
    }

    @Test
    public void testAnnotationSuperProperties() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        OntAnnotationProperty p = m.createAnnotationProperty("A");
        Assertions.assertNotNull(p.addSubPropertyOfStatement(m.getRDFSComment()));
        Assertions.assertSame(p, p.addSuperProperty(m.getRDFSLabel())
                .addSuperProperty(m.getAnnotationProperty(RDFS.seeAlso)));
        Assertions.assertEquals(3, p.superProperties().count());

        Assertions.assertSame(p, p.removeSuperProperty(m.getOWLThing()).removeSuperProperty(m.getRDFSComment()));
        Assertions.assertEquals(2, p.superProperties().count());
        p.removeSuperProperty(null);
        Assertions.assertEquals(0, p.superProperties().count());
    }

    @Test
    public void testAnnotationSubProperties() {
        OntModel m = OntModelFactory.createModel();

        OntAnnotationProperty p1 = m.createAnnotationProperty("p1");
        OntAnnotationProperty p2 = m.createAnnotationProperty("p2");
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
}
