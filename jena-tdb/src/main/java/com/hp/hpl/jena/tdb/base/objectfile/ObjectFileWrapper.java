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

import com.hp.hpl.jena.tdb.base.block.Block ;

import org.apache.jena.atlas.lib.Pair ;

/** 
 * An ObjectFile is an append-read file, that is you can append data
 * to the stream or read any block.
 */

public class ObjectFileWrapper implements ObjectFile
{
    protected ObjectFile other ;

    public ObjectFileWrapper(ObjectFile other)      { this.other = other ; }
    
    @Override
    public Block allocWrite(int maxBytes)           { return other.allocWrite(maxBytes) ; }

    @Override
    public void completeWrite(Block buffer)         { other.completeWrite(buffer) ; }

    @Override
    public void abortWrite(Block buffer)            { other.abortWrite(buffer) ; }
    
    @Override
    public long write(ByteBuffer buffer)            { return other.write(buffer) ; }
    
    @Override
    public void reposition(long id)                 { other.reposition(id) ; }

    @Override
    public void truncate(long size)                 { other.truncate(size) ; }

    @Override
    public ByteBuffer read(long id)                 { return other.read(id) ; }

    @Override
    public String getLabel()                        { return other.getLabel()  ; }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()   { return other.all() ; }

    @Override
    public void sync()                              { other.sync() ; }

    @Override
    public void close()                             { other.close() ; }

    @Override
    public long length()                            { return other.length() ; }
    
    @Override
    public boolean isEmpty()                        { return other.isEmpty() ; }
}
