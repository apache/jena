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

import java.io.IOException ;
import org.apache.jena.atlas.io.IO ;

/** Implementation of {@link BinaryDataFile} adding write buffering to {@link BinaryDataFileRAF}
 *  Adds write buffering.
 *  Note: No read buffering provided.
 */

// Replaced by BinaryDataFileWriteBuffered.

public class ReadAppendFile extends BinaryDataFileRAF {
    private static final int SIZE = 128*1024 ;
    private byte[] buffer ;
    private int bufferLength ;
    private boolean pendingOutput ;
    
    public ReadAppendFile(String filename) {
        this(filename, SIZE) ;
    }
    
    public ReadAppendFile(String filename, int bufferSize) {
        super(filename) ;
        buffer = new byte[bufferSize] ;
    }
    
    @Override
    public void open() {
        super.open() ;
        bufferLength = 0 ;
        pendingOutput = false ;
    }

    @Override
    public void close() {
        if ( ! isOpen() )
            return ;
        flush$() ;
        super.close() ;
    }

    @Override
    public long length() {
        return super.length()+bufferLength ;
    }
    
    @Override
    public void truncate(long posn) {
        checkOpen() ;
        if ( posn > (writePosition-bufferLength) )
            flush$() ; 
        super.truncate(posn) ;
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
        // Large.  Write directly.
        try { file.write(buf, off, len) ; }
        catch (IOException e) { IO.exception(e); }
    }
    
    @Override
    public void sync()  {
        checkOpen() ;
        flush$() ;
    }
    
    @Override
    protected void flush$() {
        writeBuffer() ;
        super.flush$() ;
    }

    private void writeBuffer() {
        if ( pendingOutput ) {
            try { 
                file.write(buffer, 0, bufferLength) ;
                bufferLength = 0 ;
            } catch (IOException e) { IO.exception(e); }
        }
    }
    
    @Override
    protected void switchToReadMode() {
        if ( ! readMode && pendingOutput )
            writeBuffer() ;
        super.switchToReadMode() ;
    }
}

