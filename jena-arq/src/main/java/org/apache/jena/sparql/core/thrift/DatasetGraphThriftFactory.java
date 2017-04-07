package org.apache.jena.sparql.core.thrift;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Quad;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.riot.thrift.wire.RDF_Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mosaic.IDFactory;
import org.apache.jena.sparql.util.Symbol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

public interface DatasetGraphThriftFactory {
	
	static final IDFactory ID_FACTORY = IDFactory.valueOf(DatasetGraphThriftFactory.class);
	
	static final Symbol THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME = Symbol.create(ID_FACTORY.suffix("thriftServerInetSocketAddressName"));
	
	static final Symbol THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT = Symbol.create(ID_FACTORY.suffix("thriftServerInetSocketAddressPort"));
	
	static final Symbol THRIFT_SERVER_DATASET_GRAPH = Symbol.create(ID_FACTORY.suffix("thriftServerDatasetGraph"));
	
	static String createUUID() {
		return UUID.randomUUID().toString();
	}

	public static TTransport transport(final InputStream inputStream) {
		return new TIOStreamTransport(inputStream);
	}

	public static TTransport transport(final OutputStream outputStream) {
		return new TIOStreamTransport(outputStream);
	}
	
	public static ByteBuffer toThrift(final Node node) {
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
	        final TTransport transport = transport(byteArrayOutputStream);
	        final TProtocol protocol = TRDF.protocol(transport);
			ThriftConvert.convert(node, false).write(protocol);
			TRDF.flush(protocol);
			return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
	}
	
	public static ByteBuffer toThrift(final Triple triple) {
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
	        final TTransport transport = transport(byteArrayOutputStream);
	        final TProtocol protocol = TRDF.protocol(transport);
        	ThriftConvert.convert(triple, false).write(protocol);
			TRDF.flush(protocol);
			return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
	}
	
	public static ByteBuffer toThrift(final Quad quad) {
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
	        final TTransport transport = transport(byteArrayOutputStream);
	        final TProtocol protocol = TRDF.protocol(transport);
        	ThriftConvert.convert(quad, false).write(protocol);
			TRDF.flush(protocol);
			return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
	}
	
	public static Node thriftToNode(final ByteBuffer byteBuffer) {
		try {
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
			final TTransport tTransport = transport(byteArrayInputStream);
			final TProtocol tProtocol = TRDF.protocol(tTransport);
			final RDF_Term rdf_Term = new RDF_Term();
			rdf_Term.read(tProtocol);
			return ThriftConvert.convert(rdf_Term);
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
	}
	
	public static Triple thriftToTriple(final ByteBuffer byteBuffer) {
		try {
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
			final TTransport tTransport = transport(byteArrayInputStream);
			final TProtocol tProtocol = TRDF.protocol(tTransport);
			final RDF_Triple rdf_Triple = new RDF_Triple();
			rdf_Triple.read(tProtocol);
			return ThriftConvert.convert(rdf_Triple);
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
	}
	
	public static Quad thriftToQuad(final ByteBuffer byteBuffer) {
		try {
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
			final TTransport tTransport = transport(byteArrayInputStream);
			final TProtocol tProtocol = TRDF.protocol(tTransport);
			final RDF_Quad rdf_Quad = new RDF_Quad();
			rdf_Quad.read(tProtocol);
			return ThriftConvert.convert(rdf_Quad);
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
	}


}
