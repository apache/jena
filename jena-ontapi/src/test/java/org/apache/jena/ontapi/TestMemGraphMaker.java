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

package org.apache.jena.ontapi;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DoesNotExistException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TestMemGraphMaker implements GraphMaker {
    private boolean isOpen = true;
    private final Map<String, Graph> storage = new HashMap<>();

    @Override
    public Graph createGraph(String name) throws AlreadyExistsException {
        checkOpen();
        if (storage.containsKey(name)) {
            throw new AlreadyExistsException("Already contains graph " + name);
        }
        var graph = GraphMemFactory.createDefaultGraph();
        storage.put(name, graph);
        return graph;
    }

    @Override
    public Graph openGraph(String name) throws DoesNotExistException {
        var res = storage.get(name);
        if (res == null) {
            throw new DoesNotExistException("Can't find graph " + name);
        }
        return res;
    }

    @Override
    public void removeGraph(String name) throws DoesNotExistException {
        if (storage.remove(name) == null) {
            throw new DoesNotExistException("Can't find graph " + name);
        }
    }

    @Override
    public boolean hasGraph(String name) {
        return storage.containsKey(name);
    }

    @Override
    public Stream<String> names() {
        return storage.keySet().stream();
    }

    @Override
    public void close() {
        isOpen = false;
    }

    private void checkOpen() {
        if (!isOpen) {
            throw new IllegalStateException("GraphMaker is closed");
        }
    }
}
