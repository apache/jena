/*
  (c) Copyright 2009 TopQuadrant, Inc.
  [See end of file]
  $Id: TestRegisterGraphListener.java,v 1.2 2009-12-13 05:13:56 jeremy_carroll Exp $
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
 * @author Jeremy
 *
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

/*
    (c) Copyright 2009 TopQuadrant, Inc.
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
