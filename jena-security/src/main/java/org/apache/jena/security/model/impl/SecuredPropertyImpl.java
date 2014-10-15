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
package org.apache.jena.security.model.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredProperty;

/**
 * Implementation of SecuredProperty to be used by a SecuredItemInvoker proxy.
 */
public class SecuredPropertyImpl extends SecuredResourceImpl implements
		SecuredProperty
{
	/**
	 * Get an instance of SecuredProperty
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param property
	 *            The property to secure
	 * @return The SecuredProperty
	 */
	public static SecuredProperty getInstance( final SecuredModel securedModel,
			final Property property )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (property == null)
		{
			throw new IllegalArgumentException("Property may not be null");
		}

		// check that property has a securedModel.
		Property goodProp = property;
		if (goodProp.getModel() == null)
		{
			final Node n = property.asNode();
			if (property.isAnon())
			{
				goodProp = securedModel.createProperty(n.getBlankNodeId()
						.getLabelString());
			}
			else
			{
				goodProp = securedModel.createProperty(property.asNode()
						.getURI());
			}
		}

		final ItemHolder<Property, SecuredProperty> holder = new ItemHolder<Property, SecuredProperty>(
				goodProp);
		final SecuredPropertyImpl checker = new SecuredPropertyImpl(
				securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (goodProp instanceof SecuredProperty)
		{
			if (checker.isEquivalent((SecuredProperty) goodProp))
			{
				return (SecuredProperty) goodProp;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(
				property.getClass(), checker));
	}

	// the item holder that contains this SecuredProperty
	private final ItemHolder<? extends Property, ? extends SecuredProperty> holder;

	/**
	 * Constructor
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use.
	 * @param graphIRI
	 *            the graph IRI to validate against.
	 * @param holder
	 *            The item holder that will contain this SecuredProperty.
	 */
	private SecuredPropertyImpl(
			final SecuredModel securedModel,
			final ItemHolder<? extends Property, ? extends SecuredProperty> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public int getOrdinal()
	{
		checkRead();
		return holder.getBaseItem().getOrdinal();
	}

	@Override
	public Property inModel( final Model m )
	{
		return (Property) super.inModel(m);
	}

	@Override
	public boolean isProperty()
	{
		return true;
	}
}
