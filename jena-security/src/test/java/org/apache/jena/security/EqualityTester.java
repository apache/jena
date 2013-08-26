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

import org.junit.Assert;

public class EqualityTester
{

	public static void testEquality( final String label, final Object o1,
			final Object o2 )
	{
		Assert.assertEquals(label, o1, o2);
		Assert.assertEquals(label + " inverse", o2, o1);
		Assert.assertEquals(label + " hashCode", o1.hashCode(), o2.hashCode());
	}

	public static void testInequality( final String label, final Object o1,
			final Object o2 )
	{
		if ((o1 == null) && (o2 == null))
		{
			Assert.fail(label + ": both arguments are null");
		}
		if ((o1 == null) || (o2 == null))
		{
			return;
		}
		Assert.assertFalse(label, o2.equals(o1));
		Assert.assertFalse(label, o1.equals(o2));

	}

}
