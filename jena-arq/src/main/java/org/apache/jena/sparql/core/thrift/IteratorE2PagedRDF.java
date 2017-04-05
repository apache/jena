package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;

import org.apache.jena.riot.thrift.StreamRDF2Thrift;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

public abstract class IteratorE2PagedRDF<E> extends IteratorE2Paged<E> {
	
    protected TTransport transport;

    protected TProtocol protocol;
    
    protected boolean encodeValues = false;
	
	protected StreamRDF2Thrift streamRDF2Thrift;

	public IteratorE2PagedRDF(final Iterator<E> iterator) {
		super(iterator);
		// TODO We don't use the following because it wraps the OutputStream in a BufferedOutputStream
//		TRDF.protocol(outputStreamPaged);
	    transport = new TIOStreamTransport(outputStreamPaged);
	    
	    protocol = TRDF.protocol(transport);
	    streamRDF2Thrift = new StreamRDF2Thrift(protocol, false);
	}

}
