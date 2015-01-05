/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Helper for converting between the binary representation of Nodes, Triples and
 * Quads and their Jena API equivalents
 * 
 */
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
