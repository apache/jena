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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.RefLong ;
import org.junit.Test ;


public class TestRefLong extends BaseTest
{
    @Test public void ref_01()
    {
        RefLong ref1 = new RefLong() ;
        assertEquals(0, ref1.value()) ;
        RefLong ref2 = new RefLong() ;
        assertNotSame(ref1, ref2) ;
    }
    
    @Test public void ref_02()
    {
        RefLong ref = new RefLong() ;
        assertEquals(0, ref.value()) ;
        ref.inc() ;
        assertEquals(1, ref.value()) ;
        ref.dec() ;
        assertEquals(0, ref.value()) ;
    }
    
    @Test public void ref_03()
    {
        RefLong ref = new RefLong(99) ;
        assertEquals(99, ref.value()) ;
        long x = ref.incAndGet() ;
        assertEquals(100, x) ;
        assertEquals(100, ref.value()) ;
        x = ref.getAndInc() ;
        assertEquals(100, x) ;
        assertEquals(101, ref.value()) ;
    }
    
    @Test public void ref_04()
    {
        RefLong ref = new RefLong(99) ;
        assertEquals(99, ref.value()) ;
        long x = ref.decAndGet() ;
        assertEquals(98, x) ;
        assertEquals(98, ref.value()) ;
        x = ref.getAndDec() ;
        assertEquals(98, x) ;
        assertEquals(97, ref.value()) ;
    }

}
