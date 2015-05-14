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
import org.apache.jena.atlas.io.IO ;

/** Implementation of {@link BinaryDataFile} in memory for testing
 * and development use. Raw performance is not an objective.
 * 
 * <li>This implementation is thread-safe. 
 */
public class BinaryDataFileMem implements BinaryDataFile {

    private int INC = 1024 ;
    private byte[] buffer = null ;
    private boolean readMode ;
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
    synchronized
    public void open() {
        if ( buffer != null )
            throw new RuntimeIOException("Already open") ;
        buffer = new byte[INC] ; 
        writePosition = 0 ;
        readMode = true ;
    }

    @Override
    synchronized
    public boolean isOpen() {
        return buffer != null ;
    }
    
    @Override
    synchronized
    public int read(long posn, byte[] b, int start, int length) {
        checkOpen() ;
        switchToReadMode() ;
        if ( posn >= writePosition ) 
            return -1 ;
        checkRead(posn) ;
        checkStart(start) ;
        int x = Math.min(writePosition-start, length) ;
        System.arraycopy(buffer, (int)posn, b, start, x) ;
        return x ;
    }

    private void checkRead(long posn) {
        if ( posn < 0 )
            IO.exception(String. format("Position out of bounds: %d in [0,%d)", posn, writePosition)) ;
    }

    private void checkStart(long start) {
        if ( start < 0 )
            IO.exception(String. format("Start point out of bounds: %d in [0,%d)", start, writePosition)) ;
    }

    @Override
    synchronized
    public long write(byte[] b, int start, int length) {
        checkOpen() ;
        switchToWriteMode() ;
        ensureBuffer(length);
        System.arraycopy(b, start, buffer, writePosition, length) ;
        long x = writePosition ; 
        writePosition += length ;
        return x ; 
    }

    @Override
    synchronized
    public void truncate(long length) {
        checkOpen() ;
        switchToWriteMode() ;
        writePosition = (int)length ;
    }

    @Override
    synchronized
    public void sync() {
        checkOpen() ;
    }

    @Override
    synchronized
    public void close() {
        if ( ! isOpen() )
            return ;
        buffer = null ;
    }

    @Override
    synchronized
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

