/**
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

import java.io.IOException ;
import java.io.RandomAccessFile ;
import java.nio.channels.Channel ;
import java.nio.channels.FileChannel ;
import java.util.HashMap ;
import java.util.Map ;

import org.openjena.atlas.io.IO ;

public class ChannelManager
{
    // Because "FileManager" is already in use
    // ChannelManager
    
    // Filebase ==> OpenFileRef
    // ChannelRef
    
    public static FileChannel open(String filename)
    {
        return open(filename, "rw") ;
    }
    
    public static FileChannel open(String filename, String mode)
    {
        return openref$(filename, "rw") ;
    }
    
    static private Map<String, FileChannel> name2channel = new HashMap<String, FileChannel>() ;
    static private Map<FileChannel, String> channel2name = new HashMap<FileChannel, String>() ;
    
    private static FileChannel openref$(String filename, String mode)
    {
        FileChannel chan = name2channel.get(filename) ;
        if ( chan != null )
        {
            // Temp - for now, only journal files.
            if ( filename.endsWith(".jrnl") )
                // Scream - it's currently open.
                throw new FileException("Already open: "+filename) ;
        }
        chan = open$(filename, mode) ;
        name2channel.put(filename, chan) ;
        channel2name.put(chan, filename) ;
        return chan ;
    }
    
    private static FileChannel open$(String filename, String mode)
    {
        try {
            // "rwd" - Syncs only the file contents
            // "rws" - Syncs the file contents and metadata
            // "rw"  - OS write behind possible
            // "r"   - read only
            RandomAccessFile out = new RandomAccessFile(filename, mode) ;
            FileChannel channel = out.getChannel() ;
            return channel ;
        } catch (IOException ex) { throw new FileException("Failed to open: "+filename+" (mode="+mode+")", ex) ; }
    }
    
    public static void close(Channel chan)
    {
        IO.close(chan) ;
        String name = channel2name.remove(chan) ;
        name2channel.remove(name) ;
    }
}

