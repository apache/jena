package org.apache.jena.sparql.core.thrift;

import java.nio.ByteBuffer;

public class Page {

	public static final int PAGE_CAPACITY_DEFAULT = 8192; 
	
	public static Page createDefault() {
		return new Page(ByteBuffer.allocate(PAGE_CAPACITY_DEFAULT));
	}
	
	protected ByteBuffer buffer;

	public Page(final ByteBuffer buffer) {
		super();
		this.buffer = buffer;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public String toString() {
		return getBuffer().toString();
	}
}
