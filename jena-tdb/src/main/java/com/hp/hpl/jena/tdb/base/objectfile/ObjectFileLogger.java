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

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;

public class ObjectFileLogger implements ObjectFile
{
    protected final ObjectFile other ;
    private static Logger defaultLogger = LoggerFactory.getLogger(ObjectFile.class) ; 
    private final Logger log ;
    private final String label  ;

    public ObjectFileLogger(String label, ObjectFile other)
    {
        this.other = other ;
        this.label = label ;
        log = defaultLogger ;
    }

    @Override
    public Block allocWrite(int maxBytes)
    {
        Block blk = other.allocWrite(maxBytes) ;
        info("allocWrite("+maxBytes+") -> "+blk.getId()) ;
        return blk ;
    }

    @Override
    public void completeWrite(Block buffer)
    {
        info("completeWrite("+buffer.getId()+")") ;
        other.completeWrite(buffer) ;
    }

    @Override
    public void abortWrite(Block buffer)
    {
        info("abortWrite("+buffer.getId()+")") ;
        other.abortWrite(buffer) ;
    }

    @Override
    public long write(ByteBuffer buffer)
    {
        info("write"+buffer) ;
        return other.write(buffer) ;
    }

    @Override
    public void reposition(long id)
    {
        info("reposition("+id+")") ;
        other.reposition(id) ;
    }

    @Override
    public ByteBuffer read(long id)
    {
        info("read("+id+")") ;
        return other.read(id) ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        info("all()") ;
        return other.all() ;
    }

    @Override
    public void truncate(long size)
    {
        info("truncate("+size+")") ;
        other.truncate(size) ;
    }

    @Override
    public void sync()
    {
        info("sync") ;
        other.sync() ;
    }

    @Override
    public void close()
    {
        info("close") ;
        other.close() ;
    }

    @Override
    public String getLabel()
    {
        return other.getLabel() ;
    }

    @Override
    public long length()
    {
        return other.length() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        return other.isEmpty() ;
    }

    private void info(String string)
    {
        if ( label != null )
            string = label+": "+string ;
        log.info(string) ; 
    }
}
