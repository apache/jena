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
import java.nio.ByteBuffer ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.FileOps ;


public class BufferChannelFile implements BufferChannel
{
    private FileBase file ;

    public BufferChannelFile(String filename)
    {
        file = new FileBase(filename) ;
    }
    
    @Override
    public BufferChannel duplicate()
    {
        return new BufferChannelFile(file.filename) ;
    }

    @Override
    public long position()
    {
        try { return file.channel.position() ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public void position(long pos)
    {
        try { file.channel.position(pos) ; } 
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public void truncate(long length)
    {
        try { 
            // http://bugs.sun.com/view_bug.do?bug_id=6191269
            if ( length < file.channel.position() )
                file.channel.position(length) ;
            file.channel.truncate(length) ;
        }
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public int read(ByteBuffer buffer)
    {
        try { return file.channel.read(buffer) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }
    
    
    @Override
    public int read(ByteBuffer buffer, long loc)
    {
        try { return file.channel.read(buffer, loc) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public int write(ByteBuffer buffer)
    {
        try { return file.channel.write(buffer) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public int write(ByteBuffer buffer, long loc)
    {
        try { return file.channel.write(buffer, loc) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public long size()
    {
        try { return file.channel.size() ; }
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public void sync()
    { 
        try { file.channel.force(true) ; }
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public void close()
    {
        try { file.channel.close() ; }
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public String getLabel()
    {
        return FileOps.basename(file.getFilename()) ;
    }

    @Override
    public String toString()
    {
        return file.getFilename() ;
    }

    @Override
    public String getFilename()
    {
        return file.getFilename() ;
    }
    
}
