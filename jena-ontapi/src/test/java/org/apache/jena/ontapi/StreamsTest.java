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

import org.apache.jena.ontapi.impl.UnionGraphImpl;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamsTest {

    private static final Map<Integer, String> SPLITERATOR_CONSTANTS = getSpliteratorConstants();

    private static void assertTrueConstant(Stream<?> s, int expected) {
        int actual = getCharacteristics(s);
        Assertions.assertTrue(hasCharacteristics(actual, expected),
                "Expected: " + SPLITERATOR_CONSTANTS.get(expected) + ", but found: " + actual);
    }

    private static void assertFalseConstant(Stream<?> s, int expected) {
        int actual = getCharacteristics(s);
        Assertions.assertFalse(hasCharacteristics(actual, expected),
                "Stream should not have " + SPLITERATOR_CONSTANTS.get(expected));
    }

    private static int getCharacteristics(Stream<?> s) {
        return s.spliterator().characteristics();
    }

    private static boolean hasCharacteristics(int actual, int expected) {
        return (actual & expected) == expected;
    }

    private static Map<Integer, String> getSpliteratorConstants() {
        return directFields(Spliterator.class, int.class)
                .collect(Collectors.toMap(f -> getValue(f, Integer.class), Field::getName));
    }

    @SuppressWarnings("SameParameterValue")
    private static Stream<Field> directFields(Class<?> vocabulary, Class<?> type) {
        return Arrays.stream(vocabulary.getDeclaredFields()).
                filter(field -> Modifier.isPublic(field.getModifiers())).
                filter(field -> Modifier.isStatic(field.getModifiers())).
                filter(field -> type.equals(field.getType()));
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> T getValue(Field field, Class<T> type) {
        try {
            return type.cast(field.get(null));
        } catch (IllegalAccessException e) {
            throw new OntJenaException(e);
        }
    }

    @Test
    public void testSetBasedMethods() {
        OntModel m = OntModelFactory.createModel();
        OntClass.Named a = m.createOntClass("C1");
        OntDataProperty p = m.createDataProperty("D1")
                .addSuperProperty(m.createDataProperty("D2").addSuperProperty(m.getOWLBottomDataProperty()));

        Supplier<Stream<?>> s1 = () -> a.superClasses(false);
        Supplier<Stream<?>> s2 = () -> a.subClasses(false);
        Supplier<Stream<?>> s3 = () -> a.subClasses(true);
        Supplier<Stream<?>> s4 = () -> a.superClasses(true);

        Supplier<Stream<?>> s7 = () -> p.superProperties(false);
        Supplier<Stream<?>> s8 = () -> p.subProperties(false);
        Supplier<Stream<?>> s9 = () -> p.superProperties(true);
        Supplier<Stream<?>> s10 = () -> p.subProperties(true);

        Supplier<Stream<?>> s11 = p::content;

        Stream.of(s1, s2, s3, s4, s7, s8, s9, s10, s11).forEach(s -> {
            assertTrueConstant(s.get(), Spliterator.NONNULL);
            assertTrueConstant(s.get(), Spliterator.DISTINCT);
            assertTrueConstant(s.get(), Spliterator.IMMUTABLE);
        });
    }

    @Test
    public void testObjectsMethods() {
        OntModel m = OntModelFactory.createModel();
        OntObject o = m.getOWLThing();

        Supplier<Stream<?>> s1 = () -> o.objects(RDFS.comment, OntAnnotationProperty.class);
        Supplier<Stream<?>> s2 = () -> o.objects(RDFS.comment);
        Supplier<Stream<?>> s3 = o::spec;
        Supplier<Stream<?>> s4 = o::annotations;
        Supplier<Stream<?>> s5 = o::statements;
        Supplier<Stream<?>> s6 = () -> o.statements(RDFS.seeAlso);

        Stream.of(s1, s2, s3, s4, s5, s6).forEach(s -> {
            assertTrueConstant(s.get(), Spliterator.NONNULL);
            assertTrueConstant(s.get(), Spliterator.DISTINCT);
            assertFalseConstant(s.get(), Spliterator.IMMUTABLE);
        });
    }

    @Test
    public void testSimpleModelStreams() {
        OntModel m = OntModelFactory.createModel();

        assertTrueConstant(m.statements(), Spliterator.NONNULL);
        assertTrueConstant(m.statements(), Spliterator.SIZED);
        assertTrueConstant(m.statements(), Spliterator.DISTINCT);
        assertFalseConstant(m.statements(), Spliterator.IMMUTABLE);
        assertFalseConstant(m.statements(), Spliterator.ORDERED);

        assertTrueConstant(m.localStatements(), Spliterator.NONNULL);
        assertTrueConstant(m.localStatements(), Spliterator.SIZED);
        assertTrueConstant(m.localStatements(), Spliterator.DISTINCT);
        assertFalseConstant(m.localStatements(), Spliterator.IMMUTABLE);
        assertFalseConstant(m.localStatements(), Spliterator.ORDERED);

        Supplier<Stream<?>> s1 = () -> m.statements(null, RDF.type, OWL2.Class);
        Supplier<Stream<?>> s2 = () -> m.localStatements(null, RDF.type, OWL2.Class);
        Stream.of(s1, s2).forEach(s -> {
            assertTrueConstant(s.get(), Spliterator.NONNULL);
            assertFalseConstant(s.get(), Spliterator.SIZED);
            assertTrueConstant(s.get(), Spliterator.DISTINCT);
            assertFalseConstant(s.get(), Spliterator.IMMUTABLE);
            assertFalseConstant(s.get(), Spliterator.ORDERED);
        });
    }

    @Test
    public void testNonSizedModelStreams() {
        OntModel m = OntModelFactory.createModel().addImport(OntModelFactory.createModel().setID("base").getModel());
        assertTrueConstant(m.localStatements(), Spliterator.SIZED);
        assertFalseConstant(m.statements(), Spliterator.SIZED);
        assertFalseConstant(m.statements(null, RDF.type, OWL2.Class), Spliterator.SIZED);
        assertFalseConstant(m.localStatements(null, RDF.type, OWL2.Class), Spliterator.SIZED);
    }

    @Test
    public void testNonDistinctModelStreams() {
        String ns = "http://ex#";
        UnionGraph g = new UnionGraphImpl(GraphFactory.createGraphMem(), false);
        OntModel a = OntModelFactory.createModel(g).setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("x", ns);
        OntModel b = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("x", ns);
        a.setID(ns + "a");
        b.setID(ns + "b");
        a.addImport(b);
        b.createOntClass(a.createOntClass(ns + "C").getURI());

        assertTrueConstant(a.localStatements(), Spliterator.DISTINCT);
        assertFalseConstant(a.statements(), Spliterator.DISTINCT);
        assertFalseConstant(a.statements(null, RDF.type, OWL2.Class), Spliterator.DISTINCT);
        assertTrueConstant(a.localStatements(null, RDF.type, OWL2.Class), Spliterator.DISTINCT);

        assertFalseConstant(a.ontObjects(OntClass.Named.class), Spliterator.DISTINCT);

        Assertions.assertEquals(2, a.classes().count());
        Assertions.assertEquals(2, a.ontObjects(OntClass.Named.class).count());
        Assertions.assertEquals(2, a.ontEntities().count());
        Assertions.assertEquals(2, a.statements(null, RDF.type, OWL2.Class).count());

        Assertions.assertEquals(1, a.classes().distinct().count());
        Assertions.assertEquals(1, a.ontObjects(OntClass.Named.class).distinct().count());
        Assertions.assertEquals(1, a.ontEntities().distinct().count());
        Assertions.assertEquals(1, a.statements(null, RDF.type, OWL2.Class).distinct().count());
    }
}
