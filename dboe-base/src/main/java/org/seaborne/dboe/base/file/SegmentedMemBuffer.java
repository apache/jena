/*
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

import org.apache.jena.atlas.io.IO ;

/** A segmented, expanding buffer of bytes.
 *  This class does not copy the underlying bytes when the file grows.
 *  Hence, the performance is more predictable.
 *  (Resizing a fixed size buffer is a copy c.f. performance issues with
 *  {@code ArrayList} or {@code ByteArrayOutputStream}
 *  as they go from small to large. 
 */
public class SegmentedMemBuffer {
    // See java.nio.channels.FileChannel
    private static int DFT_CHUNK = 1024*1024 ;
    private static int DFT_SEGMENTS = 1000 ;
    private final int CHUNK ;
    // The file pointer and marker of the allocated end point.
    private int endSegment = 0 ;
    private int endOffset = 0 ;
    private long fileEnd = 0 ;
    private List<byte[]> space = null ;
    private boolean isOpen ;

    public SegmentedMemBuffer() { this(DFT_CHUNK) ; }
    
    public SegmentedMemBuffer(int chunk) { 
        this.CHUNK = chunk ;
        space = new ArrayList<>(DFT_SEGMENTS) ;
        // "Auto open"
        isOpen = true ;
    }

    public void open() {
        isOpen = true ;
//        if ( storage != null )
//            throw new RuntimeIOException("Already open") ;
//        storage = new ArrayList<>(DFT_SEGMENTS) ;
    }
    
    public boolean isOpen() {
        return space != null ;
    }
    
    public int read(long posn, byte[] b) {
        return read(posn, b, 0, b.length) ; 
    }
    
    public int read(long posn, byte[] b, int start, int length) {
        checkOpen() ;
        long x = fileEnd ;
        if ( posn >= fileEnd ) 
            return -1 ;
        checkRead(posn) ;
        checkStart(start) ;
        
        int len = length ;
        if ( posn+length > x )
            len = (int)(x-posn) ;
        arrayCopyOut(space, posn, b, start, len) ;
        return len ;
    }

    public void write(long posn, byte[] b ) {
        write(posn, b, 0, b.length) ; 
    }

    public void write(long posn, byte[] b, int start, int length) {
        checkOpen() ;
        arrayCopyIn(b, start, space, fileEnd, length) ;   // append.
    }

    public void truncate(long length) {
        if ( length < 0 )
            IO.exception(String.format("truncate: bad length : %d", length)) ;
        checkOpen() ;
        fileEnd = Math.min(fileEnd, length) ;
        // clear above?
    }

    public void sync() {
        checkOpen() ;
    }

    public void close() {
        if ( ! isOpen() )
            return ;
        isOpen = false ;
        space.clear() ;
        space = null ;
    }

    public long length() {
        return fileEnd ;
    }

    private void checkOpen() {
        if ( ! isOpen )
            IO.exception("Not open") ;
    }

    private void checkRead(long posn) {
        if ( posn < 0 )
            IO.exception(String.format("Position out of bounds: %d in [0,%d)", posn, fileEnd)) ;
    }

    private void checkStart(long start) {
        if ( start < 0 )
            IO.exception(String.format("Start point out of bounds: %d in [0,%d)", start, fileEnd)) ;
    }
    
    private int getSegment(long posn) { return (int)(posn / CHUNK) ; }

    private int getOffset(long posn) { return (int) (posn % CHUNK) ; }

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
                byte[] buffer = new byte[CHUNK] ;
                space.add(buffer) ;
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
}

