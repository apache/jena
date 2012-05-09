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

package com.hp.hpl.jena.tdb.transaction;

import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelMem ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileStorage ;

public class TestObjectFileTransStorage extends AbstractTestObjectFileTrans
{
    @Override
    ObjectFile createFile(String basename)
    {
//        String dir = ConfigTest.getTestingDir() ;
//        Location loc = new Location(dir) ;
//        String fn = loc.getPath(basename) ;
//        FileOps.deleteSilent(fn) ;
//        BufferChannel chan = new BufferChannelFile(fn) ;
//        return new ObjectFileStorage(chan) ;
        
        BufferChannel chan = BufferChannelMem.create() ;
        // Small buffer
        return new ObjectFileStorage(chan,10) ;
        
    }


    @Override
    void deleteFile(String basename)
    {
//        String dir = ConfigTest.getTestingDir() ;
//        Location loc = new Location(dir) ;
//        String fn = loc.getPath(basename) ;
//        FileOps.delete(fn) ;
    }
}
