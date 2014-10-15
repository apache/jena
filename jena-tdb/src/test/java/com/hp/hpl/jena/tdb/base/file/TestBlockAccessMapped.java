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

public class TestBlockAccessMapped extends AbstractTestBlockAccessFixedSize
{
    static String filename = ConfigTest.getTestingDir()+"/test-file-access-mapped" ;
    
    static final int BlockSize = 64 ;
    public TestBlockAccessMapped()
    {
        super(BlockSize) ;
    }

    @AfterClass public static void cleanup() { FileOps.deleteSilent(filename) ; } 
    
    static int counter = 0 ;
    
    @Override
    protected BlockAccess make()
    {
    	String fn = filename + "-"+(counter++) ;
    	FileOps.deleteSilent(fn) ;
        return new BlockAccessMapped(fn, BlockSize) ;
        
    }
}
