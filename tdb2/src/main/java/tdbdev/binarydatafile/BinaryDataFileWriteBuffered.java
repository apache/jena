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

package tdbdev.binarydatafile;

import org.apache.jena.atlas.RuntimeIOException ;

/** Implementation of {@link BinaryDataFile} adding write buffering to {@link BinaryDataFileRAF}
 *  Adds write buffering.
 *  Note: No read buffering provided.
 */

public class BinaryDataFileWriteBuffered implements BinaryDataFile {
    private static final int SIZE = 128*1024 ;
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
        other.open() ;
        bufferLength = 0 ;
        pendingOutput = false ;
    }

    @Override
    public void close() {
        if ( ! isOpen() )
            return ;
        writeBuffer();
        other.close() ;
    }

    @Override
    public boolean isOpen() {
        return other.isOpen() ;
    }

    @Override
    public long position() {
        return other.position() ;
    }

    @Override
    public void position(long posn) {
        other.position(posn) ;
    }

    @Override
    public long length() {
        return other.length()+bufferLength ;
    }
    
    @Override
    public void truncate(long posn) {
        checkOpen() ;
        if ( pendingOutput && posn >= other.length() )
            writeBuffer() ;
        other.truncate(posn) ;
    }

    private void checkOpen() {
        if ( ! other.isOpen() )
            throw new RuntimeIOException("Not open") ;
    }

    @Override
    public int read(byte[] b, int start, int length) {
        checkOpen() ;
        switchToReadMode() ;
        return other.read(b, start, length) ;
    }
    
    @Override
    public void write(byte[] buf, int off, int len) {
        checkOpen() ;
        switchToWriteMode() ;
        
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
            return ;
        } 
        // Larger than tehbuffer space.  Write directly.
        other.write(buf, off, len) ;
    }
    
    @Override
    public void sync()  {
        writeBuffer() ;
        other.sync(); 
    }
    
    private void writeBuffer() {
        if ( pendingOutput ) {
                other.write(buffer, 0, bufferLength) ;
                bufferLength = 0 ;
        }
    }
    
    protected void switchToWriteMode() {
    }
    
    protected void switchToReadMode() {
        writeBuffer() ;
    }
}

