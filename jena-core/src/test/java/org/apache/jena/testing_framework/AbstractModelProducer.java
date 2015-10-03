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

import java.util.ArrayList;
import java.util.List;

import org.xenei.junit.contract.IProducer;
import org.apache.jena.rdf.model.Model;

/**
 * An abstract implementation of the ModelProducerInterface.
 * 
 * This class handles tracking of the created models and closing them. It also
 * provides a callback for the implementing class to perform extra cleanup when
 * the model is closed.
 * 
 */
public abstract class AbstractModelProducer<T extends Model> implements
		IProducer<T> {

	/**
	 * The list of graphs that have been opened in this test.
	 */
	protected List<Model> modelList = new ArrayList<Model>();

	/**
	 * @return true if the models returned by this poducer are independent,
	 *         false otherwise.
	 * 
	 */
	abstract public boolean areIndependent();

	/**
	 * The method to create a new model.
	 * 
	 * @return a newly constructed model of type under test.
	 */
	abstract protected T createNewModel();

	@Override
	final public T newInstance() {
		T retval = createNewModel();
		modelList.add(retval);
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
	protected void afterClose(T g) {
	}

	@SuppressWarnings("unchecked")
    @Override
	final public void cleanUp() {
		for (Model m : modelList) {
			if (!m.isClosed()) {
				m.close();
			}
			afterClose((T) m);
		}
		modelList.clear();
	}
	
}
