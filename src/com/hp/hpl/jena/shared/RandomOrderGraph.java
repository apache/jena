/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RandomOrderGraph.java,v 1.1 2003-12-04 17:26:44 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.shared;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;

/**
 * Wraps a graph and randomizes the order of find results.
 * @author jjc
 *
 * 
 */
public class RandomOrderGraph extends WrappedGraph {
	
	public static Graph createDefaultGraph() {
		return new RandomOrderGraph(Factory.createDefaultGraph());
	}
	public static Model createDefaultModel() {
		return ModelFactory.createModelForGraph(createDefaultGraph());
	}
    final private int bufsz;
	/**
	 * @param base
	 */
	public RandomOrderGraph(int bufsz, Graph base) {
		super(base);
		this.bufsz = bufsz;
	}
	/**
	 * @param base
	 */
	public RandomOrderGraph(Graph base) {
		this(10,base);
	}
	
	public ExtendedIterator find( TripleMatch m )
	{ return new RandomOrderIterator(bufsz,super.find( m )); 
	}

	public ExtendedIterator find( Node s, Node p, Node o )
	{ return new RandomOrderIterator(bufsz,super.find( s, p, o )); 
	}
	

}

/*
 (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
