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

package org.seaborne.dboe.base.objectfile;

import org.apache.jena.atlas.lib.FileOps ;
import org.junit.AfterClass ;
import org.seaborne.dboe.ConfigTest ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.base.file.BufferChannelFile ;
import org.seaborne.dboe.base.objectfile.ObjectFile ;
import org.seaborne.dboe.base.objectfile.ObjectFileStorage ;

public class TestObjectFileDisk extends AbstractTestObjectFile
{
    static String filename = ConfigTest.getTestingDir()+"/test-objectfile" ;

    @AfterClass public static void cleanup() { FileOps.deleteSilent(filename) ; } 
    
    @Override
    protected ObjectFile make()
    {
        FileOps.deleteSilent(filename) ;
        BufferChannel chan = BufferChannelFile.create(filename) ;
        // No buffering.
        return new ObjectFileStorage(chan, -1) ;
    }
    
    @Override
    protected void release(ObjectFile file)
    {
        file.truncate(0) ;
        file.close() ;
    }
}
