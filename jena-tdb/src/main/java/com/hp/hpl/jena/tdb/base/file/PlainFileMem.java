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


public class PlainFileMem extends PlainFile
{
    public PlainFileMem()
    {
        super() ;
        byteBuffer = ByteBuffer.allocate(0) ;
        ensure(0) ;
    }

    @Override
    protected ByteBuffer allocateBuffer(long size)
    {
        filesize = size ;
        ByteBuffer bb = ByteBuffer.allocate((int)size) ;
        // If copy-over
        if ( true )
        {
            bb.put(byteBuffer) ;
            bb.position(0) ;
        }
        return bb ;
    }

    @Override
    public void close()
    {}

    @Override
    public void sync()
    {}
    
}
