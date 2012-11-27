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

import org.apache.jena.atlas.lib.FileOps ;
import org.junit.AfterClass ;

import com.hp.hpl.jena.tdb.ConfigTest ;

public class TestBlockAccessDirect extends AbstractTestBlockAccessFixedSize
{
    static String filename = ConfigTest.getTestingDir()+"/test-file-access-direct" ;
    
    static final int BlockSize = 50 ;
    public TestBlockAccessDirect()
    {
        super(BlockSize) ;
    }

    @AfterClass public static void cleanup() { FileOps.deleteSilent(filename) ; } 
    
    @Override
    protected BlockAccess make()
    {
        FileOps.deleteSilent(filename) ;
        return new BlockAccessDirect(filename, BlockSize) ;
    }
}
