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
package org.apache.jena.security;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class SecurityEvaluatorParameters extends Suite
{

	private class TestClassRunnerForParameters extends BlockJUnit4ClassRunner
	{
		private final int fParameterSetNumber;

		private final List<Object[]> fParameterList;

		TestClassRunnerForParameters( final Class<?> type,
				final List<Object[]> parameterList, final int i )
				throws InitializationError
		{
			super(type);
			fParameterList = parameterList;
			fParameterSetNumber = i;
		}

		@Override
		protected Statement classBlock( final RunNotifier notifier )
		{
			return childrenInvoker(notifier);
		}

		@Override
		public Object createTest() throws Exception
		{
			return getTestClass().getOnlyConstructor().newInstance(
					fParameterList.get(fParameterSetNumber));
		}

		@Override
		protected String getName()
		{
			return String.format("[%s]", fParameterSetNumber);
		}

		@Override
		protected Annotation[] getRunnerAnnotations()
		{
			return new Annotation[0];
		}

		@Override
		protected String testName( final FrameworkMethod method )
		{
			return String.format("%s[%s]", method.getName(),
					fParameterList.get(fParameterSetNumber)[0]);
		}

		@Override
		protected void validateConstructor( final List<Throwable> errors )
		{
			validateOnlyOneConstructor(errors);
		}
	}

	private final ArrayList<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public SecurityEvaluatorParameters( final Class<?> klass ) throws Throwable
	{
		super(klass, Collections.<Runner> emptyList());
		final List<Object[]> parametersList = new ArrayList<Object[]>();

		final boolean[] bSet = { true, false };

		for (final boolean create : bSet)
		{
			for (final boolean read : bSet)
			{
				for (final boolean update : bSet)
				{
					for (final boolean delete : bSet)
					{
						for (final boolean forceTripleCheck : bSet)
						{
							parametersList
									.add(new Object[] { new MockSecurityEvaluator(
											true, create, read, update, delete,
											forceTripleCheck) });
						}
					}
				}
			}
		}

		for (int i = 0; i < parametersList.size(); i++)
		{
			runners.add(new TestClassRunnerForParameters(getTestClass()
					.getJavaClass(), parametersList, i));
		}
	}

	@Override
	protected List<Runner> getChildren()
	{
		return runners;
	}

}