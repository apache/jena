package org.apache.jena.permissions.graph;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class RecordingGraphListener implements GraphListener {

    private boolean add;
    private boolean delete;
    private boolean event;

    public boolean isAdd() {
        return add;
    }

    public boolean isDelete() {
        return delete;
    }

    public boolean isEvent() {
        return event;
    }

    @Override
    public void notifyAddArray(final Graph g, final Triple[] triples) {
        add = true;
    }

    @Override
    public void notifyAddGraph(final Graph g, final Graph added) {
        add = true;
    }

    @Override
    public void notifyAddIterator(final Graph g, final Iterator<Triple> it) {
        add = true;
    }

    @Override
    public void notifyAddList(final Graph g, final List<Triple> triples) {
        add = true;
    }

    @Override
    public void notifyAddTriple(final Graph g, final Triple t) {
        add = true;
    }

    @Override
    public void notifyDeleteArray(final Graph g, final Triple[] triples) {
        delete = true;
    }

    @Override
    public void notifyDeleteGraph(final Graph g, final Graph removed) {
        delete = true;
    }

    @Override
    public void notifyDeleteIterator(final Graph g, final Iterator<Triple> it) {
        delete = true;
    }

    @Override
    public void notifyDeleteList(final Graph g, final List<Triple> L) {
        delete = true;
    }

    @Override
    public void notifyDeleteTriple(final Graph g, final Triple t) {
        delete = true;
    }

    @Override
    public void notifyEvent(final Graph source, final Object value) {
        event = true;
    }

    public void reset() {
        add = false;
        delete = false;
        event = false;
    }

}