package org.apache.jena.sparql.core.thrift;

import java.io.IOException;
import java.io.InputStream;

public abstract class InputStreamPaged extends InputStream {

	protected Page page = null;

	protected abstract Page nextPage();
	
	@Override
	public int read() throws IOException {
		if (page == null || !page.getBuffer().hasRemaining()) {
			page = nextPage();
		}
		/*
		 * If the call to nextPage returns either null or a page with no elements (i.e. ByteBuffer.allocate(0)) return -1 (i.e. EOF) otherwise get a byte.
		 */
		return ((page == null || !page.getBuffer().hasRemaining()) ? -1 : (page.getBuffer().get() & 0xff));
	}

}
