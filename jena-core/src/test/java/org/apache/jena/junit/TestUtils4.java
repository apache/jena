/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.junit;

import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Basis for Jena test cases which provides assertFalse and assertDiffer.
 * Often the logic of the names is clearer than using a negation.
 * JUnit4.
 */
public class TestUtils4 {
	// do not instantiate, do not subclass.
	private TestUtils4() {}

	/**
	 * assert that the two objects must be unequal according to .equals().
	 *
	 * @param title
	 *            a labelling string for the assertion failure text
	 * @param x
	 *            an object to test; the subject of a .equals()
	 * @param y
	 *            the other object; the argument of the .equals()
	 */
	public static void assertDiffer(String title, Object x, Object y) {
		if (x == null ? y == null : x.equals(y))
			fail((title == null ? "objects should be different, but both were: "
					: title)
					+ x);
	}

	/**
	 * assert that the two objects must be unequal according to .equals().
	 *
	 * @param x
	 *            an object to test; the subject of a .equals()
	 * @param y
	 *            the other object; the argument of the .equals()
	 */
	public static void assertDiffer(Object x, Object y) {
		assertDiffer(null, x, y);
	}

	/**
	 * assert that the object <code>x</code> must be of the class
	 * <code>expected</code>.
	 */
	public static void assertInstanceOf(Class<?> expected, Object x) {
		if (x == null)
			fail("expected instance of " + expected + ", but had null");
		if (!expected.isInstance(x))
			fail("expected instance of " + expected + ", but had instance of "
					+ x.getClass());
	}

	/**
	 * Fail unless <code>subClass</code> has <code>superClass</code> as a
	 * parent, either a superclass or an implemented (directly or not)
	 * interface.
	 */
	public static void assertHasParent(Class<?> subClass, Class<?> superClass) {
		if (TestUtils.hasAsParent(subClass, superClass) == false)
			fail("" + subClass + " should have " + superClass + " as a parent");
	}

	/**
	 * Tests o1.equals( o2 ) && o2.equals(o1) && o1.hashCode() == o2.hashCode()
	 *
	 * @param o1
	 * @param o2
	 */
	public static void assertEquivalent(Object o1, Object o2) {
		assertEquals(o1, o2);
		assertEquals(o2, o1);
		assertEquals(o1.hashCode(), o2.hashCode());
	}

	/**
	 * Tests o1.equals( o2 ) && o2.equals(o1) && o1.hashCode() == o2.hashCode()
	 *
	 * @param o1
	 * @param o2
	 */
	public static void assertEquivalent(String msg, Object o1, Object o2) {
		assertEquals(msg, o1, o2);
		assertEquals(msg, o2, o1);
		assertEquals(msg, o1.hashCode(), o2.hashCode());
	}

	/**
	 * Tests o1.equals( o2 ) && o2.equals(o1) && o1.hashCode() == o2.hashCode()
	 *
	 * @param o1
	 * @param o2
	 */
	public static void assertNotEquivalent(String msg, Object o1, Object o2) {
		assertNotEquals(msg, o1, o2);
		assertNotEquals(msg, o2, o1);
	}

	// FIXME this is to be removed when testing is complete
	public static void logAssertEquals(Class<?> clazz, String msg, Object obj1,
			Object obj2) {
		if (obj1 == null && obj2 == null) {
			return;
		}

		if (obj1 != null) {
			if (obj1 == obj2) {
				return;
			}
			if (obj1.equals(obj2)) {
				return;
			}
		}
		LoggerFactory.getLogger(clazz).warn(
				String.format("%s expected: %s got: %s", msg, obj1, obj2));
		System.out.println(String.format("[%sWARNING] %s expected: %s got: %s",
				clazz, msg, obj1, obj2));
	}

	// FIXME this is to be removed when testing is complete
	public static void logAssertTrue(Class<?> clazz, String msg, boolean value) {
		if (value) {
			return;
		}

		LoggerFactory.getLogger(clazz).warn(String.format("%s", msg));
		System.out.println(String.format("[%s WARNING] %s ", clazz, msg));
	}

	// FIXME this is to be removed when testing is complete
	public static void logAssertFalse(Class<?> clazz, String msg, boolean value) {
		if (!value) {
			return;
		}

		LoggerFactory.getLogger(clazz).warn(String.format("%s", msg));
		System.out.println(String.format("[%s WARNING] %s ", clazz, msg));
	}

	// FIXME this is to be removed when testing is complete
	public static void logAssertSame(Class<?> clazz, String msg, Object obj1,
			Object obj2) {
		if (obj1 == null && obj2 == null) {
			return;
		}

		if (obj1 != null) {
			if (obj1 == obj2) {
				return;
			}
		}
		LoggerFactory.getLogger(clazz).warn(
				String.format("%s expected: %s got: %s", msg, obj1, obj2));
		System.out
				.println(String.format("[%s WARNING] %s expected: %s got: %s",
						clazz, msg, obj1, obj2));
	}
}
