package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class IteratorE2PagedRDFNode extends IteratorE2PagedRDF<Node> {
	
	public IteratorE2PagedRDFNode(final Iterator<Node> iterator) {
		super(iterator);
	}

	@Override
	protected void writeElement(final Node node) {
		streamRDF2Thrift.triple(new Triple(Node.ANY, Node.ANY, node));
	}

}
