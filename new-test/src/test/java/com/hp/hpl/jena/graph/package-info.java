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
package com.hp.hpl.jena.graph;

/**
 * General tests for Graph implementations
 * 
 Testing guidelines/suggestions

 Interface tests are built so that developers may test that implementations meet the contract
 set out in the interface and accompanying documentation.

 The major items under test use an instance of the GraphProducerInterface to create the graph
 being tested.  An implementation of  GraphProducerInterface must track all the tests created
 during the test and close all of them when requested.  There is an AbstractGrapProducer that 
 handles most of that functionality but requires a createNewGraph() implementation.

 TESTS
 =====

 Interface tests are noted as Abstract(INTERFACENAME)Test.  Tests generally extend the 
 AbstractGraphProducerUser class.  This class handles cleaning up all the graphs at the end of 
 the tests and provides a hook for implementers to plug in their GraphProducerInterface.

 In general to implement a test requires a few lines of code as is noted in the example below
 where the new Foo graph implementation is being tested.
 <pre>
 <code>
 public class FooGraphTest extends AbstractGraphTest {

 // the graph producer to use while running
 GraphProducerInterface graphProducer = new FooGraphTest.GraphProducer();

 @Override
 protected GraphProducerInterface getGraphProducer() {
 return graphProducer;
 }

 // the implementation of the graph producer.
 public static class GraphProducer extends AbstractGraphProducer {
 @Override
 protected Graph createNewGraph() {
 return new FooGraph();
 }
 }

 }
 </code>
 </pre>
 SUITES
 ======

 Test suites are named as Abstract(INTERFACENAME)Suite.  Suites contain several tests that 
 excersize all of the tests for the components of the object under test.  For example the graph
 suite includes tests for the graph iteself, the reifier, finding literals, recursive subgraph
 extraction, event manager, and transactions.  Running the suites is a bit more complicated then
 running the tests.

 Suites are created using the JUnit 4 @RunWith(Suite.class) and  @Suite.SuiteClasses({ })
 annotations.  This has several effects that the developer should know about:
 1. The suite class does not get instantiated during the run.
 2. The test class names must be known at coding time (not run time) as they are listed in the
 annotation.
 3. Configuration of the tests has to occur during the static initialization phase of class 
 loading.

 To meet these requirements the AbstractGraphSuite has a static variable that holds the instance
 of the GraphProducerInterface and a number of local static implementations of the Abstract tests that 
 implement the "getGraphProducer()" method by returning the static instance.  The names of the 
 local graphs are then used in the @Suite.SuiteClasses annotation.  This makes creating an 
 instance of the AbstractGraphSuite for a graph implementation fairly simple as is noted below.

 <code>
 public class FooGraphSuite extends AbstractGraphSuite {

 @BeforeClass
 public static void beforeClass() {
 setGraphProducer(new GraphProducer());
 }

 public static class GraphProducer extends AbstractGraphProducer {
 @Override
 protected Graph createNewGraph() {
 return new FooGraph();
 }
 }

 }
 </code>

 Note that the beforeClass() method is annotated with @BeforeClass.  the @BeforeClass causes it 
 to be run once before any of the test methods in the class. This will set the static
 instance of the graph producer before the suite is run so that it is provided to the enclosed
 tests. 

 */

