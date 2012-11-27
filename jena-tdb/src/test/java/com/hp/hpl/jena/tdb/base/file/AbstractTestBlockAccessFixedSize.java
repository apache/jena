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

import static com.hp.hpl.jena.tdb.base.BufferTestLib.sameValue ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.block.Block ;

public abstract class AbstractTestBlockAccessFixedSize extends BaseTest
{
    // Fixed block tests.
    
    int blkSize ;
    
    protected AbstractTestBlockAccessFixedSize(int blkSize)
    {
        this.blkSize = blkSize ;
    }
    
    protected abstract BlockAccess make() ;
    protected static Block data(BlockAccess file, int len)
    {
        Block b = file.allocate(len) ;
        for (int i = 0 ; i < len ; i++ )
            b.getByteBuffer().put((byte)(i&0xFF)) ;
        return b ;
    }

    private BlockAccess file ;
    @Before public void before() { file = make() ; }
    @After  public void after()  { file.close() ; }

    @Test public void fileaccess_01()
    {
        assertTrue(file.isEmpty()) ;
    }
    
    @Test public void fileaccess_02()
    {
        Block b = data(file, blkSize) ;
        file.write(b) ;
    }

    @Test public void fileaccess_03()
    {
        Block b1 = data(file, blkSize) ;
        file.write(b1) ;
        long x = b1.getId() ;
        Block b9 = file.read(x) ;
        assertNotSame(b1, b9) ;
        assertTrue(sameValue(b1, b9)) ;
        b9 = file.read(x) ;
        assertNotSame(b1, b9) ;
        assertTrue(sameValue(b1, b9)) ;
    }
    
    @Test public void fileaccess_04()
    {
        Block b1 = data(file, blkSize) ;
        Block b2 = data(file, blkSize) ;
        file.write(b1) ;
        file.write(b2) ;
        
        long x = b1.getId() ;
        Block b8 = file.read(b1.getId()) ;
        Block b9 = file.read(b1.getId()) ;
        assertNotSame(b8, b9) ;
        assertTrue(b8.getId() == b9.getId()) ;
    }
    
    @Test(expected=FileException.class)
    public void fileaccess_05()
    {
        Block b1 = data(file, 10) ;
        Block b2 = data(file, 20) ;
        file.write(b1) ;
        
        // Should not work. b2 not written.   
        Block b2a = file.read(b2.getId()) ;
    }    
}
