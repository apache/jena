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

package org.apache.jena.query.text;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.QuadAction;

/**
 * Test fixture document producer
 */
public class DummyDocProducer implements TextDocProducer {

    public DummyDocProducer(DatasetGraph dsg, TextIndex textIndex) {}

    int count;

    @Override
    public void start() {
        count = 0;
    }

    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
        count++;
    }

    @Override
    public void finish() {}

    public int getNumQuads() { return count ;}
}