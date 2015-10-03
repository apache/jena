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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

/**
 * This testing listener records the event names and data, and provides a method
 * for comparing the actual with the expected history.
 */
public class AbstractRecordingListener {

	@SuppressWarnings("unchecked")
	public static boolean checkEquality(final Object o1, final Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			final Object[] o1a = (Object[]) o1;
			final Object[] o2a = (Object[]) o2;

			if (o1a.length == o2a.length) {
				for (int i = 0; i < o1a.length; i++) {
					if (!checkEquality(o1a[i], o2a[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		} else if ((o1 instanceof Collection<?>)
				&& (o2 instanceof Collection<?>)) {
			return checkEquality(((Collection<Object>) o1).toArray(),
					((Collection<Object>) o2).toArray());

		} else if ((o1 instanceof Model) && (o2 instanceof Model)) {
			return checkEquality(((Model) o1).listStatements().toList(),
					((Model) o2).listStatements().toList());

		} else if ((o1 instanceof Statement) && (o2 instanceof Statement)) {
			return checkEquality(((Statement) o1).asTriple(),
					((Statement) o2).asTriple());

		} else {
			return o1.equals(o2);
		}
	}

	private List<Object> history = new ArrayList<Object>();

	protected final void record(String tag, Object x, Object y) {
		history.add(tag);
		history.add(x);
		history.add(y);
	}

	protected final void record(String tag, Object info) {
		history.add(tag);
		history.add(info);
	}

	public final int differ(Object... things) {
		for (int i = 0; i < history.size(); i++) {
			if (!things[i].equals(history.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public final boolean has(Object... things) {
		return history.equals(Arrays.asList(things));
	}

	public final void assertHas(Object... things) {
		if (has(things) == false) {
			int idx = differ(things);
			Assert.fail("expected " + Arrays.asList(things) + " but got "
					+ history + " differ at position " + idx);
		}
	}

	public final void assertEmpty() {
		if (history.size() > 0) {
			Assert.fail("Should be no history but got " + history);
		}
	}

	public final int size() {
		return history.size();
	}

	public final boolean has(List<?> things) {
		return history.equals(things);
	}

	public final boolean hasStart(List<Object> L) {
		return L.size() <= history.size()
				&& L.equals(history.subList(0, L.size()));
	}

	public final boolean hasEnd(List<Object> L) {
		return L.size() <= history.size()
				&& L.equals(history.subList(history.size() - L.size(),
						history.size()));
	}

	public final void assertHas(List<?> things) {
		if (has(things) == false)
			Assert.fail("expected " + things + " but got " + history);
	}

	public final void assertContains(Object... things) {
		if (contains(things) == false)
			Assert.fail(String.format("expected %s but got %s",
					Arrays.asList(things), history));
	}

	public final void assertHasStart(Object... start) {
		List<Object> L = Arrays.asList(start);
		if (hasStart(L) == false)
			Assert.fail("expected " + L + " at the beginning of " + history);
	}

	public final void assertHasEnd(Object... end) {
		List<Object> L = Arrays.asList(end);
		if (hasEnd(L) == false)
			Assert.fail("expected " + L + " at the end of " + history);
	}

	public final void clear() {
		history.clear();
	}

	public final boolean contains(Object... objects) {
		for (int i = 0; i < history.size(); i++) {
			if (history.get(i).equals(objects[0])) {
				boolean found = true;
				for (int j = 1; j < objects.length; j++) {
					if (i + j >= history.size()) {
						found = false;
						break;
					}
					if (!history.get(i + j).equals(objects[j])) {
						found = false;
						break;
					}
				}
				if (found) {
					return true;
				}
			}

		}
		return false;

	}

	public final Iterator<Object> from(Object start) {
		Iterator<Object> iter = history.iterator();
		while (iter.hasNext() && !iter.next().equals(start))
		{}
		return iter;
	}
}
