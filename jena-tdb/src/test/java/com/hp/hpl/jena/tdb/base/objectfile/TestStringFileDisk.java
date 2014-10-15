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

import static org.apache.jena.atlas.lib.FileOps.clearDirectory ;
import org.apache.jena.atlas.lib.FileOps ;

import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;

public class TestStringFileDisk extends AbstractTestStringFile
{
    String fn = null ;
    
    @Override
    protected StringFile createStringFile()
    {
        String dir = ConfigTest.getTestingDir() ;
      clearDirectory(dir) ;
      Location loc = new Location(dir) ;
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
