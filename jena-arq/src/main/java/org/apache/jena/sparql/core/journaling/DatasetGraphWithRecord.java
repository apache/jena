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

package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.ext.com.google.common.collect.Lists.newArrayList;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.graph.GraphFactory.createGraphMem;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWithLock;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadAddition;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadDeletion;

/**
 * A {@link DatasetGraph} implementation with two key affordances. First, this class keeps a record of operations
 * conducted against it. This enables the implementation of {@link #abort()}, by reversing that record and running it
 * backwards. Second, this class has "copy-on-add" semantics for {@link #addGraph(Node, Graph)}. This means that the
 * transactional semantics of a given {@link Graph} are discarded on add and replaced with those of this class, so that
 * transactional semantics are uniform and therefore useful.
 *
 * This class supports MRSW locking. It can only be used with an underlying DatasetGraph implementation that provides
 * MRSW or weaker locking. If the provided locking is weaker, then this class will only be able to support that level of
 * locking. Because there is only one record of operations in use in this class at any time, using this class with an
 * underlying implementation that supports stronger-than-MRSW semantics will have unpredictable results.
 */
public class DatasetGraphWithRecord extends DatasetGraphWithLock {

	/**
	 * A record of operations for use in rewinding transactions.
	 */
	private ReversibleOperationRecord<QuadOperation<?, ?>> record = new ListBackedOperationRecord<>(new ArrayList<>());

	/**
	 * Mutex permitting only one writer at a time to mutate the operation record.
	 */
	private final Lock recordLock = new ReentrantLock(true);

	/**
	 * Indicates whether we should be recording operations. True iff we are in a WRITE-transaction and not aborting.
	 */
	private boolean recording = false;

	private boolean isRecording() {
		return recording;
	}

	private void startRecording() {
		// enforce the single-writer constraint
		recordLock.lock();
		recording = true;
	}

	private void stopRecording() {
		recording = false;
	}

	/**
	 * @param dsg the DatasetGraph that will back this one
	 */
	public DatasetGraphWithRecord(final DatasetGraph dsg) {
		super(dsg);
	}

	/**
	 * @param dsg the DatasetGraph that will back this one
	 * @param record the operation record to use with this DatasetGraph
	 */
	public DatasetGraphWithRecord(final DatasetGraph dsg, final ReversibleOperationRecord<QuadOperation<?, ?>> record) {
		super(dsg);
		this.record = record;
	}

	/**
	 * Guards a mutation to the state of this dataset.
	 *
	 * @param data the data with which to mutate this dataset
	 * @param mutator the kind of change to make
	 */
	private <T> void mutate(final T data, final Consumer<T> mutator) {
		if (allowedToWrite()) mutator.accept(data);
		else throw new JenaTransactionException("Tried to write in a non-WRITE transaction!");
	}

	@Override
	public void add(final Quad quad) {
		mutate(quad, _add);
	}

	@Override
	public void delete(final Quad quad) {
		mutate(quad, _delete);
	}

	@Override
	public void addGraph(final Node graphName, final Graph graph) {
		mutate(graph, _addGraph(graphName));
	}

	@Override
	public void removeGraph(final Node graphName) {
		mutate(graphName, _removeGraph);
	}

	/**
	 * A mutator that adds a graph to this dataset.
	 */
	private Consumer<Graph> _addGraph(final Node name) {
		return g -> {
			super.addGraph(name, createGraphMem());
			g.find(ANY, ANY, ANY).forEachRemaining(t -> add(new Quad(name, t)));
		};
	}

	/**
	 * A mutator that removes a graph from this dataset.
	 */
	private final Consumer<Node> _removeGraph = graphName -> {
		// delete all triples in this graph in the backing store
		deleteAny(graphName, ANY, ANY, ANY);
		// remove the graph itself
		super.removeGraph(graphName);
	};

	/**
	 * A mutator that adds a quad to this dataset.
	 */
	private final Consumer<Quad> _add = quad -> {
		if (!contains(quad)) {
			super.add(quad);
			if (isRecording()) record.accept(new QuadAddition(quad));
		}
	};

	/**
	 * A mutator that deletes a quad from this dataset.
	 */
	private final Consumer<Quad> _delete = quad -> {
		if (contains(quad)) {
			super.delete(quad);
			if (isRecording()) record.accept(new QuadDeletion(quad));
		}
	};

	/**
	 * @return true iff we are outside a transaction or inside a WRITE transaction
	 */
	private boolean allowedToWrite() {
		return !isInTransaction() || isInTransaction() && isTransactionType(WRITE);
	}

	@Override
	public void add(final Node g, final Node s, final Node p, final Node o) {
		add(new Quad(g, s, p, o));
	}

	@Override
	public void delete(final Node g, final Node s, final Node p, final Node o) {
		delete(new Quad(g, s, p, o));
	}

	@Override
	public void deleteAny(final Node g, final Node s, final Node p, final Node o) {
		newArrayList(find(g, s, p, o)).forEach(this::delete);
	}

	@Override
	public void clear() {
		deleteAny(ANY, ANY, ANY, ANY);
		super.clear();
	}

	@Override
	protected boolean abortImplemented() {
		return true;
	}

	@Override
	protected void _begin(final ReadWrite readWrite) {
		super._begin(readWrite);
		if (readWrite.equals(WRITE)) startRecording();
	}

	@Override
	protected void _commit() {
		stopRecording();
		record.clear();
		recordLock.unlock();
		super._commit();
	}

	@Override
	protected void _abort() {
		_end();
	}

	@Override
	protected void _end() {
		if (isRecording()) {
			try {
				// stop recording operations from this thread
				stopRecording();
				// and unwind the record
				record.reverse().consume(op -> op.inverse().actOn(this));
			} finally {
				recordLock.unlock();
			}
		}
		super._end();
	}

	@Override
	public void close() {
		if (isRecording()) {
			stopRecording();
			record.clear();
			recordLock.unlock();
		}
		super.close();
	}
}
