/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.testing_framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.apache.jena.util.CollectionFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * Basis for Jena test cases which provides assertFalse and assertDiffer.
 * Often the logic of the names is clearer than using a negation.
 */
public class TestUtils {
	// do not instantiate
	protected TestUtils() {
		throw new UnsupportedOperationException("Do not instantiate TestUtils");
	}

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
	 * Answer a Set formed from the elements of the List <code>L</code>.
	 */
	public static <T> Set<T> listToSet(List<T> L) {
		return CollectionFactory.createHashedSet(L);
	}

	/**
	 * Answer a List of the substrings of <code>s</code> that are separated by
	 * spaces.
	 */
	public static List<String> listOfStrings(String s) {
		List<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens())
			result.add(st.nextToken());
		return result;
	}

	/**
	 * Answer a Set of the substrings of <code>s</code> that are separated by
	 * spaces.
	 */
	public static Set<String> setOfStrings(String s) {
		Set<String> result = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens())
			result.add(st.nextToken());
		return result;
	}

	/**
	 * Answer a list containing the single object <code>x</code>.
	 */
	public static <T> List<T> listOfOne(T x) {
		List<T> result = new ArrayList<T>();
		result.add(x);
		return result;
	}

	/**
	 * Answer a Set containing the single object <code>x</code>.
	 */
	public static <T> Set<T> setOfOne(T x) {
		Set<T> result = new HashSet<T>();
		result.add(x);
		return result;
	}

	/**
	 * Answer a fresh list which is the concatenation of <code>L</code> then
	 * <code>R</code>. Neither <code>L</code> nor <code>R</code> is updated.
	 */
	public static <T> List<T> append(List<? extends T> L, List<? extends T> R) {
		List<T> result = new ArrayList<T>(L);
		result.addAll(R);
		return result;
	}

	/**
	 * Answer an iterator over the space-separated substrings of <code>s</code>.
	 */
	protected static ExtendedIterator<String> iteratorOfStrings(String s) {
		return WrappedIterator.create(listOfStrings(s).iterator());
	}

	/**
	 * Answer the constructor of the class <code>c</code> which takes arguments
	 * of the type(s) in <code>args</code>, or <code>null</code> if there isn't
	 * one.
	 */
	public static Constructor<?> getConstructor(Class<?> c, Class<?>[] args) {
		try {
			return c.getConstructor(args);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Answer true iff <code>subClass</code> is the same class as
	 * <code>superClass</code>, if its superclass <i>is</i>
	 * <code>superClass</code>, or if one of its interfaces hasAsInterface that
	 * class.
	 */
	public static boolean hasAsParent(Class<?> subClass, Class<?> superClass) {
		if (subClass == superClass || subClass.getSuperclass() == superClass)
			return true;
		Class<?>[] is = subClass.getInterfaces();
		for (int i = 0; i < is.length; i += 1)
			if (hasAsParent(is[i], superClass))
				return true;
		return false;
	}

	/**
	 * Fail unless <code>subClass</code> has <code>superClass</code> as a
	 * parent, either a superclass or an implemented (directly or not)
	 * interface.
	 */
	public static void assertHasParent(Class<?> subClass, Class<?> superClass) {
		if (hasAsParent(subClass, superClass) == false)
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

	private static URL getURL(String fn) {
		URL u = TestUtils.class.getClassLoader().getResource(fn);
		if (u == null) {
			throw new RuntimeException(new FileNotFoundException(fn));
		}
		return u;
	}

	public static String getFileName(String fn) {

		try {
			return getURL(fn).toURI().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static InputStream getInputStream(String fn) throws IOException {
		return getURL(fn).openStream();
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

	public static String safeName(String s) {
		// Safe from Eclipse
		s = s.replace('(', '[');
		s = s.replace(')', ']');
		return s;

	}
}
