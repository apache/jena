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

package org.seaborne.dboe.trans.data;

import java.nio.ByteBuffer ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.base.file.BinaryDataFile ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.migrate.L ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.StateMgrData ;
import org.seaborne.dboe.transaction.txn.TransactionalComponentLifecycle ;
import org.seaborne.dboe.transaction.txn.TxnId ;

/** Transactional {@link ObjectFile}.
 *  An object file is append-only and allows only one writer at a time.
 *  As a result, all reader, see the file up to the last commit point at the time 
 *  they started.  The sole writer sees more of the file.
 */

public class TransBinaryDataFile extends TransactionalComponentLifecycle<TransBinaryDataFile.TxnObjectFile>
    implements BinaryDataFile {

    /*
     * The file is written to as we go along so abort requires some action. We
     * can't recover from just the file, without any redo or undo recovery
     * action.
     */
    
    private final FileState stateMgr ;
    
    // Space for 0xFFFF = (64k)  
    private static final String baseUuidStr = "867458e6-f8c8-11e4-b702-3417eb9b0000" ; 
    private final ComponentId componentId ;

    // The current committed position.
    // This is also the abort point.
    // And the limit as seen by readers.
    // Recovery record: (length, position)
    private final AtomicLong length ;
    private final AtomicLong position ; 
    
    static class TxnObjectFile {
        final long length ; 
        final long position ; 

        TxnObjectFile(long length, long position) {
            this.length = length ;
            this.position = position ;
        }
    }

    static class FileState extends StateMgrData {
        FileState(BufferChannel bufferChannel, long length, long position) {
            super(bufferChannel, length, position) ;
        }
        private static int idxLength = 0 ; 
        private static int idxPosition = 1 ;
        long length()               { return get(idxLength) ; }
        long position()             { return get(idxPosition) ; }
        void length(long len)       { set(idxLength, len) ; } 
        void position(long posn)    { set(idxPosition, posn) ; }
    }

    private final BinaryDataFile binFile ;
    
    public TransBinaryDataFile(BinaryDataFile binFile, BufferChannel bufferChannel, int id) {
        super() ;
        stateMgr = new FileState(bufferChannel, 0L, 0L) ;
        this.binFile = binFile ;
        
        // These may be updated by recovery. Start by setting to the
        // "clean start" settings.
        
        length   = new AtomicLong(binFile.length()) ;
        position = new AtomicLong(binFile.position()) ;
        
        // Common code
        byte[] bytes = L.uuidAsBytes(baseUuidStr) ;
        // Set half word
        byte lo = (byte)(id&0xFF) ;
        byte hi = (byte)((id >> 8) &0xFF) ;
        bytes[bytes.length-2] = lo ;
        bytes[bytes.length-1] = hi ;
        // Common code
        componentId = new ComponentId("Trans-TransBinaryDataFile"+id, bytes) ;
    }
    
    @Override
    public ComponentId getComponentId() {
        return componentId ;
    }
    
    private boolean recoveryAction = false ; 

    @Override
    public void startRecovery() {
        recoveryAction = false ;
    }
    
    // XXX StateMgr length , position.to give naming 

    @Override
    public void recover(ByteBuffer ref) {
        stateMgr.setState(ref);
        length.set(stateMgr.length()) ;
        position.set(stateMgr.position()) ;
        recoveryAction = true ;
    }

    @Override
    public void finishRecovery() {
        if ( recoveryAction ) {
            binFile.truncate(length.get()) ;
            binFile.sync();
        }
    }
    
    @Override
    public void cleanStart() { }

    @Override
    protected TxnObjectFile _begin(ReadWrite readWrite, TxnId txnId) {
        // Atomic read across the two because it's called from within 
        // TransactionCoordinator.begin$ where there is a lock.
        long xLength = length.get() ;
        long xPosition = position.get() ;
        return new TxnObjectFile(xLength, xPosition) ;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, TxnObjectFile state) {
        stateMgr.length(binFile.length()) ;
        stateMgr.position(binFile.position());  
        return stateMgr.getState() ;
    }

    @Override
    protected void _commit(TxnId txnId, TxnObjectFile state) {
        if ( isWriteTxn() ) {
            // Force to disk.
            binFile.sync();
            stateMgr.writeState();
            // Move visible commit point forward.
            length.set(binFile.length()) ;
            position.set(binFile.position()) ;
        }
    }

    @Override
    protected void _commitEnd(TxnId txnId, TxnObjectFile state) {
    }

    @Override
    protected void _abort(TxnId txnId, TxnObjectFile state) {
        if ( isWriteTxn() ) {
            // One will imply the other.
            binFile.truncate(state.length) ;
            //binFile.reposition(state.position) ;
            // sync to make sure the file looses the unwanted part.
            // Neater, no abandoned sections
            binFile.sync() ;
        }
    }

    @Override
    protected void _complete(TxnId txnId, TxnObjectFile state) {}

    @Override
    protected void _shutdown() {}
    
    private void checkBoundsReader(long requestedPoint, TxnObjectFile state) { }
    
    @Override
    public void open() {
        // XXX ???
        binFile.open();
    }

    @Override
    public boolean isOpen() {
        return binFile.isOpen() ;
    }

    @Override
    public int read(byte[] b, int start, int length) {
        checkTxn();
        if ( isReadTxn() ) {
            // XXX Restrict view.
            //checkBoundsReader(requestedPoint, TxnObjectFile state)
        }
        return binFile.read(b, start, length) ;
    }

    @Override
    public void write(byte[] b, int start, int length) {
        checkWriteTxn();
        binFile.write(b, start, length);
    }

    // **** Multithreaded use.
    // ==> read(position, byte[]) 
    
    @Override
    public void position(long posn) {
        binFile.position(posn);
    }

    // The BinaryDataFile part, with transaction testing.
    @Override
    public void truncate(long size) {
        checkWriteTxn();
        binFile.truncate(size) ;
    }

    @Override
    public void sync() {
        super.checkTxn();
        binFile.sync() ;
    }

    @Override
    public void close() {
        super.checkTxn();
        binFile.close() ;
    }

    @Override
    public long length() {
        super.checkTxn();
        if ( isReadTxn() )
            return getDataState().length ;
            // Reader. Check bounds.
        return binFile.length() ;
    }

    @Override
    public long position() {
        super.checkTxn();
        return binFile.position() ;
    }

    @Override
    public boolean isEmpty() {
        super.checkTxn();
        return binFile.isEmpty() ;
    }    
}

