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

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;

public class TestVocabRSS extends VocabTestBase
    {
    public TestVocabRSS(String name)
        { super(name); }

     public static TestSuite suite()
        { return new TestSuite( TestVocabRSS.class ); }

    public void testRSS()
        {
		String ns = "http://purl.org/rss/1.0/";
        assertResource( ns + "channel", RSS.channel );
        assertResource( ns + "item", RSS.item );
        assertProperty( ns + "description", RSS.description );
        assertProperty( ns + "image", RSS.image );
        assertProperty( ns + "items", RSS.items );
        assertProperty( ns + "link", RSS.link );
        assertProperty( ns + "name", RSS.name );
        assertProperty( ns + "textinput", RSS.textinput );
        assertProperty( ns + "title", RSS.title );
        assertProperty( ns + "url", RSS.url );
        }
    }
