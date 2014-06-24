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

package com.hp.hpl.jena.util.iterator.test;


import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.iterator.*;

public class TestMapIterators extends ModelTestBase
    {
    
    public TestMapIterators( String name )
        { super( name ); }

    public void testCloseClosesBaseIterator()
        {
        Map1<String, String> map = new Map1<String, String>() 
            {
            @Override
            public String map1( String o )
                { return null; }
            };
        LoggingClosableIterator<String> base = new LoggingClosableIterator<>( null );
        Map1Iterator<String, String> mit = new Map1Iterator<>( map, base );
        mit.close();
        assertTrue( "base must have been closed by closing map", base.isClosed() );
        }
    }
