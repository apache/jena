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

package org.seaborne.dboe.transdata;

import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.lib.Pair ;
import org.seaborne.dboe.base.block.Block ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.transaction.txn.* ;

import com.hp.hpl.jena.query.ReadWrite ;

/** Transactional {@link ObjectFile}.
 *  An object file is append-only and allows only one writer at a time.
 *  As a result, all reader, see the file up to the last commit point at the time 
 *  they started.  The sole writer sees more of the file.
 */

public class TransObjectFile extends TransactionalComponentLifecycle<TransObjectFile.TxnObjectFile>
    implements ObjectFile {

    /*
     * The file is written to as we go along so abort requires some action. We
     * can't recover from just the file, without any redo or undo recovery
     * action. The length/position of the file may be duff, there is a possble
     * abandoned section of the file.
     * 
     * But even if a partial entry, we don't corrupt data. Assumes no references
     * to the abandoned area of the file.
     */
    
    // Space for 0xFFFF = (64k)  
    private static final String baseUuidStr = "95e0f729-ad29-48b2-bd70-e37386630000" ; 
    
    //General machinery?
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

    private final ObjectFile objFile ;
    
    public TransObjectFile(ObjectFile objFile, int id) {
        super() ;
        this.objFile = objFile ;
        
        // Atomic 
        length   = new AtomicLong(objFile.length()) ;
        position = new AtomicLong(objFile.position()) ;
        
        // Common code
        byte[] bytes = L.uuidAsBytes(baseUuidStr) ;
        // Set half word
        byte lo = (byte)(id&0xFF) ;
        byte hi = (byte)((id >> 8) &0xFF) ;
        bytes[bytes.length-2] = lo ;
        bytes[bytes.length-1] = hi ;
        // Common code
        componentId = new ComponentId("Trans-TransObjectFile"+id, bytes) ;
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

    @Override
    public void recover(ByteBuffer ref) {
        long xLength = ref.getLong() ;
        long xPosition = ref.getLong() ;
        length.set(xLength) ;
        position.set(xPosition) ;
        recoveryAction = true ;
    }

    @Override
    public void finishRecovery() {
        if ( recoveryAction )
            objFile.sync();
    }

    @Override
    protected TxnObjectFile _begin(ReadWrite readWrite, TxnId txnId) {
        // Atomic read across the two because it's called from within 
        // TransactionCoordinator.begin$ where there is a lock.
        long xLength = length.get() ;
        long xPosition = position.get() ;
        return new TxnObjectFile(length.get(), position.get()) ;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, TxnObjectFile state) {
        ByteBuffer x = ByteBuffer.allocate(2*Long.BYTES) ;
        x.putLong(state.length) ;
        x.putLong(state.position) ;
        return x ;
    }

    @Override
    protected void _commit(TxnId txnId, TxnObjectFile state) {
        if ( isWriteTxn() ) {
            // Force to disk.
            objFile.sync();
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
    
    private void checkBoundsReader(long requestedPoint, TxnObjectFile state) {
        
    }
    
    // The Object file part, with transaction testing.
    @Override
    public Block allocWrite(int maxBytes) {
        super.requireWriteTxn();
        return objFile.allocWrite(maxBytes) ;
    }

    @Override
    public void completeWrite(Block buffer) {
        super.requireWriteTxn();
        objFile.completeWrite(buffer) ;
    }

    @Override
    public void abortWrite(Block buffer) {
        super.requireWriteTxn();
        objFile.abortWrite(buffer) ;
    }

    @Override
    public long write(ByteBuffer buffer) {
        super.requireWriteTxn();
        return objFile.write(buffer) ;
    }

    @Override
    public void reposition(long id) {
        super.requireWriteTxn();
        objFile.reposition(id) ;
    }

    @Override
    public void truncate(long size) {
        super.requireWriteTxn();
        objFile.truncate(size) ;
    }

    @Override
    public ByteBuffer read(long id) {
        super.checkTxn();
        if ( isReadTxn() )
            // Reader. Check bounds.
            checkBoundsReader(id, super.getState()) ;
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
            // XXX
            return -1 ;
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

