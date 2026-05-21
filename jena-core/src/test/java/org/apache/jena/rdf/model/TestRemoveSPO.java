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

package org.apache.jena.rdf.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.rdf.model.helpers.ModelCreator;
import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.test.JenaTestLib;

public class TestRemoveSPO extends AbstractModelTestBase {

    public TestRemoveSPO(ModelCreator modelFactory, final String name) {
        super(modelFactory, name);
    }

    public void testRemoveSPOCallsGraphDeleteTriple() {
        final List<Triple> deleted = new ArrayList<>();
        final Graph base = new WrappedGraph(model.getGraph()) {
            @Override
            public void delete(final Triple t) {
                deleted.add(t);
            }
        };
        model = ModelFactory.createModelForGraph(base);
        model.remove(ModelHelper.resource("R"), ModelHelper.property("P"), ModelHelper.rdfNode(model, "17"));
        Assert.assertEquals(JenaTestLib.listOfOne(NodeCreateUtils.createTriple("R P 17")), deleted);
    }

    public void testRemoveSPOReturnsModel() {
        Assert.assertSame(model, model.remove(ModelHelper.resource("R"), ModelHelper.property("P"), ModelHelper.rdfNode(model, "17")));
    }
}
