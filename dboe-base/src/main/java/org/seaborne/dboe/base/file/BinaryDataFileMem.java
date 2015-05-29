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

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.atlas.io.IO ;

/** Implementation of {@link BinaryDataFile} in memory for testing
 * and development use. Raw performance is not an objective.
 * 
 * <li>This implementation is thread-safe. 
 */
public class BinaryDataFileMem implements BinaryDataFile {

    private static int CHUNK = 1024*1024 ;
    private boolean readMode ;
    
    // The file pointer and marker of the allocated end point.
    private int endSegment = 0 ; 
    private int endOffset = 0 ;
    private long fileEnd = 0 ;
    
    private List<byte[]> storage = null ;
    
    private static int getSegment(long posn) { return (int)(posn / CHUNK) ; } 
    private static int getOffset(long posn) { return (int) (posn % CHUNK) ; }
    
    // Like system.arrayCopy except in/out of List<byte[]> 
    private void arrayCopyIn(byte[] src, final int srcStart, List<byte[]> dest, final long destStart, final int length) {
        // Check destPos <= file length 
        if ( length == 0 )
            return ;
        int len = length ;
        int srcPosn = srcStart ;
        int seg = getSegment(destStart) ;
        int offset = getOffset(destStart) ;
        while( len > 0 ) {
            int z = Math.min(len, CHUNK-offset) ;
            // Beyond end of file?
            if ( seg >= dest.size() ) {
                allocate(dest) ;
            }
            System.arraycopy(src, srcPosn, dest.get(seg), offset, z);
            srcPosn += z ;
            len -= z ;
            seg += 1 ;
            offset += z ;
            offset %= CHUNK ;
        }
        seg -= 1 ;
        if ( seg == dest.size()-1 ) {
            endSegment = seg ;
            endOffset = offset ;
        }
        fileEnd = Math.max(fileEnd, destStart+length) ;
    }
                                        
    private void arrayCopyOut(List<byte[]> src, final long srcStart, byte[] dest, final int destStart, final int length) {
        // Check srcPos <= file length
    
        int len = length ;
        len = Math.min(len, (int)(fileEnd-srcStart)) ;
        int dstPosn = destStart ;
        int seg = getSegment(srcStart) ;
        int offset = getOffset(srcStart) ;
        while( len > 0 ) {
            int z = Math.min(len, CHUNK-offset) ;
            System.arraycopy(src.get(seg), offset, dest, dstPosn, z);
            dstPosn += z ;
            len -= z ;
            seg += 1 ;
            offset += z ;
            offset %= CHUNK ;
        }
    }
                         
    private static void allocate(List<byte[]> space) {
        byte[] buffer = new byte[CHUNK] ;
        space.add(buffer) ;
    }
    
    public BinaryDataFileMem() { }
    
    @Override
    synchronized
    public void open() {
        if ( storage != null )
            throw new RuntimeIOException("Already open") ;
        storage = new ArrayList<>(1000) ;
        readMode = true ;
    }

    @Override
    synchronized
    public boolean isOpen() {
        return storage != null ;
    }
    
    @Override
    synchronized
    public int read(long posn, byte[] b, int start, int length) {
        checkOpen() ;
        switchToReadMode() ;
        if ( posn >= fileEnd ) 
            return -1 ;
        checkRead(posn) ;
        checkStart(start) ;
        
        int len = length ;
        
        if ( posn+length > fileEnd )
            len = (int)(fileEnd-posn) ;
        
        arrayCopyOut(storage, posn, b, start, len) ;
        return len ;
    }

    private void checkRead(long posn) {
        if ( posn < 0 )
            IO.exception(String.format("Position out of bounds: %d in [0,%d)", posn, fileEnd)) ;
    }

    private void checkStart(long start) {
        if ( start < 0 )
            IO.exception(String.format("Start point out of bounds: %d in [0,%d)", start, fileEnd)) ;
    }

    @Override
    synchronized
    public long write(byte[] b, int start, int length) {
        checkOpen() ;
        switchToWriteMode() ;
        long x = fileEnd ;
        arrayCopyIn(b, start, storage, fileEnd, length) ;   // append.
//        try {
//            System.arraycopy(b, start, buffer, writePosition, length) ;
//        } catch (ArrayIndexOutOfBoundsException ex) {
//            // Should not happen, but ...
//            FmtLog.error(BinaryDataFileMem.class,
//                         "Bad arraycopy(src[%d], %d, dest[%d], $d, %d)",
//                         b.length, start, buffer.length, writePosition, length) ;
//        }
        return x ; 
    }

    @Override
    synchronized
    public void truncate(long length) {
        if ( length < 0 )
            IO.exception(String.format("truncate: bad length : %d", length)) ;
        checkOpen() ;
        switchToWriteMode() ;
        fileEnd = Math.max(fileEnd, length) ;
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
        storage = null ;
    }

    @Override
    synchronized
    public long length() {
        return fileEnd ;
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

