/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test.helpers;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.PrefixMapping ;

/**
 * Interface that defines the Testing Model Factory.
 * 
 * Implementations of this class will produce models that are to be tested by the
 * AbstractTestPackage implementations.
 */
public interface TestingModelFactory
{
	/**
	 * Create the default model for testing
	 * @return the model.
	 */
	abstract public Model createModel();

	/**
	 * get the prefix mapping for the default model.
	 * @return a PrefixMapping
	 */
	abstract public PrefixMapping getPrefixMapping();

	/**
	 * Get a model for the specified graph.
	 * @param base the graph to make the model for.
	 * @return the model
	 */
	abstract public Model createModel( Graph base );

}
