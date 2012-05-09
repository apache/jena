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

import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.block.Block ;

import static com.hp.hpl.jena.tdb.base.BufferTestLib.* ;

public abstract class AbstractTestBlockAccessVarSize extends AbstractTestBlockAccessFixedSize
{
    protected AbstractTestBlockAccessVarSize()
    {
        super(25) ;
    }
    
    @Test
    public void fileaccess_50()
    {
        BlockAccess file = make() ;
        Block b1 = data(file, 10) ;
        Block b2 = data(file, 20) ;
        file.write(b1) ;
        file.write(b2) ;
        
        Block b1a = file.read(b1.getId()) ;
        Block b2a = file.read(b2.getId()) ;

        assertNotSame(b1a, b1) ;
        assertNotSame(b2a, b2) ;
        sameValue(b1, b1a) ;
        sameValue(b2, b2a) ;
    }        
}
