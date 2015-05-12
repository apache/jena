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

import org.apache.thrift.transport.TTransport ;
import tdbdev.binarydatafile.BinaryDataFile ;

/** A file transport that support random access read and
 *  buffered append write.
 *  <p>
 *  Adapter TTransport -&gt; BinaryDataFile
 */
public class TReadAppendFileTransport extends TTransport {
    private BinaryDataFile file ;
    
    public TReadAppendFileTransport(BinaryDataFile file) {
        this.file = file ;
    }
    
    @Override
    public boolean isOpen() {
        return file.isOpen() ;
    }

    @Override
    public void open() {
        file.open() ; 
    }

    @Override
    public void close() {
        file.close() ; 
    }
    
    public void position(long posn) {
        file.position(posn); 
    }

    public long position() {
        return file.position(); 
    }

    public void truncate(long posn) {
        file.truncate(posn); 
    }

    public BinaryDataFile getBinaryDataFile() { return file ; }

    //
//    public long getWriteLocation() {
//        return file.
//    }
//
//    public long getFileLength() {
//        try { return file.length()+bufferLength ; }
//        catch (IOException e) { IO.exception(e); return -1 ; }
//    }
//
//    public long getReadLocation() {
//        return readPosn ;
//    }
//
//    public long getFilePointer() {
//        try { return file.getFilePointer() ; }
//        catch (IOException e) { IO.exception(e); return -1L ; }
//    }
//    
//    public void seek(long posn) {
//        try {
//            if ( ! readMode )
//                flush$() ;
//            file.seek(posn) ;
//            readPosn = posn ;
//        }
//        catch (IOException ex) { IO.exception(ex) ; }
//    }
//    
//    public void truncate(long posn) {
//        if ( readMode )
//            // Avoid seek if already writing.
//            setForWriting() ;
//        else if ( pendingOutput )
//            flush$() ;
//        // Now write mode.
//        try { file.getChannel().truncate(posn) ; }
//        catch (IOException ex) { IO.exception(ex); } 
//        readPosn = Math.min(readPosn,  posn) ;
//        if ( writePosn > posn ) {
//            writePosn = posn ;
//            //Need the seek?
//            // Yes.
//            try { file.seek(posn) ; }
//            catch (IOException ex) { IO.exception(ex) ; }
//        }
//    }

    @Override
    public int read(byte[] buf, int off, int len) {
        return file.read(buf, off, len) ;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        file.write(buf, off, len) ;
    }
    
    @Override
    public void flush()  {
        file.sync(); 
    }
}

