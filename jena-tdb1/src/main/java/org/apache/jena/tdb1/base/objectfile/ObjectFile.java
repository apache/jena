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

package org.apache.jena.tdb1.base.objectfile;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Sync ;

/** 
 * An ObjectFile is an append-read file, that is you can append data
 * to the stream or read any block.
 */

public interface ObjectFile extends Sync, Closeable
{
    public static final String type = "object" ;
    
    /** A label to identify this ObjectFile */ 
    public String getLabel() ;

    /** Write out the buffer - return the accessor number */ 
    public long write(ByteBuffer buffer) ;

    /** Read a buffer at the accessor number. */
    public ByteBuffer read(long id) ;
    
    /** Length, in units used by read/write for ids */
    public long length() ;
    
    /** Any objects in this file? */
    public boolean isEmpty() ;

    /** Reset the "append" point; may only be moved earlier.
     * The new position must correspond to a position returned by
     * {@link #write(ByteBuffer)}.
     */
    public void reposition(long id) ;
    
    /** Truncate the file */
    public void truncate(long size) ;

    /** All the contents as ByteBuffers - debugging aid */
    public Iterator<Pair<Long, ByteBuffer>> all() ;
}
