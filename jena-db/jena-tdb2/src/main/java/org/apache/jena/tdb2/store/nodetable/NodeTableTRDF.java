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

package org.apache.jena.tdb2.store.nodetable;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.thrift.RiotThriftException;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.ThriftConvert;
import org.apache.jena.riot.thrift.wire.RDF_Term;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/** NodeTable using Thrift for the I/O and storage. */

public class NodeTableTRDF extends NodeTableNative {
    // Write buffering is done in the underlying BinaryDataFile
    private final BinaryDataFile diskFile;
    private final TReadAppendFileTransport transport;
    private final TProtocol protocol;

    public NodeTableTRDF(Index nodeToId, BinaryDataFile objectFile) {
        super(nodeToId);
        try {
            this.diskFile = objectFile;
            transport = new TReadAppendFileTransport(diskFile);
            if ( ! transport.isOpen() )
                transport.open();
            this.protocol = TRDF.protocol(transport);
        }
        catch (Exception ex) {
            throw new TDBException("NodeTableTRDF", ex);
        }
    }

    @Override
    protected NodeId writeNodeToTable(Node node) {
        RDF_Term term = ThriftConvert.convert(node, true);
        try {
            long x = diskFile.length();
            // Paired : [*]
            NodeId nid = NodeIdFactory.createPtr(x);
            term.write(protocol);
            //transport.flush();
            return nid;
        }
        catch(TransactionException ex) { throw ex; }
        catch (Exception ex) {
            throw new TDBException("NodeTableThrift/Write", ex);
        }
    }

    @Override
    protected Node readNodeFromTable(NodeId id) {
        try {
            // Paired : [*]
            long x = id.getPtrLocation();
            transport.readPosition(x);
            RDF_Term term = new RDF_Term();
            term.read(protocol);
            Node n = ThriftConvert.convert(term);
            return n;
        }
        catch (TException ex) {
            throw new TDBException("NodeTableTRDF/Read", ex);
        }
        catch (RiotThriftException ex) {
            Log.error(this, "Bad encoding: NodeId = "+id);
            throw ex;
        }
    }

    @Override
    protected void syncSub() {
        try { transport.flush(); }
        catch (Exception ex) { throw new TDBException("NodeTableTRDF", ex); }
    }

    @Override
    protected void closeSub() {
        if ( transport.isOpen() ) {
            try { transport.close(); }
            catch (Exception ex) { throw new TDBException("NodeTableTRDF", ex); }
        }
    }

    public Index getIndex()             { return nodeHashToId; }
    public BinaryDataFile getData()     { return diskFile; }
}