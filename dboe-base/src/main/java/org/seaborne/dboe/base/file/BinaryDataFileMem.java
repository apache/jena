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

package org.seaborne.dboe.base.file;

import org.apache.jena.atlas.RuntimeIOException ;

/** Implementation of {@link BinaryDataFile} in memory for testing.
 */
public class BinaryDataFileMem implements BinaryDataFile {

    private int INC = 1024 ;
    private byte[] buffer = null ;
    private boolean readMode ;
    private int readPosition ;
    private int writePosition ;
    
    private void ensureBuffer(int len) {
        if ( writePosition+len > buffer.length ) {
            byte[] buffer2 = new byte[buffer.length+INC] ;
            System.arraycopy(buffer, 0, buffer2, 0, writePosition) ;
            buffer = buffer2 ;
        }
    }
    
    public BinaryDataFileMem() { }
    
    @Override
    public void open() {
        if ( buffer != null )
            throw new RuntimeIOException("Already open") ;
        buffer = new byte[INC] ; 
        writePosition = 0 ;
        readPosition = 0 ;
        readMode = true ;
    }

    @Override
    public boolean isOpen() {
        return buffer != null ;
    }
    
    @Override
    public int read(byte[] b, int start, int length) {
        checkOpen() ;
        switchToReadMode() ;
        int x = Math.min(length, writePosition-readPosition) ;
        if ( readPosition >= writePosition )
            return -1 ;
        System.arraycopy(buffer, readPosition, b, start, x) ;
        readPosition += x ;
        return x ;
    }

    @Override
    public void write(byte[] b, int start, int length) {
        checkOpen() ;
        switchToWriteMode() ;
        ensureBuffer(length);
        System.arraycopy(b, start, buffer, writePosition, length) ;
        writePosition += length ;
    }

    @Override
    public long position() {
        checkOpen() ;
        return readPosition ; 
    }

    @Override
    public void position(long posn) {
        checkOpen() ;
        readPosition = (int)posn ;
        if ( readPosition < 0 )
            readPosition = 0 ;
    }

    @Override
    public void truncate(long length) {
        checkOpen() ;
        switchToWriteMode() ;
        writePosition = (int)length ;
    }

    @Override
    public void sync() {
        checkOpen() ;
    }

    @Override
    public void close() {
        if ( ! isOpen() )
            return ;
        buffer = null ;
    }

    @Override
    public long length() {
        return writePosition ;
    }
    
    private void switchToReadMode() {
        readMode = true ;
    }

    private void switchToWriteMode() {
        readMode = false ;
    }

    private void checkOpen() {
        if ( ! isOpen() ) 
            throw new RuntimeIOException("Not open") ;
    }
}

