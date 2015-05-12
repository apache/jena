/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.index.ext ;

import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.nio.channels.FileChannel ;

import org.apache.jena.atlas.io.IO ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.sys.FileLib ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Single, unsegmented file with ByteBuffer */
public class PlainFilePersistent extends PlainFile {
    private static Logger log = LoggerFactory.getLogger(PlainFilePersistent.class) ;

    private FileChannel   file ;

    // Plain file over mmapped ByteBuffer
    PlainFilePersistent(Location loc, String filename) {
        this(loc.getPath(filename, "dat")) ;
    }

    PlainFilePersistent(String filename) {
        file = FileLib.openManaged(filename) ;
        // long filesize = file.out.length() ;
        // if ( channel.size() == 0 ) {}
        byteBuffer = allocateBuffer(filesize) ;
    }

    @Override
    public void sync() {
        FileLib.sync(file) ;
    }

    @Override
    public void close() {
        FileLib.close(file) ;
    }

    @Override
    protected ByteBuffer allocateBuffer(long size) {
        try {
            return file.map(FileChannel.MapMode.READ_WRITE, 0, size) ;
        } catch (IOException ex) {
            IO.exception(ex) ;
            return null ;
        }
    }
}
