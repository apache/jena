package org.apache.jena.sparql.core.thrift;

import org.apache.jena.sparql.core.Quad;

public class IteratorPaged2RDFQuad extends IteratorPaged2RDF<Quad> {

	public IteratorPaged2RDFQuad(final InputStreamPaged inputStreamPaged) {
		super(inputStreamPaged);
	}

	@Override
	protected void quad(final Quad quad) {
		hasNext = true;
		next = quad;
	}
}
