package org.apache.jena.sparql.core.thrift;

import java.nio.ByteBuffer;

public class IteratorPage {

	protected ByteBuffer buffer = null;

	public IteratorPage(final ByteBuffer buffer) {
		super();
		this.buffer = buffer;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public String toString() {
		return this.buffer.toString();
	}
}
