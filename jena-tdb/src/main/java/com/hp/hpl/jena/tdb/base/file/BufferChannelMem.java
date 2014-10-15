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

package com.hp.hpl.jena.tdb.base.file ;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.ByteBufferLib ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.StorageException ;

public class BufferChannelMem implements BufferChannel {
    private static Logger log       = LoggerFactory.getLogger(BufferChannelMem.class) ;
    // The "file pointer" is the position of this buffer. 
    private ByteBuffer    bytes ;                                                      
    private String        name ;
    private static int    INIT_SIZE = 1024 ;
    private static int    INC_SIZE  = 1024 ;

    private final boolean TRACKING ;

    static public BufferChannel create() {
        return new BufferChannelMem("unnamed") ;
    }

    static public BufferChannel create(String name) {
        return new BufferChannelMem(name) ;
    }

    private BufferChannelMem() {
        // Unitialized blank.
        TRACKING = false ;
    }

    private BufferChannelMem(String name) {
        bytes = ByteBuffer.allocate(1024) ;
        bytes.limit(0) ;
        this.name = name ;
        TRACKING = false ;
        // Debugging : pick a filename.
        // TRACKING = name.endsWith("prefixes.dat") ;
    }

    @Override
    synchronized public BufferChannel duplicate() {
        BufferChannelMem chan = new BufferChannelMem() ;
        int x = bytes.position() ;
        bytes.rewind() ;
        chan.bytes = bytes.slice() ;
        chan.bytes.position(0) ;
        bytes.position(x) ;
        return chan ;
    }

    @Override
    synchronized public long position() {
        checkIfClosed() ;
        return bytes.position() ;
    }

    @Override
    synchronized public void position(long pos) {
        checkIfClosed() ;
        if ( pos < 0 || pos > bytes.capacity() )
            throw new StorageException("Out of range: " + pos) ;
        bytes.position((int)pos) ;
    }

    @Override
    synchronized public int read(ByteBuffer buffer) {
        checkIfClosed() ;
        if ( TRACKING )
            log("read<<[" + buffer.capacity() + "]") ;

        int x = bytes.position() ;

        int len = buffer.limit() - buffer.position() ;
        if ( len > bytes.remaining() )
            len = bytes.remaining() ;
        // Copy out, moving the position of the bytes of stroage.
        for (int i = 0; i < len; i++) {
            byte b = bytes.get() ;
            buffer.put(b) ;
        }
        if ( TRACKING )
            log("read>>") ;
        return len ;
    }

    @Override
    synchronized public int read(ByteBuffer buffer, long loc) {
        checkIfClosed() ;
        if ( TRACKING )
            log("read<<@" + loc) ;
        if ( loc < 0 || loc > bytes.limit() )
            throw new StorageException("Out of range(" + name + "[read]): " + loc + " [0," + bytes.limit() + ")") ;
        if ( loc == bytes.limit() )
            log.warn("At the limit(" + name + "[read]): " + loc) ;
        int x = bytes.position() ;
        bytes.position((int)loc) ;
        int len = read(buffer) ;
        bytes.position(x) ;
        if ( TRACKING )
            log("read>>@" + loc) ;
        return len ;
    }

    @Override
    synchronized public int write(ByteBuffer buffer) {
        checkIfClosed() ;
        if ( TRACKING )
            log("write<<[" + buffer.capacity() + "]") ;
        int len = buffer.limit() - buffer.position() ;
        int posn = bytes.position() ;

        int freespace = bytes.capacity() - bytes.position() ;

        if ( len > freespace ) {
            int inc = len - freespace ;
            inc += INC_SIZE ;
            ByteBuffer bb2 = ByteBuffer.allocate(bytes.capacity() + inc) ;
            bytes.position(0) ;
            // Copy contents; make written bytes area the same as before.
            bb2.put(bytes) ;
            bb2.limit(bytes.limit()) ; // limit is used as the end of active
                                       // bytes.
            bb2.position(posn) ;
            bytes = bb2 ;
        }

        if ( bytes.limit() < posn + len )
            bytes.limit(posn + len) ;

        bytes.put(buffer) ;

        if ( TRACKING )
            log("write>>") ;
        return len ;
    }

    // Invert : write(ByteBuffer) = write(ByteBuffer,posn)
    @Override
    synchronized public int write(ByteBuffer buffer, long loc) {
        checkIfClosed() ;
        if ( TRACKING )
            log("write<<@" + loc) ;
        if ( loc < 0 || loc > bytes.limit() )
            // Can write at loc = bytes()
            throw new StorageException("Out of range(" + name + "[write]): " + loc + " [0," + bytes.limit() + ")") ;
        int x = bytes.position() ;
        bytes.position((int)loc) ;
        int len = write(buffer) ;
        bytes.position(x) ;
        if ( TRACKING )
            log("write>>@" + loc) ;
        return len ;
    }

    @Override
    synchronized public void truncate(long size) {
        checkIfClosed() ;
        int x = (int)size ;
        if ( x < 0 )
            throw new StorageException("Out of range: " + size) ;
        if ( x > bytes.limit() )
            return ;

        if ( bytes.position() > x )
            bytes.position(x) ;
        bytes.limit(x) ;
    }

    @Override
    synchronized public long size() {
        checkIfClosed() ;
        return bytes.limit() ;
    }

    @Override
    synchronized public boolean isEmpty() {
        checkIfClosed() ;
        return size() == 0 ;
    }

    @Override
    synchronized public void sync() {
        checkIfClosed() ;
    }

    @Override
    synchronized public void close() {
        checkIfClosed() ;
        bytes = null ;
    }

    private void checkIfClosed() {
        if ( bytes == null )
            throw new StorageException("Closed: " + name) ;
    }

    @Override
    synchronized public String getLabel() {
        return name ;
    }

    @Override
    synchronized public String toString() {
        return name ;
    }

    @Override
    public String getFilename() {
        return null ;
    }

    private void log(String op) {
        if ( TRACKING ) {
            String msg = op + " [" + name + "] " + ByteBufferLib.details(bytes) ;
            log.debug(msg) ;
        }
    }
}
