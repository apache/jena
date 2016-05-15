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

package dev;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionalComponentLifecycle ;
import org.seaborne.dboe.transaction.txn.TxnId ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TransactionalComponentFile extends TransactionalComponentLifecycle<TransactionalComponentFile.FileState> {

    private static Logger LOG = LoggerFactory.getLogger(TransactionalComponentFile.class) ; 
    
    // Implemented as two files.
    // The component state is the current file.
    
    // The file state.
    static class FileState {
        // The base file name + ".1", ".2" etc.
        String filename ;
    }

    private final String baseFilename;

    
    // The readers see one file, the writer the other.
    // All a writer can do is rewrite the file.
    
    // == a BinaryDataFile with rewrite only => new interface.
    // ==> Writer gets an OutputStream.
    
    // One of:
    // 1/ New interface : Interface is an OutputStream / InputStream
    // 2/ BinaryDataFile but unsupported positionals.
    // 3/ Super interface of BinaryDataFile
    
    // Or even a BinaryDataFile, append-only but with a starting point.
    // i.e. keeps history, needs GC
    
    public TransactionalComponentFile(ComponentId id, String baseFilename) {
        super(id);
        this.baseFilename = baseFilename ;
    }

    @Override
    public void startRecovery() {}

    @Override
    public void recover(ByteBuffer ref) {
        String filename = fromByteBuffer(ref) ;
    }

    // To ByteBufferLib?
    static String fromByteBuffer(ByteBuffer bb) {
        try {
            // InStreamUTF8
            // BlockUTF8
            int x = bb.remaining() ;
            byte[] b = new byte[x] ;
            bb.get(b) ;
            return StrUtils.fromUTF8bytes(b) ;
        } catch (RuntimeException ex) {
            LOG.error("Failed to build a string: "+ex.getMessage(), ex);
            throw ex ; 
            //return null ;
        }
    }
    
    // To ByteBufferLib?
    static void toByteBuffer(ByteBuffer bb, String str) {
        try {
            byte[] b = StrUtils.asUTF8bytes(str) ;
            bb.put(b) ;
        } catch (RuntimeException ex) {
            LOG.error("Failed to put a string: "+ex.getMessage(), ex);
            throw ex ; 
        }
    }

    @Override
    public void finishRecovery() {}

    @Override
    public void cleanStart() {}

    @Override
    protected FileState _begin(ReadWrite readWrite, TxnId txnId) {
        return null;
    }

    @Override
    protected boolean _promote(TxnId txnId, FileState state) {
        return false;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, FileState state) {
        return null;
    }

    @Override
    protected void _commit(TxnId txnId, FileState state) {}

    @Override
    protected void _commitEnd(TxnId txnId, FileState state) {}

    @Override
    protected void _abort(TxnId txnId, FileState state) {}

    @Override
    protected void _complete(TxnId txnId, FileState state) {}

    @Override
    protected void _shutdown() {} 
    
}
