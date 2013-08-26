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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;


/**
 * A generic InvocationHandler that handles the general invocation of the
 * security methods.
 */
public class SecuredItemInvoker implements InvocationHandler
{
	// the equals() method
	private static Method EQUALS;
	// the toString() method
	private static Method TO_STRING;
	// the hashCode() method.
	private static Method HASH_CODE;
	// the instance of SecuredItem that this proxy is using. Must be
	// package-private for ItemHolder use.
	/* package-private */final SecuredItem securedItem;

	// populate the static fields.
	static
	{
		try
		{
			SecuredItemInvoker.EQUALS = Object.class.getMethod("equals",
					Object.class);
			SecuredItemInvoker.TO_STRING = Object.class.getMethod("toString");
			SecuredItemInvoker.HASH_CODE = Object.class.getMethod("hashCode");
		}
		catch (final SecurityException e)
		{
			throw new RuntimeException(e);
		}
		catch (final NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param securedClass
	 *            The class of the object that is being protected.
	 * @param securedItem
	 *            The implementation of the SecuredItem version of the object.
	 */
	public SecuredItemInvoker( final Class<?> securedClass,
			final SecuredItem securedItem )
	{
		this.securedItem = securedItem;
	}

	@Override
	public Object invoke( final Object proxy, final Method method,
			final Object[] args ) throws Throwable
	{

		// check for the special case methods
		if (SecuredItemInvoker.EQUALS.equals(method))
		{
			if (Proxy.isProxyClass(args[0].getClass()))
			{
				return args[0].equals(securedItem);
			}
			else
			{
				return securedItem.equals(args[0]);
			}
		}

		if (SecuredItemInvoker.HASH_CODE.equals(method))
		{
			return securedItem.hashCode();
		}

		if (SecuredItemInvoker.TO_STRING.equals(method))
		{
			return securedItem.toString();
		}

		try
		{
			final Method m = securedItem.getClass().getMethod(method.getName(),
					method.getParameterTypes());
			if (!Modifier.isAbstract(m.getModifiers()))
			{
				try
				{
					SecuredItemImpl.incrementUse();
					try
					{
						return method.invoke(securedItem, args);
					}
					finally
					{
						SecuredItemImpl.decrementUse();
					}

				}
				catch (final java.lang.reflect.InvocationTargetException e2)
				{
					if (e2.getTargetException() instanceof RuntimeException)
					{
						throw e2.getTargetException();
					}
					throw e2;
				}
			}
		}
		catch (final NoSuchMethodException e2)
		{
			// acceptable
		}

		// if we get here then the method is not being proxied so call the
		// original method on the base item.
		return method.invoke(securedItem.getBaseItem(), args);

	}
}
