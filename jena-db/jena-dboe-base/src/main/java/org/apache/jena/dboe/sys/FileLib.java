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

package org.apache.jena.dboe.sys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.dboe.base.file.ChannelManager;
import org.apache.jena.dboe.base.file.FileException;

// => IO
public class FileLib
{
    // Check whether still used.

    public static FileChannel openUnmanaged(String filename) {
        return openUnmanaged(filename, "rw");
    }

    // And operation from BufferChannelFile

    public static FileChannel openUnmanaged(String filename, String mode) {
        try {
            @SuppressWarnings("resource")
            RandomAccessFile out = new RandomAccessFile(filename, mode);
            FileChannel channel = out.getChannel();
            return channel;
        }
        catch (FileNotFoundException e) {
            IO.exception(e);
            return null;
        }
    }

    // TODO remove and call ChannelManager directly
    public static FileChannel openManaged(String filename) {
        return openManaged(filename, "rw");
    }

    // TODO remove and call ChannelManager directly
    public static FileChannel openManaged(String filename, String mode) {
        return ChannelManager.acquire(filename, mode);
    }

    public static long size(FileChannel channel) {
        try {
            return channel.size();
        } catch (IOException ex)
        { IO.exception(ex); return -1L ; }
    }

    public static void truncate(FileChannel channel, long posn) {
        try { channel.truncate(posn); }
        catch (IOException ex) { IO.exception(ex); }
    }

    public static void close(FileChannel channel) {
        ChannelManager.release(channel);
    }

    public static void sync(FileChannel channel) {
        try {
            channel.force(true);
        } catch (IOException ex)
        { throw new FileException("FileBase.sync", ex); }
    }
}
