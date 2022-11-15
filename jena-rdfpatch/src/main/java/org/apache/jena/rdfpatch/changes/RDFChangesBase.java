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

package org.apache.jena.rdfpatch.changes;

import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFChanges;

public class RDFChangesBase implements RDFChanges {
    // Logically, this is an abstract class.
    // But to ensure every operation has a mention here, we do not add "abstract".

    @Override
    public void start() {}

    @Override
    public void finish() {}

    @Override
    public void segment() {}

    @Override
    public void header(String field, Node value) {}

    @Override
    public void add(Node g, Node s, Node p, Node o) {}

    @Override
    public void delete(Node g, Node s, Node p, Node o) { }

    @Override
    public void addPrefix(Node graph, String prefix, String uriStr) {}

    @Override
    public void deletePrefix(Node graph, String prefix) {}

    @Override
    public void txnBegin() {}

    @Override
    public void txnCommit() {}

    @Override
    public void txnAbort() {}

}
