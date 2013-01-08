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

package com.hp.hpl.jena.graph.test;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;

/**
 * These tests are for listeners that add or delete
 * other listeners.
 * It motivates the use of, e.g. CopyOnWriteArrayList
 * for storing listeners.
 */
public class TestRegisterGraphListener extends GraphTestBase {

	private ComeAndGoListener all[];
	private Graph graph;
	
	private static final class SimpleListener extends ComeAndGoListener {
		@Override
		void doSomeDamage() {}
	}

	abstract private static class ComeAndGoListener implements GraphListener {

		// Was I registered when start() was called, and have not been unregistered.
		boolean inPlay = false;
		// currently registered or not.
		boolean registered = false;
		boolean notified = false;
		void registerWith(Graph g) {
			registered= true;
		   g.getEventManager().register(this);	
		}
		void unregisterFrom(Graph g) {
			registered = false;
			inPlay = false;
			g.getEventManager().unregister(this);
		}
		void start() {
			if (registered) inPlay = true;
		}
		void check() {
			if (inPlay && !notified) fail("listener that was in-play was not notified of adding triple.");
		}
		
        @Override
        final public void notifyAddTriple(Graph g, Triple t) {
			notified = true;
			doSomeDamage();
		}
		abstract void doSomeDamage();
		
		@Override
        public void notifyAddArray(Graph g, Triple[] triples) {}
		@Override
        public void notifyAddGraph(Graph g, Graph added) {}
		@Override
        public void notifyAddIterator(Graph g, Iterator<Triple> it) {}
		@Override
        public void notifyAddList(Graph g, List<Triple> triples) {}
		@Override
        public void notifyDeleteArray(Graph g, Triple[] triples) {}
		@Override
        public void notifyDeleteGraph(Graph g, Graph removed) {}
		@Override
        public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {}
		@Override
        public void notifyDeleteList(Graph g, List<Triple> L) {}
		@Override
        public void notifyDeleteTriple(Graph g, Triple t) {}
		@Override
        public void notifyEvent(Graph source, Object value) {}
		
	}
	public TestRegisterGraphListener(String name) {
		super(name);
	}
	
	private void testAddingTriple(int addMe, ComeAndGoListener ...allx) {
		graph = newGraph();
		all = allx;
		for (int i=0;i<addMe;i++) {
			all[i].registerWith(graph); 
		} // 4 is unregistered.
		for (ComeAndGoListener l:all) {
			l.start();
		}
		graph.add(triple("make a change" ));
		for (ComeAndGoListener l:all) {
			l.check();
		}
	}
	
	public void testAddOne() {
		testAddingTriple(2,
				new ComeAndGoListener(){
					@Override
					void doSomeDamage() {
						all[2].registerWith(graph);
					}},
				new SimpleListener(),
				new SimpleListener());
	}
	
	public void testDelete2nd() {
		testAddingTriple(3,
				new ComeAndGoListener(){
					@Override
					void doSomeDamage() {
						all[1].unregisterFrom(graph);
					}},
				new SimpleListener(),
				new SimpleListener());
	}
	public void testDelete1st() {
		testAddingTriple(3,
				new SimpleListener(),
				new ComeAndGoListener(){
					@Override
					void doSomeDamage() {
						all[0].unregisterFrom(graph);
					}},
				new SimpleListener());
	}
	public void testDeleteSelf() {
		testAddingTriple(3,
				new ComeAndGoListener(){
					@Override
					void doSomeDamage() {
						unregisterFrom(graph);
					}},
				new SimpleListener(),
				new SimpleListener());
	}
	public void testDeleteAndAddSelf() {
		testAddingTriple(3,
				new ComeAndGoListener(){
					@Override
					void doSomeDamage() {
						unregisterFrom(graph);
						registerWith(graph);
					}},
				new SimpleListener(),
				new SimpleListener());
	}

}
