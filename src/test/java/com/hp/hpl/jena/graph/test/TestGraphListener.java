/*
  (c) Copyright 2009 TopQuadrant, Inc.
  [See end of file]
  $Id: TestGraphListener.java,v 1.1 2009-12-11 18:00:31 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.graph.test;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.shared.ReificationStyle;

/**
 * Version of graph tests that set up a listener that copies all changes
 * and verifies that after every notification modified graph
 * and original are isomorphic.
 * @author Jeremy Carroll
 *
 */
public class TestGraphListener extends MetaTestGraph {

	

	public TestGraphListener(String name) {
		super(name);
	}
    public TestGraphListener( Class<? extends Graph> graphClass, String name, ReificationStyle style ) 
    { super( graphClass, name, style ); }
    
    public static TestSuite suite()
    { return MetaTestGraph.suite( TestGraphListener.class, GraphMemFaster.class ); }
	/**
	 * A listener to check that a graph is being tracked
	 * correctly by its events.
	 * @author Jeremy
	 *
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
