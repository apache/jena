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

import java.nio.ByteBuffer;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;

/** Single file, single ByteBuffer */
public abstract class PlainFile implements Sync, Closeable
{
    protected long filesize = -1 ;
    protected ByteBuffer byteBuffer = null ;

    PlainFile() {}
    
    public final ByteBuffer getByteBuffer()
    {
//        if ( byteBuffer == null )
//            byteBuffer = allocateBuffer(filesize) ;
        return byteBuffer ;
    }
    
    public final ByteBuffer ensure(int newSize)
    {
        if ( filesize > newSize )
            return getByteBuffer() ;
       byteBuffer = allocateBuffer(newSize) ;
       filesize = newSize ;
       return byteBuffer ;
    }
    
    public final long getFileSize() { return filesize ; }
    
    @Override
    public abstract void sync() ;

    @Override
    public abstract void close() ;

    protected abstract ByteBuffer allocateBuffer(long size) ;
}
