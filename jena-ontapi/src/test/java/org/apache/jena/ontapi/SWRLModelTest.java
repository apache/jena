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

import org.apache.jena.graph.Graph;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SWRL;
import org.apache.jena.vocabulary.SWRLB;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

public class SWRLModelTest {

    private static OntSWRL.Variable getVariable(OntModel m, String localName) {
        return m.ontObjects(OntSWRL.Variable.class)
                .filter(r -> localName.equals(r.getLocalName())).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void testSWRLObjectsOnFreshOntology() {
        String uri = "http://test.com/swrl-1";
        String ns = uri + "#";

        OntModel m = OntModelFactory.createModel()
                .setID(uri).getModel()
                .setNsPrefix("test", ns)
                .setNsPrefix("SWRL", SWRL.NS)
                .setNsPrefixes(OntModelFactory.STANDARD);

        OntClass.Named cl1 = m.createOntClass(ns + "Class1");
        OntClass.Named cl2 = m.createOntClass(ns + "Class2");
        OntDataProperty d = m.createDataProperty(ns + "DP");
        OntObjectProperty.Named p = m.createObjectProperty(ns + "OP");
        OntIndividual i1 = cl1.createIndividual(ns + "Individual1");

        OntClass.OneOf cl3 = m.createObjectOneOf(i1);
        OntIndividual i2 = cl3.createIndividual();

        OntSWRL.Variable var1 = m.createSWRLVariable(ns + "Variable1");
        OntSWRL.DArg dArg1 = m.createTypedLiteral(12).inModel(m).as(OntSWRL.DArg.class);
        OntSWRL.DArg dArg2 = var1.as(OntSWRL.DArg.class);

        OntSWRL.Atom.WithBuiltin atom1 = m.createBuiltInSWRLAtom(m.createResource(ns + "AtomPredicate1"),
                Arrays.asList(dArg1, dArg2));
        OntSWRL.Atom.WithClass atom2 = m.createClassSWRLAtom(cl2, i2.as(OntSWRL.IArg.class));
        OntSWRL.Atom.WithSameIndividuals atom3 = m.createSameIndividualsSWRLAtom(i1.as(OntSWRL.IArg.class),
                var1.as(OntSWRL.IArg.class));
        OntSWRL.Atom.WithDataProperty atom4 = m.createDataPropertySWRLAtom(d, i2.as(OntSWRL.IArg.class), dArg2);
        OntSWRL.Atom.WithObjectProperty atom5 = m.createObjectPropertySWRLAtom(p, var1, i1.as(OntSWRL.IArg.class));

        OntSWRL.Imp imp = m.createSWRLImp(Collections.singletonList(atom1), Arrays.asList(atom2, atom3, atom4, atom5));
        imp.addAnnotation(m.getRDFSComment(), "This is SWRL Imp").annotate(m.getRDFSLabel(), cl1.createIndividual());


        Assertions.assertEquals(2, atom1.arguments().count());
        Assertions.assertEquals(1, atom2.arguments().count());
        Assertions.assertEquals(2, atom3.arguments().count());
        Assertions.assertEquals(2, atom4.arguments().count());
        Assertions.assertEquals(2, atom5.arguments().count());

        Assertions.assertEquals(18, imp.spec().count());
        Assertions.assertEquals(8, atom1.spec().count());
        Assertions.assertEquals(3, atom2.spec().count());
        Assertions.assertEquals(4, atom3.spec().count());
        Assertions.assertEquals(4, atom4.spec().count());
        Assertions.assertEquals(4, atom5.spec().count());

        // literals(2) and variables(1):
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.DArg.class).count());
        // individuals(2 anonymous, 1 named) and variables(1):
        Assertions.assertEquals(4, m.ontObjects(OntSWRL.IArg.class).count());

        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Builtin.class).count());

        Assertions.assertEquals(5, m.ontObjects(OntSWRL.Atom.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Atom.Unary.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Atom.Binary.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Variable.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Imp.class).count());
        Assertions.assertEquals(8, m.ontObjects(OntSWRL.class).count());

        //noinspection MappingBeforeCount
        Assertions.assertEquals(5, m.statements(null, RDF.type, SWRL.AtomList)
                .map(OntStatement::getSubject)
                .map(s -> s.as(RDFList.class))
                .count());
    }

    @Test
    public void testSWRLObjectsOnLoadOntology() {
        Graph g = RDFIOTestUtils.loadResourceAsModel("/swrl-test.owl", Lang.RDFXML).getGraph();
        OntModel m = OntModelFactory.createModel(g);


        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Imp.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Atom.WithObjectProperty.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Atom.WithDifferentIndividuals.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Atom.WithDataRange.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Atom.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Atom.WithBuiltin.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Atom.Unary.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Atom.Binary.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Variable.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.IArg.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.DArg.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Arg.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Builtin.class).count());
        Assertions.assertEquals(7, m.ontObjects(OntSWRL.class).count());

        OntSWRL.Imp imp = m.ontObjects(OntSWRL.Imp.class).findFirst().orElseThrow(AssertionError::new);
        Assertions.assertEquals(2, imp.getBodyList().members().count());
        Assertions.assertEquals(1, imp.getHeadList().members().count());

        OntSWRL.Variable x = getVariable(m, "x");
        OntSWRL.Variable y = getVariable(m, "y");
        OntSWRL.Variable z = getVariable(m, "z");

        // modify:
        imp.getBodyList().addFirst(m.createDifferentIndividualsSWRLAtom(x, y));
        m.createSWRLImp(Collections.emptyList(),
                Collections.singletonList(m.createDataRangeSWRLAtom(XSD.xdouble.inModel(m).as(OntDataRange.Named.class), z)));


        Assertions.assertEquals(2, m.ontObjects(OntSWRL.Imp.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Atom.WithObjectProperty.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Atom.WithDifferentIndividuals.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Atom.WithDataRange.class).count());
        Assertions.assertEquals(5, m.ontObjects(OntSWRL.Atom.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Atom.WithBuiltin.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Atom.Unary.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntSWRL.Atom.Binary.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Variable.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.IArg.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.DArg.class).count());
        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Arg.class).count());
        Assertions.assertEquals(0, m.ontObjects(OntSWRL.Builtin.class).count());
        Assertions.assertEquals(10, m.ontObjects(OntSWRL.class).count());
    }

    @Test
    public void testCoreSWRLBuiltins() {
        String uri = "http://test.com/swrl-2";
        String ns = uri + "#";

        OntModel m = OntModelFactory.createModel()
                .setID(uri).getModel()
                .setNsPrefix("test", ns)
                .setNsPrefix("swrl", SWRL.NS)
                .setNsPrefix("swrlb", SWRLB.NS)
                .setNsPrefixes(OntModelFactory.STANDARD);

        OntSWRL.Variable var1 = m.createSWRLVariable("v1");
        OntSWRL.Variable var2 = m.createSWRLVariable("v2");
        OntSWRL.Atom<?> a = m.createBuiltInSWRLAtom(SWRLB.equal,
                Arrays.asList(m.createTypedLiteral(1d).as(OntSWRL.DArg.class), var1));
        OntSWRL.Atom<?> b = m.createBuiltInSWRLAtom(SWRLB.add,
                Arrays.asList(var1, m.createTypedLiteral(2d).as(OntSWRL.DArg.class), var2));
        OntSWRL.Atom<?> c = m.createBuiltInSWRLAtom(m.getResource(ns + "del"),
                Arrays.asList(var2, m.createTypedLiteral(2d).as(OntSWRL.DArg.class)));


        Assertions.assertEquals(3, m.ontObjects(OntSWRL.Atom.WithBuiltin.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.Builtin.class).count());

        Assertions.assertEquals(7, a.spec().count());
        Assertions.assertEquals(9, b.spec().count());
        Assertions.assertEquals(8, c.spec().count());

        Assertions.assertEquals(0, a.getPredicate().spec().count());
        Assertions.assertEquals(0, b.getPredicate().spec().count());
        Assertions.assertEquals(1, c.getPredicate().spec().count());
    }

    @Test
    public void testAssembleSWRLAtomsWithAnonymousIndividuals() {
        OntModel m = OntModelFactory.createModel()
                .setNsPrefix("swrl", SWRL.NS)
                .setNsPrefixes(OntModelFactory.STANDARD);

        OntClass.Named c = m.createOntClass("C");
        OntObjectProperty.Named p1 = m.createObjectProperty("P1");
        OntDataProperty p2 = m.createDataProperty("P2");
        Resource i1 = m.createResource();
        Resource i2 = m.createResource();
        Resource i3 = m.createResource();
        Literal v = m.createLiteral("v");

        OntSWRL.Atom.WithClass a1 = m.createResource(SWRL.ClassAtom)
                .addProperty(SWRL.argument1, i1)
                .addProperty(SWRL.classPredicate, c)
                .as(OntSWRL.Atom.WithClass.class);

        Assertions.assertEquals(c, a1.getPredicate());
        Assertions.assertEquals(i1, a1.getArg());

        OntSWRL.Atom.WithSameIndividuals a2 = m.createResource(SWRL.SameIndividualAtom)
                .addProperty(SWRL.argument1, i1)
                .addProperty(SWRL.argument2, i2)
                .as(OntSWRL.Atom.WithSameIndividuals.class);
        Assertions.assertEquals(i1, a2.getFirstArg());
        Assertions.assertEquals(i2, a2.getSecondArg());
        Assertions.assertEquals(OWL2.sameAs, a2.getPredicate());

        OntSWRL.Atom.WithObjectProperty a3 = m.createResource(SWRL.IndividualPropertyAtom)
                .addProperty(SWRL.argument1, i1)
                .addProperty(SWRL.argument2, i3)
                .addProperty(SWRL.propertyPredicate, p1)
                .as(OntSWRL.Atom.WithObjectProperty.class);
        Assertions.assertEquals(i1, a3.getFirstArg());
        Assertions.assertEquals(i3, a3.getSecondArg());
        Assertions.assertEquals(p1, a3.getPredicate());

        OntSWRL.Atom.WithDataProperty a4 = m.createResource(SWRL.DatavaluedPropertyAtom)
                .addProperty(SWRL.argument1, i1)
                .addProperty(SWRL.argument2, v)
                .addProperty(SWRL.propertyPredicate, p2)
                .as(OntSWRL.Atom.WithDataProperty.class);
        Assertions.assertEquals(i1, a4.getFirstArg());
        //noinspection AssertEqualsBetweenInconvertibleTypes
        Assertions.assertEquals(v, a4.getSecondArg());
        Assertions.assertEquals(p2, a4.getPredicate());


        Assertions.assertEquals(3, m.ontObjects(OntSWRL.IArg.class).count());
        Assertions.assertEquals(1, m.ontObjects(OntSWRL.DArg.class).count());
        Assertions.assertEquals(4, m.ontObjects(OntSWRL.Atom.class).count());
    }
}
