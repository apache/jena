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
 * Creates the graph for testing. Implementations must track the creation of
 * graphs created with newGraph and close them when closeGraphs is called.
 * 
 */
//public interface GraphProducerInterface<T> {
//
//	/**
//	 * Returns a new Graph to take part in the test.
//	 * 
//	 * @return The graph implementation to test.
//	 */
//	public abstract Graph newGraph();
//
//	/**
//	 * provides a hook to close down graphs. When called all graphs created by
//	 * the newGraph() method should be closed. Note that some graphs may have
//	 * been closed during the test, so graphs should be tested for being closed
//	 * prior to closing.
//	 */
//	public abstract void closeGraphs();
// }
