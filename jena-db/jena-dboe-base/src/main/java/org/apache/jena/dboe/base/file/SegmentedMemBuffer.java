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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.ByteBufferLib;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A segmented, expanding buffer of bytes.
 *  This class does not copy the underlying bytes when the file grows.
 *  Hence, the performance is more predictable.
 *  (Resizing a fixed size buffer is a copy c.f. performance issues with
 *  {@code ArrayList} or {@code ByteArrayOutputStream}
 *  as they go from small to large.
 */
public class SegmentedMemBuffer {
    private static Logger log = LoggerFactory.getLogger(SegmentedMemBuffer.class);

    // See java.nio.channels.FileChannel
    private static int DFT_CHUNK = 1024*1024;
    private static int DFT_SEGMENTS = 1000;
    private final int CHUNK;
    // The file pointer and marker of the allocated end point.
    private long dataLength = 0;
    private List<byte[]> space = null;
    private boolean isOpen;
    private final boolean TRACKING = false;

    public SegmentedMemBuffer() { this(DFT_CHUNK); }

    public SegmentedMemBuffer(int chunk) {
        this.CHUNK = chunk;
        space = new ArrayList<>(DFT_SEGMENTS);
        // "Auto open"
        isOpen = true;
    }

    public void open() {
        isOpen = true;
//        if ( storage != null )
//            throw new RuntimeIOException("Already open");
//        storage = new ArrayList<>(DFT_SEGMENTS);
    }

    public boolean isOpen() {
        return space != null;
    }

    public int read(long posn, ByteBuffer bb) {
        if ( TRACKING )
            log("read<<%s", ByteBufferLib.details(bb));
        checkOpen();
        checkPosition(posn);
        int len = bb.remaining();
        if ( posn+dataLength > dataLength )
            len = (int)(dataLength-posn);
        arrayCopyOut(space, posn, bb);
        return len;
    }

    public int read(long posn, byte[] b) {
        if ( TRACKING )
            log("read<<[%d]", b.length);
        return read$(posn, b, 0, b.length);
    }

    public int read(long posn, byte[] b, int start, int length) {
        if ( TRACKING )
            log("read<<[%d],%s,%s", b.length, start, length);
        return read$(posn, b, start, length);
    }

    private int read$(long posn, byte[] b, int start, int length) {
        checkOpen();
        if ( posn >= dataLength )
            return -1;
        checkPosition(posn);
        if ( length == 0 )
            return 0;
        checkByteArray(b, start, length);
        int len = length;
        if ( posn+length > dataLength )
            len = (int)(dataLength-posn);
        arrayCopyOut(space, posn, b, start, len);
        return len;
    }

    public void write(long posn, ByteBuffer bb ) {
        if ( TRACKING )
            log("read<<%s", ByteBufferLib.details(bb));
        checkOpen();
        if ( posn != dataLength )
            checkPosition(posn);
        arrayCopyIn(bb, space, posn);
    }

    public void write(long posn, byte[] b ) {
        if ( TRACKING )
            log("read<<[%d]", b.length);
        write$(posn, b, 0, b.length);
    }

    public void write(long posn, byte[] b, int start, int length) {
        if ( TRACKING )
            log("read<<[%d],%d,%d", b.length, start, length);
        write$(posn, b, start, length);
    }

    private void write$(long posn, byte[] b, int start, int length) {
        checkOpen();
        checkPosition(posn);
        if ( length == 0 )
            return;
        checkByteArray(b,start,length);
        arrayCopyIn(b, start, space, dataLength, length);
    }

    public void truncate(long length) {
        if ( TRACKING )
            log("truncate(%d)", length);
        if ( length < 0 )
            IO.exception(String.format("truncate: bad length : %d", length));
        checkOpen();
        dataLength = Math.min(dataLength, length);
        // clear above?
    }

    public void sync() {
        checkOpen();
    }

    public void close() {
        if ( ! isOpen() )
            return;
        isOpen = false;
        space.clear();
        space = null;
    }

    public long length() {
        return dataLength;
    }

    private void checkOpen() {
        if ( ! isOpen )
            IO.exception("Not open");
    }

    private void checkPosition(long posn) {
        // Allows posn to be exactly the byte beyond the end
        if ( posn < 0 || posn > dataLength )
            IO.exception(String.format("Position out of bounds: %d in [0,%d]", posn, dataLength));
    }

    private void checkByteArray(byte[] b, int start, int length) {
        if ( start < 0 || start >= b.length )
            IO.exception(String.format("Start point out of bounds of byte array: %d in [0,%d)", start, b.length));
        if ( length < 0 || start+length > b.length )
            IO.exception(String.format("Start/length out of bounds of byte array: %d/%d in [0,%d)", start, length, b.length));
    }

    private int getSegment(long posn) { return (int)(posn / CHUNK); }

    private int getOffset(long posn) { return (int) (posn % CHUNK); }

    // c.f. System.arrayCopy
    private void arrayCopyIn(byte[] src, final int srcStart, List<byte[]> dest, final long destStart, final int length) {
        if ( length == 0 )
            return;
        int len = length;
        int srcPosn = srcStart;
        int seg = getSegment(destStart);
        int offset = getOffset(destStart);
        while( len > 0 ) {
            int z = Math.min(len, CHUNK-offset);
            // Beyond end of file?
            if ( seg >= dest.size() ) {
                byte[] buffer = new byte[CHUNK];
                space.add(buffer);
            }
            System.arraycopy(src, srcPosn, dest.get(seg), offset, z);
            srcPosn += z;
            len -= z;
            seg += 1;
            offset += z;
            offset %= CHUNK;
        }
        dataLength = Math.max(dataLength, destStart+length);
    }

    private void arrayCopyOut(List<byte[]> src, final long srcStart, byte[] dest, final int destStart, final int length) {
        int len = length;
        len = Math.min(len, (int)(dataLength-srcStart));
        int dstPosn = destStart;
        int seg = getSegment(srcStart);
        int offset = getOffset(srcStart);
        while( len > 0 ) {
            int z = Math.min(len, CHUNK-offset);
            System.arraycopy(src.get(seg), offset, dest, dstPosn, z);
            dstPosn += z;
            len -= z;
            seg += 1;
            offset += z;
            offset %= CHUNK;
        }
    }

    private void arrayCopyIn(ByteBuffer bb, List<byte[]> dest, long destStart) {
        if ( bb.remaining() == 0 )
            return;
        int length = bb.remaining();
        int len = bb.remaining();
        int srcPosn = bb.position();
        int seg = getSegment(destStart);
        int offset = getOffset(destStart);
        while( len > 0 ) {
            int z = Math.min(len, CHUNK-offset);
            // Beyond end of file?
            if ( seg >= dest.size() ) {
                byte[] buffer = new byte[CHUNK];
                space.add(buffer);
            }
            byte[] bytes = dest.get(seg);
            bb.get(bytes, offset, z);
            srcPosn += z;
            len -= z;
            seg += 1;
            offset += z;
            offset %= CHUNK;
        }
        dataLength = Math.max(dataLength, destStart+length);
    }

    private void arrayCopyOut(List<byte[]> src, long srcStart, ByteBuffer bb) {
        int len = bb.remaining();
        len = Math.min(len, (int)(dataLength-srcStart));
        int dstPosn = bb.position();
        int seg = getSegment(srcStart);
        int offset = getOffset(srcStart);
        while( len > 0 ) {
            int z = Math.min(len, CHUNK-offset);
            byte[] bytes = src.get(seg);
            bb.put(bytes, offset, z);
            dstPosn += z;
            len -= z;
            seg += 1;
            offset += z;
            offset %= CHUNK;
        }
    }

    private void log(String fmt, Object... args) {
        if ( TRACKING )
            FmtLog.debug(log, fmt, args);
    }

}

