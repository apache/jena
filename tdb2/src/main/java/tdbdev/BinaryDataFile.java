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

package tdbdev;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;

/** An append-only binary file
 * 
 * An {@link ObjectFile} is a series of length+binary records.
 * A {@link BinaryDataFile} does not record the length and assumes the
 * entires are selef defining.
 * 
 *  @see ObjectFile 
 */
public interface BinaryDataFile extends Closeable, Sync {

    /** Read into a byte array, returning the number of bytes read. 
     * 
     * @param location  Byte offset for startof reading. 
     * @param b byte array
     * 
     * @return The number of bytes read
     */
    public default int read(long location, byte b[]) {
        return read(location, b, 0, b.length);
    }

    /** Read into a byte array, returning the number of bytes read.
     * 
     * @param b
     * @param start of bytesarray to read into
     * @param length Maximum number of bytes to read. 
     * @return The number of bytes read
     */
 
    public int read(long location, byte b[], int start, int length) ;

    /** Write bytes - bytes are always written to the end of the file */ 
    public default void write(byte b[]) {
        write(b, 0, b.length) ;
    }
    
    /** Write bytes - bytes are always written to the end of the file */ 
    public void write(byte b[], int start, int length) ;

    /** Truncate the file */ 
    public void truncate(long length) ; 

    @Override
    public void sync() ;
    
    @Override
    public void close() ;
}

