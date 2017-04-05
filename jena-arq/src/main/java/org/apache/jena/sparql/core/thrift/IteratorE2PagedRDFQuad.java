package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;

import org.apache.jena.sparql.core.Quad;

public class IteratorE2PagedRDFQuad extends IteratorE2PagedRDF<Quad> {
	
	public IteratorE2PagedRDFQuad(final Iterator<Quad> iterator) {
		super(iterator);
	}

	@Override
	protected void writeElement(final Quad quad) {
		streamRDF2Thrift.quad(quad);
	}
}
