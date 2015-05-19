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

import static org.apache.jena.atlas.lib.FileOps.clearDirectory ;
import org.apache.jena.atlas.lib.FileOps ;
import org.seaborne.dboe.ConfigTestDBOE ;
import org.seaborne.dboe.base.file.FileFactory ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.objectfile.StringFile ;

public class TestStringFileDisk extends AbstractTestStringFile
{
    String fn = null ;
    
    @Override
    protected StringFile createStringFile()
    {
        String dir = ConfigTestDBOE.getTestingDir() ;
      clearDirectory(dir) ;
      Location loc = Location.create(dir) ;
      fn = loc.getPath("xyz", "node") ;
      FileOps.delete(fn) ;
      return FileFactory.createStringFileDisk(fn) ;
    }

    @Override
    protected void removeStringFile(StringFile f)
    {
        f.close() ;
        FileOps.delete(fn) ;
    }
}
