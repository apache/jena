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

import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileStorage ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;

public class FileFactory
{
    public static StringFile createStringFileDisk(String filename)
    { return new StringFile(createObjectFileDisk(filename)) ; }

    public static StringFile createStringFileMem(String filename)
    { return new StringFile(createObjectFileMem(filename)) ; }
    
    public static ObjectFile createObjectFileDisk(String filename)
    {
        BufferChannel file = BufferChannelFile.create(filename) ; 
        return new ObjectFileStorage(file) ;
    }

    public static ObjectFile createObjectFileMem(String filename)
    { 
        BufferChannel file = BufferChannelMem.create(filename) ; 
        return new ObjectFileStorage(file) ;
    }
    
    public static PlainFile createPlainFileDisk(String filename)
    { return new PlainFilePersistent(filename) ; }
    
    public static PlainFile createPlainFileMem()
    { return new PlainFileMem() ; }
}
