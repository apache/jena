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
import java.util.concurrent.atomic.AtomicReference ;

import com.hp.hpl.jena.query.ReadWrite ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.ComponentIds ;
import org.seaborne.dboe.transaction.txn.TransactionalComponentLifecycle ;
import org.seaborne.dboe.transaction.txn.TxnId ;

/** Manage a single binary (not too large) object.
 * It is written and read from a file in one action, 
 * so changes completely replace the original contents.
 * The whole object is writtern to the journal during prepare.  
 */
public class TransBlob extends TransactionalComponentLifecycle<TransBlob.BlobState> {

    public static ComponentId baseTransBlob = ComponentIds.idBlobBase ; 
    
    // The last commited state.
    // Immutable ByteBuffer.
    // This must be replaced, not updated, by any writer committer
   
    private final AtomicReference<ByteBuffer> blobRef = new AtomicReference<>() ;
    
    static class BlobState {
        ByteBuffer $txnBlob ;
        BlobState(ByteBuffer bb) {
            setByteBuffer(bb) ;
        }
        void setByteBuffer(ByteBuffer bb) {
            $txnBlob = bb ;
        }
        ByteBuffer getByteBuffer() { return $txnBlob ; } 
    }

    private final BufferChannel file ;
    private final ComponentId componentId ;
    
    public TransBlob(ComponentId cid, BufferChannel file) {
        this.componentId = cid ; 
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
        int len = file.write(blob) ;
        if ( len != x )
            throw new RuntimeIOException("Short write: "+len+" of "+x) ;
        file.sync(); 
        blob.rewind() ;
    }

    public void setBlob(ByteBuffer bb) {
        checkWriteTxn();
        getState().setByteBuffer(bb);
    }
    
    public ByteBuffer getBlob() {
        if ( isActiveTxn() )
            return getState().getByteBuffer() ;
        return blobRef.get() ;
    }

    @Override
    public ComponentId getComponentId() {
        return componentId ;
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
    protected BlobState _begin(ReadWrite readWrite, TxnId txnId) {
        ByteBuffer blob = blobRef.get() ;
        // Save reference to ByteBuffer into the transaction state.
        return new BlobState(blob) ;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, BlobState state) {
        return state.getByteBuffer() ;
    }

    @Override
    protected void _commit(TxnId txnId, BlobState state) {
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
    public String toString()    { return componentId.label() ; } 

}

