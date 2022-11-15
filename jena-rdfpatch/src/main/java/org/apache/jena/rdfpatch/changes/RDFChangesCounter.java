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

public class RDFChangesCounter implements RDFChanges {

    protected PatchSummary summary = new PatchSummary();

    public RDFChangesCounter() {}

    public void reset() {
        summary.reset();
    }

    public PatchSummary summary() {
        return summary.clone();
    }

    @Override
    public void start() {
        summary.countStart++;
    }

    @Override
    public void finish() {
        summary.countFinish++;
    }

    @Override
    public void segment() {
        summary.countSegment++;
    }

    @Override
    public void header(String field, Node value) {
        summary.countHeader++;
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        summary.countAddData++;
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        summary.countDeleteData++;
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        summary.countAddPrefix++;
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        summary.countDeletePrefix++;
    }

    @Override
    public void txnBegin() {
        summary.countTxnBegin++;
    }

    @Override
    public void txnCommit() {
        summary.countTxnCommit++;
    }

    @Override
    public void txnAbort() {
        summary.countTxnAbort++;
    }
}
