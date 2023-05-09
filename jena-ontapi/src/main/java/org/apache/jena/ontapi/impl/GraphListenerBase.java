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

package org.apache.jena.ontapi.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.SimpleEventManager;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Base implementation of {@link GraphListener}.
 */
public abstract class GraphListenerBase extends SimpleEventManager {

    protected abstract void addTripleEvent(Graph g, Triple t);

    protected abstract void deleteTripleEvent(Graph g, Triple t);

    public Stream<GraphListener> listeners() {
        return listeners.stream();
    }

    @Override
    public void notifyAddTriple(Graph g, Triple ts) {
        addTripleEvent(g, ts);
        super.notifyAddTriple(g, ts);
    }

    @Override
    public void notifyAddArray(Graph g, Triple[] ts) {
        for (Triple t : ts) {
            addTripleEvent(g, t);
        }
        super.notifyAddArray(g, ts);
    }

    @Override
    public void notifyAddList(Graph g, List<Triple> ts) {
        ts.forEach(t -> addTripleEvent(g, t));
        super.notifyAddList(g, ts);
    }

    @Override
    public void notifyAddIterator(Graph g, List<Triple> ts) {
        ts.forEach(t -> addTripleEvent(g, t));
        super.notifyAddIterator(g, ts);
    }

    @Override
    public void notifyAddIterator(Graph g, Iterator<Triple> ts) {
        ts.forEachRemaining(t -> addTripleEvent(g, t));
        super.notifyAddIterator(g, ts);
    }

    @Override
    public void notifyDeleteTriple(Graph g, Triple t) {
        deleteTripleEvent(g, t);
        super.notifyDeleteTriple(g, t);
    }

    @Override
    public void notifyDeleteArray(Graph g, Triple[] ts) {
        for (Triple t : ts) {
            deleteTripleEvent(g, t);
        }
        super.notifyDeleteArray(g, ts);
    }

    @Override
    public void notifyDeleteList(Graph g, List<Triple> ts) {
        ts.forEach(t -> deleteTripleEvent(g, t));
        super.notifyDeleteList(g, ts);
    }

    @Override
    public void notifyDeleteIterator(Graph g, List<Triple> ts) {
        ts.forEach(t -> deleteTripleEvent(g, t));
        super.notifyDeleteIterator(g, ts);
    }

    @Override
    public void notifyDeleteIterator(Graph g, Iterator<Triple> ts) {
        ts.forEachRemaining(t -> deleteTripleEvent(g, t));
        super.notifyDeleteIterator(g, ts);
    }

}
