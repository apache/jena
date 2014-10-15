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
package org.apache.jena.security.impl;

import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;

/**
 * A class that holds the original item and the secured version of it.
 * 
 * This class is used by the Invoker to return secured versions of the object
 * during
 * calls that return the called class for cascading.
 * 
 * @param <Base>
 *            The base class that is being secured
 * @param <Secured>
 *            The implementation (proxy) of the secured class.
 */
public class ItemHolder<Base, Secured extends SecuredItem>
{
	/**
	 * The base item that is being secured
	 */
	private final Base baseItem;
	/**
	 * The proxy to the base class that implements the security.
	 */
	private Secured securedItem;

	/**
	 * Constructor.
	 * 
	 * @param baseItem
	 *            The base item.
	 */
	public ItemHolder( final Base baseItem )
	{
		super();
		this.baseItem = baseItem;
	}

	/**
	 * Get the base item.
	 * 
	 * This method is used in the proxy to get call to the underlying instance.
	 * 
	 * @return The instance that is being protected.
	 */
	public Base getBaseItem()
	{
		return baseItem;
	}

	/**
	 * Get the secured item.
	 * 
	 * This method is used in the invocation handler to get the instance of the
	 * proxy that made the
	 * on which a method call was made. Generally used in returing the original
	 * object to support
	 * cascading.
	 * 
	 * @return the proxy.
	 */
	public Secured getSecuredItem()
	{
		return securedItem;
	}

	/**
	 * Creates the proxy, saves it as the securedItem and returns it.
	 * 
	 * @param handler
	 *            The SecuredItemInvoker to create the proxy with.
	 * @return The proxy.
	 */
	@SuppressWarnings( "unchecked" )
	public final Secured setSecuredItem( final SecuredItemInvoker handler )
	{
		final Set<Class<?>> ifac = new LinkedHashSet<Class<?>>();
		if (baseItem.getClass().isInterface())
		{
			ifac.add(baseItem.getClass());
		}
		ifac.addAll(ClassUtils.getAllInterfaces(baseItem.getClass()));
		if (handler.securedItem.getClass().isInterface())
		{
			ifac.add(handler.securedItem.getClass());
		}
		ifac.addAll(ClassUtils.getAllInterfaces(handler.securedItem.getClass()));

		securedItem = (Secured) Proxy.newProxyInstance(
				SecuredItemImpl.class.getClassLoader(),
				ifac.toArray(new Class<?>[ifac.size()]), handler);
		return securedItem;
	}

}
