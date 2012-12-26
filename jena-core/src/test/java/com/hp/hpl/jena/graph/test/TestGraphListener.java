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


import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler ;
import com.hp.hpl.jena.mem.GraphMem ;

/**
 * Version of graph tests that set up a listener that copies all changes
 * and verifies that after every notification modified graph
 * and original are isomorphic.
 */
public class TestGraphListener extends MetaTestGraph {
	public TestGraphListener(String name) {
		super(name);
	}
    public TestGraphListener( Class<? extends Graph> graphClass, String name) 
    { super( graphClass, name); }
    
    public static TestSuite suite()
    { return MetaTestGraph.suite( TestGraphListener.class, GraphMem.class ); }
	/**
	 * A listener to check that a graph is being tracked
	 * correctly by its events.
	 */
	protected class CheckChanges implements GraphListener {

		protected Graph copy, original;
		final String desc;
		public CheckChanges(String description, Graph g) {
			original = g;
			desc = description;
			copy = TestGraphListener.super.getGraph();
		}


		protected void verify() {
			 assertIsomorphic(desc+" has not been tracked correctly. [delegating,copy-from-listener]",
					original,copy
					);
		}

		@Override
        public void notifyAddIterator(Graph g, Iterator<Triple> it) {
			while (it.hasNext()) copy.add(it.next());
			verify();		
	    }

		@Override
        public void notifyAddTriple(Graph g, Triple t) {
			copy.add(t);
			verify();
		}

		@Override
        public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
			while (it.hasNext()) copy.delete(it.next());
			verify();
		}


		@Override
        public void notifyDeleteTriple(Graph g, Triple t) {
			copy.delete(t);
			verify();
		}

		@Override
        public void notifyEvent(Graph source, Object value) {
			if (value instanceof GraphEvents) {
				if (GraphEvents.removeAll.equals(value)) {
					notifyRemoveAll(source,Triple.ANY);
				} else {
					GraphEvents event = (GraphEvents)value;
					if ("remove".equals(event.getTitle())) {
						notifyRemoveAll(source,(Triple)event.getContent());
					}
				}
			}
			verify();
		}


		public void notifyRemoveAll(Graph source, Triple t) {
			SimpleBulkUpdateHandler.removeAll(copy, t.getSubject(), t.getPredicate(), t.getObject());
			verify();
			
		}


		@Override
        public void notifyAddList(Graph g, List<Triple> triples) {
			notifyAddIterator(g, triples.iterator());
		}


		@Override
        public void notifyDeleteArray(Graph g, Triple[] triples) {
			notifyDeleteIterator(g,Arrays.asList(triples).iterator());
		}

		@Override
        public void notifyAddArray(Graph g, Triple[] triples) {
			notifyAddIterator(g,Arrays.asList(triples).iterator());
		}
		@Override
        public void notifyAddGraph(Graph g, Graph added) {
			notifyAddIterator(g,added.find(Triple.ANY));
		}



		@Override
        public void notifyDeleteGraph(Graph g, Graph removed) {
			notifyDeleteIterator(g,removed.find(Triple.ANY));
		}



		@Override
        public void notifyDeleteList(Graph g, List<Triple> list) {
			notifyDeleteIterator(g, list.iterator());
		}


	}

    @Override
	public Graph getGraph() { 
    	Graph g = Factory.createGraphMem();
    	
    	g.getEventManager().register(new CheckChanges("simple tracking",g));
	    return g; 
	}
}
