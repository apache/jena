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

package org.apache.jena.dboe.trans.data;

import java.nio.ByteBuffer ;
import java.util.concurrent.atomic.AtomicReference ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.dboe.base.file.BufferChannel;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionalComponentLifecycle;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.query.ReadWrite ;

/** Manage a single binary (not too large) object.
 * It is written and read from a file in one action, 
 * so changes completely replace the original contents.
 * The whole object is written to the journal during prepare.  
 */
public class TransBlob extends TransactionalComponentLifecycle<TransBlob.BlobState> {

    // The last commited state.
    // Immutable ByteBuffer.
    private final AtomicReference<ByteBuffer> blobRef = new AtomicReference<>() ;
    private final BufferChannel file ;

    static class BlobState {
        boolean hasChanged = false; 
        ByteBuffer $txnBlob ;
        
        BlobState(ByteBuffer bb) {
            setByteBuffer(bb) ;
        }
        void setByteBuffer(ByteBuffer bb) {
            $txnBlob = bb ;
            // Could compare - seems like added complexity.
            hasChanged = true;
        }

        ByteBuffer getByteBuffer() { return $txnBlob ; } 
    }
    
    public TransBlob(ComponentId cid, BufferChannel file) {
        super(cid) ;
        this.file = file ;
        read() ;
    }
    
    private void read() {
        long x = file.size() ;
        ByteBuffer blob = ByteBuffer.allocate((int)x) ;
        int len = file.read(blob) ;
        if ( len != x )
            throw new RuntimeIOException("Short read: "+len+" of "+x) ;
        blob.rewind() ;
        blobRef.set(blob) ; 
    }

    private void write() {
        ByteBuffer blob = blobRef.get();
        blob.rewind() ;
        int x = blob.remaining() ;
        file.truncate(0);
        int len = file.write(blob) ;
        if ( len != x )
            throw new RuntimeIOException("Short write: "+len+" of "+x) ;
        file.sync(); 
        blob.rewind() ;
    }

    /** Set the byte buffer.
     * The byte buffer should not be accessed except by {@link #getBlob}.
     * We avoid a copy in and copy out - we trust the caller.
     * The byte buffer should be configured for read if used with {@link #getString}.
     */
    public void setBlob(ByteBuffer bb) {
        checkWriteTxn();
        getDataState().setByteBuffer(bb);
    }
    
    public ByteBuffer getBlob() {
        if ( isActiveTxn() )
            return getDataState().getByteBuffer() ;
        return blobRef.get() ;
    }

    /**  Set data from string - convenience operation */ 
    public void setString(String dataStr) {
        checkWriteTxn();
        if ( dataStr == null ) {
            setBlob(null);
            return ;
        }

        // Attempt to reuse the write-transaction byte buffer
        // We can't reuse if it's the blobRef (shared by other transactions)
        // but if it's a new to this write transaction buffer we can reuse.
        
        int maxNeeded = dataStr.length()*4 ;
        ByteBuffer bb = getDataState().getByteBuffer() ;
        if ( bb == blobRef.get() )
            bb = ByteBuffer.allocate(maxNeeded) ;
        else if ( bb.capacity() >= maxNeeded )
            bb.clear() ;
        else
            bb = ByteBuffer.allocate(maxNeeded) ;
        Bytes.toByteBuffer(dataStr, bb) ;
        bb.flip() ;
        setBlob(bb);
    }
    
    /**  Get data as string - convenience operation */ 
    public String getString() {
        ByteBuffer bb = getBlob() ;
        if (bb == null )
            return null ;
        int x = bb.position() ;
        String s = Bytes.fromByteBuffer(bb) ;
        bb.position(x) ;
        return s ;
    }

    private boolean recoveryChange = false ; 
    @Override
    public void startRecovery() {
        recoveryChange = false ;
    }

    @Override
    public void recover(ByteBuffer ref) {
        blobRef.set(ref) ;
        recoveryChange = true ;
    }

    @Override
    public void finishRecovery() {
        if ( recoveryChange )
            write() ;
    }

    @Override
    public void cleanStart() { }
    
    @Override
    protected BlobState _begin(ReadWrite readWrite, TxnId txnId) {
        return createState();
    }

    private BlobState createState() {
        ByteBuffer blob = blobRef.get() ;
        // Save reference to ByteBuffer into the transaction state.
        return new BlobState(blob) ;
    }
    
    @Override
    protected BlobState _promote(TxnId txnId, BlobState state) {
        // Our write state is the read state.
        return createState();
    }
    
    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, BlobState state) {
        if ( ! state.hasChanged )
            return null;
        return state.getByteBuffer() ;
    }

    @Override
    protected void _commit(TxnId txnId, BlobState state) {
        if ( ! state.hasChanged )
            return;
        // NB Change reference. 
        blobRef.set(state.getByteBuffer()) ;
        write() ;
    }

    @Override
    protected void _commitEnd(TxnId txnId, BlobState state) {}

    @Override
    protected void _abort(TxnId txnId, BlobState state) {}

    @Override
    protected void _complete(TxnId txnId, BlobState state) {}

    @Override
    protected void _shutdown() {}
    
    @Override
    public String toString()    { return getComponentId().label() ; } 

}

