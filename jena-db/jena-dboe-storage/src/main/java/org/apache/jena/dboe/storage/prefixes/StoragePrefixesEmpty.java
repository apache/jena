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

package org.apache.jena.dboe.storage.prefixes;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixEntry;

public class StoragePrefixesEmpty implements StoragePrefixes {

    @Override
    public String get(Node graphNode, String prefix) {
        return null;
    }

    @Override
    public Iterator<PrefixEntry> get(Node graphNode) {
        return Iter.nullIterator();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return Iter.nullIterator();
    }

    @Override
    public void add(Node graphNode, String prefix, String iriStr) {
        throw new UnsupportedOperationException("StoragePrefixesEmpty.add");
    }

    @Override
    public void delete(Node graphNode, String prefix) {
        throw new UnsupportedOperationException("StoragePrefixesEmpty.delete");
    }

    @Override
    public void deleteAll(Node graphNode) {
        throw new UnsupportedOperationException("StoragePrefixesEmpty.delete");
    }

    @Override
    public Iterator<Pair<Node, PrefixEntry>> listMappings() {
        return Iter.nullIterator();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }
}
