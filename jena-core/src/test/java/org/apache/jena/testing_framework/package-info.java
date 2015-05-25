/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.apache.jena.testing_framework;

/**
 * Foo set of classes providing support for testing.
 * <p>
 * Testing guidelines/suggestions.
 * </p><p>
 * Interface tests are built so that developers may test that implementations meet the contract
 * set out in the interface and accompanying documentation.
 * </p>
 * <h4>Producers</h4>
 * <p>
 * The test and suites use an instance of the [INTERFACE]ProducerInterface to create an instance
 * of the the Object being tested.   
 * </p>
 * <h4>Tests</h4>
 * <p>
 * Interface tests are noted as Abstract[INTERFACE]Test.  Implementations of [INTERFACE] should
 * create a concrete implementation of Abstract[INTERFACE]Test with an [INTERFACE]Producer to create
 * instances of the Object.  Passing the test indicates a compliance with the base interface 
 * definition.
 * </p><p>
 * In general to implement a test requires a few lines of code as is noted in the example below
 * where the new Foo graph implementation is being tested.</p>
 * <pre><code>
 * public class FooGraphTest extends AbstractGraphTest {
 * 
 *   // the graph producer to use while running
 *   GraphProducerInterface graphProducer = new FooGraphTest.GraphProducer();
 * 
 *   @Override
 *   protected GraphProducerInterface getGraphProducer() {
 *     return graphProducer;
 *   }
 * 
 *   // the implementation of the graph producer.
 *   public static class GraphProducer extends AbstractGraphProducer {
 * 
 *     @Override
 *     protected Graph createNewGraph() {
 *       return new FooGraph();
 *     }
 *   }
 * }
 * </code></pre>
 * <h4>Suites</h4>
 * <p>
 * Test suites are named as Abstract[INTERFACE]Suite.  Suites contain several tests (see above)
 * that exercise all of the tests for the components of the object under test.  For example the 
 * graph suite includes tests for the graph itself, the reifier, finding literals, recursive 
 * subgraph extraction, event manager, and transactions.  Running the suites is a bit more 
 * complicated then running the tests.
 * </p>
 * Suites are created using the JUnit 4 <code>@RunWith(Suite.class)</code and 
 * <code>@Suite.SuiteClasses({ })</code> annotations.  This has several effects that the developer 
 * should know about:</p>
 * <ul>
 * <li>The suite class does not get instantiated during the run.</li>
 * <li>The test class names must be known at coding time (not run time) as they are listed in the
 * annotation.</li>
 * <li>Configuration of the tests has to occur during the static initialization phase of class 
 * loading.</li>
 * </ul>
 * <p>
 * To meet these requirements the AbstractGraphSuite has a static variable that holds the instance
 * of the GraphProducerInterface and a number of local static implementations of the Abstract tests
 * that implement the "getGraphProducer()" method by returning the static instance.  The names of 
 * the local graphs are then used in the @Suite.SuiteClasses annotation.  This makes creating an
 * instance of the AbstractGraphSuite for a graph implementation fairly simple as is noted below.
 * </p>
 * <pre><code>
 * public class FooGraphSuite extends AbstractGraphSuite {
 *   @BeforeClass
 *   public static void beforeClass() {
 *     setGraphProducer(new GraphProducer());
 *   }
 *   
 *   public static class GraphProducer extends AbstractGraphProducer {
 *     @Override
 *     protected Graph createNewGraph() {
 *       return new FooGraph();
 *     }
 *   }
 * }
 * </code></pre>
 * <p>
 * <b>Note:</b> that the beforeClass() method is annotated with @BeforeClass.  the @BeforeClass 
 * causes it to be run once before any of the test methods in the class. This will set the static
 * instance of the graph producer before the suite is run so that it is provided to the enclosed
 * tests.
 * </p> 
 */
