package org.apache.jena.hadoop.rdf.types.converters;

import java.io.ByteArrayOutputStream;

import org.apache.jena.riot.thrift.wire.RDF_Quad;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.riot.thrift.wire.RDF_Triple;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;

public class ThriftConverter {

    private static ThreadLocal<TMemoryInputTransport> inputTransports = new ThreadLocal<>();
    private static ThreadLocal<TProtocol> inputProtocols = new ThreadLocal<>();

    private static ThreadLocal<ByteArrayOutputStream> outputStreams = new ThreadLocal<>();
    private static ThreadLocal<TTransport> outputTransports = new ThreadLocal<>();
    private static ThreadLocal<TProtocol> outputProtocols = new ThreadLocal<>();

    private static TMemoryInputTransport getInputTransport() {
        TMemoryInputTransport transport = inputTransports.get();
        if (transport != null)
            return transport;

        transport = new TMemoryInputTransport();
        inputTransports.set(transport);
        return transport;
    }

    private static TProtocol getInputProtocol() {
        TProtocol protocol = inputProtocols.get();
        if (protocol != null)
            return protocol;

        protocol = new TCompactProtocol(getInputTransport());
        inputProtocols.set(protocol);
        return protocol;
    }

    private static ByteArrayOutputStream getOutputStream() {
        ByteArrayOutputStream output = outputStreams.get();
        if (output != null)
            return output;

        output = new ByteArrayOutputStream();
        outputStreams.set(output);
        return output;
    }

    private static TTransport getOutputTransport() {
        TTransport transport = outputTransports.get();
        if (transport != null)
            return transport;

        transport = new TIOStreamTransport(getOutputStream());
        outputTransports.set(transport);
        return transport;
    }

    private static TProtocol getOutputProtocol() {
        TProtocol protocol = outputProtocols.get();
        if (protocol != null)
            return protocol;

        protocol = new TCompactProtocol(getOutputTransport());
        outputProtocols.set(protocol);
        return protocol;
    }

    public static byte[] toBytes(RDF_Term term) throws TException {
        ByteArrayOutputStream output = getOutputStream();
        output.reset();

        TProtocol protocol = getOutputProtocol();
        term.write(protocol);

        return output.toByteArray();
    }

    public static void fromBytes(byte[] bs, RDF_Term term) throws TException {
        TMemoryInputTransport transport = getInputTransport();
        transport.reset(bs);
        TProtocol protocol = getInputProtocol();
        term.read(protocol);
    }

    public static void fromBytes(byte[] buffer, RDF_Triple triple) throws TException {
        TMemoryInputTransport transport = getInputTransport();
        transport.reset(buffer);
        TProtocol protocol = getInputProtocol();
        triple.read(protocol);
    }

    public static byte[] toBytes(RDF_Triple triple) throws TException {
        ByteArrayOutputStream output = getOutputStream();
        output.reset();

        TProtocol protocol = getOutputProtocol();
        triple.write(protocol);

        return output.toByteArray();
    }

    public static void fromBytes(byte[] buffer, RDF_Quad quad) throws TException {
        TMemoryInputTransport transport = getInputTransport();
        transport.reset(buffer);
        TProtocol protocol = getInputProtocol();
        quad.read(protocol);
    }

    public static byte[] toBytes(RDF_Quad quad) throws TException {
        ByteArrayOutputStream output = getOutputStream();
        output.reset();

        TProtocol protocol = getOutputProtocol();
        quad.write(protocol);

        return output.toByteArray();
    }
}
