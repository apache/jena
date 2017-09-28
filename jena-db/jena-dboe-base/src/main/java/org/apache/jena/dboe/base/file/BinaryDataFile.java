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

import java.io.RandomAccessFile ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;

/** An append-only, read-anywhere, binary file.
 * A {@code BinaryDataFile} does not record the length and assumes the
 * entries are self-defining.
 * 
 *  @see RandomAccessFile
 */
public interface BinaryDataFile extends Closeable, Sync {
    // What about java.nio.channels.FileChannel?
    // On OpenJDK,  RandomAccessFile and FileChannelImpl both dive into native code.
    //  
    // The choice seems to come down to ByteBuffers vs byte[]
    // which in turn is a small/large data (scattered data)
    // issue.  We are currently expecting small(er) I/O so byte[]
    // and being like Thrift is better.

    // byte[] vs ByteBuffer
    
    /** Open the file */
    public void open() ;
    
    /** Is it open? */ 
    public boolean isOpen() ;
    
    /** Read into a byte array, returning the number of bytes read. 
     * Reads are at an absolute position and a read is atomic/thread-safe.
     * 
     * @param posn Location of the read operation.
     * @param b byte array
     * 
     * @return The number of bytes read
     */
    public default int read(long posn, byte b[]) {
        return read(posn, b, 0, b.length);
    }

    /** Read into a byte array, returning the number of bytes read.
     * Reads are at an absolute position and a read is atomic/thread-safe.
     * 
     * @param posn Location of the read operation.
     * @param b
     * @param start of bytesarray to read into
     * @param length Maximum number of bytes to read. 
     * @return The number of bytes read
     */
 
    public int read(long posn, byte b[], int start, int length) ;

    /** Write bytes - bytes are always written to the end of the file.
     * Return the location where the write started.
     */ 
    public default long write(byte b[]) {
        return write(b, 0, b.length) ;
    }
    
    /** Write bytes - bytes are always written to the end of the file.
     * Return the location where the write started.
     */ 
    public long write(byte b[], int start, int length) ;
    
    /** Return the length of the file (including any buffered writes) */
    public long length() ;

    /** Truncate the file */ 
    public void truncate(long length) ; 

    /** Return whether this is an empty file or not */ 
    public default boolean isEmpty() { return length() == 0 ; } 

    @Override
    public void sync() ;
    
    @Override
    public void close() ;
}

