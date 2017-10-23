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

package org.apache.jena.dboe.base.file ;

import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.nio.channels.FileChannel ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.dboe.sys.FileLib;

public class BufferChannelFile implements BufferChannel {
    private String      filename ;
    private FileChannel file ;

    /** Create a BufferChannelFile */
    public static BufferChannelFile create(String filename) {
        return create(filename, "rw") ;
    }

    /** Create a BufferChannelFile */
    public static BufferChannelFile create(String filename, String mode) {
        FileChannel base = ChannelManager.acquire(filename, mode) ;
        return new BufferChannelFile(filename, base) ;
    }

    /** Create a BufferChannelFile with unmanaged file resources - use with care */
    public static BufferChannelFile createUnmanaged(String filename, String mode) {
        FileChannel channel = FileLib.openUnmanaged(filename, mode) ;
        return new BufferChannelFile(filename, channel) ;
    }

    private BufferChannelFile(String filename, FileChannel channel) {
        this.filename = filename ;
        this.file = channel ;
    }

    @Override
    public BufferChannel duplicate() {
        return new BufferChannelFile(filename, file) ;
    }

    @Override
    public long position() {
        try {
            return file.position() ;
        }
        catch (IOException e) {
            IO.exception(e) ;
            return -1 ;
        }
    }

    @Override
    public void position(long pos) {
        try {
            file.position(pos) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }

    @Override
    public void truncate(long length) {
        try {
            // http://bugs.sun.com/view_bug.do?bug_id=6191269
            if ( length < file.position() )
                file.position(length) ;
            file.truncate(length) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }

    @Override
    public int read(ByteBuffer buffer) {
        try {
            return file.read(buffer) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
            return -1 ;
        }
    }

    @Override
    public int read(ByteBuffer buffer, long loc) {
        try {
            return file.read(buffer, loc) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
            return -1 ;
        }
    }

    @Override
    public int write(ByteBuffer buffer) {
        try {
            return file.write(buffer) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
            return -1 ;
        }
    }

    @Override
    public int write(ByteBuffer buffer, long loc) {
        try {
            return file.write(buffer, loc) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
            return -1 ;
        }
    }

    @Override
    public long size() {
        return FileLib.size(file) ; 
    }

    @Override
    public boolean isEmpty() {
        return size() == 0 ;
    }

    @Override
    public void sync() {
        FileLib.sync(file) ;
    }

    @Override
    public void close() {
        FileLib.close(file) ;
    }

    @Override
    public String getLabel() {
        return filename ;
    }

    @Override
    public String toString() {
        return filename ;
    }

    @Override
    public String getFilename() {
        return filename ;
    }

}
