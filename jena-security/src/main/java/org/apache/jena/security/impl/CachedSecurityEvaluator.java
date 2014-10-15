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
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.jena.security.SecurityEvaluator;

/**
 * A SecurityEvaluator that can be cached for later use.
 */
public class CachedSecurityEvaluator implements InvocationHandler
{
	private final SecurityEvaluator wrapped;
	private final Principal origPrincipal;

	// The getPrincipal() method.
	private static Method GET_PRINCIPAL;

	static
	{
		try
		{
			CachedSecurityEvaluator.GET_PRINCIPAL = SecurityEvaluator.class
					.getMethod("getPrincipal");
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
	 * Create an instance.
	 * @param evaluator The security evaluator we are caching.
	 * @param runAs The principal that we want to use when checking the permissions.
	 * @return The proxied SecurityEvaluator.
	 */
	public static SecurityEvaluator getInstance(
			final SecurityEvaluator evaluator, final Principal runAs )
	{
		final Set<Class<?>> ifac = new LinkedHashSet<Class<?>>();
		if (evaluator.getClass().isInterface())
		{
			ifac.add(evaluator.getClass());
		}
		ifac.addAll(ClassUtils.getAllInterfaces(evaluator.getClass()));

		return (SecurityEvaluator) Proxy.newProxyInstance(
				SecuredItemImpl.class.getClassLoader(),
				ifac.toArray(new Class<?>[ifac.size()]),
				new CachedSecurityEvaluator(evaluator, runAs));
	}

	/**
	 * 
	 * @param wrapped
	 * @param runAs
	 */
	private CachedSecurityEvaluator( final SecurityEvaluator wrapped,
			final Principal runAs )
	{
		origPrincipal = runAs;
		this.wrapped = wrapped;
	}

	@Override
	public Object invoke( final Object proxy, final Method method,
			final Object[] args ) throws Throwable
	{
		// check for the special case methods
		if (CachedSecurityEvaluator.GET_PRINCIPAL.equals(method))
		{
			return origPrincipal;
		}

		// if we get here then the method is not being proxied so call the
		// original method
		// on the base item.
		return method.invoke(wrapped, args);

	}
}
