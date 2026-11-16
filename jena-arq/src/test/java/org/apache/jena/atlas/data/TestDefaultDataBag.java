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

package org.apache.jena.atlas.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.system.SerializationFactoryFinder;

public class TestDefaultDataBag {
    @Test
    public void iteratorAtThresholdUsesMemory() {
        Binding binding = BindingFactory.binding(Var.alloc("x"), NodeFactory.createLiteralString("value"));
        DefaultDataBag<Binding> bag = new DefaultDataBag<>(new ThresholdPolicyCount<>(1),
                                                          SerializationFactoryFinder.bindingSerializationFactory());
        try {
            bag.add(binding);
            assertEquals(List.of(binding), Iter.toList(bag.iterator()));
        } finally {
            bag.close();
        }
    }
}
