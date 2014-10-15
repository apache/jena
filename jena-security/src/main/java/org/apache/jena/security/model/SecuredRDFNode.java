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
package org.apache.jena.security.model;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.impl.SecuredItem;

/**
 * The interface for secured RDFNode instances.
 * 
 * Use one the SecuredRDFNode derived class Factories to create instances
 */
public interface SecuredRDFNode extends RDFNode, SecuredItem
{

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public Node asNode() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public <T extends RDFNode> boolean canAs( final Class<T> view )
			throws AccessDeniedException;

	@Override
	public SecuredModel getModel();

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public RDFNode inModel( final Model m ) throws AccessDeniedException;

}
