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

package org.seaborne.dboe.trans.data;

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.base.block.Block ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.StateMgrData ;
import org.seaborne.dboe.transaction.txn.TransactionalComponentLifecycle ;
import org.seaborne.dboe.transaction.txn.TxnId ;

/** Transactional {@link ObjectFile}.
 *  An object file is append-only and allows only one writer at a time.
 *  As a result, all readers see the file up to the last commit point at the time 
 *  they started.  The sole writer sees more of the file.
 */

public class TransObjectFile extends TransactionalComponentLifecycle<TransObjectFile.TxnObjectFile>
    implements ObjectFile {

    /*
     * The file is written to as we go along so abort requires some action. We
     * can't recover from just the file, without any redo or undo recovery
     * action. The length/position of the file may be duff, there is a possible
     * abandoned section of the file.
     * 
     * But even if a partial entry, we don't corrupt data. Assumes no references
     * to the abandoned area of the file.
     */
    
    private final ObjectFileState stateMgr ;

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

    static class ObjectFileState extends StateMgrData {
        ObjectFileState(BufferChannel bufferChannel, long length, long position) {
            super(bufferChannel, length, position) ;
        }
        private static int idxLength = 0 ; 
        private static int idxPosition = 1 ;
        long length()               { return get(idxLength) ; }
        long position()             { return get(idxPosition) ; }
        void length(long len)       { set(idxLength, len) ; } 
        void position(long posn)    { set(idxPosition, posn) ; }
    }

    private final ObjectFile objFile ;
    
    public TransObjectFile(ObjectFile objFile, ComponentId cid, BufferChannel bufferChannel) {
        super(cid) ;
        stateMgr = new ObjectFileState(bufferChannel, 0L, 0L) ;
        this.objFile = objFile ;
        
        // These may be updated by recovery. Start by setting to the
        // "clean start" settings.
        
        length   = new AtomicLong(objFile.length()) ;
        position = new AtomicLong(objFile.position()) ;
    }
    
    private boolean recoveryAction = false ; 

    @Override
    public void startRecovery() {
        recoveryAction = false ;
    }
    
    @Override
    public void recover(ByteBuffer ref) {
        stateMgr.setState(ref);
        length.set(stateMgr.length()) ;
        position.set(stateMgr.position()) ;
        recoveryAction = true ;
    }

    @Override
    public void finishRecovery() {
        // If we did a truncate.
//        if ( recoveryAction )
//            objFile.sync();
    }
    
    @Override
    public void cleanStart() { }

    @Override
    protected TxnObjectFile _begin(ReadWrite readWrite, TxnId txnId) {
        return createState();
    }
    
    private TxnObjectFile createState() {
        // Atomic read across the two because it's called from within 
        // TransactionCoordinator.begin$ or promote$ where there is a lock.
        long xLength = length.get() ;
        long xPosition = position.get() ;
        return new TxnObjectFile(xLength, xPosition) ;
    }
    
    @Override
    protected TxnObjectFile _promote(TxnId txnId, TxnObjectFile state) {
        // Our write state is the read state.
        return createState();
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, TxnObjectFile state) {
        stateMgr.length(objFile.length()) ;
        stateMgr.position(objFile.position());  
        return stateMgr.getState() ;
    }

    @Override
    protected void _commit(TxnId txnId, TxnObjectFile state) {
        if ( isWriteTxn() ) {
            // Force to disk.
            objFile.sync();
            stateMgr.writeState();
            // Move visible commit point forward.
            length.set(objFile.length()) ;
            position.set(objFile.position()) ;
        }
    }

    @Override
    protected void _commitEnd(TxnId txnId, TxnObjectFile state) {
    }

    @Override
    protected void _abort(TxnId txnId, TxnObjectFile state) {
        if ( isWriteTxn() ) {
            // One will imply the other.
            objFile.truncate(state.length) ;
            //objFile.reposition(state.position) ;
            // sync to make sure the file looses the unwanted part.
            // Neater, no abandoned sections (outside crashes).
            objFile.sync() ;
        }
    }

    @Override
    protected void _complete(TxnId txnId, TxnObjectFile state) {}

    @Override
    protected void _shutdown() {}
    
    private void checkBoundsReader(long requestedPoint, TxnObjectFile state) { }
    
    // The Object file part, with transaction testing.
    @Override
    public Block allocWrite(int maxBytes) {
        checkWriteTxn();
        return objFile.allocWrite(maxBytes) ;
    }

    @Override
    public void completeWrite(Block buffer) {
        checkWriteTxn();
        objFile.completeWrite(buffer) ;
    }

    @Override
    public void abortWrite(Block buffer) {
        checkWriteTxn();
        objFile.abortWrite(buffer) ;
    }

    @Override
    public long write(ByteBuffer buffer) {
        checkWriteTxn();
        return objFile.write(buffer) ;
    }

    @Override
    public void reposition(long id) {
        checkWriteTxn();
        objFile.reposition(id) ;
    }

    @Override
    public void truncate(long size) {
        checkWriteTxn();
        objFile.truncate(size) ;
    }

    @Override
    public ByteBuffer read(long id) {
        super.checkTxn();
        if ( isReadTxn() )
            // Reader. Check bounds.
            checkBoundsReader(id, super.getDataState()) ;
        return objFile.read(id) ;
    }

    @Override
    public String getLabel() {
        return objFile.getLabel() ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all() {
        super.checkTxn();
        return objFile.all() ;
    }

    @Override
    public void sync() {
        super.checkTxn();
        objFile.sync() ;
    }

    @Override
    public void close() {
        super.checkTxn();
        objFile.close() ;
    }

    @Override
    public long length() {
        super.checkTxn();
        if ( isReadTxn() )
            return getDataState().length ;
            // Reader. Check bounds.
        return objFile.length() ;
    }

    @Override
    public long position() {
        super.checkTxn();
        return objFile.position() ;
    }

    @Override
    public boolean isEmpty() {
        super.checkTxn();
        return objFile.isEmpty() ;
    }    
}

