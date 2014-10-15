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

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;

public abstract class AbstractTestStringFile extends BaseTest
{
    StringFile f = null ;
    
    @Before public void setup() { f = createStringFile() ; }
    @After public void teardown() { removeStringFile(f) ; }
    
    protected abstract StringFile createStringFile() ;
    protected abstract void removeStringFile(StringFile f) ;

    @Test public void object_file_01()
    {
        String x1 = "abc" ;
        long id1 = f.write(x1) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    @Test public void object_file_02()
    {
        String x1 = "" ;
        
        long id1 = f.write(x1) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    @Test public void object_file_03()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "deeeef" ;
        
        long id1 = f.write(x1) ;
        long id2 = f.write(x2) ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        test(id1, x1) ;
        test(id2, x2) ;
        test(0, x1) ;
    }
    
    @Test public void object_file_04()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "deeeef" ;
        
        long id1 = f.write(x1) ;
        long id2 = f.write(x2) ;
        // Read in opposite order
        test(id2, x2) ;
        test(id1, x1) ;
    }

    @Test public void object_file_05()
    {
        String x = "孫子兵法" ;
        
        long id = f.write(x) ;
        test(0, x) ;
    }
    
    @Test public void object_file_06()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "孫子兵法" ;
        
        long id1 = f.write(x1) ;
        f.flush() ;
        long id2 = f.write(x2) ;
        // No flush.
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        String z = f.read(id2) ;
        
        test(id2, x2) ;
        test(id1, x1) ;
        test(0, x1) ;
    }
    
    @Test public void object_file_07()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "孫子兵法" ;
        
        long id1 = f.write(x1) ;
        long id2 = f.write(x2) ;
        f.flush() ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        test(id2, x2) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    @Test public void object_file_08()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "孫子兵法" ;
        
        long id1 = f.write(x1) ;
        f.flush() ;
        long id2 = f.write(x2) ;
        f.flush() ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        test(id2, x2) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    private void test(long id, String x)
    {
        String y = f.read(id) ;
        assertEquals(x, y) ;
    }
}
