package org.apache.jena.sparql.core.thrift;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class IteratorPaged2RDFNode extends IteratorPaged2RDF<Node> {

	public IteratorPaged2RDFNode(final InputStreamPaged inputStreamPaged) {
		super(inputStreamPaged);
	}

	@Override
	protected void triple(final Triple triple) {
		hasNext = true;
		next = triple.getObject();
	}

	
}
