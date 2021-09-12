/**
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

package org.apache.jena.riot.thrift;

import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.function.Consumer;

import org.apache.jena.query.ResultSet ;
import org.apache.jena.riot.protobuf.ProtobufRDF;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.thrift.wire.RDF_StreamRow ;
import org.apache.thrift.protocol.TProtocol ;

/** Operations on binary RDF encoding with <a href="http://thrift.apache.org/">Apache Thrift</a>.
 * See also {@link ThriftConvert}, for specific functions on binary RDF.
 * <p>
 * Encoding use Protobuf is available in {@link ProtobufRDF}.
 *
 * @deprecated Use {@link ThriftRDF}
 */
@Deprecated
public class BinRDF {

    /**
     * Create an {@link StreamRDF} for output.  A filename ending {@code .gz} will have
     * a gzip compressor added to the output path. A filename of "-" is {@code System.out}.
     * The file is closed when {@link StreamRDF#finish()} is called unless it is {@code System.out}.
     * Call {@link StreamRDF#start()}...{@link StreamRDF#finish()}.
     *
     * @param filename The file
     * @return StreamRDF A stream to send to.
     * @deprecated Use {@link ThriftRDF#streamToFile(String)} instead
     */
    @Deprecated
    public static StreamRDF streamToFile(String filename) {
        return ThriftRDF.streamToFile(filename);
    }

    /**
     * Create an {@link StreamRDF} for output.  A filenames ending {@code .gz} or {@code .bz2} will have
     * the respective compressor added to the output path. A filename of "-" is {@code System.out}.
     * The file is closed when {@link StreamRDF#finish()} is called unless it is {@code System.out}.
     * Call {@link StreamRDF#start()}...{@link StreamRDF#finish()}.
     *
     * @param filename The file
     * @param withValues - whether to encode numeric values as values.
     * @return StreamRDF A stream to send to.
     * @deprecated Use {@link ThriftRDF#streamToFile(String,boolean)} instead
     */
    @Deprecated
    public static StreamRDF streamToFile(String filename, boolean withValues) {
        return ThriftRDF.streamToFile(filename, withValues);
    }

    /**
     * Create an {@link StreamRDF} for output.
     * The {@code OutputStream} is closed when {@link StreamRDF#finish()} is called unless it is {@code System.out}.
     * Call {@link StreamRDF#start()}...{@link StreamRDF#finish()}.
     * @param out OutputStream
     * @return StreamRDF A stream to send to.
     * @deprecated Use {@link ThriftRDF#streamToOutputStream(OutputStream)} instead
     */
    @Deprecated
    public static StreamRDF streamToOutputStream(OutputStream out) {
        return ThriftRDF.streamToOutputStream(out);
    }

    /**
     * Create an {@link StreamRDF} for output.
     * The {@code OutputStream} is closed when {@link StreamRDF#finish()} is called unless it is {@code System.out}.
     * Call {@link StreamRDF#start()}...{@link StreamRDF#finish()}.
     * @param out OutputStream
     * @param withValues - whether to encode numeric values as values.
     * @return StreamRDF A stream to send to.
     * @deprecated Use {@link ThriftRDF#streamToOutputStream(OutputStream,boolean)} instead
     */
    @Deprecated
    public static StreamRDF streamToOutputStream(OutputStream out, boolean withValues) {
        return ThriftRDF.streamToOutputStream(out, withValues);
    }

    /**
     * Create an {@link StreamRDF} for output.
     * The {@code OutputStream} is closed when {@link StreamRDF#finish()} is called unless it is {@code System.out}.
     * Call {@link StreamRDF#start()}...{@link StreamRDF#finish()}.
     * @param protocol Output and encoding.
     * @return StreamRDF A stream to send to.
     * @deprecated Use {@link ThriftRDF#streamToTProtocol(TProtocol)} instead
     */
    @Deprecated
    public static StreamRDF streamToTProtocol(TProtocol protocol) {
        return ThriftRDF.streamToTProtocol(protocol);
    }

    /**
     * Create an {@link StreamRDF} for output.
     * The {@code OutputStream} is closed when {@link StreamRDF#finish()} is called unless it is {@code System.out}.
     * Call {@link StreamRDF#start()}...{@link StreamRDF#finish()}.
     * @param protocol Output and encoding.
     * @param withValues - whether to encode numeric values as values.
     * @return StreamRDF A stream to send to.
     * @deprecated Use {@link ThriftRDF#streamToTProtocol(TProtocol,boolean)} instead
     */
    @Deprecated
    public static StreamRDF streamToTProtocol(TProtocol protocol, boolean withValues) {
        return ThriftRDF.streamToTProtocol(protocol, withValues);
    }

    /**
     * Decode the contents of the file and send to the {@link StreamRDF}.
     * A filename ending {@code .gz} will have a gzip decompressor added.
     * A filename of "-" is {@code System.in}.
     * @param filename The file.
     * @param dest Sink
     * @deprecated Use {@link ThriftRDF#fileToStream(String,StreamRDF)} instead
     */
    @Deprecated
    public static void fileToStream(String filename, StreamRDF dest) {
        ThriftRDF.fileToStream(filename, dest);
    }

    /**
     * Decode the contents of the input stream and send to the {@link StreamRDF}.
     * @param in InputStream
     * @param dest StreamRDF
     * @deprecated Use {@link ThriftRDF#inputStreamToStream(InputStream,StreamRDF)} instead
     */
    @Deprecated
    public static void inputStreamToStream(InputStream in, StreamRDF dest) {
        ThriftRDF.inputStreamToStream(in, dest);
    }

    /**
     * Decode the contents of the TProtocol and send to the {@link StreamRDF}.
     * @param protocol TProtocol
     * @param dest Sink
     * @deprecated Use {@link ThriftRDF#protocolToStream(TProtocol,StreamRDF)} instead
     */
    @Deprecated
    public static void protocolToStream(TProtocol protocol, StreamRDF dest) {
        ThriftRDF.protocolToStream(protocol, dest);
    }

    /**
     * Send the contents of a RDF-encoded Thrift file to an "action"
     * @param protocol TProtocol
     * @param action   Code to act on the row.
     * @deprecated Use {@link ThriftRDF#apply(TProtocol,Consumer)} instead
     */
    @Deprecated
    public static void apply(TProtocol protocol, Consumer<RDF_StreamRow> action) {
        ThriftRDF.apply(protocol, action);
    }

    /** Debug help - print details of a Thrift stream.
     * Destructive on the InputStream.
     * @param out OutputStream
     * @param in InputStream
     * @deprecated Use {@link ThriftRDF#dump(OutputStream,InputStream)} instead
     */
    @Deprecated
    public static void dump(OutputStream out, InputStream in) {
        ThriftRDF.dump(out, in);
    }

    /**
     * @deprecated Use {@link ThriftRDF#readResultSet(InputStream)} instead
     */
    @Deprecated
    public static ResultSet readResultSet(InputStream in) {
        return ThriftRDF.readResultSet(in);
    }

    /**
     * @deprecated Use {@link ThriftRDF#readResultSet(TProtocol)} instead
     */
    @Deprecated
    public static ResultSet readResultSet(TProtocol protocol) {
        return ThriftRDF.readResultSet(protocol);
    }

    /**
     * @deprecated Use {@link ThriftRDF#writeResultSet(OutputStream,ResultSet)} instead
     */
    @Deprecated
    public static void writeResultSet(OutputStream out, ResultSet resultSet) {
        ThriftRDF.writeResultSet(out, resultSet);
    }

    /**
     * @deprecated Use {@link ThriftRDF#writeResultSet(OutputStream,ResultSet,boolean)} instead
     */
    @Deprecated
    public static void writeResultSet(OutputStream out, ResultSet resultSet, boolean withValues) {
        ThriftRDF.writeResultSet(out, resultSet, withValues);
    }

    /**
     * @deprecated Use {@link ThriftRDF#writeResultSet(TProtocol,ResultSet)} instead
     */
    @Deprecated
    public static void writeResultSet(TProtocol protocol, ResultSet resultSet) {
        ThriftRDF.writeResultSet(protocol, resultSet);
    }

    /**
     * @deprecated Use {@link ThriftRDF#writeResultSet(TProtocol,ResultSet,boolean)} instead
     */
    @Deprecated
    public static void writeResultSet(TProtocol protocol, ResultSet resultSet, boolean encodeValues) {
        ThriftRDF.writeResultSet(protocol, resultSet, encodeValues);
    }
}

