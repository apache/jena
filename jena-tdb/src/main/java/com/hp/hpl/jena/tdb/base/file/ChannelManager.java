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
import java.nio.channels.FileChannel ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

public class ChannelManager
{
    // Make per "location"?
    
    // Because "FileManager" is already in use
    // ChannelManager
    
    // FileBase ==> OpenFileRef, ChannelRef
    
    public static FileChannel acquire(String filename)
    {
        return acquire(filename, "rw") ;
    }
    
    public static FileChannel acquire(String filename, String mode)
    {
        return openref$(filename, mode) ;
    }
    
    static private Map<String, FileChannel> name2channel = new HashMap<>() ;
    static private Map<FileChannel, String> channel2name = new HashMap<>() ;
    
    private static FileChannel openref$(String filename, String mode)
    {
        // Temp - for now, only journal files are tracked.
        if ( ! filename.endsWith(".jrnl") )
        {
            return open$(filename, mode) ;
        }
        
        FileChannel chan = name2channel.get(filename) ;
        if ( chan != null )
        {
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
            @SuppressWarnings("resource")
            RandomAccessFile out = new RandomAccessFile(filename, mode) ;
            FileChannel channel = out.getChannel() ;
            return channel ;
        } catch (IOException ex) { throw new FileException("Failed to open: "+filename+" (mode="+mode+")", ex) ; }
    }
    
    public static void release(String filename)
    {
        FileChannel channel = name2channel.get(filename) ;
        if ( channel != null )
            release(channel) ;
    }
    
    public static void release(FileChannel chan)
    {
        // Always close even if not managed.
        try { chan.close() ; } catch (Exception ex) {}
        String name = channel2name.remove(chan) ;
        if ( name != null )
            name2channel.remove(name) ;
    }
    
    public static void reset()
    {
        releaseAll(null) ;
    }
    
    /** Shutdown all the files matching the prefix (typically a directory) */  
    public static void releaseAll(String prefix)
    {
        // Use an iterator explicitly so we can remove from the map.
        List<FileChannel> x = new ArrayList<>() ;
        for ( String fn : name2channel.keySet() )
        {
            if ( prefix == null || fn.startsWith(prefix) )
            {
                x.add(name2channel.get(fn)) ;
                // Don't call release here - potential CME problems.
                // Could use an explicit iterator on teh keySet and .remove from that but
                // then we nearly duplicate the code in release.
            }
        }
        for ( FileChannel chan : x )
            release(chan) ;
    }
}

