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

package org.seaborne.tdb2.store.nodetable;

import java.io.IOException ;
import java.io.RandomAccessFile ;

import org.apache.jena.atlas.io.IO ;
import org.apache.thrift.transport.TTransport ;
import org.apache.thrift.transport.TTransportException ;
import org.seaborne.dboe.DBOpEnvException ;

/** A file transport that support random access read and
 *  buffered append write.s
 */
public class TReadAppendFileTransport extends TTransport {

    private static final int SIZE = 100 ; //16*1024 ;
    private byte[] buffer = new byte[SIZE] ;
    private int bufferLength = 0 ;
    
    private boolean pendingOutput = false ;
    private boolean readMode = true ;
    private RandomAccessFile file ;
    private long writePosn = -1 ;
    private long readPosn = -1 ;
    private final String filename ;
    
    public TReadAppendFileTransport(String filename) {
        this.filename = filename ;
    }
    
    @Override
    public boolean isOpen() {
        return file != null ;
    }

    @Override
    public void open() throws TTransportException {
        try {
            file = new RandomAccessFile(filename, "rw") ;
            readPosn = 0 ;
            writePosn = file.length() ;
            // Initially reading.
            readMode = true ;
        } 
        catch (IOException ex) { IO.exception(ex) ; }
    }

    @Override
    public void close() {
        try { flush() ; }
        catch (TTransportException e) { throw new DBOpEnvException(e) ; }
        IO.close(file) ;
        file = null ;
    }

    public long getWriteLocation() {
        return writePosn ;
    }

    public long getFileLength() {
        try { return file.length()+bufferLength ; }
        catch (IOException e) { IO.exception(e); return -1 ; }
    }

    public long getReadLocation() {
        return readPosn ;
    }

    public long getFilePointer() {
        try { return file.getFilePointer() ; }
        catch (IOException e) { IO.exception(e); return -1L ; }
    }
    
    public void seek(long posn) {
        try {
            if ( ! readMode )
                flush$() ;
            file.seek(posn) ;
            readPosn = posn ;
        }
        catch (IOException ex) { IO.exception(ex) ; }
    }
    
    public void truncate(long posn) {
        if ( readMode )
            // Avoid seek if already writing.
            setForWriting() ;
        else if ( pendingOutput )
            flush$() ;
        // Now write mode.
        try { file.getChannel().truncate(posn) ; }
        catch (IOException ex) { IO.exception(ex); } 
        readPosn = Math.min(readPosn,  posn) ;
        if ( writePosn > posn ) {
            writePosn = posn ;
            //Need the seek?
            // Yes.
            try { file.seek(posn) ; }
            catch (IOException ex) { IO.exception(ex) ; }
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        checkOpen() ;
        setForReading() ;
        try { 
            long z = file.getFilePointer() ;
            // Buffer?
            int x = file.read(buf, off, len) ;
            readPosn += x ;
            return x ;
        } 
        catch (IOException e) { IO.exception(e); return -1 ; }
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        checkOpen() ;
        setForWriting() ;
        writePosn += len ;
        
        if ( false ) {
            // No buffering
            try { file.write(buf, off, len) ; }
            catch (IOException e) { IO.exception(e); }
            bufferLength = 0 ;
            return ;
        }
        
        // No room.
        if ( bufferLength + len >= SIZE )
            flush$() ;

        if ( bufferLength + len < SIZE ) {
            System.arraycopy(buf, off, buffer, bufferLength, len);
            bufferLength += len ;
            pendingOutput = true ;
            return ;
        } 
        // Large.  write directly.
        try { file.write(buf, off, len) ; }
        catch (IOException e) { IO.exception(e); }
        bufferLength = 0 ;
    }
    
    @Override
    public void flush()  throws TTransportException {
        checkOpen() ;
        flush$() ;
    }
    
    private void flush$() {
        // Already write mode.
        try { file.write(buffer, 0, bufferLength) ; }
        catch (IOException e) { IO.exception(e); }
        bufferLength = 0 ;
      }

    private void setForReading() {
        if ( ! readMode ) {
            if ( pendingOutput )
                flush$() ;
            try { file.seek(readPosn); }
            catch (IOException e) { IO.exception(e); }
        }
        readMode = true ;
    }

    private void setForWriting() {
        if ( readMode ) {
            readMode = false ;
            try { file.seek(writePosn); }
            catch (IOException e) { IO.exception(e); }
        }
    }

    private void checkOpen() throws TTransportException  {
        if ( ! isOpen() ) 
            throw new TTransportException("Not open") ;
    }
    
}

