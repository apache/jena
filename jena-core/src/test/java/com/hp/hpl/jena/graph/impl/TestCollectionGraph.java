package com.hp.hpl.jena.graph.impl;

import java.util.HashSet;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.CollectionGraph;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;

public class TestCollectionGraph extends AbstractTestGraph {
	 
	public TestCollectionGraph(String name) {
		super(name);
	}

	@Override
	public Graph getGraph() {
		return new CollectionGraph( new HashSet<Triple>() );
	}

}
