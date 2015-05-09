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

package org.seaborne.tdb2.store.nodetable;

import java.io.* ;

import org.apache.jena.graph.Node ;
import org.apache.jena.riot.thrift.TRDF ;
import org.apache.jena.riot.thrift.ThriftConvert ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
//import org.apache.jena.tdb.base.objectfile.ObjectFile ;
//import org.apache.jena.tdb.index.Index ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.apache.thrift.transport.TSeekableFile ;
import org.apache.thrift.transport.TSimpleFileTransport ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.store.NodeId ;

public class NodeTableThrift extends NodeTableNative2 {

    private TSimpleFileTransport file ;
    private final TProtocol protocol ;
    private long position ;
    //private final Index nodeToId ;
    
    public NodeTableThrift(Index nodeToId, String objectFile)
    {
        super(nodeToId, null);
        try {
            file = new TSimpleFileTransport(objectFile, true, true, true) ;
            position = file.length() ;
            this.protocol = TRDF.protocol(file) ;
        }
        catch (TException ex) {
            throw new TDBException("NodeTableThrift", ex) ;
        }
    }
    
    @Override
    protected NodeId writeNodeToTable(Node node) {
        RDF_Term term = ThriftConvert.convert(node, true) ;
        try {
            position = file.length() ;
            file.seek(position); 
            NodeId nid = NodeId.create(position) ;
            term.write(protocol);
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
            file.seek(x);
            RDF_Term term = new RDF_Term() ;
            term.read(protocol); 
            Node n = ThriftConvert.convert(term) ;
            return n ;
        }
        catch (TException ex) {
            throw new TDBException("NodeTableThrift", ex) ;
        }
    }
 

    // Better?
    static class ReadWriteFile implements TSeekableFile {
        //public class TStandardFile implements TSeekableFile {

        protected String path_ = null;
        protected RandomAccessFile inputFile_ = null;

        public ReadWriteFile(String path) throws IOException {
            path_ = path;
            inputFile_ = new RandomAccessFile(path_, "rw");
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(inputFile_.getFD());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(path_);
        }

        @Override
        public void close() throws IOException {
            if(inputFile_ != null) {
                inputFile_.close();
            }
        }

        @Override
        public long length() throws IOException {
            return inputFile_.length();
        }

        @Override
        public void seek(long pos) throws IOException {
            inputFile_.seek(pos);
        }
    }

}