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
package org.apache.jena.permissions.model;

import org.apache.jena.graph.Node ;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.shared.ReadDeniedException;

/**
 * The interface for secured RDFNode instances.
 * 
 * Use one the SecuredRDFNode derived class Factories to create instances
 */
public interface SecuredRDFNode extends RDFNode, SecuredItem
{

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public Node asNode() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public <T extends RDFNode> boolean canAs( final Class<T> view )
			throws ReadDeniedException;

	@Override
	public SecuredModel getModel();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public RDFNode inModel( final Model m ) throws ReadDeniedException;

}
