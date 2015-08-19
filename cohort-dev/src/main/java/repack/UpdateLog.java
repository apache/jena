/*
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
 
package repack;

import java.io.* ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.riot.thrift.TRDF ;
import org.apache.jena.riot.thrift.ThriftConvert ;
import org.apache.jena.riot.thrift.wire.RDF_Term ;
import org.apache.jena.sparql.core.* ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.thrift.TException ;
import org.apache.thrift.protocol.TProtocol ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;

/** Record changes to a dataset.
 * Replay
 */
public class UpdateLog {
    
    private static final byte ADD = 1 ;
    private static final byte NO_ADD = 2 ;
    private static final byte DEL = 2 ;
    private static final byte NO_DEL = 3 ;
    private static final byte END = 99 ;

    static { LogCtl.setLog4j(); }
    
//    static class DatasetGraphSwitch extends DatasetGraphWrapper {
//        
//    }
    
    public static void main(String ... argv) throws TException  {
        DatasetGraphTDB dsgBase = (DatasetGraphTDB)TDBFactory.createDatasetGraph() ;
        
        repack(dsgBase) ;
        
        
        
        
        //DatasetGraph dsgBase = DatasetGraphFactory.createMem() ;
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        DatasetChanges changes = new ThriftChangeLog(out) ;
        DatasetGraph dsg = new DatasetGraphMonitor(dsgBase, changes, false) ;
        changes.start();
        Quad q1 = SSE.parseQuad("(<g> <s> :p :o)") ;
        Txn.executeWrite(dsgBase, ()->dsg.add(q1));
        changes.finish() ;
        
        DatasetGraph dsg2 = DatasetGraphFactory.createMem() ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        replay(in, new UpdateApply(dsg2)) ;
        System.out.println(dsg2) ;
    }
    
    // TestTransactionCoordinatorControl ++ start writer -- go no writers -- test.
    
    /** Safe repack - go into "no writer" mode, clone the  
     */
    private static void repackSafe(DatasetGraphTDB dsg, Location newLocation) {
        TransactionCoordinator txnMgr = dsg.getTxnSystem().getTxnMgr() ; 
        txnMgr.execAsWriter(()->{
            // No writers.
            dsg.begin(ReadWrite.READ);
            DatasetGraphTDB dsg2 = CloneTDB.cloneDatasetSimple(dsg, newLocation) ;
            txnMgr.execExclusive(null);
            switchDatasets(dsg, dsg2) ;
            // New transactions go to dsg2 ;
        }) ;
        // Drain all old R transactions.
        txnMgr.startExclusiveMode();
        txnMgr.finishExclusiveMode();
        // dsg is now free.
    }
    
    private static void switchDatasets(DatasetGraphTDB dsg, DatasetGraphTDB dsg2) {}

    private static void repack(DatasetGraphTDB dsg) {
        // Clonign requires a consistent 
        
        // transaction
        TransactionCoordinator txnMgr = dsg.getTxnSystem().getTxnMgr() ;
        
        txnMgr.execAsWriter(()->{
            // Start collecting replay logs.
            
            // Start read transaction for this timepoint for the clone.
            dsg.begin(ReadWrite.READ);
        }) ;

        // clone
        
        txnMgr.execAsWriter(()->{
            // replay logs.
            // ???
            // Set up switch
            // Switch (for new transactions).
            // End clone transaction.
            dsg.end() ;
        }) ;
    }

    public static void replay(InputStream in, DatasetChanges processor)  throws TException {
        in = new BufferedInputStream(in, 1024*1024) ;
        TProtocol protocol = TRDF.protocol(in) ;
        
        for(;;) {
            byte b = protocol.readByte() ;
            if ( b == END )
                break ;
            QuadAction quadAction = byteToQuadAction(b) ;
            Node g = read(protocol) ;
            Node s = read(protocol) ;
            Node p = read(protocol) ;
            Node o = read(protocol) ;
            processor.change(quadAction, g, s, p, o); 
        }
    }

    static RDF_Term rdfTerm = new RDF_Term() ;
    

    private static Node read(TProtocol protocol) throws TException {
        // Recycle.
        rdfTerm.clear() ;
        rdfTerm.read(protocol);
        return ThriftConvert.convert(rdfTerm, null) ;
    }
    
    private static byte quadActionToByte(QuadAction qaction) {
        switch(qaction) {
            case ADD: return ADD ;
            case DELETE : return DEL ; 
            case NO_ADD : return NO_ADD ;
            case NO_DELETE : return NO_DEL ;
            default:
                throw new InternalErrorException("Bad QuadAction") ;
        }
    }

    private static QuadAction byteToQuadAction(byte b) {
        if ( b == ADD ) return QuadAction.ADD ;
        if ( b == DEL ) return QuadAction.DELETE ;
        if ( b == NO_ADD ) return QuadAction.NO_ADD ;
        if ( b == NO_DEL ) return QuadAction.NO_DELETE ;
        throw new InternalErrorException(String.format("Bad byte value for QuadAction: 0x%02X",Byte.toUnsignedInt(b))) ;
    }
    
    static class UpdateApply implements DatasetChanges {
        private final DatasetGraph dsg;

        UpdateApply(DatasetGraph dsg) { this.dsg = dsg ; }

        @Override
        public void start() {}

        @Override
        public void change(QuadAction qAction, Node g, Node s, Node p, Node o) {
            switch(qAction) {
                case ADD:       dsg.add(g, s, p, o) ; break ;
                case DELETE:    dsg.delete(g, s, p, o) ; break ;
                default:        /* no-op */
            }
        }

        @Override
        public void finish() {}

        @Override
        public void reset() {} 
    }
    
    static class ThriftChangeLog implements DatasetChanges {
        private static final int BUFSIZE_OUT = 128*1024 ;
        private final TProtocol protocol;
        private RDF_Term termBuffer = new RDF_Term() ;

        public ThriftChangeLog(OutputStream out) {
            BufferedOutputStream bout = new BufferedOutputStream(out, BUFSIZE_OUT) ;
            this.protocol = TRDF.protocol(bout) ;
        }

        @Override
        public void start() {}

        @Override
        public void change(QuadAction qaction, Node g, Node s, Node p, Node o) {
            ///
            byte b = quadActionToByte(qaction);
            try {
                protocol.writeByte(b);
                write(g);
                write(s);
                write(p);
                write(o);
            } catch (TException ex) { throw new InternalErrorException(ex) ; } 
        }

        private void write(Node n) throws TException {
            termBuffer.clear(); 
            ThriftConvert.toThrift(n, null, termBuffer, true) ;
            termBuffer.write(protocol);
        }

        @Override
        public void finish() {
            try {
                protocol.writeByte(END);
                protocol.getTransport().flush() ; 
            } catch (TException ex) { throw new InternalErrorException(ex) ; } 
        }

        @Override
        public void reset() {}

    }
}


