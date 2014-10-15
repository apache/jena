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

import com.hp.hpl.jena.rdf.model.Bag;

import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredBag;
import org.apache.jena.security.model.SecuredModel;

/**
 * Implementation of SecuredBag to be used by a SecuredItemInvoker proxy.
 */
public class SecuredBagImpl extends SecuredContainerImpl implements SecuredBag
{
	/**
	 * Get an instance of SecuredBag
	 * 
	 * @param securedModel
	 *            The Secured Model to use.
	 * @param bag
	 *            The bag to secure
	 * @return The SecuredBag
	 */
	public static SecuredBag getInstance( final SecuredModel securedModel,
			final Bag bag )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (bag == null)
		{
			throw new IllegalArgumentException("Bag may not be null");
		}
		final ItemHolder<Bag, SecuredBag> holder = new ItemHolder<Bag, SecuredBag>(
				bag);
		final SecuredBagImpl checker = new SecuredBagImpl(securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (bag instanceof SecuredBag)
		{
			if (checker.isEquivalent((SecuredBag) bag))
			{
				return (SecuredBag) bag;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(bag.getClass(),
				checker));
	}

	/**
	 * Constructor.
	 * 
	 * @param securedModel
	 *            The Secured Model to use.
	 * @param holder
	 *            The holder that will contain this SecuredBag.
	 */
	protected SecuredBagImpl( final SecuredModel securedModel,
			final ItemHolder<? extends Bag, ? extends SecuredBag> holder )
	{
		super(securedModel, holder);
	}
}
