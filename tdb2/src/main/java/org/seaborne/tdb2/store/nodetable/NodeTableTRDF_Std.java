/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.store.nodetable ;

import org.apache.jena.graph.Node ;
import org.apache.jena.riot.thrift.TRDF ;
import org.apache.jena.riot.thrift.ThriftConvert ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.apache.thrift.transport.TSimpleFileTransport ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.store.NodeId ;

public class NodeTableTRDF_Std extends NodeTableNative {
    // TFileTransport does not support writing.
    // Write own, with length recording.
    // TIOStreamTransport
    
    // (1) TSimpleFileTransport+ buffering on RandomAccessFile
    // (2) TIOStreamTransport with a append, recording output file.
    // (3) Two transports, in and out.
    
    // ** (2)
    // Separate in and out.
    //   Part of "BinaryFile" which flushes across in-out swaps.
    
    private TSimpleFileTransport file ;
    private final TProtocol      protocol ;
    private long                 position ;

    // private final Index nodeToId ;

    public NodeTableTRDF_Std(Index nodeToId, String objectFile) {
        super(nodeToId) ;
        try {
            file = new TSimpleFileTransport(objectFile, true, true, true) ;
            position = file.length() ;
            this.protocol = TRDF.protocol(file) ;
        }
        catch (TException ex) {
            throw new TDBException("NodeTableTRDF", ex) ;
        }
    }

    @Override
    protected NodeId writeNodeToTable(Node node) {
        RDF_Term term = ThriftConvert.convert(node, true) ;
        try {
            position = file.length() ;
            file.seek(position) ;
            NodeId nid = NodeId.create(position) ;
            term.write(protocol) ;
            return nid ;
        }
        catch (TException ex) {
            throw new TDBException("NodeTableThrift", ex) ;
        }
    }

    @Override
    protected Node readNodeFromTable(NodeId id) {
        try {
            long x = id.getId() ;
            file.seek(x) ;
            RDF_Term term = new RDF_Term() ;
            term.read(protocol) ;
            Node n = ThriftConvert.convert(term) ;
            return n ;
        }
        catch (TException ex) {
            throw new TDBException("NodeTableTRDF", ex) ;
        }
    }

    @Override
    protected void syncSub() {
        try {
            file.flush() ;
        }
        catch (Exception ex) {
            throw new TDBException("NodeTableTRDF", ex) ;
        }
    }

    @Override
    protected void closeSub() {
        try {
            file.close() ;
        }
        catch (Exception ex) {
            throw new TDBException("NodeTableTRDF", ex) ;
        }
    }
}