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

package com.hp.hpl.jena.graph;

import static org.junit.Assert.*;
import org.junit.Test;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.Map1;

public class LiteralsTest {

	public LiteralsTest() {
	}

	static final Map1<Triple, Node> getObject = new Map1<Triple, Node>() {
		@Override
		public Node map1(Triple o) {
			return o.getObject();
		}
	};

	@Test
	public void testFloatVsDouble() {
		Node A = NodeCreateUtils.create("'1'xsd:float");
		Node B = NodeCreateUtils.create("'1'xsd:double");
		assertFalse(A.equals(B));
		assertFalse(A.sameValueAs(B));
		assertFalse(B.sameValueAs(A));
		assertFalse(A.matches(B));
		assertFalse(B.matches(A));
	}
}
