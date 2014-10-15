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

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;
import java.nio.channels.FileChannel ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;


/**
 * Interface to storage : a simplified version of FileChannel. Read and write
 * bytes, passed via ByteBuffers, addressed by file location. This interface is
 * not suitable for memory mapped I/O - there is no ability to use slices of a
 * memort mapped file. This interface does not insert size of ByteBuffer - size
 * of ByteBuffer passed to read controls the number of bytes read. Having our
 * own abstraction enables us to implement memory-backed versions.
 * 
 * @see BlockAccess
 * @see FileChannel
 */
public interface BufferChannel extends Sync, Closeable
{
    // This is a simple, low level "file = array of bytes" interface"
    // This interface does not support slicing - so it's not suitable for memory mapped I/O
    // TODO Consider use of allocateDirect 
    
    /** Return another channel to the same storage but with independent position.
     * Chaos may result due to concurrent use.
     */
    public BufferChannel duplicate() ;
    
    /** return the position */
    public long position() ;
    
    /** set the position */
    public void position(long pos) ;

    /** Read into a ByteBuffer. Returns the number of bytes read. -1 for end of file.
     */
    public int read(ByteBuffer buffer) ;
    
    /** Read into a ByteBuffer, starting at position loc. Return the number of bytes read.
     * loc must be within the file.
     */
    public int read(ByteBuffer buffer, long loc) ;

    /** Write from ByteBuffer, starting at position loc.  
     * Return the number of bytes written
     */
    public int write(ByteBuffer buffer) ;
    
    /** Write from ByteBuffer, starting at position loc.  
     * Return the number of bytes written.
     * loc must be within 0 to length - writing at length is append */
    public int write(ByteBuffer buffer, long loc) ;
    
    /** Truncate the file.
     * @see FileChannel#truncate(long)
     */
    public void truncate(long size) ;
    
    /** Length of storage, in bytes.*/
    public long size() ;
    
    /** Is it empty? */
    public boolean isEmpty() ;

    /** useful display string */ 
    public String getLabel() ; 
    
    /** Filename for this BufferChannel (maybe null) */ 
    public String getFilename() ; 
    
}
