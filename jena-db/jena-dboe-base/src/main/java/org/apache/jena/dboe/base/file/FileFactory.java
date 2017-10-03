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

public class FileFactory {
    
    public static BinaryDataFile createBinaryDataFile(FileSet fileset, String ext) {
        String x = fileset.filename(ext) ;
        if ( fileset.isMem() ) {
            return new BinaryDataFileMem() ;
        } else {
            BinaryDataFile bdf = new BinaryDataFileRandomAccess(x) ;
            bdf = new BinaryDataFileWriteBuffered(bdf) ;
            return bdf ;
        }
    }

    public static BinaryDataFile createBinaryDataFile() {
        return new BinaryDataFileMem() ;
    }

    public static BufferChannel createBufferChannel(FileSet fileset, String ext) {
        String x = fileset.filename(ext) ;
        if ( fileset.isMem() )
            return BufferChannelMem.create(x) ;
        else
            return BufferChannelFile.create(x) ;
    }

    public static BufferChannel createBufferChannelMem() {
        return createBufferChannel(FileSet.mem(), null) ;
    }
}
