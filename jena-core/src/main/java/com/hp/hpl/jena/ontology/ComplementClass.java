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

// Package
///////////////
package com.hp.hpl.jena.ontology;



// Imports
///////////////
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * <p>
 * Class description that is formed from the complement of another class description
 * </p>
 */
public interface ComplementClass 
    extends BooleanClassDescription
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

	/**
	 * <p>Answer the class that the class described by this class description
	 * is a complement of.</p>
	 * @return The class that this class is a complement of.
	 */
	public OntClass getOperand();
	
	
	/**
	 * <p>Set the class that the class represented by this class expression is
	 * a complement of. Any existing value for <code>complementOf</code> will
	 * be replaced.</p>
	 */
	public void setOperand( Resource cls );
}
