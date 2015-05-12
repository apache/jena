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

import java.io.RandomAccessFile ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;

/** An append-only, read anywhere, binary file.
 *  
 * 
 * An {@link ObjectFile} is a series of length+binary records.
 * A {@link BinaryDataFile} does not record the length and assumes the
 * entires are self-defining.
 * 
 *  @see ObjectFile
 *  @see RandomAccessFile
 */
public interface BinaryDataFile extends Closeable, Sync {

    /** Open the file */
    public void open() ;
    
    /** Is it open */ 
    public boolean isOpen() ;
    
    /** Read into a byte array, returning the number of bytes read. 
     * 
     * @param b byte array
     * 
     * @return The number of bytes read
     */
    public default int read(byte b[]) {
        return read(b, 0, b.length);
    }

    /** Read into a byte array, returning the number of bytes read.
     * 
     * @param b
     * @param start of bytesarray to read into
     * @param length Maximum number of bytes to read. 
     * @return The number of bytes read
     */
 
    public int read(byte b[], int start, int length) ;

    /** Write bytes - bytes are always written to the end of the file */ 
    public default void write(byte b[]) {
        write(b, 0, b.length) ;
    }
    
    /** Write bytes - bytes are always written to the end of the file */ 
    public void write(byte b[], int start, int length) ;
    
    /** Get the read position */
    public long position() ;

    /** Set the read position. 
     * <p>
     * If &lt; 0, then set to zero.
     * <p> If beyond the current length of the file, keep as provided, 
     * a write may extend the file before the next read. 
     * 
     * @param posn The new position.
     */
    public void position(long posn) ;

    /** 
     * Return the length of the file (including any buffered writes) 
     */
    public long length() ;

    /** Truncate the file */ 
    public void truncate(long length) ; 

    @Override
    public void sync() ;
    
    @Override
    public void close() ;
}

