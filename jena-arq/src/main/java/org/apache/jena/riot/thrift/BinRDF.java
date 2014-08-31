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

import java.io.BufferedOutputStream ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Action ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.thrift.wire.RDF_StreamRow ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.apache.thrift.transport.TTransportException ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Operations on binary RDF (which uses <a href="http://thrift.apache.org/">Apache Thrift</a>).
 * See also {@linkplain ThriftConvert}, for specific functions on binary RDF.
 * @See ThriftConvert 
 */
public class BinRDF {
    
    private static int BUFSIZE_IN   = 128*1024 ;
    private static int BUFSIZE_OUT  = 128*1024 ;

    /** 
     * Create an {@linkplain StreamRDF} for output.  A filename ending {@code .gz} will have
     * a gzip compressor added to the output path. A filename of "-" is {@code System.out}.
     * The file is closed when {@linkplain StreamRDF#finish()} is called unless it is {@code System.out}.  
     * Call {@linkplain StreamRDF#start()}...{@linkplain StreamRDF#finish()}.
     * 
     * @param filename The file
     * @return StreamRDF A stream to send to.
     */
    
    public static StreamRDF streamToFile(String filename) {
        return streamToFile(filename, false) ;
    }
    
    /** 
     * Create an {@linkplain StreamRDF} for output.  A filename ending {@code .gz} will have
     * a gzip compressor added to the output path. A filename of "-" is {@code System.out}.
     * The file is closed when {@linkplain StreamRDF#finish()} is called unless it is {@code System.out}.  
     * Call {@linkplain StreamRDF#start()}...{@linkplain StreamRDF#finish()}.
     * 
     * @param filename The file
     * @param withValues - whether to encode numeric values as values.
     * @return StreamRDF A stream to send to.
     */
    public static StreamRDF streamToFile(String filename, boolean withValues) {
        OutputStream out = IO.openOutputFile(filename) ;
        // Is this internally buffered as well?
        BufferedOutputStream bout = new BufferedOutputStream(out, BUFSIZE_OUT) ;
        TProtocol protocol = TRDF.protocol(bout) ;
        return new StreamRDF2Thrift(protocol, withValues) ;
    }
    
    /** 
     * Create an {@linkplain StreamRDF} for output.
     * The {@code OutputStream} is closed when {@linkplain StreamRDF#finish()} is called unless it is {@code System.out}.  
     * Call {@linkplain StreamRDF#start()}...{@linkplain StreamRDF#finish()}.
     * @param out OutputStream
     * @return StreamRDF A stream to send to. 
     */
    public static StreamRDF streamToOutputStream(OutputStream out) {
        return streamToOutputStream(out, false) ;
    }
    
    /** 
     * Create an {@linkplain StreamRDF} for output.
     * The {@code OutputStream} is closed when {@linkplain StreamRDF#finish()} is called unless it is {@code System.out}.  
     * Call {@linkplain StreamRDF#start()}...{@linkplain StreamRDF#finish()}.
     * @param out OutputStream
     * @param withValues - whether to encode numeric values as values.
     * @return StreamRDF A stream to send to. 
     */
    public static StreamRDF streamToOutputStream(OutputStream out, boolean withValues) {
        return new StreamRDF2Thrift(out, withValues) ;
    }

    /** 
     * Create an {@linkplain StreamRDF} for output.
     * The {@code OutputStream} is closed when {@linkplain StreamRDF#finish()} is called unless it is {@code System.out}.  
     * Call {@linkplain StreamRDF#start()}...{@linkplain StreamRDF#finish()}.
     * @param protocol Output and encoding.
     * @return StreamRDF A stream to send to. 
     */
    public static StreamRDF streamToTProtocol(TProtocol protocol) {
        return streamToTProtocol(protocol, false) ;
    }

    /** 
     * Create an {@linkplain StreamRDF} for output.
     * The {@code OutputStream} is closed when {@linkplain StreamRDF#finish()} is called unless it is {@code System.out}.  
     * Call {@linkplain StreamRDF#start()}...{@linkplain StreamRDF#finish()}.
     * @param protocol Output and encoding.
     * @param withValues - whether to encode numeric values as values.
     * @return StreamRDF A stream to send to. 
     */
    public static StreamRDF streamToTProtocol(TProtocol protocol, boolean withValues) {
        return new StreamRDF2Thrift(protocol, withValues) ;
    }

    /**
     * Decode the contents of the file and send to the {@linkplain StreamRDF}.
     * A filename ending {@code .gz} will have a gzip decompressor added.
     * A filename of "-" is {@code System.in}.
     * @param filename The file.
     * @param dest Sink
     */
    public static void fileToStream(String filename, StreamRDF dest) {
        InputStream in = IO.openFile(filename) ;
        TProtocol protocol = TRDF.protocol(in) ;
        protocolToStream(protocol, dest) ;
    }
        
    /**
     * Decode the contents of the input stream and send to the {@linkplain StreamRDF}.
     * @param in InputStream
     * @param dest StreamRDF
     */
    public static void inputStreamToStream(InputStream in, StreamRDF dest) {
        TProtocol protocol = TRDF.protocol(in) ;
        protocolToStream(protocol, dest) ;
    }

    /**
     * Decode the contents of the TProtocol and send to the {@linkplain StreamRDF}.
     * @param protocol TProtocol
     * @param dest Sink
     */
    public static void protocolToStream(TProtocol protocol, StreamRDF dest) {
        PrefixMap pmap = PrefixMapFactory.create() ;
        final Thrift2StreamRDF s = new Thrift2StreamRDF(pmap, dest) ;
        dest.start() ;
        // ** Java8
        //apply(protocol, z -> TRDF.visit(z, s)) ;
        
        applyVisitor(protocol, s)  ;
        
        dest.finish() ;
        // No need to flush - we read from the protocol ; 
    }

    // ** Java7 support
    public static void applyVisitor(TProtocol protocol, final VisitorStreamRowTRDF visitor) {
        Action<RDF_StreamRow> action = new Action<RDF_StreamRow>() {
            @Override
            public void apply(RDF_StreamRow z) { TRDF.visit(z, visitor) ; }
        } ;
        apply(protocol, action) ;
    }
    
    /**
     * Send the contents of a RDF-encoded Thrift file to an "action" 
     * @param protocol TProtocol
     * @param action   Code to act on the row.
     */
    public static void apply(TProtocol protocol, Action<RDF_StreamRow> action) {
        RDF_StreamRow row = new RDF_StreamRow() ;
        while(protocol.getTransport().isOpen()) {
            try { row.read(protocol) ; }
            catch (TTransportException e) {
                if ( e.getType() == TTransportException.END_OF_FILE )
                    break ;
            }
            catch (TException ex) { TRDF.exception(ex) ; }
            action.apply(row) ;
            row.clear() ;
        }
    }
    
    /** Debug help - print details of a Thrift stream.
     * Destructive on the InputStream. 
     * @param out OutputStream 
     * @param in InputStream
     */
    public static void dump(OutputStream out, InputStream in) {
        IndentedWriter iOut = new IndentedWriter(out) ;
        StreamRowTRDFPrinter printer = new StreamRowTRDFPrinter(iOut) ;
        TProtocol protocol = TRDF.protocol(in) ;
        BinRDF.applyVisitor(protocol, printer) ;
        iOut.flush() ;
    }


    public static ResultSet readResultSet(InputStream in) {
        return readResultSet(TRDF.protocol(in)) ;
    }
    
    public static ResultSet readResultSet(TProtocol protocol) {
        Thift2Binding t2b = new Thift2Binding(protocol) ;
        List<String> varsNames = Var.varNames(t2b.getVars()) ;
        return new ResultSetStream(varsNames, null, t2b) ;
    }

    public static void writeResultSet(OutputStream out, ResultSet resultSet) {
        writeResultSet(out, resultSet, false) ;
    }
    
    public static void writeResultSet(OutputStream out, ResultSet resultSet, boolean withValues) {
        BufferedOutputStream bout = 
            ( out instanceof BufferedOutputStream ) 
            ? (BufferedOutputStream)out 
            : new BufferedOutputStream(out, TRDF.OutputBufferSize) ;
        writeResultSet(TRDF.protocol(bout), resultSet, withValues) ;
        IO.flush(out) ;
    }
    
    public static void writeResultSet(TProtocol protocol, ResultSet resultSet) {
        writeResultSet(protocol, resultSet, false) ;
    }
    
    public static void writeResultSet(TProtocol protocol, ResultSet resultSet, boolean encodeValues) {
        List<Var> vars = Var.varList(resultSet.getResultVars()) ;
        try ( Binding2Thrift b2t = new Binding2Thrift(protocol, vars, encodeValues) ) {
            for ( ; resultSet.hasNext() ; ) {
                Binding b = resultSet.nextBinding() ;
                b2t.output(b) ;
            }
        }
        //Done by Binding2Thrift.close() -- LibThriftRDF.flush(protocol) ;
    }
    
}

