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

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.lib.NodeLib.hash ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TestNodeLib extends BaseTest
{
    // Tests of TDBs NodeLib
    @Test public void hash1() 
    {
        Node x1 = NodeFactoryExtra.parseNode("<http://example/x>") ;
        Node x2 = NodeFactoryExtra.parseNode("<http://example/x>") ;
        assertEquals(hash(x1), hash(x2)) ;
    }
    
    @Test public void hash2() 
    {
        Node x1 = NodeFactoryExtra.parseNode("<http://example/x1>") ;
        Node x2 = NodeFactoryExtra.parseNode("<http://example/x2>") ;
        assertNotEquals(hash(x1), hash(x2)) ;
    }
    
    @Test public void hash3() 
    {
        Node x1 = NodeFactoryExtra.parseNode("<lex>") ;
        Node x2 = NodeFactoryExtra.parseNode("'lex'") ;
        Node x3 = NodeFactoryExtra.parseNode("_:lex") ;
        assertNotEquals(hash(x1), hash(x2)) ;
        assertNotEquals(hash(x2), hash(x3)) ;
        assertNotEquals(hash(x3), hash(x1)) ;
    }
    
    @Test public void hash4() 
    {
        Node x1 = NodeFactoryExtra.parseNode("123") ;
        Node x2 = NodeFactoryExtra.parseNode("'123'") ;
        assertNotEquals(hash(x1), hash(x2)) ;
    }

    @Test public void hash5() 
    {
        Node x1 = NodeFactoryExtra.parseNode("123") ;
        Node x2 = NodeFactoryExtra.parseNode("123.0") ;
        Node x3 = NodeFactoryExtra.parseNode("123e0") ;
        assertNotEquals(hash(x1), hash(x2)) ;
        assertNotEquals(hash(x2), hash(x3)) ;
        assertNotEquals(hash(x3), hash(x1)) ;
    }
}
