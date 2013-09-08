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
package com.hp.hpl.jena.testing_framework;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;

/**
 * An abstract implementation of the GraphProducerInterface.
 * 
 * This class handles tracking of the created graphs and closing them. It also
 * provides a callback for the implementing class to perform extra cleanup when
 * the graph is closed.
 * 
 */
public abstract class AbstractGraphProducer implements GraphProducerInterface {

	/**
	 * The list of graphs that have been opened in this test.
	 */
	protected List<Graph> graphList = new ArrayList<Graph>();

	/**
	 * The method to create a new graph.
	 * 
	 * @return a newly constructed graph of type under test.
	 */
	abstract protected Graph createNewGraph();

	@Override
	final public Graph newGraph() {
		Graph retval = createNewGraph();
		graphList.add(retval);
		return retval;
	}

	/**
	 * Method called after the graph is closed. This allows the implementer to
	 * perform extra cleanup activities, like deleting the file associated with
	 * a file based graph.
	 * <p>
	 * By default this does nothing.
	 * </p>
	 * 
	 * @param g
	 *            The graph that is closed
	 */
	protected void afterClose(Graph g) {
	};

	@Override
	final public void closeGraphs() {
		for (Graph g : graphList) {
			if (!g.isClosed()) {
				g.close();
			}
			afterClose(g);
		}
		graphList.clear();
	}

}
