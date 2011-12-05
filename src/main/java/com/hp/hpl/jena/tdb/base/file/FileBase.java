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

import java.io.IOException ;
import java.io.RandomAccessFile ;
import java.nio.channels.FileChannel ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.BlockException ;

public class FileBase implements Sync, Closeable
{
    static private Logger log = LoggerFactory.getLogger(FileBase.class) ; 
    // Usually used as a mixin, which java does not support very well.
    public final String filename ;
    public FileChannel channel ;
    public RandomAccessFile out ;
    public static boolean DEBUG = false ;
    private final boolean DebugThis  ;
    private static long counter = 0 ;
    private final long id ;

    public FileBase(String filename)
    {
        this(filename, "rw") ;
    }
    
    public FileBase(String filename, String mode)
    {
        DebugThis = DEBUG && filename.contains("nodes.dat-jrnl") ;
        id  = (counter++) ;
        
        if ( DebugThis )
            log.debug("open: ["+id+"]"+filename) ;
        this.filename = filename ;
        try {
            // "rwd" - Syncs only the file contents
            // "rws" - Syncs the file contents and metadata
            // "rw" - cached?
            out = new RandomAccessFile(filename, mode) ;
            channel = out.getChannel() ;
        } catch (IOException ex) { throw new BlockException("Failed to create FileBase", ex) ; }
    }

    public long size()
    {
        try {
            return channel.size() ;
        } catch (IOException ex)
        { IO.exception(ex) ; return -1L ; }
    }
    
    @Override
    public void close()
    {
        if ( DebugThis )
            log.debug("close: ["+id+"]: "+filename) ;
        try {
            channel.close() ;
            channel = null ;
            out = null ;
        } catch (IOException ex)
        { throw new FileException("FileBase.close", ex) ; }

    }

    @Override
    public void sync()
    {
        if ( DebugThis ) 
            log.debug("sync: ["+id+"]: "+filename) ;
        try {
            channel.force(false) ;
        } catch (IOException ex)
        { throw new FileException("FileBase.sync", ex) ; }
    }

    public String getFilename() { return filename ; }  
}
