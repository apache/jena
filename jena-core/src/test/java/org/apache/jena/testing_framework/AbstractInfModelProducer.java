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

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;

/**
 * An abstract implementation of the ModelProducerInterface.
 * 
 * This class handles tracking of the created models and closing them. It also
 * provides a callback for the implementing class to perform extra cleanup when
 * the model is closed.
 * 
 */
public abstract class AbstractInfModelProducer<T extends InfModel> extends
		AbstractModelProducer<T> {

	
	public boolean canBeEmpty( Model m ) 
	{
		return false;
	}
	/**
	 * @return true if the models returned by this poducer are independent,
	 *         false otherwise.
	 * 
	 */
	abstract public Reasoner getReasoner();

	/**
	 * Returns the model that was used in the reasoner.bind() call.
	 * 
	 * @return Model
	 */
	abstract public Model getBoundModel();

	/**
	 * Populate the model with test data for the derivation test.
	 * 
	 * @param model
	 */
	abstract public void populateModel(InfModel model);

	/**
	 * Using the reasoner and the populated model, this statement should return
	 * a derivation iterator when getDerivation() is called.
	 * 
	 * This is not a complete test but a simple test to show that the system
	 * works in this specific case. A complete set of tests is left for later
	 * development.
	 * 
	 * @return The Statement that should have at least one derivation.
	 */
	abstract public Statement getDerivationStatement();

	/**
	 * Using the reasoner and the populated model, this statement should return
	 * an EMPTY derivation iterator when getDerivation() is called.
	 * 
	 * This is not a complete test but a simple test to show that the system
	 * works in this specific case. A complete set of tests is left for later
	 * development.
	 * 
	 * @return The Statement that should have no derivations.
	 */
	abstract public Statement getNoDerivationStatement();

	/**
	 * @return true if the InfModel supports the getDeductionsModel() call.
	 */
	abstract public boolean supportsDeductionsModel();

}
