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

package org.apache.jena.dboe.base.file;

import org.apache.jena.atlas.RuntimeIOException ;

/** Implementation of {@link BinaryDataFile} adding write buffering to another 
 * {@link BinaryDataFile} file such as a {@link BinaryDataFileRandomAccess}.
 *  <li>Thread-safe.
 *  <li>No read buffering provided.
 *  <li>The write buffer is flushed when switching to read.
 */

public class BinaryDataFileWriteBuffered implements BinaryDataFile {
    private static final int SIZE = 128*1024 ;
    private final Object sync = new Object() ; 
    private byte[] buffer ;
    private int bufferLength ;
    private boolean pendingOutput ;
    private final BinaryDataFile other ;
    
    public BinaryDataFileWriteBuffered(BinaryDataFile other) {
        this(other, SIZE) ;
    }
    
    public BinaryDataFileWriteBuffered(BinaryDataFile other, int bufferSize) {
        this.other = other ;
        buffer = new byte[bufferSize] ;
    }
    
    @Override
    public void open() {
        synchronized(sync) {
            other.open() ;
            bufferLength = 0 ;
            pendingOutput = false ;
        }
    }

    @Override
    public void close() {
        synchronized(sync) {
            if ( ! isOpen() )
                return ;
            writeBuffer();
            other.close() ;
        }
    }    

    @Override
    public boolean isOpen() {
        synchronized(sync) {
            return other.isOpen() ;
        }
    }    

    @Override
    public long length() {
        synchronized(sync) {
            return other.length()+bufferLength ;
        }
    }    

    @Override
    public void truncate(long posn) {
        synchronized(sync) {
            checkOpen() ;
            if ( pendingOutput && posn >= other.length() )
                writeBuffer() ;
            other.truncate(posn) ;
        }
    }    

    private void checkOpen() {
        if ( ! other.isOpen() )
            throw new RuntimeIOException("Not open") ;
    }    

    @Override
    public int read(long posn, byte[] b, int start, int length) {
        synchronized(sync) {
            // Overlap with buffered area
            // We flush the write buffer for a read so no need to check.
            checkOpen() ;
            switchToReadMode() ;
            return other.read(posn, b, start, length) ;
        }
    }    

    @Override
    public long write(byte[] buf, int off, int len) {
        synchronized(sync) {
            checkOpen() ;
            switchToWriteMode() ;
            long x = length() ;
        
//        if ( false ) {
//            // No buffering
//            try { file.write(buf, off, len) ; }
//            catch (IOException e) { IO.exception(e); }
//            bufferLength = 0 ;
//            return ;
//        }

            // No room.
            if ( bufferLength + len >= SIZE )
                writeBuffer() ;

            if ( bufferLength + len < SIZE ) {
                // Room to buffer
                System.arraycopy(buf, off, buffer, bufferLength, len);
                bufferLength += len ;
                pendingOutput = true ;
                return x ;
            } 
            // Larger than the buffer space.  Write directly.
            other.write(buf, off, len) ;
            return x ;
        }
    }    

    @Override
    public void sync()  {
        synchronized(sync) {
            writeBuffer() ;
            other.sync(); 
        }
    }
    
    private void writeBuffer() {
        if ( pendingOutput ) {
            pendingOutput = false ;
            other.write(buffer, 0, bufferLength) ;
            bufferLength = 0 ;
        }
    }

    // Inside synchronization
    protected void switchToWriteMode() {
    }

    // Inside synchronization
    protected void switchToReadMode() {
        writeBuffer() ;
    }
}

