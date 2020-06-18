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

package org.apache.jena.graph;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.testing_framework.AbstractRecordingListener;
import org.apache.jena.testing_framework.GraphHelper;

/**
 * This testing listener records the event names and data, and provides a method
 * for comparing the actual with the expected history.
 */
public class RecordingGraphListener extends AbstractRecordingListener implements
		GraphListener {

	@Override
	public void notifyAddTriple(Graph g, Triple t) {
		record("add", g, t);
	}

	@Override
	public void notifyAddArray(Graph g, Triple[] triples) {
		record("add[]", g, triples);
	}

	@Override
	public void notifyAddList(Graph g, List<Triple> triples) {
		record("addList", g, triples);
	}

	@Override
	public void notifyAddIterator(Graph g, Iterator<Triple> it) {
		record("addIterator", g, GraphHelper.iteratorToList(it));
	}

	@Override
	public void notifyAddGraph(Graph g, Graph added) {
		record("addGraph", g, added);
	}

	@Override
	public void notifyDeleteTriple(Graph g, Triple t) {
		record("delete", g, t);
	}

	@Override
	public void notifyDeleteArray(Graph g, Triple[] triples) {
		record("delete[]", g, triples);
	}

	@Override
	public void notifyDeleteList(Graph g, List<Triple> triples) {
		record("deleteList", g, triples);
	}

	@Override
	public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
		record("deleteIterator", g, GraphHelper.iteratorToList(it));
	}

	@Override
	public void notifyDeleteGraph(Graph g, Graph removed) {
		record("deleteGraph", g, removed);
	}

	@Override
	public void notifyEvent(Graph source, Object event) {
		record("someEvent", source, event);
	}

}
