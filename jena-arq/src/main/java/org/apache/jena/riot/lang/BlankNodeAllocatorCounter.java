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

package org.apache.jena.riot.lang;

import static java.lang.String.format;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeToLabel;

/**
 * Allocate blank nodes according a counter.
 * <p>
 * This can be used to track blank nodes by encounter order in a parser.
 * <p>
 * Use {@link NodeToLabel#createBNodeByLabelEncoded()} to recover the increment.
 * <p>
 * Use {@link BlankNodeAllocatorLabelEncoded} and {@link NodeToLabel#createBNodeByLabelEncoded()}
 * for round-trip of labels used in the original data input.
 */
public class BlankNodeAllocatorCounter implements BlankNodeAllocator
{
    private AtomicLong counter = new AtomicLong(0);

    public BlankNodeAllocatorCounter()  {}

    @Override
    public void reset()         { counter = new AtomicLong(0); }

    @Override
    public Node alloc(String label) { return create(label); }

    @Override
    public Node create() {
        long x = counter.getAndIncrement();
        Node n = create(format("%04d", x));
        return n;
    }

    private Node create(String label) {
        return NodeFactory.createBlankNode(label);
    }
}
