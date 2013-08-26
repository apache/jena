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
package org.apache.jena.security.contract.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import junit.framework.Test;


import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;


public class ModelTestSuite extends ParentRunner<Test>
{
	private SecTestPackage pkg;
	
	public ModelTestSuite( Class<?> testClass ) throws Exception
	{
		super( Test.class );
		pkg = new SecTestPackage();
	}

	@Override
	protected List<Test> getChildren()
	{
		List<Test> lst = new ArrayList<Test>();
		Enumeration<Test> enm = pkg.tests();
		while (enm.hasMoreElements())
		{
			lst.add( enm.nextElement() );
		}
		return lst;
	}

	@Override
	protected Description describeChild( Test child )
	{
		return Description.createTestDescription( child.getClass(), child.toString() );
	}

	@Override
	protected void runChild( Test child, RunNotifier notifier )
	{
		Method setUp = null;
		try
		{
			setUp = child.getClass().getMethod("setUp" );
		}
		catch (SecurityException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new RuntimeException( e1 );
		}
		catch (NoSuchMethodException e1)
		{
		}
		Method tearDown = null;
		try
		{
			tearDown = child.getClass().getMethod("tearDown" );
		}
		catch (SecurityException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new RuntimeException( e1 );
		}
		catch (NoSuchMethodException e1)
		{
		}
		for (Method m : child.getClass().getMethods())
		{
			if (m.getName().startsWith( "test" ) && m.getParameterTypes().length == 0)
			{
				Description desc = Description.createTestDescription( child.getClass(), child.toString() );
				notifier.fireTestStarted( desc );
				try
				{
					if (setUp != null)
					{
						setUp.invoke(child);
					}
					m.invoke(child);
					if (tearDown != null)
					{
						tearDown.invoke( child );
					}
					notifier.fireTestFinished( desc );
				}
				catch (IllegalArgumentException e)
				{
					notifier.fireTestFailure( new Failure(desc, e));
				}
				catch (IllegalAccessException e)
				{
					notifier.fireTestFailure( new Failure(desc, e));
				}
				catch (InvocationTargetException e)
				{
					notifier.fireTestFailure( new Failure(desc, e.getTargetException()));
				}
				catch (RuntimeException e) {
					notifier.fireTestFailure( new Failure(desc, e));
					throw e;
				}
			}
		}
	}
}
