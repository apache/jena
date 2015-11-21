/**
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

package org.apache.jena.sparql.core.journaling;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestListBackedOperationRecord extends Assert {

	private static interface MockOp extends InvertibleOperation<Object, Object, MockOp, MockOp> {}

	private final MockOp mockOp1 = new MockOp() {

		@Override
		public MockOp inverse() {
			return this;
		}

		@Override
		public Object data() {
			return null;
		}

		@Override
		public void actOn(final Object service) {}
	};
	private final MockOp mockOp2 = mockOp1;
	private MockOp mockOp3 = mockOp2;
	private List<MockOp> ops;

	@Before
	public void setup() {
		ops = new ArrayList<>(asList(mockOp1, mockOp2, mockOp3));
	}

	@Test
	public void testAdd() {
		final List<MockOp> results = new ArrayList<>();
		final ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(results);
		testRecord.accept(mockOp1);
		testRecord.accept(mockOp2);
		testRecord.accept(mockOp3);
		assertEquals(3, results.size());
		assertEquals(mockOp1, results.get(0));
		assertEquals(mockOp2, results.get(1));
		assertEquals(mockOp3, results.get(2));
	}

	@Test
	public void testConsume() {
		class MarkerException extends RuntimeException {}
		ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(ops);
		testRecord.consume(op -> {}); // /dev/null
		assertTrue(ops.isEmpty());
		// now let's try with a problematic consumer
		mockOp3 = new MockOp() {

			@Override
			public MockOp inverse() {
				throw new MarkerException();
			}

			@Override
			public Object data() {
				return null;
			}

			@Override
			public void actOn(final Object service) {}

		};
		ops = new ArrayList<>(asList(mockOp1, mockOp2, mockOp3));
		testRecord = new ListBackedOperationRecord<>(ops);
		try {
			testRecord.consume(MockOp::inverse);
			fail("Should not have been able to consume the last op!");
		} catch (MarkerException e) {
			// should be one op left
			assertEquals(1, ops.size());
			// and it should be mockOp3
			assertEquals(mockOp3, ops.get(0));
		}
	}

	@Test
	public void testClear() {
		final ListBackedOperationRecord<MockOp> testRecord = new ListBackedOperationRecord<>(ops);
		testRecord.clear();
		assertTrue(ops.isEmpty());
	}

	@Test
	public void testReverse() {
		final ListBackedOperationRecord<MockOp> testRecordReversed = new ListBackedOperationRecord<>(ops).reverse();
		final List<MockOp> results = new ArrayList<>();
		testRecordReversed.consume(results::add);
		assertEquals(mockOp3, results.get(0));
		assertEquals(mockOp2, results.get(1));
		assertEquals(mockOp1, results.get(2));
	}
}
