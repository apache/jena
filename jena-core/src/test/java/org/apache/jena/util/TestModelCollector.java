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

package org.apache.jena.util;

import static java.util.Collections.singleton;
import static java.util.stream.Collector.Characteristics.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.generate;
import static java.util.stream.Stream.iterate;
import static org.apache.jena.ext.com.google.common.collect.Lists.newArrayList;
import static org.apache.jena.graph.NodeFactory.createLiteralByValue;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.ModelCollector.*;
import org.junit.Test;

public class TestModelCollector extends BaseTest {

    private static final Model EMPTY_MODEL = ModelFactory.createDefaultModel();
    private static final Node PREDICATE = NodeFactory.createURI("p");
    private static final Node SUBJECT = NodeFactory.createURI("s");
    private static final RDFDatatype INTEGER_TYPE = TypeMapper.getInstance().getTypeByValue(555);

    @Test
    public void testCharacteristics() {
        Set<Characteristics> characteristics = ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
        assertEquals(characteristics, new UnionModelCollector().characteristics());
        assertEquals(characteristics, new IntersectionModelCollector().characteristics());
        characteristics = ImmutableSet.of(CONCURRENT, UNORDERED, IDENTITY_FINISH);
        assertEquals(characteristics, new ConcurrentModelCollector(null).characteristics());
    }

    Stream<Model> fromTriples(Supplier<Triple> kernel, byte size) {
        AtomicInteger count = new AtomicInteger();
        return generate(kernel).collect(groupingBy(x -> count.incrementAndGet() / size)).values().stream()
                .map(CollectionGraph::new).map(ModelFactory::createModelForGraph);
    }

    private static void test(Stream<Model> data, Model expectedResults, ModelCollector testCollector) {
        assertTrue(data.collect(testCollector).isIsomorphicWith(expectedResults));
    }

    @Test
    public void unionOfEmptyStreamOfModelsIsEmpty() {
        collectors().forEach(this::unionOfEmptyStreamOfModelsIsEmpty);
    }

    private void unionOfEmptyStreamOfModelsIsEmpty(ModelCollector testCollector) {
        assertTrue(Stream.<Model>empty().collect(testCollector).isEmpty());
    }

    private static ArrayList<ModelCollector> collectors() {
        return newArrayList(new UnionModelCollector(), new IntersectionModelCollector());
    }

    @Test
    public void unionOfStreamOfEmptyModelsIsEmpty() {
        collectors().forEach(this::unionOfStreamOfEmptyModelsIsEmpty);

    }

    private void unionOfStreamOfEmptyModelsIsEmpty(ModelCollector testCollector) {
        Stream<Model> models = numbers().limit(10).map(x -> createDefaultModel());
        test(models, EMPTY_MODEL, testCollector);
    }

    private Triple sampleFromNum(int i) {
        return Triple.create(SUBJECT, PREDICATE, createLiteralByValue(i, INTEGER_TYPE));
    }

    @Test
    public void allStatementsPresentInUnionOfDisjointModels() {
        ModelCollector testCollector = new UnionModelCollector();
        Model expectedResults = createDefaultModel();
        Stream<Triple> addTestStatementsToRubric = numbers().limit(10).map(this::sampleFromNum)
                .peek(t -> expectedResults.add(expectedResults.asStatement(t)));
        Stream<Model> models = addTestStatementsToRubric.map(this::intoModel);
        test(models, expectedResults, testCollector);
    }

    private static Stream<Integer> numbers() {
        return generate(new AtomicInteger()::getAndIncrement);
    }

    @Test
    public void noStatementsPresentInIntersectionOfDisjointModels() {
        ModelCollector testCollector = new IntersectionModelCollector();
        Stream<Model> models = iterate(0, i -> i + 1).limit(10).map(this::sampleFromNum).map(this::intoModel);
        test(models, EMPTY_MODEL, testCollector);
    }
    
    private Model intoModel(Triple t) {
        return createModelForGraph(new CollectionGraph(singleton(t)));
    }

}
