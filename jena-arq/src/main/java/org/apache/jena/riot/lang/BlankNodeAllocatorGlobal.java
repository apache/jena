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

package org.apache.jena.riot.lang;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * Allocate blank nodes by creating a randomly generated blank node.
 * This allocator has arbitrary sized internal state needed to record
 * the label to node mapping.
 */

public class BlankNodeAllocatorGlobal implements BlankNodeAllocator
{
    Map<String, Node> map = new HashMap<>();
    
    public BlankNodeAllocatorGlobal()  {}

    @Override
    public void reset()         { map.clear(); }

    @Override
    public Node alloc(String label) {
        return map.computeIfAbsent(label, x->create());
    }

    @Override
    public Node create() {
        return NodeFactory.createBlankNode();
    }
}
